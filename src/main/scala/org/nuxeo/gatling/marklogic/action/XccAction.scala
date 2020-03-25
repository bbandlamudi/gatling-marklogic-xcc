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
 * This file is derived from https://github.com/codecentric/gatling-jdbc and has been modified by MarkLogic to add support for XCC.
 *
 * Contributors:
 *     Mads Hansen, MarkLogic Corporation
 */
package org.nuxeo.gatling.marklogic.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.action.ChainableAction
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.nuxeo.gatling.marklogic.XccMarkLogicComponents

import scala.util.Try

trait XccAction extends ChainableAction with NameGen {

  def execute(session: Session, xccMarkLogicComponents: XccMarkLogicComponents): Unit = {
    xccMarkLogicComponents.xccExecutorService.submit(new Runnable { override def run(): Unit = sendQuery(session) })
  }

  def sendQuery(session:Session)

  def log(start: Long, end: Long, tried: Try[_], requestName: Expression[String], session: Session, statsEngine: StatsEngine): Session = {
    val (status, message) = tried match {
      case scala.util.Success(_) => (OK, None)
      case scala.util.Failure(exception) => (KO, Some(exception.getMessage))
    }
    requestName.apply(session).foreach { resolvedRequestName =>
      statsEngine.logResponse(session, resolvedRequestName, start, end, status, None, message)
    }
    session.logGroupRequestTimings(start, end)
  }

}
