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

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen

class XccMarkLogicGetCall(requestName: String, uri: Expression[String], xccMarkLogicComponents:XccMarkLogicComponents, statsEngine: StatsEngine, clock: Clock, val  next:Action)
  extends Action with ChainableAction with NameGen {

  override def name: String = genName("xccMarkLogicGetCall")

  override def execute(session: Session): Unit = {

    val start = clock.nowMillis
    val request = xccMarkLogicComponents.newAdhocQuery(s"fn:doc('${uri(session).toOption.get}')")
    val result = xccMarkLogicComponents.call(request)
    val end = clock.nowMillis

    if (result == "")
      statsEngine.logResponse(session, requestName, start, end, OK, None, None)
    else
      statsEngine.logResponse(session, requestName, start, end, KO, None, Some(result))

    next ! session
  }

}
