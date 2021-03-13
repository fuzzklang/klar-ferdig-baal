package com.example.team_23

import android.content.res.AssetManager
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class ExampleInstrumentedTest {
    /*@Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.team_23", appContext.packageName)
    }*/

    @Ignore  ("Skip RSS parsing for now")
    @Test
    fun testFetchRSSFromAPI() {
        // TODO: implement actual API call
        val tag = "testFetchRSSFromAPI"

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val endpoint = "https://api.met.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period"

        Log.d(tag, "Calling Parser. Expecting no RSS items")
        //val rssFeedStringNoAlert = assets.
        //testParseRss()   // Simulated API-call. No alerts.
        Log.d(tag, "Calling Parser. Expecting several RSS items")
        //testParseRss(rssFeedString)          // Simulated API-call. Alerts from May 2019.

        // Use to test when we can access API-Proxy
        /*runBlocking {
            try {
                Log.d(tag, "URL: $url")
                // Must add non-generic User-Agent to Get-request Header to access MetAlerts API
                // as described in Terms of Service. Not necessary towards IN2000-proxy
                //val httpResponse = Fuel.get(url).awaitString()

                val httpResponse = Fuel.get(url)
                        .header(Headers.USER_AGENT, "Team23-IN2000_IFI_V2021 fuzzklang@gmail.com").awaitString()
                Log.d(tag, httpResponse)
                testParseRSS(httpResponse)
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }*/
    }

    fun testParseRss(rssFeed: String) {
        val tag = "testParseRss"
        Log.d(tag, "running testParseRSS")
        //val inputStream: InputStream = rssFeed.byteInputStream()  // Not working?
        val inputStream: InputStream = rssFeed.byteInputStream()
        // Parses RSS feed and returns list of RssItem
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(inputStream) as List<RssItem>

        Log.d(tag, "PRINTING ALL RSS-ITEMS:")
        Log.d(tag,"Size rssItems-list: ${rssItems.size}")
        for (i in rssItems) {
            Log.d(tag, "RSS-Item: $i")
        }
    }

    // Fetches CAP-alert from links given in RSS-feed items
    @Test
    fun testFetchCapFromApi() {
        val tag = "testFetchCapFromApi"

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        Log.d(tag, "Calling Parser. Expecting ")
        //testParseCap(capAlert)          // Simulated API-call. Alerts from May 2019.

        // Use to test when we can access API-Proxy
        /*runBlocking {
            try {
                Log.d(tag, "URL: $url")
                // Must add non-generic User-Agent to Get-request Header to access MetAlerts API
                // as described in Terms of Service. Not necessary towards IN2000-proxy
                //val httpResponse = Fuel.get(url).awaitString()

                val httpResponse = Fuel.get(url)
                        .header(Headers.USER_AGENT, "Team23-IN2000_IFI_V2021 fuzzklang@gmail.com").awaitString()
                Log.d(tag, httpResponse)
                testParseRSS(httpResponse)
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }*/
    }

    fun testParseCap(capAlert: String) {
        val tag = "testParseCap"
        Log.d(tag, "running testParseCap")
        //val inputStream: InputStream = rssFeed.byteInputStream()  // Not working?
        val inputStream: InputStream = capAlert.byteInputStream()
        // Parses RSS feed and returns list of RssItem
        val rssItems: List<RssItem> = MetAlertsRssParser().parse(inputStream) as List<RssItem>

        Log.d(tag, "PRINTING ALL RSS-ITEMS:")
        Log.d(tag,"Size rssItems-list: ${rssItems.size}")
        for (i in rssItems) {
            Log.d(tag, "RSS-Item: $i")
        }
    }
}