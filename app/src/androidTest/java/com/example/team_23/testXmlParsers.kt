package com.example.team_23

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.api.CapParser
import com.example.team_23.api.MetAlertsRssParser
import com.example.team_23.api.dataclasses.Alert
import com.example.team_23.api.dataclasses.Area
import com.example.team_23.api.dataclasses.Info
import com.example.team_23.api.dataclasses.RssItem
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.lang.IllegalStateException

/* Kjører tester på parserne (MetAlertsRssParser og CapParser).
 * Henter XML-testfiler fra assets-mappen.
 * Disse er basert på RSS-feeden og CAP-varslene fra MetAlerts
 * TODO: vurder å flytte testfiler til en egen debug-mappe
 */
@RunWith(AndroidJUnit4::class)
class testXmlParsers {
    val tag = "testXmlParsers"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    /* =============================
     * ===== RSS-parser-tester =====
     * =============================
     */
    @Test
    fun testParseRssFeedWithItems() {
        // Mulig at denne testen inneholder for mye og bør splittes opp i flere tester
        val input: InputStream = appContext.assets.open("testRssFeedWithAlerts.xml")
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(input)
        assertEquals(3, rssItems.size)
        val rssItem1 = rssItems[0]
        val rssItem2 = rssItems[1]
        val rssItem3 = rssItems[2]
        assertEquals("Test Item-tittel 1", rssItem1.title)
        assertEquals("Test Item-tittel 2", rssItem2.title)
        assertEquals("Test Item-tittel 3", rssItem3.title)
        assertEquals("Test beskrivelse 1", rssItem1.description)
        assertEquals("Test beskrivelse 2", rssItem2.description)
        assertEquals("Test beskrivelse 3", rssItem3.description)
        val expectedLink1 = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190521063816855.1909&period=2019-05"
        val expectedLink2 = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190519070744754.1907&period=2019-05"
        val expectedLink3 = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190519070524042.1906&period=2019-05"
        assertEquals(expectedLink1, rssItem1.link)
        assertEquals(expectedLink2, rssItem2.link)
        assertEquals(expectedLink3, rssItem3.link)
    }

    @Test
    fun testParseRssFeedEmptyStream() {
        val input: InputStream = appContext.assets.open("testEmptyFile.xml")
        var exceptionThrown = false
        try {
            MetAlertsRssParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun testParseRssFeedNoItems() {
        val input: InputStream = appContext.assets.open("testRssFeedNoItems.xml")
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(input)
        assertEquals(0, rssItems.size)
    }

    @Test
    fun testParseRssFeedMissingEndTag() {
        // Forventer XmlPullParserException
        val input: InputStream = appContext.assets.open("testRssFeedMissingEndTag.xml")
        var exceptionThrown = false
        try {
            MetAlertsRssParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun testParseRssFeedMalformattedXml() {
        // Forventer XmlPullParserException
        val input: InputStream = appContext.assets.open("testMalformattedRssFeed.xml")
        var exceptionThrown = false
        try {
            MetAlertsRssParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }


    /* =============================
     * ===== Cap-parser-tester =====
     * =============================
     */
    @Test
    fun testParseCapEmptyStream() {
        val input: InputStream = appContext.assets.open("testEmptyFile.xml")
        var exceptionThrown = false
        try {
            CapParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun testParseCapFeedMissingEndTag() {
        // Forventer XmlPullParserException
        val input: InputStream = appContext.assets.open("testCapAlertMissingEndTag.xml")
        var exceptionThrown = false
        try {
            CapParser().parse(input)
        } catch (exception: XmlPullParserException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun testParseCapFeedCheckAttributes() {
        val input: InputStream = appContext.assets.open("testCapAlert.xml")
        val capAlert: Alert = CapParser().parse(input)
        assertEquals("test identifier 2.49.0.1.578.0.190521063816855.1909", capAlert.identifier)
        assertEquals("Test 2019-05-21T06:38:16+00:00", capAlert.sent)
        assertEquals("Test Actual", capAlert.status)
        assertEquals("Test Update", capAlert.msgType)
    }

    @Test
    fun testParseCapFeedCheckInfoCount() {
        val input: InputStream = appContext.assets.open("testCapAlert.xml")
        val capAlert: Alert = CapParser().parse(input)
        assertEquals(1, capAlert.infoItemsNo.size)
        assertEquals(1, capAlert.infoItemsEn.size)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoNorwegian() {
        val input: InputStream = appContext.assets.open("testCapAlert.xml")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoItemsNo[0]
        assertEquals("no", info.lang)
        assertEquals("Skogbrannfare", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Vær forsiktig med åpen ild.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoEnglish() {
        val input: InputStream = appContext.assets.open("testCapAlert.xml")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoItemsEn[0]
        assertEquals("en-GB", info.lang)
        assertEquals("Forest fire danger", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Be careful with open fire.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAreaAttributes() {
        // Tester kun Area i info-elementet som er på norsk.
        // Tester for øyeblikket ikke area.polygon
        val input: InputStream = appContext.assets.open("testCapAlert.xml")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoItemsNo[0]
        val area: Area = info.area
        assertEquals("Hordaland", area.areaDesc)
    }

    @Test
    fun testParseCapSeveralInfosNorwegian() {
        val input: InputStream = appContext.assets.open("testCapAlertSeveralInfoElements.xml")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoItemsNo[1]
        assertEquals("no", info.lang)
        assertEquals("Test Skogbrannfare 2", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Vær forsiktig med åpen ild.", info.instruction)
    }

    @Test
    fun testParseCapSeveralInfosEnglish() {
        val input: InputStream = appContext.assets.open("testCapAlertSeveralInfoElements.xml")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoItemsEn[1]
        assertEquals("en-GB", info.lang)
        assertEquals("Test Forest fire danger 2", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Be careful with open fire.", info.instruction)
    }
}