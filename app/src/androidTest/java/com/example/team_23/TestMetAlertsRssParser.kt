package com.example.team_23

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.dataclasses.metalerts_dataclasses.RssItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream

/* Kjører tester på MetAlertsRssParseren.
 * Henter XML-testfiler fra assets-mappen (debug), og parser disse med parserne.
 * XML-testfilene er basert på RSS-feeden fra MetAlerts, og noen er gjort ugyldige/feilformatert.
 *
 * Testfilene har endingen .test kun for å unngå at Android Studio ga masse feilmeldinger under kompilering.
 * Med filendingen .xml tolket Android Studio testfilene som feilformatert XML og klager på dette.
 */
@RunWith(AndroidJUnit4::class)
class TestMetAlertsRssParser {
    //val tag = "TestMetAlertsRssParser"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen
    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

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
}