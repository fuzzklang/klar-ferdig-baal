package com.example.team_23

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.team_23.api.Alert
import com.example.team_23.api.CapParser
import com.example.team_23.api.MetAlertsRssParser
import com.example.team_23.api.RssItem
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking
import org.junit.Ignore

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

    @Test
    fun testParsingLocalRssFiles() {
        val tag = "testParsingLocalXmlFiles"
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        var input: InputStream

        input = appContext.assets.open("minimalRssTest.xml")
        val rssFeedStringMinTest = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser on Minimal Test. Expecting no RSS items")
        parseRss(rssFeedStringMinTest)

        // Simulated API-call. From March 2021 (no alerts)
        input = appContext.assets.open("rssFeedNoAlert.xml")
        val rssFeedStringNoAlert = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser. Expecting no RSS items")
        parseRss(rssFeedStringNoAlert)

        // Simulated API-call. Alerts from May 2019.
        input = appContext.assets.open("rssFeedWithAlerts.xml")
        val rssFeedStringWithAlerts = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser. Expecting several RSS items")
        parseRss(rssFeedStringWithAlerts)
    }

    @Test
    fun testParsingLocalCapFiles() {
        val tag = "testParsingLocalCapFiles"
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val input = appContext.assets.open("capAlert.xml")
        val capAlertString = input.bufferedReader().use { it.readText() }
        Log.d(tag, "Calling Parser CAP Alert. Expecting corresponding objects")
        parseCap(capAlertString)
    }

    @Ignore  ("Skip fetching from API for now")
    @Test
    fun testFetchRSSFromAPI() {
        // TODO: implement actual API call
        val tag = "testFetchRSSFromAPI"

        val endpoint = "https://api.met.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period"

        // Use to test when we can access API-Proxy
        runBlocking {
            try {
                Log.d(tag, "URL: $url")
                // Must add non-generic User-Agent to Get-request Header to access MetAlerts API
                // as described in Terms of Service. Not necessary towards IN2000-proxy
                //val httpResponse = Fuel.get(url).awaitString()

                val httpResponse = Fuel.get(url)
                        .header(Headers.USER_AGENT, "Team23-IN2000_IFI_V2021 fuzzklang@gmail.com").awaitString()
                Log.d(tag, httpResponse)
                parseRss(httpResponse)
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }
    }

    // Fetches CAP-alert from links given in RSS-feed items
    @Ignore ("Skip fetching from API for now")
    @Test
    fun testFetchCapFromApi() {
        val tag = "testFetchCapFromApi"
        val url = "https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190521063816855.1909&period=2019-05"
        runBlocking {
            try {
                Log.d(tag, "URL: $url")
                // Must add non-generic User-Agent to Get-request Header to access MetAlerts API
                // as described in Terms of Service. Not necessary towards IN2000-proxy
                //val httpResponse = Fuel.get(url).awaitString()  // Alternative
                val httpResponse = Fuel.get(url)
                        .header(Headers.USER_AGENT, "Team23-IN2000_IFI_V2021 fuzzklang@gmail.com").awaitString()
                Log.d(tag, httpResponse)
                parseCap(httpResponse)
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }
    }

    private fun parseRss(rssFeed: String) {
        val tag = "testParseRss"
        Log.d(tag, "running testParseRSS")
        val inputStream: InputStream = rssFeed.byteInputStream()
        // Parses RSS feed and returns list of RssItem
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(inputStream)

        Log.d(tag, "PRINTING ALL RSS-ITEMS:")
        Log.d(tag,"Size rssItems-list: ${rssItems.size}")
        for (i in rssItems) {
            Log.d(tag, "RSS-Item: $i")
        }
    }

    private fun parseCap(capAlert: String) {
        val tag = "testParseCap"
        Log.d(tag, "running testParseCap")
        //val inputStream: InputStream = rssFeed.byteInputStream()  // Not working?
        val inputStream: InputStream = capAlert.byteInputStream()
        // Parses RSS feed and returns list of RssItem
        val alert: Alert = CapParser().parse(inputStream)
        Log.d(tag, "ALERT: ${alert}")
    }
}