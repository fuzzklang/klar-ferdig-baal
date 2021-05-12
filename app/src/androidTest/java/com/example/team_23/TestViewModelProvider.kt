package com.example.team_23

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.team_23.utils.ViewModelProvider
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.location.LocationServices
import org.junit.Assert.assertSame
import org.junit.Test

class TestViewModelProvider {
    //val tag = "TestViewModelProvider"
    // appContext: Konteksten som appen testes under. Trengs for å aksessere assets-mappen

    @Test
    fun testSameInstanceReturnedOfKartViewModel() {
        // Forventer at samme instans av kartViewModel returneres fra ViewModelProvider
        // hver gang .getKartViewModel kalles
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(appContext)
        val firstInstanceKartViewModel = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        val expectingIdenticalInstance = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        assertSame(expectingIdenticalInstance, firstInstanceKartViewModel)
    }

    @Test
    fun testSameInstanceReturnedOfKartViewModelRepeated() {
        // Forventer at samme instans av kartViewModel returneres fra ViewModelProvider
        // hver gang .getKartViewModel kalles. Repeterer kallene til ViewModelProvider
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(appContext)
        val firstInstanceKartViewModel = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        var expectingIdenticalInstance: KartViewModel = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        repeat(500) {
            // Repeter kall på .getKartViewModel 500 ganger
            expectingIdenticalInstance = ViewModelProvider.getKartViewModel(fusedLocationProvider)
        }
        assertSame(expectingIdenticalInstance, firstInstanceKartViewModel)
    }
}