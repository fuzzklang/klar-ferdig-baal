package com.example.team_23.model

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.CapParser
import com.example.team_23.model.api.MetAlertsRssParser
import com.example.team_23.model.dataclasses.*
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.RssItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
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
    private val directionsUrlOrigin = "https://maps.googleapis.com/maps/api/directions/json?origin="
    private val directionsUrlDestination = "&destination="
    private val mode = "&mode=walking"
    private val directionsUrlKey = "&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"
    private val gson = Gson()
    private var routes: List<Routes>? = null

    //Places API
    private val placesUrlStart = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="
    private val placesUrlEnd = "&inputtype=textquery&fields=formatted_address,name,geometry&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"
    private var places: List<Candidates>? = null

    //Geocode API
    private val placesURL = "https://maps.googleapis.com/maps/api/geocode/json?"
    private val key = "AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"
    private var placeName: String? = null

    suspend fun getPlaceNameFromLatLng(latlng: LatLng): String? {
        Log.d(tag, "Soker etter sted fra Geocode API")

        val geocodePath = "${placesURL}latlng=${latlng.latitude},${latlng.longitude}&key=${key}"
        Log.d(tag, "url: $geocodePath")
        try{
            val httpResponse = apiService.fetchData(geocodePath)
            if (httpResponse != null)  Log.d(tag, "Fikk respons fra Geocode API")
            val response = gson.fromJson(httpResponse, Address_components2::class.java)
            placeName = response.long_name
            Log.d(tag, "getPlaceNameFromLatLng: $placeName")
        } catch (exception: IOException) {
            Log.w(tag, "Feil under henting av types til sted: ${exception.message}")
        }
        return placeName
    }

    suspend fun searchLocation(place: String): List<Candidates>?{
        Log.d(tag, "Soker etter sted fra Google!")
        val placesPath = "${placesUrlStart}${place}${placesUrlEnd}"
        try{
        val httpResponse = apiService.fetchData(placesPath)
        if (httpResponse != null)  Log.d(tag, "Fikk respons fra Places API")
        val response = gson.fromJson(httpResponse, MainBase::class.java)
        places = response.candidates
            Log.d("places", places.toString())

    } catch (exception: IOException) {
        Log.w(tag, "Feil under henting av latlng til sted: ${exception.message}")
    }
    return places

    }

    // Henter Json fra Direction API (Google) og parser ved hjelp av Gson til dataklasser.
    suspend fun getRoutes(origin_lat : Double?, origin_lon : Double?, destination_lat : Double?, destination_lon : Double?): List<Routes>? {

        Log.d(tag, "Henter ruter fra Google!")
        val directionPath = "${directionsUrlOrigin}${origin_lat},${origin_lon}${directionsUrlDestination}${destination_lat},${destination_lon}${mode}${directionsUrlKey}"
        try {
            val httpResponse = apiService.fetchData(directionPath)
            if (httpResponse != null)  Log.d(tag, "Fikk respons fra Directions API")
            val response = gson.fromJson(httpResponse, Base::class.java)
            routes = response.routes

        } catch (exception: IOException) {
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
    fun getCampfireSpots(): List<Campfire> {
        // Gjør det slik som dette for å unngå å gi Context som parameter.
        // Men usikker på om dette får utilsiktede konsekvenser.
        val file = "res/raw/campfire_spots.json"
        val bonfireSpotsStream = this.javaClass.classLoader?.getResourceAsStream(file)
        val jsonString = bonfireSpotsStream?.bufferedReader()?.readText()
        bonfireSpotsStream?.close()
        Log.d(tag, "getBonfireSpots: henter bålplassoversikt fra res/raw. Returnert jsonString er ikke null eller tom: ${! jsonString.isNullOrEmpty()}")
        /* Parse json-streng til bålplass-objekter */
        val bType = object: TypeToken<List<Campfire>>() {}.type
        var bonfireList = mutableListOf<Campfire>()
        if (jsonString != null)
            bonfireList = gson.fromJson(jsonString, bType)
        return bonfireList
    }
}