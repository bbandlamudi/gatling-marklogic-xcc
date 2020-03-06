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

import java.net.URI

import com.marklogic.xcc
import com.marklogic.xcc.exceptions.RequestException
import com.marklogic.xcc.{AdhocQuery, Content, ContentSourceFactory, ModuleInvoke, Request, ResultSequence}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.CoreComponents
import io.gatling.core.session.Session
import io.gatling.core.protocol.{Protocol, ProtocolKey}

case class XccMarkLogicProtocol(uri: String) extends Protocol {
  type Components = XccMarkLogicComponents
}

object XccMarkLogicProtocol {

  def apply(uri: String) = new XccMarkLogicProtocol(uri)

  val DefaultXccProtocol = new XccMarkLogicProtocol("xcc://root:root@localhost:8000")

  val XccMarkLogicProtocolKey : ProtocolKey[XccMarkLogicProtocol, XccMarkLogicComponents] = new ProtocolKey[XccMarkLogicProtocol, XccMarkLogicComponents] {

    def defaultProtocolValue(configuration: GatlingConfiguration) : XccMarkLogicProtocol =
      throw new IllegalStateException("Can't provide default value for XccMarkLogicProtocol")

    override def protocolClass: Class[Protocol] = classOf[XccMarkLogicProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def newComponents(coreComponents: CoreComponents): XccMarkLogicProtocol => XccMarkLogicComponents = {
        xccMarkLogicProtocol => XccMarkLogicComponents(xccMarkLogicProtocol)
    }

  }

}

case class XccMarkLogicComponents(xccMarkLogicProtocol: XccMarkLogicProtocol) extends ProtocolComponents {

  val session: xcc.Session = ContentSourceFactory.newContentSource(new URI(xccMarkLogicProtocol.uri)).newSession()

  def call(content: Content): String = {
    try {
      session.insertContent(content)
      ""
    } catch {
      case e: RequestException => e.getMessage
    }
  }

  def call(request: Request): ResultSequence = {
      session.submitRequest(request) //TODO: return ResultSequence, so that calls could be chained and responses analyzed?
  }

  def newAdhocQuery(query: String): AdhocQuery = {
    session.newAdhocQuery(query)
  }

  def newModuleInvoke(module: String): ModuleInvoke = {
    session.newModuleInvoke(module)
  }

  override def onStart: Session => Session = ProtocolComponents.NoopOnStart
  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit

}
