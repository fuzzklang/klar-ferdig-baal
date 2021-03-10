package com.example.team_23

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class API {
    // Fetch data from API-proxy, Coroutine Scope for asynchronous data fetching
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    private val params = "event=forestFire&show=all"
    val period = "period=2019-05"

    fun fetchData() {
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
}

// Source: https://api.met.no/weatherapi/metalerts/1.1/documentation#DESCRIPTION
// Corresponds to Alert described in MET report
// contains selected fields from CAP-alert
data class Alert (
    val identifier: String, // Unique identifier issued by MET or NVE
    val sent: String,       // Time of sending
    val status: String,     // 'Actual' or 'Test' (should be ignored)
    val msgType: String,    // 'Alert': first message. 'Update': replaces previous messages. 'Cancel': cancel previous erroneous message, or danger ended.
)

// Contains more detailed information on an Alert
data class Info (
    val event: String,        // In our project only 'forestFire' relevant
    val responseType: String, // 'Monitor': act according to <instruction>. 'AllClear': danger ended.
    // val urgency: String,   // 'Future', 'Immediate'. Unsure if actually used by MetAlerts
    val severity: String,     // 'Extreme', 'Severe', 'Moderate', 'Minor'.
    val certainty: String,    // 'Likely', 'Possible', 'Observed'.
    val effective: String,    // Message is valid from this time
    val onset: String,        // Starting time of event
    val expires: String,      // Message is valid until this time
    val instruction: String   // Description of recommended action
    // + much more
)

// Area which alert is valid for
data class Area (
    val area: String,     // Box enclosing geographical description (?)
    val areaDesc: String, // Description of geographical area for alert
    val polygon: String   // Polygon encircling the current area
)