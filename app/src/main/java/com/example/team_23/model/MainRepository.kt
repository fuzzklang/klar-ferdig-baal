package com.example.team_23.model

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.dataclasses.Base
import com.example.team_23.model.dataclasses.Bonfire
import com.example.team_23.model.dataclasses.Routes
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.RssItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*

class MainRepository(private val apiService: ApiServiceImpl, private val fusedLocationProviderClient: FusedLocationProviderClient) {
    private val tag = "MainRepository"

    // API-Dokumentasjon: https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/documentation
    private val endpoint = "https://in2000-apiproxy.ifi.uio.no/weatherapi/metalerts/1.1/"
    // Opsjonene i permanentOptions-listen blir med i alle API-kall til MetAlerts.
    // Legg til evt. flere options i denne listen
    private val permanentOptions = listOf("event=forestFire")

    // Directions API
    private val mapsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=59.911491,10.757933&destination=59.26754,10.40762&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"  // Hardkodet for testing. TODO: noe som skal oppdateres?
    private val directionsURL_origin = "https://maps.googleapis.com/maps/api/directions/json?origin="
    private val directionsURL_destination = "&destination="
    private val directionsURL_key = "&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"
    private val gson = Gson()
    var routes: List<Routes>? = null

    // Henter Json fra Direction API (Google) og parser ved hjelp av Gson til dataklasser.
    suspend fun getRoutes(origin_lat : Double?, origin_lon : Double?, destination_lat : Double?, destination_lon : Double?): List<Routes>? {

        Log.d(tag, "Henter ruter fra Google!")
        val direction_path = "${directionsURL_origin}${origin_lat},${origin_lon}${directionsURL_destination}${destination_lat},${destination_lon}${directionsURL_key}"
        try {
            val httpResponse = apiService.fetchData(direction_path)
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
            // For å sikre at desimaltall skrives med punktum (og ikke komma) brukes Locale.US.
            // kotlin.format bruker enhetens Locale som default. F.eks. ble desimaltall skrevet med
            // komma når systemspråk er norsk. Førte til feil i API-kall.
            options.add("lat=%.2f".format(Locale.US, lat))
            options.add("lon=%.2f".format(Locale.US, lon))
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
    @Throws(SecurityException::class)
    fun getLocation(): LiveData<Location?> {
        Log.d(tag, "getLocation ble kalt")
        val liveDataLocation = MutableLiveData<Location>()
        try {
            val locationTask = fusedLocationProviderClient.lastLocation
            locationTask.addOnSuccessListener {
                // Resultat ("it") kan være null dersom system ikke har informasjon om nåværende lokasjon.
                if (it == null) {
                    Log.d(tag, "getLocation: onSuccessListener. Resultat (Location) er null.")
                } else {
                    Log.d(tag,"getLocation: onSuccessListener. Resultat: ${it.latitude}, ${it.longitude}")
                }
                liveDataLocation.postValue(it)
            }
        } catch (ex: SecurityException) {
            Log.w("MainRepo.getLocation", "Error when getting location")
        }
        return liveDataLocation
    }

    /* Parser JSON-filen med bålplasser og returnerer en liste med HashMaps.
    * TODO: gjøre om til asynkront Coroutine-kall (pga. fillesning)?
    */
    fun getBonfireSpots(): List<Bonfire> {
        // Gjør det slik som dette for å unngå å gi Context som parameter.
        // Men usikker på om dette får utilsiktede konsekvenser.
        val file = "res/raw/bonfire_spots.json"
        val bonfireSpotsStream = this.javaClass.classLoader?.getResourceAsStream(file)
        val jsonString = bonfireSpotsStream?.bufferedReader()?.readText()
        bonfireSpotsStream?.close()
        Log.d(tag, "getBonfireSpots: henter bålplassoversikt fra res/raw. Returnert jsonString er ikke null eller tom: ${! jsonString.isNullOrEmpty()}")
        /* Parse json-streng til bålplass-objekter */
        val bType = object: TypeToken<List<Bonfire>>() {}.type
        var bonfireList = mutableListOf<Bonfire>()
        if (jsonString != null)
            bonfireList = gson.fromJson(jsonString, bType)
        return bonfireList
    }
}