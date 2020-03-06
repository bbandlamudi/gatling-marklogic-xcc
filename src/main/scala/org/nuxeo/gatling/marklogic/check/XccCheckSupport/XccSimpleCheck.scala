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
package org.nuxeo.gatling.marklogic.check.XccCheckSupport

import java.util

import com.marklogic.xcc.ResultItem
import io.gatling.commons.validation.{Failure, Validation}
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session
import org.nuxeo.gatling.marklogic.XccCheck

case class XccSimpleCheck(func: List[ResultItem] => Boolean) extends XccCheck[ResultItem] {

  override def check(response: List[ResultItem], session: Session, preparedCache: util.Map[Any, Any]): Validation[CheckResult] = {
    if (func(response)) {
      CheckResult.NoopCheckResultSuccess
    } else {
      Failure("XCC check failed")
    }
  }

}
