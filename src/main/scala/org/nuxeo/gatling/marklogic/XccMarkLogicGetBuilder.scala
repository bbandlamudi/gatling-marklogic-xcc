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

import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

case class XccMarkLogicGetBuilder(requestName: String, uri: Expression[String]) extends MarkLogicActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    new XccMarkLogicGetCall(requestName, uri, components(protocolComponentsRegistry), coreComponents.statsEngine, coreComponents.clock, next)
  }

}