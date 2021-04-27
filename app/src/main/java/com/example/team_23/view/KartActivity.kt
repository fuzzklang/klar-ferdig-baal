package com.example.team_23.view
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.team_23.R
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class KartActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var apiService: ApiServiceImpl
    private lateinit var repo: MainRepository
    private lateinit var kartViewModel: KartViewModel

    private val LOCATION_PERMISSION_REQUEST = 1  // Til lokasjonsrettigheter

    private val markerList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*BEGIN COMMENT
        * Alle disse bør instansieres annetsteds!
        * For eksempel i et Factory eller tilsvarende */
        apiService = ApiServiceImpl()
        repo = MainRepository(apiService, LocationServices.getFusedLocationProviderClient(applicationContext))
        kartViewModel = KartViewModel(repo)
        /* END COMMENT */

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observer path-livedata (i fra KartViewModel), tegn polyline ved oppdatering.
        kartViewModel.path.observe(this, { paths ->
            //går gjennom punktene i polyline for å skrive det ut til kartet.
            for (i in 0 until paths.size) {
                this.mMap.addPolyline(PolylineOptions().addAll(paths[i]).color(Color.RED))
            }
        })

        /* Observer varsel-liste fra KartViewModel */
        kartViewModel.alerts.observe(this, {
            Log.d("KartActivity", "Endring skjedd i alerts-liste!")
            kartViewModel.alerts.value?.forEach {
                Log.d("KartActivity", "Alert: $it")
            }
        })
        //knapp som sender bruker til reglene
        val rulesActivityBtn = findViewById<ImageButton>(R.id.send_rules)

        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)

        }

        val infoButton = findViewById<ImageButton>(R.id.info_button)
        val infoCloseButton = findViewById<ImageButton>(R.id.info_close_button)
        val popupButton = findViewById<Button>(R.id.popupButton)
        val info = findViewById<View>(R.id.infoBox)
        val popup = findViewById<View>(R.id.popup)
        val popupCloseButton = findViewById<ImageButton>(R.id.popupCloseButton)

        var infoSynlig = true //Variabel som holder styr paa synligheten til info view
        //Funksjon som endrer synligheten til info view
        fun toggleInfo() {
            if (infoSynlig) {
                info.visibility = View.INVISIBLE
            } else{
                info.visibility = View.VISIBLE
            }
            infoSynlig = !infoSynlig
        }
        //Info knapp som endrer info sin synlighet
        infoButton.setOnClickListener {toggleInfo()}
        //Info knapp som gjør info view usynelig
        infoCloseButton.setOnClickListener{toggleInfo()}

        var popupSynlig = false

        fun togglePopup(){
            if (popupSynlig) {
                popup.visibility = View.INVISIBLE
            } else{
                popup.visibility = View.VISIBLE
            }
            popupSynlig = !popupSynlig
        }

        popupButton.setOnClickListener{togglePopup()}
        popupCloseButton.setOnClickListener{togglePopup()}

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
        mMap.setPadding(0, 2000, 0, 0)
        //lager to markere:en i Oslo og en i Tønsberg
        val oslo = LatLng(59.911491, 10.757933) // Oslo
        val tonsberg = LatLng(59.26754, 10.40762) // Tønsberg
        this.mMap.addMarker(MarkerOptions().position(oslo).title("Oslo"))
        this.mMap.addMarker(MarkerOptions().position(tonsberg).title("Tønsberg"))
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))

        getLocationAccess()

        // Når bruker trykker på kartet lages det en ny marker
        mMap.setOnMapClickListener {
            val marker = mMap.addMarker(MarkerOptions().position(it).title("Marker on click"))
            //lagrer markeren i en liste slik at man kan endre/slette den senere
            markerList.add(marker)
            kartViewModel.findRoute()
        }

        // Ved klikk på "Vis Min Lokasjon"-knappen (oppe i høyre hjørne):
        // Hent en LiveData-instans med lokasjon (fra ViewModel) som deretter blir observert
        // [Denne løsningen kan potensielt føre til en viss delay fra knappen blir klikket til kameraet flytter seg]
        mMap.setOnMyLocationButtonClickListener {
            Log.d("KartActivity", "Klikk registrert på MyLocationButton")
            val locationLiveData = kartViewModel.getLocation()
            // Når LiveDataen får koordinater flyttes kamera til oppdatert posisjon
            locationLiveData.observe(this, {
                val location = locationLiveData.value
                if (location!= null) {
                    Toast.makeText(this, "Current pos: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(location.latitude, location.longitude))
                        .zoom(6f)
                        .build()
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                } else {
                    Toast.makeText(this, "Ingen lokasjon tilgjengelig", Toast.LENGTH_SHORT).show()
                }
            })
            true
        }
    }

    /* Hjelpemetode for å få tilgangsrettigheter for lokasjon */
    private fun getLocationAccess() {
        Log.d("KartActivity", "getLocation: mMap.isMyLocationEnabled: ${mMap.isMyLocationEnabled}")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else
            Log.d("KartActivity", "getLocation: ber om lokasjonsrettigheter")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    /* Metode kalles når svar ang. lokasjonstilgang kommer tilbake. Sjekker om tillatelse er innvilget */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("KartActivity", "onRequestPermissionsResult er kalt")
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    // Usikker på når programmet evt. kommer hit
                } else {
                    Log.d("KartActivity", "Lokasjonstilgang innvilget!")
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Log.i("KartActivity", "Lokasjonsrettigheter ble ikke gitt. Appen trenger tilgang til lokasjon for enkelte funksjonaliteter")
                Toast.makeText(this, "Tilgang til lokasjon er ikke gitt", Toast.LENGTH_LONG).show()
            }
        }
    }
}