package com.example.team_23.model.api

import android.util.Log
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

// Implementasjon av ApiService-grensesnittet
class ApiServiceImpl: ApiService {
    val tag = "ApiServiceImpl"
    val client = OkHttpClient()

    /* Metode gjør http-request, returnerer en streng.
     * @param endpoint : String, options : liste av strenger som blir inkludert i request-url
     * @return String
     */
    @Throws(IOException::class)
    override suspend fun fetchData(endpoint: String, options: List<String>): String? {
        // Sårbart å lage URL selv, vurder å bruke OkHttp sin URL-Builder eller tilsvarende
        val url = endpoint + "?" + options.joinToString("&")
        // Kaller polymorf fetchData-metode. Retur-streng kan være null
        return fetchData(url)
    }

    /* Metode lager en request-URL ved hjelp av OkHttp-klient og gjør nettverkskall.
     * Returnerer streng eller null.
     */
    @Throws(IOException::class)
    override suspend fun fetchData(url: String): String? {
        // Bygg Request med OkHttp
        val request = Request.Builder()
            .url(url)
            .build()
        // Deklarer response
        var response : Response
        try {
            // Gjør nettverkskall
            response = client.newCall(request).execute()
        } catch (ex: IOException) {
            // Fang opp eventuell exception
            Log.w(tag, "Feil under nettverkskall:\n$ex")
            // Kast videre til håndtering
            throw ex
        }
        val responseString = response.body?.string()
        // Lukk Response-objektet
        response.close()
        // responseString kan være null
        return responseString
    }
}