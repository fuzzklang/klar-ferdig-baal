package com.example.team_23.model

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.api.map_dataclasses.Base
import com.example.team_23.model.api.map_dataclasses.Routes
import com.example.team_23.model.api.metalerts_dataclasses.Alert
import com.example.team_23.model.api.metalerts_dataclasses.RssItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainRepository(private val apiService: ApiServiceImpl, private val fusedLocationProviderClient: FusedLocationProviderClient) {
    private val tag = "MainRepository"

    // API-Dokumentasjon: https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/documentation
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    // Opsjonene i permanentOptions-listen blir med i alle API-kall til MetAlerts.
    // Legg til evt. flere options i denne listen
    private val permanentOptions = listOf("event=forestFire")

    // Directions API
    private val mapsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=59.911491,10.757933&destination=59.26754,10.40762&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"  // Hardkodet for testing. TODO: noe som skal oppdateres?
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
    suspend fun getRssFeed(lat: Double?, lon: Double?) : List<RssItem>? {
        val options = permanentOptions.toMutableList()
        if (lat != null && lon != null) {
            options.add("lat=%.2f".format(lat))
            options.add("lon=%.2f".format(lon))
        }
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

    /* Henter GPS-lokasjon ved hjelp av FusedLocationProviderClient
    * NB: Metoden antar at Viewet sjekker tilgangsrettigheter ("permissions") og at disse er innvilget!
    * Den kaster bare en SecurityException dersom f.eks. bruker har avslått lokasjonstilgang ("permission")
    * @returns LiveData<Location>
    */
    fun getLocation(): LiveData<Location> {
        Log.d(tag, "getLocation ble kalt")
        val liveDataLocation = MutableLiveData<Location>()
        try {
            val locationTask = fusedLocationProviderClient.lastLocation
            locationTask.addOnSuccessListener {
                Log.d(tag,"getLocation: onSuccessListener. Resultat: ${it.latitude}, ${it.longitude}")
                liveDataLocation.postValue(it)
            }
        } catch (ex: SecurityException) {
            Log.w("MainRepo.getLocation", "Error when getting location")
        }
        return liveDataLocation
    }
}