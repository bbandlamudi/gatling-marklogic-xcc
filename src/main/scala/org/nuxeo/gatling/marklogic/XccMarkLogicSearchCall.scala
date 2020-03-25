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

import com.marklogic.xcc.ResultItem
import io.gatling.commons.stats.KO
import io.gatling.core.action.Action
import io.gatling.core.session._
import io.gatling.commons.util.Clock
import io.gatling.core.check.Check
import io.gatling.core.stats.StatsEngine
import io.gatling.http.request.builder.HttpParam
import org.nuxeo.gatling.marklogic.action.XccActionWithParam

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class XccMarkLogicSearchCall[T](requestName: Expression[String],
                             query: Expression[String],
                             queryParams: List[HttpParam],
                             xccMarkLogicComponents: XccMarkLogicComponents,
                             checks: List[XccCheck[T]],
                             mapFunction: ResultItem => T,
                             statsEngine: StatsEngine,
                             clock: Clock,
                             val next: Action)
  extends XccActionWithParam {

  override def name: String = genName("xccMarkLogicSearchCall")

  override def execute(session: Session): Unit = execute(session, xccMarkLogicComponents)

  def sendQuery(session: Session): Unit = {
    val start = clock.nowMillis
    val threadExecutionContext: ExecutionContextExecutor = ExecutionContext.fromExecutorService(xccMarkLogicComponents.xccExecutorService)
    Future {
      val xccSession = xccMarkLogicComponents.newSession
      val request = xccSession.newAdhocQuery(query(session).toOption.get)
      setRequestParams(request, queryParams, session)
      xccMarkLogicComponents.call(xccSession, request).toResultItemArray.toList.map(mapFunction)
    }(threadExecutionContext)
    .onComplete {
      case scala.util.Success(value) =>
        next ! Try(performChecks(session, start, value)).recover {
          case err =>
            val logRequestName = requestName(session).toOption.getOrElse("XccSearchCall")
            statsEngine.logCrash(session, logRequestName, err.getMessage)
            session.markAsFailed
        }.get
      case fail: scala.util.Failure[_] => next ! log(start, clock.nowMillis, fail, requestName, session, statsEngine)
    }
  }

  private def performChecks(session: Session, start: Long, tried: List[T]): Session = {
    val (modifiedSession, error) = Check.check[List[T]](tried, session, checks, null)
    error match {
      case Some(failure) =>
        requestName.apply(session).map { resolvedRequestName =>
          statsEngine.logResponse(session, resolvedRequestName, start, clock.nowMillis, KO, None, None)
        }
        modifiedSession.markAsFailed
      case _ =>
        log(start, clock.nowMillis, scala.util.Success(""), requestName, modifiedSession, statsEngine)
    }
  }

}
