package com.example.team_23

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.team_23.api.dataclasses.Alert
import com.example.team_23.api.CapParser
import com.example.team_23.api.MetAlertsRssParser
import com.example.team_23.api.dataclasses.RssItem
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import java.io.InputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ApiAndXmlParseTests {
    // From Example file:
    /*@Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.team_23", appContext.packageName)
    }*/

    // Test parsing on XML files in assets-folder.
    // These are exact copies of what would be returned by an API-call to MetAlerts
    // (depending on parameters given)
    @Test
    fun testParsingLocalRssFiles() {
        val tag = "testParsingLocalRssFiles"
        // Context of the app under test.
        // Necessary to access assets-folder.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        var input: InputStream

        input = appContext.assets.open("minimalRssTest.xml")
        val rssFeedStringMinTest = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser on Minimal Test. Expecting no RSS items")
        parseAndPrintRss(rssFeedStringMinTest)

        // Simulated API-call. From March 2021 (no alerts)
        input = appContext.assets.open("rssFeedNoAlert.xml")
        val rssFeedStringNoAlert = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser. Expecting no RSS items")
        parseAndPrintRss(rssFeedStringNoAlert)

        // Simulated API-call. Alerts from May 2019.
        input = appContext.assets.open("rssFeedWithAlerts.xml")
        val rssFeedStringWithAlerts = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser. Expecting several RSS items")
        parseAndPrintRss(rssFeedStringWithAlerts)
    }

    // Parses local CAP-alert (XML-file). Does not make connection to network/API.
    @Test
    fun testParsingLocalCapFiles() {
        val tag = "testParsingLocalCapFiles"
        // Context of the app under test.
        // Necessary to access assets-folder.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val input = appContext.assets.open("capAlert.xml")
        val capAlertString = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser CAP Alert. Expecting corresponding objects")
        parseAndPrintCap(capAlertString)
    }

    // Fetches RSS feed from proxy (network), parses feed and prints content.
    // If there are any alerts, the RSS feed contains an item for each alert,
    // and each item contains a link to a detailed alert in CAP-format (also XML).
    @Test
    fun testFetchRSSFromAPI() {
        val tag = "testFetchRSSFromAPI"

        val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period&$otherParams"

        // Access data from API
        runBlocking {
            try {
                Log.d(tag, "URL: $url")
                val httpResponse = Fuel.get(url).awaitString()
                parseAndPrintRss(httpResponse) // parses and prints Rss-items
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }
    }

    // Fetches a CAP-alert from links given in RSS-feed items
    @Test
    fun testFetchCapFromApi() {
        val tag = "testFetchCapFromApi"
        // endpoint currently hard-coded:
        val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190521063816855.1909&period=2019-05"
        runBlocking {
            // TODO: gjør om til CoroutineScope/asynkront kall? Eller beholde som blokkerende kall?
            try {
                Log.d(tag, "URL: $url")
                val httpResponse = Fuel.get(url).awaitString()  // Alternative
                parseAndPrintCap(httpResponse)  // parses and prints Cap-alert
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }
    }

    // Helper function
    private fun parseAndPrintRss(rssFeed: String) {
        val tag = "parseRss"
        val inputStream: InputStream = rssFeed.byteInputStream()
        // Parses RSS feed and returns list of RssItem
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(inputStream)

        Log.d(tag, "PRINTING ALL RSS-ITEMS:")
        Log.d(tag,"Size rssItems-list: ${rssItems.size}")
        for (i in rssItems) {
            Log.d(tag, "RSS-Item: $i")
        }
    }

    // Helper function
    private fun parseAndPrintCap(capAlert: String) {
        val tag = "parseCap"
        val inputStream: InputStream = capAlert.byteInputStream()
        val alert: Alert = CapParser().parse(inputStream) // Parses CAP alert and returns Alert instance
        Log.d(tag, "[AFTER PARSING] ALERT: $alert")
    }
}