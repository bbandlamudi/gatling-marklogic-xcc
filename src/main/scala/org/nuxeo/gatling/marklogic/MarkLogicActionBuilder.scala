/*
 * (C) Copyright 2020 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc
 *     Mads Hansen, MarkLogic Corporation
 */
package org.nuxeo.gatling.marklogic

import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.session.Expression
import io.gatling.http.request.builder.{HttpParam, MultivaluedParam, ParamMap, ParamSeq, SimpleParam}
import io.gatling.core.session._

import scala.collection.mutable.ArrayBuffer
import org.nuxeo.gatling.marklogic.check.XccCheckSupport.XccCheckActionBuilder

abstract class MarkLogicActionBuilder[T]  extends XccCheckActionBuilder[T] {

  def components(protocolComponentsRegistry: ProtocolComponentsRegistry): XccMarkLogicComponents =
    protocolComponentsRegistry.components(XccMarkLogicProtocol.XccMarkLogicProtocolKey)

  protected var queryParams: ArrayBuffer[HttpParam] = ArrayBuffer.empty

  def queryParam(key: Expression[String], value: Expression[Any]): MarkLogicActionBuilder[T]  = queryParam(SimpleParam(key, value))
  def multivaluedQueryParam(key: Expression[String], values: Expression[Seq[Any]]): MarkLogicActionBuilder[T] = queryParam(MultivaluedParam(key, values))

  def queryParamSeq(seq: Seq[(String, Any)]): MarkLogicActionBuilder[T] = queryParamSeq(seq2SeqExpression(seq))
  def queryParamSeq(seq: Expression[Seq[(String, Any)]]): MarkLogicActionBuilder[T] = queryParam(ParamSeq(seq))

  def queryParamMap(map: Map[String, Any]): MarkLogicActionBuilder[T] = queryParamSeq(map2SeqExpression(map))
  def queryParamMap(map: Expression[Map[String, Any]]): MarkLogicActionBuilder[T] = queryParam(ParamMap(map))

  private def queryParam(param: HttpParam): MarkLogicActionBuilder[T] =  {
    queryParams += param
    this
  }

}