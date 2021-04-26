package com.example.team_23.model

import android.location.Location
import android.util.Log
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.api.map_dataclasses.Base
import com.example.team_23.model.api.map_dataclasses.Routes
import com.example.team_23.model.api.metalerts_dataclasses.Alert
import com.example.team_23.model.api.metalerts_dataclasses.RssItem
import com.google.gson.Gson
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainRepository(private val apiService: ApiServiceImpl) {
    private val tag = "MainRepository"

    // API-Dokumentasjon: https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/documentation
    // TODO: Finn ut i hvilken klasse URL-ene bør plasseres.
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    private val options = listOf("event=forestFire", "period=2019-05")  // legg til evt. flere options i denne listen

    private val mapsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=59.911491,10.757933&destination=59.26754,10.40762&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"  // Hardkodet for testing.
    private val gson = Gson()

    // Henter Json fra Direction API (Google) og parser ved hjelp av Gson til dataklasser.
    suspend fun getRoutes(): List<Routes>? {
        var routes: List<Routes>? = null
        Log.d(tag, "Henter ruter fra Google!")
        try {
            val httpResponse = apiService.fetchData(mapsUrl)
            if (httpResponse != null)  Log.d(tag, "Fikk respons fra Directions API")
            val response = gson.fromJson(httpResponse, Base::class.java)
            routes = response.routes
        } catch (exception: Exception) {
            Log.w(tag, "Feil under henting av rute: ${exception.message}")
        }
        return routes
    }

    /* Henter XML-data (RSS-feed) fra MetAlerts-proxyen (IN2000) og parser responsen.
     * @return liste med RssItem eller null
     */
    @Suppress("BlockingMethodInNonBlockingContext")
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
        } catch (ex: IOException) {
            Log.w(tag, "IO-feil under parsing av RSS-feed:\n$ex")
        }
        return rssItems  // Returner liste med RssItems
    }

    /* Henter XML-data (CAP Alert) fra MetAlerts-proxyen (IN2000) og parser responsen.
     * @return instans av Alert eller null.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getCapAlert(url : String) : Alert? {
        var alert : Alert? = null
        try {
            val httpResponse : String = apiService.fetchData(url) ?: throw IOException()
            val bytestream = httpResponse.byteInputStream()  // Konverter streng til inputStream
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

    suspend fun getLocation(): Location {
        // TODO: implement getlocation
        return Location("abc")
    }
}