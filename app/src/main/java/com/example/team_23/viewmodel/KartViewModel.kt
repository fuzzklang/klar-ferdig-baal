package com.example.team_23.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.dataclasses.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KartViewModel(private val repo: MainRepository): ViewModel() {
    val varsler = MutableLiveData<MutableList<Alert>>()

   // fun visBaalplasser()

    fun hentAlleVarsler() {
        // Terje: veldig usikker på hvordan gjøre/bruke Coroutines og asynkrone kall.
        Log.d("kartViewModel", "Henter varsler!")
        val varselListe = mutableListOf<Alert>()
        CoroutineScope(Dispatchers.Default).launch {
            val rssItems = repo.getRssFeed()

            // Dårlig idé med nøstede Coroutines? Men hvordan gjøre hver Alert-fetch asynkron?
            withContext(Dispatchers.Default) {
                rssItems?.forEach {
                    val alert = repo.getCapAlert(it.link!!)
                    if (alert != null) {
                        varselListe.add(alert)
                    }
                }
                /*varselListe.forEach {
                    Log.d("viewmodel", "Alert: $it")
                }*/
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