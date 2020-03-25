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

import com.marklogic.xcc.ResultSequence
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import org.nuxeo.gatling.marklogic.action.XccAction

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Try

class XccMarkLogicGetCall(requestName: Expression[String],
                          uri: Expression[String],
                          xccMarkLogicComponents:XccMarkLogicComponents,
                          statsEngine: StatsEngine,
                          clock: Clock,
                          val  next:Action)
  extends XccAction with ChainableAction with NameGen {

  override def name: String = genName("xccMarkLogicGetCall")

  override def execute(session: Session): Unit = execute(session, xccMarkLogicComponents)

  def sendQuery(session: Session): Unit = {
    val start = clock.nowMillis
    val threadExecutionContext: ExecutionContextExecutor = ExecutionContext.fromExecutorService(xccMarkLogicComponents.xccExecutorService)
    Future {
      val xccSession = xccMarkLogicComponents.newSession
      val request = xccSession.newAdhocQuery(s"fn:doc('${uri(session).toOption.get}')")
      xccMarkLogicComponents.call(xccSession, request)
    }(threadExecutionContext)
      .onComplete {
        case scala.util.Success(value) =>
          next ! Try(performChecks(session, start, value)).recover {
            case err =>
              val logRequestName = requestName(session).toOption.getOrElse("XccInsertCall")
              statsEngine.logCrash(session, logRequestName, err.getMessage)
              session.markAsFailed
          }.get
        case fail: scala.util.Failure[_] => next ! log(start, clock.nowMillis, fail, requestName, session, statsEngine)
      }(threadExecutionContext)
  }

  private def performChecks(session: Session, start: Long, result: ResultSequence): Session = {
    val end = clock.nowMillis
    requestName.apply(session).foreach { resolvedRequestName =>
      if (!result.hasNext)
        statsEngine.logResponse(session, resolvedRequestName, start, end, OK, None, None)
      else
        statsEngine.logResponse(session, resolvedRequestName, start, end, KO, None, None)
    }
    session
  }

}
