package com.example.team_23.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.dataclasses.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KartViewModel {
    // todo: Repo bør instansieres annetsteds, slik at det kun fins én av den!
    val repo = MainRepository()
    val varselListe = mutableListOf<Alert>()
    val liveDataVarsler = MutableLiveData<MutableList<Alert>>()

   // fun visBaalplasser()

    fun hentAlleVarsler() {
        Log.d("kartViewModel", "Henter varsler!")
        CoroutineScope(Dispatchers.IO).launch {
            val rssItems = repo.getRssFeed()

            // Dårlig idé med nøstede Coroutines? Men hvordan gjøre hver Alert-fetch asynkron?
            CoroutineScope(Dispatchers.IO).launch {
                rssItems?.forEach {
                    val alert = repo.getCapAlert(it.link!!)
                    if (alert != null) {
                        varselListe.add(alert)
                        liveDataVarsler.postValue(varselListe)
                    }
                }
                varselListe.forEach {
                    Log.d("viewmodel", "Alert: $it")
                }
            }
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