package com.example.team_23.model

import android.util.Log
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.api.dataclasses.RssItem
import org.xmlpull.v1.XmlPullParserException

class MainRepository {
    val tag = "MainRepository"
    // TODO: flytt instansiering av ApiServiceImpl til oppstart av app
    val apiService = ApiServiceImpl()

    // Siste spørsmålstegn i URL angir at det som følger er Options for API-kallet .
    // F.eks. kan man spesifisere at man kun ønsker varsler om skogbrann, event='forestFire'.
    // Se: https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/documentation
    val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    val options = listOf("event=forestFire")  // legg til evt. flere options i denne listen

    suspend fun getRssFeed() : List<RssItem>? {
        val httpResponse : String = apiService.fetchData(endpoint, options)
        val bytestream = httpResponse.byteInputStream()  // Konverter streng til inputStream
        var rssItems : List<RssItem>? = null
        try {
            rssItems = MetAlertsRssParser().parse(bytestream)
        } catch (exception : XmlPullParserException) {
            Log.w(tag, "Noe gikk gale under parsing av RSS-feed")
            // Print toast?
            // Usikker på om alle exceptions som kan kastes fanges.
        }
        return rssItems  // Returner liste med RssItem
    }
}