package com.example.team_23.viewmodel

import androidx.lifecycle.ViewModel
import com.example.team_23.model.MainRepository

/* [Ikke helt sikkert denne klassen/ViewModel-et trengs. Infotekst er kanskje fullstendig statisk og krever ikke interaksjon] */
class RegelViewModel(private val repo: MainRepository): ViewModel() {

    val regler = listOf(" ")

    fun hentRegler() {

    }
}