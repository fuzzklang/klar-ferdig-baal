package com.example.team_23

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.utils.ViewModelProvider
import com.google.android.gms.location.LocationServices
import org.junit.Assert.assertEquals
import org.junit.Test

class TestViewModelProvider {
    //val tag = "TestViewModelProvider"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen
    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testSameInstanceReturnedOfKartViewModel() {
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(appContext)
        val firstInstancekartViewModel = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        val expectingIdenticalInstance = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        assertEquals(firstInstancekartViewModel, expectingIdenticalInstance)
    }
}