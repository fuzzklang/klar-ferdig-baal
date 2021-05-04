package com.example.team_23.model.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

// Implementasjon av ApiService-grensesnittet
class ApiServiceImpl: ApiService {
    private val tag = "ApiServiceImpl"
    private val client = OkHttpClient()

    /* Metode gjør http-request, returnerer en streng.
     * @param endpoint : String, options : liste av strenger som blir inkludert i request-url
     * @return String
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetchData(endpoint: String, options: List<String>): String? {
        // Sårbart å lage URL selv, vurder å bruke OkHttp sin URL-Builder eller tilsvarende
        val url = endpoint + "?" + options.joinToString("&")
        // Kaller polymorf fetchData-metode. Retur-streng kan være null
        return fetchData(url)
    }

    /* Metode lager en request-URL ved hjelp av OkHttp-klient og gjør nettverkskall.
     * Returnerer streng eller null.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetchData(url: String): String? {
        return withContext(Dispatchers.IO) {
            Log.d(tag, "URL: $url")
            val request = Request.Builder() // Bygg Request med OkHttp
                    .url(url)
                    .build()
            val response : Response // Deklarer response
            try {
                response = client.newCall(request).execute()       // Gjør nettverkskall
            } catch (ex: IOException) {
                Log.w(tag, "Feil under nettverkskall:\n$ex") // Fang opp eventuell exception
                throw ex                                           // og kast videre til håndtering
            }
            val responseString = response.body?.string()
            response.close() // Lukk Response-objektet
            responseString   // responseString kan være null
        }
    }
}