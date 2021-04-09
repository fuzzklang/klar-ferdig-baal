package com.example.team_23.model.api

import kotlinx.coroutines.delay
import kotlin.random.Random

// Implementasjon av ApiService-grensesnittet
class ApiServiceImpl: ApiService {

    /* Metode gjør http-request, returnerer en streng.
     * @param url : String
     * @return String
     */
    override suspend fun fetchData(endpoint: String, options : List<String>): String {
        val httpResponse = ""

        return httpResponse
    }
}