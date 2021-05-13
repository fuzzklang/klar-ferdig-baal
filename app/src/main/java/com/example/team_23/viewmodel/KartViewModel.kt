package com.example.team_23.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.team_23.model.MainRepository
import com.example.team_23.model.dataclasses.Campfire
import com.example.team_23.model.dataclasses.Candidates
import com.example.team_23.model.dataclasses.Routes
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class KartViewModel(private val repo: MainRepository): ViewModel() {
    /* MutableLiveDataen er privat slik at ikke andre klasser utilsiktet kan endre innholdet */
    private val _allAlerts =
        MutableLiveData<MutableList<Alert>>()   // Liste med alle skogbrannfarevarsler utstedt av MetAlerts
    private var _routes =
        mutableListOf<Routes>()                            // Liste med responsen fra api-kall til Directions API
    private val _path =
        MutableLiveData<MutableList<List<LatLng>>>()         // Liste som inneholder polyline-punktene fra routes (sørg for at hele tiden samsvarer med 'routes')
    private var _location = MutableLiveData<Location?>()             // Enhetens lokasjon (GPS)
    private var _alertAtPosition = MutableLiveData<Alert?>()  // Varsel for angitt sted.
    private var _candidates = mutableListOf<Candidates>()
    private val _places = MutableLiveData<LatLng>()

    /* Immutable versjoner av LiveDataene over som er tilgjengelig for Viewene */
    val allAlerts: LiveData<MutableList<Alert>> = _allAlerts
    val alertAtPosition: LiveData<Alert?> = _alertAtPosition
    var path: LiveData<MutableList<List<LatLng>>> = _path
    var places: LiveData<LatLng> = _places

    /* Grensesnitt til View.
     * Henter varsler for nåværende sted.
     * Er avhengig av at lokasjon (livedata 'location') er tilgjengelig og oppdatert.
     */
    fun getAlertCurrentLocation() {
        val lat = _location.value?.latitude
        val lon = _location.value?.longitude
        // Feilsjekking i tilfelle ikke lokasjon tilgjengelig?
        if (lat == null || lon == null) {
            Log.w(
                "KartViewModel",
                "Advarsel: getAlertsCurrentLocation() ble kalt men lokasjon er ikke tilgjengelig."
            )
        } else {
            getAlert(lat, lon)
        }
    }

    /* Grensesnitt til View.
     * Henter varsler for sted angitt ved latitude og longitude.
     */
    fun getAlert(lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.Default).launch {
            var alert: Alert? = null
            val rssItemList = repo.getRssFeed(lat, lon)
            if (rssItemList != null && rssItemList.isNotEmpty()) {
                Log.d(
                    "KartViewModel.getAlert",
                    "Antall RSS-items returnert fra API: ${rssItemList.size}"
                )
                alert = repo.getCapAlert(rssItemList[0].link!!)
            } else {
                Log.d("KartActivity.getAlert", "Ingen varsel ble funnet")
            }
            _alertAtPosition.postValue(alert)  // Oppdater varsel-livedata med ny verdi
        }
    }

    /* Grensesnitt til View
     * Returnerer en instans av livedata med lokasjon.
     */
    fun getLocation(): LiveData<Location?> {
        Log.d(
            "KartViewModel",
            "getLocation: ${_location.value?.latitude}, ${_location.value?.longitude}"
        )
        return _location
    }

    fun findPlace(place: String) {
        //Kaller på Places API fra Google (via Repository) og oppdaterer places-Livedata
        CoroutineScope(Dispatchers.Default).launch {
            val placesFromApi = repo.searchLocation(place)
            Log.d("Kartviewmodel.findplace", placesFromApi.toString())
            if (placesFromApi != null) {
                _candidates = placesFromApi as MutableList<Candidates>

                _places.postValue(getPlacesLatLng(_candidates))
                Log.d("KartViewModel.findPlace", "Places oppdatert")
            }
        }
    }

    fun findRoute(
        origin_lat: Double?,
        origin_lon: Double?,
        destination_lat: Double?,
        destination_lon: Double?
    ) {
        // Kaller på Directions API fra Google (via Repository) og oppdaterer routes-LiveData
        CoroutineScope(Dispatchers.Default).launch {
            val routesFromApi =
                repo.getRoutes(origin_lat, origin_lon, destination_lat, destination_lon)
            Log.d("KartViewModel.findRoute", routesFromApi.toString())
            if (routesFromApi != null) {
                _routes =
                    routesFromApi as MutableList<Routes>              // Oppdater routes (hentet fra API)
                _path.postValue(getPolylinePoints(_routes)) // Oppdater _path (lat/lng-punkter) basert på ny rute
                Log.d("KartViewModel.findRoute", "Path oppdatert")
            }
        }
    }


    // Oppdaterer nåværende posisjon ved kall til repository.
    // Antar at appen har tilgang til lokasjon.
    fun updateLocation() {
        _location = repo.getLocation() as MutableLiveData<Location?>
    }

    /* Grensesnitt til View.
     * Henter alle tilgjengelige varsler og oppdaterer alerts-liste med dem.
    */
    fun getAllAlerts() {
        val varselListe = mutableListOf<Alert>()
        val varselListeMutex = Mutex()  // Lås til varselListe
        CoroutineScope(Dispatchers.Default).launch {
            val rssItems = repo.getRssFeed(null, null)
            // For hvert RssItem gjøres et API-kall til den angitte lenken hvor varselet kan hentes fra.
            // Hvert kall gjøres med en egen Coroutine slik at varslene hentes samtidig. Ellers må hvert
            // API-kall vente i tur og orden på at det forrige skal bli ferdig, noe som er tidkrevende.
            // Bruker Mutex for å sikre at ingen av trådene skriver til varselListe samtidig (unngå mulig Race Condition).
            rssItems?.forEach {  // For hvert rssItem (løkke)
                withContext(Dispatchers.Default) {          // dispatch med ny coroutine for hvert rssItem
                    val alert =
                        repo.getCapAlert(it.link!!) // Hent CAP-alert via repository. 'it': rssItem
                    if (alert != null) {
                        varselListeMutex.withLock { // Trådsikkert: ingen tråder modifiserer listen samtidig
                            varselListe.add(alert)
                        }
                    }
                }
            }
            _allAlerts.postValue(varselListe)
        }
    }

    fun getCampfireSpots(): List<Campfire> {
        return repo.getCampfireSpots()
    }

    /* Hjelpemetode for findRoute()
     * Må gå gjennom dataklasse for dataklasse (base, legs, steps og polyline)
     * for å få tak i informasjonen programmet trenger (points i polyline) for å lage rute på kartet
     */
    private fun getPolylinePoints(routes: List<Routes>?): MutableList<List<LatLng>> {
        // tmpPathList: Brukt for å konstruere hele polyline-listen, før LiveDataen oppdateres med den komplette listen.
        val tmpPathList = mutableListOf<List<LatLng>>()
        val tag = "Polyline Points"
        Log.d(tag, "Antall routes: ${routes?.size}")
        if (routes != null) {
            for (route in routes) {  // Sårbart for bugs, mutable data kan ha blitt endret.
                val legs = route.legs
                Log.d(tag, "Antall legs (i route.legs): ${legs?.size}")
                if (legs != null) {
                    for (leg in legs) {
                        val steps = leg.steps
                        Log.d(tag, "Antall steps (i leg.steps): ${steps?.size}")
                        if (steps != null) {
                            for (step in steps) {
                                val points = step.polyline?.points
                                tmpPathList.add(PolyUtil.decode(points))
                            }
                        }
                    }
                }
            }
        }
        return tmpPathList
    }

    private fun getPlacesLatLng(places: List<Candidates>?): LatLng? {
        val location = places?.get(0)?.geometry?.location
        var latlng: LatLng? = null

        if (location?.lat != null && location.lng != null){
            latlng = LatLng(location.lat.toDouble(), location.lng.toDouble())
        }

        return latlng

    }


}

