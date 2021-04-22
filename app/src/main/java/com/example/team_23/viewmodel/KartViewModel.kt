package com.example.team_23.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.map_dataclasses.Base
import com.example.team_23.model.api.map_dataclasses.Routes
import com.example.team_23.model.api.metalerts_dataclasses.Alert
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class KartViewModel(private val repo: MainRepository): ViewModel() {
    val varsler = MutableLiveData<MutableList<Alert>>()

    //liste med responsen fra api-kallet
    //private var routes:List<Routes>? = null  // TODO: delete?
    val routes = MutableLiveData<List<Routes>>()

    //liste som inneholder polyline-punktene
    val path: MutableList<List<LatLng>> = ArrayList()


    fun hentAlleVarsler() {
        val varselListe = mutableListOf<Alert>()
        val varselListeMutex = Mutex()  // Lås til varselListe

        CoroutineScope(Dispatchers.Default).launch {
            val rssItems = repo.getRssFeed()

            // For hvert RssItem gjøres et API-kall til den angitte lenken hvor varselet kan hentes fra.
            // Hvert kall gjøres med en egen Coroutine slik at varslene hentes samtidig. Ellers må hvert
            // API-kall vente i tur og orden på at det forrige skal bli ferdig, noe som er tidkrevende.
            // Bruker Mutex for å sikre at ingen av trådene skriver til varselListe samtidig (unngå mulig Race Condition).
            rssItems?.forEach {  // For hvert rssItem (løkke)
                withContext(Dispatchers.Default) {          // dispatch med ny coroutine for hvert rssItem
                    val alert = repo.getCapAlert(it.link!!) // Hent CAP-alert via repository. 'it': rssItem
                    if (alert != null) {
                        varselListeMutex.withLock { // Trådsikkert: ingen tråder modifiserer listen samtidig
                            varselListe.add(alert)
                        }
                    }
                }
            }
            varsler.postValue(varselListe)
        }
    }

    fun hentVarslerForSted(lat: String, lon: String) {

    }

    fun findRoute() {
        CoroutineScope(Dispatchers.Default).launch {
            val routesFromApi = repo.getRoutes()
            if (routesFromApi != null) {
                routes.postValue(routesFromApi)
            }
        }

    }

    fun hentPos() {

    }

    fun hentVarselForAngittRute() {

    }

    fun visBaalplasser() {

    }

    //Må gå gjennom dataklasse for dataklasse (base, legs, steps og polyline)
    // for å få tak i informasjonen jeg trenger (points i polyline) for å lage rute på kartet
    private fun getPolylinePoints() {
        val TAG = "Polyline Points"
        if (routes.value != null) {
            for (element in routes.value!!) {
                val legs = element.legs
                Log.d(TAG, legs.toString())

                if (legs != null) {
                    for (element in legs) {
                        val steps = element.steps
                        Log.d(TAG, steps.toString())

                        if (steps != null) {
                            for (element in steps) {
                                val points = element.polyline?.points
                                Log.d(TAG, points.toString())
                                path.add(PolyUtil.decode(points))
                            }
                        }
                    }
                }
            }
        }
    }
}