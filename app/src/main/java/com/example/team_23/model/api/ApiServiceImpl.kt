package com.example.team_23.model.api

import kotlinx.coroutines.delay
import kotlin.random.Random

// Implementasjon av ApiService-grensesnittet
class ApiServiceImpl: ApiService {

    // Metoden simulerer et nettverkskall, blokkerer i et sekund (med 'delay')
    // Returnerer en streng med en tilfeldig tallverdi.
    override suspend fun fetchData(endpoint: String): String {
        val randVal = Random.nextInt(0,1000)
        delay(1000)
        return "Hei Team 23 $randVal"
    }
}