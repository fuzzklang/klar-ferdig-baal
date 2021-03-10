package com.example.team_23

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking
import org.junit.Test

class APITest {
    @Test
    fun testFetchData() {
        val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
        val eventType = "event=forestFire"
        val period = "period=2019-05"
        val otherParams = "show=all"

        /*runBlocking {
            try {
                val url = "$endpoint?$eventType&$period"
                println("URL: $url")
                val httpResponse = Fuel.get("https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/").awaitString()
                println(httpResponse)
            } catch (exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
                println("${exception.cause}")
                //println("${exception.stackTrace}")
            }
        }*/
    }
}