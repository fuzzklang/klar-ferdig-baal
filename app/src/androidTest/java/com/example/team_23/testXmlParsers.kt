package com.example.team_23

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.api.MetAlertsRssParser
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
    fun testParseRssFeedWithItems() {
        // Mulig at denne testen inneholder og bør kortes ned.
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
    fun testParseRssFeedMissingEndTag() {
        // Forventer XmlPullParserException?
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

    }

    @Test
    fun testParseCapFeedNoItems() {

    }

    @Test
    fun testParseCapFeedWithItems() {

    }

    @Test
    fun testParseCapFeedMissingEndTag() {

    }

    @Test
    fun testParseCapOneInfoPerLanguage() {

    }

    @Test
    fun testParseCapSeveralInfoPerLanguage() {

    }
}