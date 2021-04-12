package com.example.team_23.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.dataclasses.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class KartViewModel(private val repo: MainRepository): ViewModel() {
    val varsler = MutableLiveData<MutableList<Alert>>()

   // fun visBaalplasser()

    fun hentAlleVarsler() {
        //Log.d("kartViewModel", "Henter varsler!")
        // val varselListe = mutableListOf<Alert>()  // Ikke trådsikker løsning

        // Forsøk på trådsikker løsning
        val varselListe = mutableListOf<Alert>()
        val mutex = Mutex()

        CoroutineScope(Dispatchers.Default).launch {
            // [Terje] veldig usikker på hvordan gjøre/bruke Coroutines og asynkrone kall,
            // men tror dette skal fungere på et vis.
            val rssItems = repo.getRssFeed()

            // For hvert RssItem kalles den tilhørende linken i en egen withContext.
            // Altså kalles hver link asynkront om jeg har forstått det rett [Terje]
            rssItems?.forEach {
                withContext(Dispatchers.Default) {
                    val alert = repo.getCapAlert(it.link!!)
                    if (alert != null) {
                        mutex.withLock { // Trådsikkert: ingen tråder modifiserer listen samtidig
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

    fun finnRuterFraTil() {

    }

    fun hentPos() {

    }

    fun hentVarselForAngittRute() {

    }
}