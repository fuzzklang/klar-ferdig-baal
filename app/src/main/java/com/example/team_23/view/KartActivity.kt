package com.example.team_23.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.team_23.R
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// import android.os.Bundle
// import androidx.appcompat.app.AppCompatActivity
// import com.example.team_23.R
// import com.example.team_23.model.MainRepository
// import com.example.team_23.model.api.ApiServiceImpl
// import com.example.team_23.viewmodel.KartViewModel
//
class KartActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val url = "https://maps.googleapis.com/maps/api/directions/json?origin=59.911491,10.757933&destination=59.26754,10.40762&key=AIzaSyAyK0NkgPMxOOTnWR5EFKdy2DzfDXGh-HI"
    private val gson = Gson()
    //liste som inneholder polyline-punktene
    private val path: MutableList<List<LatLng>> = ArrayList()
    //liste med responsen fra api-kallet
    private var base:List<Routes>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchBaseInformation()

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

        //lager en liste som skal bestå av markers
        val markerList = arrayListOf<Marker>()


        //når bruker trykker på kartet lages det en ny marker
        mMap.setOnMapClickListener {
            val marker = mMap.addMarker(MarkerOptions().position(it).title("Marker on click"))
            //lagrer markeren i en liste slik at man kan endre/slette den senere
            markerList.add(marker)

            getPolylinePoints()

            //går gjennom punktene i polyline for å skrive det ut til kartet.
            for (i in 0 until path.size) {
                this.mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }

        }


    }

    //metode som henter Json fra Direction API og parser til Gson.
    private fun fetchBaseInformation() {
        val TAG = "Json Base Fetch"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val httpResponse = Fuel.get(url).awaitString()
                val response = gson.fromJson(httpResponse, Base::class.java)
                Log.d(TAG, response.toString())
                base = response.routes
            } catch (exception: Exception) {
                Log.w(TAG, "A network request exception was thrown: ${exception.message}")
            }
        }
    }

    //Må gå gjennom dataklasse for dataklasse (base, legs, steps og polyline)
    // for å få tak i informasjonen jeg trenger (points i polyline) for å lage rute på kartet
    private fun getPolylinePoints(){
        val TAG = "Polyline Points"
        if (base != null) {
            for (element in base!!) {
                val legs = element.legs
                Log.d(TAG, legs.toString())

                if (legs != null) {
                    for (element in legs) {
                        val steps = element.steps
                        Log.d(TAG, steps.toString())

                        if (steps != null) {
                            for (element in steps) {
                                val points = element.polyline?.points
                                Log.d(TAG, points.toString())
                                path.add(PolyUtil.decode(points))
                            }
                        }
                    }
                }

            }

        }
    }
}

data class Base(val geocoded_waypoints: List<Geocoded_waypoints>?, val routes: List<Routes>?, val status: String?)

data class Bounds(val northeast: Northeast?, val southwest: Southwest?)

data class Distance(val text: String?, val value: Number?)

data class Duration(val text: String?, val value: Number?)

data class End_location(val lat: Number?, val lng: Number?)

data class Geocoded_waypoints(val geocoder_status: String?, val place_id: String?, val types: List<String>?)

data class Legs(val distance: Distance?, val duration: Duration?, val end_address: String?, val end_location: End_location?, val start_address: String?, val start_location: Start_location?, val steps: List<Steps>?, val traffic_speed_entry: List<Any>?, val via_waypoint: List<Any>?)

data class Northeast(val lat: Number?, val lng: Number?)

data class Overview_polyline(val points: String?)

data class Polyline(val points: String?)

data class Routes(val bounds: Bounds?, val copyrights: String?, val legs: List<Legs>?, val overview_polyline: Overview_polyline?, val summary: String?, val warnings: List<Any>?, val waypoint_order: List<Any>?)

data class Southwest(val lat: Number?, val lng: Number?)

data class Start_location(val lat: Number?, val lng: Number?)

data class Steps(val distance: Distance?, val duration: Duration?, val end_location: End_location?, val html_instructions: String?, val maneuver: String?, val polyline: Polyline?, val start_location: Start_location?, val travel_mode: String?)
