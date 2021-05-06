package com.example.team_23

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Area
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Info
import com.example.team_23.model.dataclasses.metalerts_dataclasses.RssItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream

/* Kjører tester på parserne (MetAlertsRssParser og CapParser).
 * Henter XML-testfiler fra assets-mappen (debug), og parser disse med parserne.
 * XML-testfilene er basert på RSS-feeden og CAP-varslene fra MetAlerts, og noen er gjort ugyldige/feilformatert.
 *
 * Testfilene har endingen .test kun for å unngå at Android Studio ga masse feilmeldinger under kompilering.
 * Med filendingen .xml tolket Android Studio testfilene som feilformatert XML og klager på dette.
 */
@RunWith(AndroidJUnit4::class)
class TestXmlParsers {
    //val tag = "testXmlParsers"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen
    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    /* =============================
     * ===== RSS-parser-tester =====
     * =============================
     */
    @Test
    fun testParseRssFeedWithItems() {
        // Input: gyldig formatert RSS-feed med varselinformasjon.
        // Forventer: ingen feil fra parser, objekter (flere rssItem) hvor klasse-attributter er satt riktig.
        // [Mulig at denne testen inneholder for mye og bør splittes opp i flere tester]
        val input: InputStream = appContext.assets.open("testRssFeedWithAlerts.xml.test")
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
        // Input: tom XML-fil.
        // Forventer: at XmlPullParserException kastes fra Parser.
        val input: InputStream = appContext.assets.open("testEmptyFile.xml.test")
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
        // Input: RssFeed uten Item (altså gyldig RSS-feed uten varsler).
        // Forventer: Tom liste i retur, ingen feilmeldinger.
        val input: InputStream = appContext.assets.open("testRssFeedNoItems.xml.test")
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(input)
        assertEquals(0, rssItems.size)
    }

    @Test
    fun testParseRssFeedMissingEndTag() {
        // Input: feilformatert XML (manglende end-tags).
        // Forventer: at XmlPullParserException kastes.
        val input: InputStream = appContext.assets.open("testRssFeedMissingEndTag.xml.test")
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
        // Input: feilformatert XML (rot-tag 'rss' er ikke lukket, '>' mangler).
        // Forventer: at XmlPullParserException kastes.
        val input: InputStream = appContext.assets.open("testMalformattedRssFeed.xml.test")
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
        // Input: tom XML-fil.
        // Forventer: at XmlPullParserException kastes fra Parser.
        val input: InputStream = appContext.assets.open("testEmptyFile.xml.test")
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
        // Input: feilformatert XML, mangler end-tags.
        // Forventer: at XmlPullParserException kastes.
        val input: InputStream = appContext.assets.open("testCapAlertMissingEndTag.xml.test")
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
        // Input: gyldig XML (Cap-varsel)
        // Sjekker at parser setter varselets attributter korrekt ('identifier', 'sent', 'status' og 'msgType')
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        assertEquals("test identifier 2.49.0.1.578.0.190521063816855.1909", capAlert.identifier)
        assertEquals("Test 2019-05-21T06:38:16+00:00", capAlert.sent)
        assertEquals("Test Actual", capAlert.status)
        assertEquals("Test Update", capAlert.msgType)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoNorwegian() {
        // Input: gyldig varsel.
        // Sjekker at attributtene i Info-elementet (norsk) settes korrekt av parser.
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoNo
        assertEquals("no", info.lang)
        assertEquals("Skogbrannfare", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Vær forsiktig med åpen ild.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAttributesInfoEnglish() {
        // Input: gyldig varsel.
        // Sjekker at attributtene i Info-elementet (engelsk) settes korrekt av parser.
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoEn
        assertEquals("en-GB", info.lang)
        assertEquals("Forest fire danger", info.event)
        assertEquals("Monitor", info.responseType)
        assertEquals("Be careful with open fire.", info.instruction)
    }

    @Test
    fun testParseCapFeedCheckAreaAttributes() {
        // Input: gyldig varsel.
        // Sjekker at Area-attributter settes korrekt av parser.
        // Tester kun Area i Info-elementet som er på norsk nå.
        // Tester for øyeblikket ikke area.polygon (streng med koordinater)
        val input: InputStream = appContext.assets.open("testCapAlert.xml.test")
        val capAlert: Alert = CapParser().parse(input)
        val info: Info = capAlert.infoNo
        val area: Area = info.area
        assertEquals("Hordaland", area.areaDesc)
    }
}