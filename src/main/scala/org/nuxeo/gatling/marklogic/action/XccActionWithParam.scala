/*
 * Copyright (c) 2020 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 *
 * Contributors:
 *     Mads Hansen, MarkLogic Corporation
 */
package org.nuxeo.gatling.marklogic.action

import com.marklogic.xcc.Request
import com.marklogic.xcc.types.ValueType
import io.gatling.core.session.Session
import io.gatling.http.request.builder.{HttpParam, MultivaluedParam, ParamMap, ParamSeq, SimpleParam}

trait XccActionWithParam extends XccAction {

  def setRequestParam(request: Request, key:String, value:Any): Unit ={
    //TODO: sniff out more specific types of values, maybe set XML and JSON nodes if there is a need
    value match {
      case _: Int => request.setNewIntegerVariable(key, value.asInstanceOf[Int])
      case _: Boolean => request.setNewVariable(key, ValueType.XS_BOOLEAN, value.asInstanceOf[Boolean])
      case _ => request.setNewStringVariable(key, value.toString())
    }
  }

  def setRequestParams(request: Request, queryParams: List[HttpParam], session:Session): Session ={
    for (param <- queryParams) {
      param match {
        case SimpleParam(key, value) =>
          for {
            key <- key(session)
            value <- value(session)
          } yield {
            setRequestParam(request, key, value)
          }
        case MultivaluedParam(key, values) =>
          for {
            key <- key(session)
            values <- values(session)
          } yield {
            values.foreach(value =>  setRequestParam(request, key, value))
          }
        case ParamSeq(seq) =>
          for {
            seq <- seq(session)
          } yield {
            seq.foreach { case (key, value) => setRequestParam(request, key, value) }
          }
        case ParamMap(map) =>
          for {
              map <- map(session)
          } yield {
            map.foreach { case (key, value) => setRequestParam(request, key, value) }
          }
      }
    }
    session
  }

}
