package com.example.team_23.view

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.team_23.R
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

// import android.os.Bundle
// import androidx.appcompat.app.AppCompatActivity
// import com.example.team_23.R
// import com.example.team_23.model.MainRepository
// import com.example.team_23.model.api.ApiServiceImpl
// import com.example.team_23.viewmodel.KartViewModel
//
class KartActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val apiService = ApiServiceImpl()
    val repo = MainRepository(apiService)
    val kartViewModel = KartViewModel(repo)  // Bør instansieres et annet sted

    val markerList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observer path-livedata (i KartViewModel), tegn polyline ved oppdatering.
        kartViewModel.path.observe(this, Observer<MutableList<List<LatLng>>> { paths ->
            //går gjennom punktene i polyline for å skrive det ut til kartet.
            for (i in 0 until paths.size) {
                this.mMap.addPolyline(PolylineOptions().addAll(paths[i]).color(Color.RED))
            }
        })


        // For testing, kaller på findRoute-metoden til kartViewModel
        kartViewModel.findRoute()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Oslo, Norway.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //lager to markere:en i Oslo og en i Tønsberg
        val oslo = LatLng(59.911491, 10.757933) // Oslo
        val tonsberg = LatLng(59.26754, 10.40762) // Tønsberg
        this.mMap.addMarker(MarkerOptions().position(oslo).title("Oslo"))
        this.mMap.addMarker(MarkerOptions().position(tonsberg).title("Tønsberg"))
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))

        //når bruker trykker på kartet lages det en ny marker
        mMap.setOnMapClickListener {
            val marker = mMap.addMarker(MarkerOptions().position(it).title("Marker on click"))
            //lagrer markeren i en liste slik at man kan endre/slette den senere
            markerList.add(marker)
        }
    }
}