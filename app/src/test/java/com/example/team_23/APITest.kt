package com.example.team_23

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.io.InputStream

class APITest {
    @Test
    fun testFetchData() {
        /*val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period"*/

        /*val endpoint = "https://api01.nve.no/hydrology/forecast/landslide/v1.0.8/api/CAP/Feed" // This one works. Also CAP-feed
        val periodFrom = "2020-11-01"
        val periodTo = "2020-11-30"
        val url = "$endpoint/$periodFrom/$periodTo"*/

        val endpoint = "https://api.met.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"
        val url = "$endpoint?$eventType&$period"

        runBlocking {
            try {
                //println("URL: $url")
                val httpResponse = Fuel.get(url).awaitString()
                println(httpResponse)
            } catch (exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
                println("${exception.cause}")
                //println("${exception.stackTrace}")
            }
        }
    }
}