package com.example.team_23

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class API {
    // Fetch data from API-proxy, Coroutine Scope for asynchronous data fetching
    // TODO: Double check architecture for to do async call (where to place coroutine)
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    private val params = "event=forestFire&show=all"
    val period = "period=2019-05"

    // Fetches all alerts from MetAlerts
    // Each item in feed contains URL to specific alert
    fun fetchAllAlerts() {
        /*CoroutineScope(Dispatchers.IO).launch {
            val APITAG = "API fetching: "
            try {
                val url = "$endpoint?$params&$period"
                val httpResponse = Fuel.get(url).awaitString()
                Log.d(APITAG, httpResponse)
            } catch (exception: Exception) {
                Log.w(APITAG, "A network request exception was thrown: ${exception.message}")
            }
        }*/
    }

    // Fetch information on specific alert, and create Alert-object
    /*fun fetchAlert(url: String): Alert {
        return Alert("","","","", Info("","",""))
    }*/
}