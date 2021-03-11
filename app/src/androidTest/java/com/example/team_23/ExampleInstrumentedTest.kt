package com.example.team_23

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
    @Test
    fun testFetchRSSFromAPI() {
        val tag = "testFetchRSSFromAPI"

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val endpoint = "https://api.met.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period"

        runBlocking {
            try {
                Log.d(tag, "URL: $url")
                // Must add non-generic User-Agent to Get-request Header to access MetAlerts API
                // as described in Terms of Service. Not necessary towards IN2000-proxy
                val httpResponse = Fuel.get(url)
                        .header(Headers.USER_AGENT, "Team23-IN2000_IFI_V2021 fuzzklang@gmail.com").awaitString()
                //val httpResponse = Fuel.get(url).awaitString()
                Log.d(tag, httpResponse)
                testParseRSS(httpResponse)
            } catch (exception: Exception) {
                Log.d(tag,"A network request exception was thrown: ${exception.message}")
            }
        }

    }

    fun testParseRSS(rssFeed: String) {
        val tag = "testParseRSS"
        Log.d(tag,"FEED:\n\n$rssFeed")
    }
}


// For testing purposes, to avoid accessing MET too many times. (as we wait for proxy-fix)
// Data from May 2019.
val rssFeedString = """
    <?xml version="1.0"?>
    <rss version="2.0">
      <channel>
        <title>MET farevarsel 2019-05</title>
        <link>https://api.met.no/?period=2019-05</link>
        <description>Farevarsler fra Meteorologisk institutt</description>
        <language>no</language>
        <copyright>Copyright The Norwegian Meteorological Institute, licensed under Norwegian license for public data (NLOD) and Creative Commons 4.0 BY</copyright>
        <pubDate>Thu, 11 Mar 2021 15:27:55 +0000</pubDate>
        <lastBuildDate>Thu, 11 Mar 2021 15:27:55 +0000</lastBuildDate>
        <category>Met</category>
        <generator>XML::LibXML::Generator 0.1</generator>
        <docs>http://blogs.law.harvard.edu/tech/rss</docs>
        <image>
          <title>MET farevarsel</title>
          <link>https://api.met.no</link>
          <url>https://api.met.no/images/logo_2013_no.png</url>
        </image>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Hordaland, 19 May 07:00 UTC til 22 May 06:00 UTC.</title>
          <description>Update: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning. </description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190521063816855.1909&amp;period=2019-05</link>
          <author>post@met.no (The Norwegian Meteorological Institute)</author>
          <category>Met</category>
          <guid>2.49.0.1.578.0.190521063816855.1909</guid>
          <pubDate>Tue, 21 May 2019 06:38:16 +0000</pubDate>
        </item>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Sogn og Fjordane, 19 May 07:00 UTC til 22 May 22:00 UTC.</title>
          <description>Update: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning.</description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190519070744754.1907&amp;period=2019-05</link>
          <author>post@met.no (The Norwegian Meteorological Institute)</author>
          <category>Met</category>
          <guid>2.49.0.1.578.0.190519070744754.1907</guid>
          <pubDate>Sun, 19 May 2019 07:07:44 +0000</pubDate>
        </item>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Hordaland, 19 May 07:00 UTC til 22 May 22:00 UTC.</title>
          <description>Update: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning. </description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190519070524042.1906&amp;period=2019-05</link>
          <author>post@met.no (The Norwegian Meteorological Institute)</author>
          <category>Met</category>
          <guid>2.49.0.1.578.0.190519070524042.1906</guid>
          <pubDate>Sun, 19 May 2019 07:05:24 +0000</pubDate>
        </item>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Hordaland, 18 May 09:00 UTC til 20 May 09:00 UTC.</title>
          <description>Alert: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning. </description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190518091251397.1905&amp;period=2019-05</link>
          <author>post@met.no (The Norwegian Meteorological Institute)</author>
          <category>Met</category>
          <guid>2.49.0.1.578.0.190518091251397.1905</guid>
          <pubDate>Sat, 18 May 2019 09:12:51 +0000</pubDate>
        </item>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Sogn og Fjordane, 17 May 07:00 UTC til 20 May 09:00 UTC.</title>
          <description>Alert: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning</description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190517064153070.1904&amp;period=2019-05</link>
          <author>post@met.no (The Norwegian Meteorological Institute)</author>
          <category>Met</category>
          <guid>2.49.0.1.578.0.190517064153070.1904</guid>
          <pubDate>Fri, 17 May 2019 06:41:53 +0000</pubDate>
        </item>
        <item>
          <title>Skogbrannfare, gult niv&#xE5;, Hordaland, 16 May 14:00 UTC til 18 May 04:00 UTC.</title>
          <description>Alert: Det er lokal skogbrannfare inntil det kommer nedb&#xF8;r av betydning.</description>
          <link>https://api.met.no/weatherapi/metalerts/1.1?cap=2.49.0.1.578.0.190516150827657.1901&amp
""".trimIndent()