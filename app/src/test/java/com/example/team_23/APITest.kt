package com.example.team_23

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.io.InputStream

class APITest {
    @Test
    fun testFetchData() {
        val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"

        runBlocking {
            try {
                val url = "$endpoint?$eventType&$period"
                //println("URL: $url")
                //val httpResponse = Fuel.get("https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/").awaitString()
                //println(httpResponse)
            } catch (exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
                println("${exception.cause}")
                //println("${exception.stackTrace}")
            }
        }
    }

    @Test
    fun testLocalXmlFiles() {
        val projectRoot = File("").absolutePath
        val xmlTestFilesDirectory = File("$projectRoot/src/test/rssTestFiles_toBeDeleted")
        val file1 = "getResponse_05_2019.xml"
        val file2 = "getResponseNoAlert.xml"
        val path = "$xmlTestFilesDirectory/$file1"
        val inputStream: InputStream = File(path).inputStream()
        // Parse with parser
        MetAlertsRssXmlParser().parse(inputStream)
    }
}