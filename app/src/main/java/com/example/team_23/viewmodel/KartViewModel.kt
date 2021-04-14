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

    fun hentAlleVarsler() {
        val varselListe = mutableListOf<Alert>()
        val varselListeMutex = Mutex()  // Lås til varselListe

        CoroutineScope(Dispatchers.Default).launch {
            // [Terje] veldig usikker på hvordan gjøre/bruke Coroutines for asynkrone kall,
            // men tror dette skal fungere på et vis.
            val rssItems = repo.getRssFeed()

            // For hvert RssItem gjøres et API-kall til den angitte lenken hvor varselet kan hentes fra.
            // Hvert kall gjøres med en egen Coroutine slik at varslene hentes samtidig. Ellers må hvert
            // API-kall vente i tur og orden på at det forrige skal bli ferdig, noe som er tidkrevende.
            // Bruker Mutex for å sikre at ingen av trådene skriver til varselListe samtidig (unngå mulig Race Condition).
            // [Terje: har ikke veldig god oversikt over dette, så godt mulig det inneholder feil!]
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

    fun finnRuterFraTil() {

    }

    fun hentPos() {

    }

    fun hentVarselForAngittRute() {

    }

    fun visBaalplasser() {

    }
}