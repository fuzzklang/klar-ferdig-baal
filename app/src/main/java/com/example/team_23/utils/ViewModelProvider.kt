package com.example.team_23.utils

import android.util.Log
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.location.FusedLocationProviderClient

class ViewModelProvider private constructor() {
    // Eget custom forsøk på en klasse som har ansvar for opprettelse av KartViewModel
    // og alle avhengighetene den har (apiService, fusedLocationProvider-> repository -> kartViewModel)
    // Skal fungere som et Singleton.

    companion object Factory {
        private val tag = "ViewModelProvider"
        private var kartViewModel: KartViewModel? = null

        fun getKartViewModel(fusedLocationProvider: FusedLocationProviderClient): KartViewModel {
            // FusedLocationProvider må instansieres i en Activity for å unngå minnelekkasjer,
            // og må derfor tas inn som parameter her.
            return if (kartViewModel == null) {
                // KartViewModel er ikke instansiert. Opprett alle avhengigheter, sett kartViewModel-attributt og returner denne.
                Log.d(tag, "Oppretter ny instans av apiService, MainRepository og KartViewModel")
                val apiServiceImpl = ApiServiceImpl()
                val repo = MainRepository(apiServiceImpl, fusedLocationProvider) // Sårbart for feil mtp. fusedLocationProvider?
                kartViewModel = KartViewModel(repo)
                return kartViewModel!!
            } else {
                // KartViewModel er allerede instansiert, returner denne instansen.
                Log.d(tag, "Returnerer tidligere instantiert kartViewModel")
                kartViewModel!!
            }
        }
    }
}