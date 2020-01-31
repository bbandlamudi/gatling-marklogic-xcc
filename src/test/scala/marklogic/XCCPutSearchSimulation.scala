package marklogic

import com.marklogic.xcc.ContentFactory
import io.gatling.core.Predef._
import org.nuxeo.gatling.marklogic.Predef._

class XCCPutSearchSimulation extends Simulation {
  val uri = "xcc://admin:admin@localhost:8000"
  val xccConf = xcc.uri(uri)

  val mlContent = ContentFactory.newContent("/${docId}.xml", """<document xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><ecm__name xsi:type="xs:string">doc</ecm__name><ecm__racl><nxml__item xsi:type="xs:string">Administrator</nxml__item><nxml__item xsi:type="xs:string">administrators</nxml__item><nxml__item xsi:type="xs:string">members</nxml__item></ecm__racl><ecm__primaryType xsi:type="xs:string">MyDocType</ecm__primaryType><my__testDefault xsi:type="xs:string">the default value</my__testDefault><my__testDefaultLong xsi:type="xs:long">0</my__testDefaultLong><ecm__id xsi:type="xs:string">${docId}</ecm__id><ecm__parentId xsi:type="xs:string">00000000-0000-0000-0000-000000000000</ecm__parentId><ecm__ancestorIds><nxml__item xsi:type="xs:string">00000000-0000-0000-0000-000000000000</nxml__item></ecm__ancestorIds><my__string xsi:type="xs:string">foo</my__string></document>""", null)

  val random = new util.Random
  val feeder = Iterator.continually(Map("docId" -> random.nextLong()))
  val insert = xcc("insert")
    .uri("/${docId}.xml")
    .insert("""<document xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><ecm__name xsi:type="xs:string">doc</ecm__name><ecm__racl><nxml__item xsi:type="xs:string">Administrator</nxml__item><nxml__item xsi:type="xs:string">administrators</nxml__item><nxml__item xsi:type="xs:string">members</nxml__item></ecm__racl><ecm__primaryType xsi:type="xs:string">MyDocType</ecm__primaryType><my__testDefault xsi:type="xs:string">the default value</my__testDefault><my__testDefaultLong xsi:type="xs:long">0</my__testDefaultLong><ecm__id xsi:type="xs:string">${docId}</ecm__id><ecm__parentId xsi:type="xs:string">00000000-0000-0000-0000-000000000000</ecm__parentId><ecm__ancestorIds><nxml__item xsi:type="xs:string">00000000-0000-0000-0000-000000000000</nxml__item></ecm__ancestorIds><my__string xsi:type="xs:string">foo</my__string></document>""")

  val search = xcc("search")
    .search("""xquery version "1.0-ml";
               import module namespace search = "http://marklogic.com/appservices/search"
                    at "/MarkLogic/appservices/search/search.xqy";

               let $query :=
               <query xmlns="http://marklogic.com/appservices/search" xmlns:search="http://marklogic.com/appservices/search" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <or-query>
                  <value-query type="string">
                    <element ns="" name="ecm__parentId"/>
                    <text>b2fa5547-91ad-4123-9225-c82e609aee49</text>
                  </value-query>
                  <container-query>
                    <element ns="" name="ecm__parentId"/>
                    <value-query type="string">
                      <element ns="" name="nxml__item"/>
                      <text>b2fa5547-91ad-4123-9225-c82e609aee49</text>
                    </value-query>
                  </container-query>
                </or-query>
                <or-query>
                  <value-query type="string">
                    <element ns="" name="ecm__name"/>
                    <text>ParallelDocumentReview</text>
                  </value-query>
                  <container-query>
                    <element ns="" name="ecm__name"/>
                    <value-query type="string">
                      <element ns="" name="nxml__item"/>
                      <text>ParallelDocumentReview</text>
                    </value-query>
                  </container-query>
                </or-query>
                <not-query>
                  <or-query>
                    <value-query type="string">
                      <element ns="" name="ecm__id"/>
                      <text>93cf0215-f07a-46ef-be5a-04065e90c754</text>
                      <text>edb60839-ea69-4819-a649-cc6837d51f4f</text>
                      <text>b2fa5547-91ad-4123-9225-c82e609aee49</text>
                      <text>070c672c-382a-4d64-a2fe-9c19a6753860</text>
                      <text>c3563452-5855-41fc-8e7a-2b7d2555f77a</text>
                      <text>00000000-0000-0000-0000-000000000000</text>
                      <text>599f3ac6-61af-43ca-827f-cdb66c8cda8d</text>
                    </value-query>
                  </or-query>
                </not-query>
               </query>
               return
               search:resolve($query)""")

  setUp(scenario("insert&search")
    .during(40, "i") {
      randomSwitch(
        50.0 -> feed(feeder).exec(insert),
        50.0 -> exec(search)
      )
    }
    .inject(atOnceUsers(20)))
    .protocols(xccConf)
}
