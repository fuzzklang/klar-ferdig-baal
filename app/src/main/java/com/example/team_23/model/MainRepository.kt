package com.example.team_23.model

import android.util.Log
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.api.dataclasses.Alert
import com.example.team_23.model.api.dataclasses.RssItem
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainRepository {
    val tag = "MainRepository"
    // TODO: flytt instansiering av ApiServiceImpl til oppstart av app
    val apiService = ApiServiceImpl()

    // API-Dokumentasjon: https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/documentation
    // Finn ut i hvilken klasse URL-ene bør plasseres.
    val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    val options = listOf("event=forestFire", "period=2019-05")  // legg til evt. flere options i denne listen

    /* Henter XML-data (RSS-feed) fra proxyen og parser denne.
     * @return liste med RssItem eller null
     */
    suspend fun getRssFeed() : List<RssItem>? {
        // Kast IO Exception dersom API-kall feiler. Usikker på om dette er ideellt, men en iaf
        // midlertidig løsning for å sikre at httpResponse er initialisert.
        var rssItems : List<RssItem>? = null
        try {
            val httpResponse : String = apiService.fetchData(endpoint, options) ?: throw IOException()
            val bytestream = httpResponse.byteInputStream()   // Konverter streng til inputStream
            rssItems = MetAlertsRssParser().parse(bytestream)
        } catch (ex : XmlPullParserException) {
            Log.w(tag, "Feil under parsing av RSS-feed:\n$ex")
            // + Print toast?
        } catch (ex: IOException) {
            Log.w(tag, "IO-feil under parsing av RSS-feed:\n$ex")
            // + Print toast?
        }
        return rssItems  // Returner liste med RssItem
    }

    /* Henter XML-data (CAP Alert) fra proxyen og parser denne.
     * @return instans av Alert eller null.
     */
    suspend fun getCapAlert(url : String) : Alert? {
        var alert : Alert? = null
        try {
            val httpResponse : String = apiService.fetchData(url) ?: throw IOException()
            val bytestream = httpResponse.byteInputStream()
            alert = CapParser().parse(bytestream)
        } catch (ex : XmlPullParserException) {
            Log.w(tag, "Feil under parsing av CAP Alert:\n$ex")
            // Print toast?
        } catch (ex: IOException) {
            Log.w(tag, "IO-feil under parsing av CAP alert:\n$ex")
            // Print toast?
        }
        return alert
    }
}