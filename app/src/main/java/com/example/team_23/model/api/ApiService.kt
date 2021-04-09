package com.example.team_23.model.api

import androidx.annotation.WorkerThread

interface ApiService {
    /* suspend: metoden gjør blokkerende kall (her nettverk)
    * @WorkerThread-annoteringen gir beskjed til kompilatoren om at denne metoden ikke skal kalles
    * på Main/UI-tråden. Hvis dette skulle skje gir den (forhåpentligvis) et varsel om dette.
    *
    * param: endpoint endepunktet, options : http-request options
    */
    @WorkerThread
    suspend fun fetchData(endpoint: String, options : List<String>): String
}