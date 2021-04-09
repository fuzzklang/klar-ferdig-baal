package com.example.team_23.model.api

import android.util.Log
import com.example.team_23.model.api.dataclasses.RssItem
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
* TODO: delete this file and replace with repository/ApiService from MVVM
* TODO: SLETT FIL
* Code is really messy at the moment.
* Need to figure out how to fetch data asynchronously (Coroutine Scope) and return values
* in a well-structured manner.
* Also need to set content of views accordingly. Probably depends on choosing an architecture,
* and using something like a Model/ViewModel
* */
class API {
    // Fetch data from API-proxy, Coroutine Scope for asynchronous data fetching
    // TODO: Double check architecture for where to do async call (where to place coroutine)
    // The variables below are for development/testing.
    // They should be replaced by a method-parameter or something similar.
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    private val params = "event=forestFire&show=all"
    //val period = "period=2019-05"
    val period = ""

    // Fetches all alerts from MetAlerts
    // Each item in feed contains URL to specific alert
    fun fetchAllAlerts() {
        CoroutineScope(Dispatchers.IO).launch {
            val APITAG = "API fetching: "
            try {
                val url = "$endpoint?$params&$period"
                val httpResponse = Fuel.get(url).awaitByteArray()
                parseAndPrintRssFeed(httpResponse)
            } catch (exception: Exception) {
                Log.w(APITAG, "A network request exception was thrown: ${exception.message}")
            }
        }
    }

    private fun parseAndPrintRssFeed(httpResponse: ByteArray) {
        // Get list of RssItems, and print each item
        val rssItems = MetAlertsRssParser().parse(httpResponse.inputStream())
        if (rssItems.isEmpty()) Log.d("parseAndPrintRssFeed", "There are no Alerts currently.")
        for (rssItem in rssItems) {
            fetchAndPrintAlert(rssItem)
        }
    }

    // Fetch information on specific alert, and create Alert-object
    private fun fetchAndPrintAlert(rssItem: RssItem){
        // Check if no link provided. Shouldn't occur
        val url = rssItem.link
                ?: throw IllegalArgumentException("No link to CAP provided in RSS item: $rssItem")
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val httpResponse = Fuel.get(url).awaitByteArray()
                val alert = CapParser().parse(httpResponse.inputStream())
                Log.d("fetchAndPrintAlert", alert.toString())
            }
        } catch (exception: Exception) {
            Log.w("fetchAndPrintAlert", "A network request exception was thrown: ${exception.message}")
        }
    }
}