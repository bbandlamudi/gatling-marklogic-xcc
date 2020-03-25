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

import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.check._
import io.gatling.core.session._
import org.nuxeo.gatling.marklogic.XccCheck

object XccSingleTCheck {

  trait XccSingleTCheckType

  def singleTPreparer[T]: Preparer[List[T], T] = something => something.headOption.toValidation("Empty Sequence")

  def singleTCheckMaterializer[T]: CheckMaterializer[XccSingleTCheckType, XccCheck[T], List[T], T] =
    new CheckMaterializer[XccSingleTCheckType, XccCheck[T], List[T], T](identity) {

      override protected def preparer: Preparer[List[T], T] = singleTPreparer[T]

    }

  def singleTExtractor[T]: Expression[Extractor[T, T]] =
    new Extractor[T, T] {
      override def name: String = "singleT"

      override def apply(prepared: T): Validation[Option[T]] = Some(prepared).success

      override def arity: String = "single"
    }.expressionSuccess

  def singleTResult[T] = new DefaultFindCheckBuilder[XccSingleTCheckType, T, T](
    singleTExtractor[T],
    displayActualValue = true
  )
}
