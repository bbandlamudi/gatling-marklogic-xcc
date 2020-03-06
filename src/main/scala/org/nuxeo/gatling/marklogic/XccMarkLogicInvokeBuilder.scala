/*
 * (C) Copyright 2020 MarkLogic Corporation
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
 *     Mads Hansen, MarkLogic Corporation
 */
package org.nuxeo.gatling.marklogic

import com.marklogic.xcc.ResultItem
import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

case class XccMarkLogicInvokeBuilder(requestName: Expression[String], moduleUri: Expression[String])
  extends MarkLogicActionBuilder[ResultItem]  {

  def mapResult[T](mapFunction: ResultItem => T) = XccMarkLogicInvokeWithMappingActionBuilder(requestName, moduleUri, mapFunction)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    new XccMarkLogicInvokeCall(requestName, moduleUri, queryParams.toList, components(protocolComponentsRegistry), checks.toList, x => x, coreComponents.statsEngine, coreComponents.clock, next)
  }

}

case class XccMarkLogicInvokeWithMappingActionBuilder[T](requestName: Expression[String], moduleUri: Expression[String], mapFunction: ResultItem => T)
  extends MarkLogicActionBuilder[T] {

  override def build(ctx: ScenarioContext, next:Action) : Action = {
    import ctx._
    new XccMarkLogicInvokeCall(requestName, moduleUri, queryParams.toList, components(ctx.protocolComponentsRegistry), checks.toList, mapFunction, coreComponents.statsEngine, coreComponents.clock, next)
  }
}