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

import io.gatling.core.check.{CheckMaterializer, DefaultFindCheckBuilder, Extractor, Preparer}
import org.nuxeo.gatling.marklogic.XccCheck
import io.gatling.commons.validation.{Validation, _}
import io.gatling.core.session.{Expression, _}

object XccManyTCheck {

  trait XccManyTCheckType

  def manyTPreparer[T]: Preparer[List[T], List[T]] = something => something.success

  def manyTCheckMaterializer[T]: CheckMaterializer[XccManyTCheckType, XccCheck[T], List[T], List[T]] =
    new CheckMaterializer[XccManyTCheckType, XccCheck[T], List[T], List[T]](identity) {

      override protected def preparer: Preparer[List[T], List[T]] = manyTPreparer

    }

  def manyTExtractor[T]: Expression[Extractor[List[T], List[T]]] =
    new Extractor[List[T], List[T]] {
      override def name: String = "manyT"

      override def apply(prepared: List[T]): Validation[Option[List[T]]] = Some(prepared).success

      override def arity: String = "findAll"
    }.expressionSuccess

  def manyTResults[T] = new DefaultFindCheckBuilder[XccManyTCheckType, List[T], List[T]](
    manyTExtractor,
    displayActualValue = true
  )

}
