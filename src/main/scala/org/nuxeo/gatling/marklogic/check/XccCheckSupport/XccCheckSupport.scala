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

import com.marklogic.xcc.ResultItem
import io.gatling.core.check.{CheckBuilder, CheckMaterializer, DefaultFindCheckBuilder, FindCheckBuilder}
import org.nuxeo.gatling.marklogic.XccCheck

import scala.annotation.implicitNotFound

trait XccCheckSupport {

  def simpleCheck = XccSimpleCheck

  @Deprecated
  val xccSingleResponse = singleResponse[ResultItem]

  def singleResponse[T]: DefaultFindCheckBuilder[XccSingleTCheck.XccSingleTCheckType, T, T] = XccSingleTCheck.singleTResult[T]

  implicit def xccSingleTCheckMaterializer[T]: CheckMaterializer[XccSingleTCheck.XccSingleTCheckType, XccCheck[T], List[T], T] = XccSingleTCheck.singleTCheckMaterializer[T]

  @Deprecated
  val xccManyResponse = manyResponse[ResultItem]

  def manyResponse[T]: DefaultFindCheckBuilder[XccManyTCheck.XccManyTCheckType, List[T], List[T]] = XccManyTCheck.manyTResults[T]

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for XCC.")
  implicit def findCheckBuilder2XccCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, P, X])(implicit CheckMaterializer: CheckMaterializer[A, XccCheck[P], List[P], P]): XccCheck[P] =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for XCC.")
  implicit def checkBuilder2XccCheck[A, P, X](checkBuilder: CheckBuilder[A, P, X])(implicit materializer: CheckMaterializer[A, XccCheck[P], List[P], P]): XccCheck[P] =
    checkBuilder.build(materializer)

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for XCC.")
  implicit def findManyCheckBuilder2XccCheck[A, P, X](findCheckBuilder: FindCheckBuilder[A, List[P], X])(implicit CheckMaterializer: CheckMaterializer[A, XccCheck[P], List[P], List[P]]): XccCheck[P] =
    findCheckBuilder.find.exists

  @implicitNotFound("Could not find a CheckMaterializer. This check might not be valid for XCC.")
  implicit def checkManyBuilder2XccCheck[A, P, X](checkBuilder: CheckBuilder[A, List[P], X])(implicit materializer: CheckMaterializer[A, XccCheck[P], List[P], List[P]]): XccCheck[P] =
    checkBuilder.build(materializer)

}
