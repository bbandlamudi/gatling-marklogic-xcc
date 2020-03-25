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
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CompletableFuture, CompletionStage, ExecutorService, Executors, ThreadFactory}

import akka.Done
import akka.actor.ActorSystem
import com.marklogic.xcc.exceptions.RequestException
import com.marklogic.xcc.{AdhocQuery, Content, ContentSourceFactory, ModuleInvoke, Request, ResultSequence}
import com.typesafe.scalalogging.{StrictLogging}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.CoreComponents
import io.gatling.core.session.Session
import io.gatling.core.protocol.{Protocol, ProtocolKey}

import scala.collection.mutable

case class XccMarkLogicProtocol(uri: String) extends Protocol {
  type Components = XccMarkLogicComponents
  val contentSource = ContentSourceFactory.newContentSource(new URI(uri))
  val xccUri = this.uri
}

object XccMarkLogicProtocol extends StrictLogging {

  def apply(uri: String) = new XccMarkLogicProtocol(uri)

  val DefaultXccProtocol = new XccMarkLogicProtocol("xcc://root:root@localhost:8000")

  val XccMarkLogicProtocolKey : ProtocolKey[XccMarkLogicProtocol, XccMarkLogicComponents] = new ProtocolKey[XccMarkLogicProtocol, XccMarkLogicComponents] {
    type Protocol = XccMarkLogicProtocol
    type Components = XccMarkLogicComponents

    def defaultProtocolValue(configuration: GatlingConfiguration) : XccMarkLogicProtocol =
      throw new IllegalStateException("Can't provide default value for XccMarkLogicProtocol")

    def protocolClass: Class[io.gatling.core.protocol.Protocol] =
      classOf[XccMarkLogicProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def newComponents(coreComponents: CoreComponents): XccMarkLogicProtocol => XccMarkLogicComponents = {
        xccMarkLogicProtocol => {
          val system = ActorSystem(xccMarkLogicProtocol.contentSource.getConnectionProvider.getHostName + xccMarkLogicProtocol.contentSource.getConnectionProvider.getPort)
          XccMarkLogicComponents.componentsFor(xccMarkLogicProtocol, system)
        }
    }
  }
}

object XccMarkLogicComponents {
  private val componentsCache = mutable.Map[ActorSystem, XccMarkLogicComponents]()

  def componentsFor(xccMarkLogicProtocol: XccMarkLogicProtocol, system: ActorSystem): XccMarkLogicComponents = synchronized {
    if (componentsCache.contains(system)) {
      // Reuse shared components to avoid creating multiple loggers and executor services in the same simulation
      val shared: XccMarkLogicComponents = componentsCache(system)
      XccMarkLogicComponents(xccMarkLogicProtocol, shared.xccExecutorService)
    } else {
      // In integration tests, multiple simulations may be submitted to different Gatling instances of the same JVM
      // Make sure that each actor system gets it own set of shared components
      // This solves the "one set of components per scenario" problem as they are shared across scenarios
      // This also solves the "one set of components per JVM" problem as they are only shared per actor system

      // Create one executor service to handle the plugin tasks
      val xccExecutorService = Executors.newCachedThreadPool(
        new ThreadFactory(){
          val identifierGenerator = new AtomicLong()
          override def newThread(r: Runnable): Thread =
            new Thread(r, "gating-marklogic-xcc-plugin-" + identifierGenerator.getAndIncrement() )
        }
      )

      val xccMarkLogicComponents = XccMarkLogicComponents(xccMarkLogicProtocol, xccExecutorService)
      componentsCache.put(system, xccMarkLogicComponents)
      system.registerOnTermination(xccMarkLogicComponents.shutdown())
      xccMarkLogicComponents
    }
  }
}

case class XccMarkLogicComponents(xccMarkLogicProtocol: XccMarkLogicProtocol, xccExecutorService: ExecutorService)
  extends ProtocolComponents with StrictLogging {

  private val xccUri = new URI(xccMarkLogicProtocol.xccUri)

  // Currently, we DONT want to reuse ContentSource, we want HTTP 401 for performance test runs, to mimic current application behavior
  def newSession(): com.marklogic.xcc.Session = ContentSourceFactory.newContentSource(xccUri).newSession

  def call(content: Content): String = {
    var xccSession = None: Option[com.marklogic.xcc.Session]
    try {
      xccSession =  Some(newSession)
      xccSession.headOption.get.insertContent(content)
      ""
    } catch {
      case e: RequestException => {
        e.getMessage}
    } finally {
      xccSession.headOption.get.close()
    }
  }

  def call(xccSession: com.marklogic.xcc.Session, request: Request): ResultSequence = {
    try {
      val result = xccSession.submitRequest(request) //TODO: return ResultSequence, so that calls could be chained and responses analyzed?
      result
    } finally {
      xccSession.close()
    }
  }

  def newAdhocQuery(xccSession: com.marklogic.xcc.Session, query: String): AdhocQuery = xccSession.newAdhocQuery(query)

  def newModuleInvoke(xccSession: com.marklogic.xcc.Session, module: String): ModuleInvoke = xccSession.newModuleInvoke(module)

  override def onStart: Session => Session = ProtocolComponents.NoopOnStart
  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit

  def shutdown(): CompletionStage[Done] = {
    logger.info("Shutting down thread pool")
    val missed = xccExecutorService.shutdownNow().size()
    if (missed > 0){
      logger.warn("{} tasks were not completed because of shutdown", missed)
    }
    logger.info("Shut down complete")
    CompletableFuture.completedFuture(Done)
  }

}
