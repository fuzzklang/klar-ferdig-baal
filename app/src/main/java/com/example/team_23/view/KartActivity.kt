package com.example.team_23.view
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.team_23.R
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.metalerts_dataclasses.Alert
import com.example.team_23.model.api.metalerts_dataclasses.Info
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
    private var hasLocationAccess = false

    private var marker: Marker? = null

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
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

        val warningArea = findViewById<TextView>(R.id.warningArea)
        val warningInfo = findViewById<TextView>(R.id.warningInfo)
        val warningLevel = findViewById<TextView>(R.id.warningLevel)
        val warningLevelImg = findViewById<ImageView>(R.id.warningLevelImg)
        val warningLevelColor = findViewById<View>(R.id.warningLevelColor)

        // Observer varsel-liste fra KartViewModel
        // NB: Denne skal brukes kun til varsel-overlay, og ikke til popup-boks
        kartViewModel.allAlerts.observe(this, {
            Log.d("KartActivity", "Endring skjedd i alerts-liste!")
            // TODO: implementere overlay
        })

        // Knapp som sender bruker til reglene
        val rulesActivityBtn = findViewById<Button>(R.id.send_rules)

        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)

        }

        val infoButton = findViewById<Button>(R.id.info_button)
        val infoCloseButton = findViewById<ImageButton>(R.id.info_close_button)
        val popupButton = findViewById<Button>(R.id.popupButton)
        val info = findViewById<View>(R.id.infoBox)
        val popup = findViewById<View>(R.id.popup)
        val popupCloseButton = findViewById<ImageButton>(R.id.popupCloseButton)
        val menu = findViewById<View>(R.id.menu)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val levelsButton = findViewById<Button>(R.id.levelsButton)
        val levelsPopup = findViewById<View>(R.id.levelsPopup)
        val levelsPopupCloseBtn = findViewById<ImageButton>(R.id.levelsCloseButton)

        var menuSynlig = false
        var infoSynlig = true   // Variabel som holder styr paa synligheten til info view
        var popupSynlig = false


        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)
            if(menuSynlig){
                menu.visibility = View.GONE
                menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                menuSynlig = !menuSynlig
            }
        }

        // Funksjon som endrer synligheten til info view
        fun toggleInfo() {
            if (infoSynlig) {
                info.visibility = View.GONE
                mMap.uiSettings.isScrollGesturesEnabled = true
            } else{
                info.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
                if(menuSynlig){
                    menu.visibility = View.GONE
                    menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                    menuSynlig = !menuSynlig
                }
            }
            infoSynlig = !infoSynlig
        }
        //Info knapp som endrer info sin synlighet
        infoButton.setOnClickListener {toggleInfo()}
        //Info knapp som gjør info view usynelig
        infoCloseButton.setOnClickListener{toggleInfo()}


        fun togglePopup(){
            if (popupSynlig) {
                popup.visibility = View.GONE
                mMap.uiSettings.isScrollGesturesEnabled = true
            } else {
                popup.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
                if(menuSynlig) {
                    menu.visibility = View.GONE
                    menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                    menuSynlig = !menuSynlig
                }
            }
            popupSynlig = !popupSynlig
        }

        popupButton.setOnClickListener{togglePopup()}
        popupCloseButton.setOnClickListener{togglePopup()}

        fun toggleMenu() {
            if(menuSynlig) {
                menu.visibility = View.GONE
                mMap.uiSettings.isScrollGesturesEnabled = true
                menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
            } else {
                menu.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
                menuButton.background = resources.getDrawable(R.drawable.closemenubutton,theme)
                if (infoSynlig) {
                    toggleInfo()
                }
                if(popupSynlig) {
                    togglePopup()
                }
            }
            menuSynlig = !menuSynlig
        }

        menuButton.setOnClickListener{toggleMenu()}

        var levelsPopupSynlig = true

        fun toggleLevelsPopup(){
            if (levelsPopupSynlig){
                levelsPopup.visibility = View.VISIBLE
                popup.visibility = View.GONE
                mMap.uiSettings.isScrollGesturesEnabled = true
            } else{
                levelsPopup.visibility = View.GONE
                popup.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
            }
            levelsPopupSynlig = !levelsPopupSynlig
        }

        levelsButton.setOnClickListener { toggleLevelsPopup() }
        levelsPopupCloseBtn.setOnClickListener { toggleLevelsPopup() }

        kartViewModel.getAllAlerts()

        kartViewModel.alertAtPosition.observe(this, {
            // Observerer endringer i alertAtPosition (type LiveData<Alert>)
            val alert: Alert? = kartViewModel.alertAtPosition.value
            Log.d("KartActivity", "Oppdatering observert i alertAtPosition. Alert: $it")
            // Løkken viser kun siste info-item siden løkken overskriver tidligere info lagt inn.
            if (alert != null) {
                alert.infoItemsNo.forEach { info: Info ->
                    warningArea.text = info.area.areaDesc
                    warningInfo.text = info.instruction
                    when {
                        info.severity.toString() == "Moderate" -> {
                            warningLevel.text = "Moderat skogbrannfare"
                            warningLevelImg.background = resources.getDrawable(R.drawable.yellowwarning,theme)
                            warningLevelColor.background = resources.getDrawable(R.color.yellow,theme)
                        }
                        info.severity.toString() == "Severe" -> {
                            warningLevel.text = "Betydelig skogbrannfare"
                            warningLevelImg.background = resources.getDrawable(R.drawable.orangewarning,theme)
                            warningLevelColor.background = resources.getDrawable(R.color.orange,theme)
                        }
                        else -> {
                            warningLevel.text = "?"
                        }
                    }
                }
                togglePopup()
            } else {
                // Ingen varsel (alert er null)
                Toast.makeText(this, "Ingen varsler for dette området", Toast.LENGTH_SHORT).show()  // TODO: flytt streng resources
            }
        })

        val varslerHer = findViewById<Button>(R.id.varsler_her)
        varslerHer.setOnClickListener{
            getLocationAccess()  // Sjekk at vi har tilgang til lokasjon fra system.
            // 'location'-variabelen i KartViewModel får en ny instans av LiveData *hver gang*
            // lokasjon oppdateres. Må derfor lage en observer for den *siste og nyeste instansen* av location.
            // Da hentes varselet først når LiveDataen har blitt fylt med informasjon om lokasjon.
            // Ellers vil den ikke ha tilgang på lokasjons-informasjonen.
            // NB: Ikke ideellt hvis flere 'observere' trenger å observere samme instans av 'location'.
            val latestKnownLocation = kartViewModel.getLocation()  // type: LiveData<Location>
            latestKnownLocation.observe(this, {
                kartViewModel.getAlertCurrentLocation()  // Hent varsler når vi har lokasjon
                //kartViewModel.getAlert(it.latitude, it.longitude) // Alternativt. Disse er ekvivalente. Sikrere?
            })
        }
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
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))

        // Sjekk at tilgang til lokasjon
        getLocationAccess()
        Log.d("KartActivity.onMapReady", "mMap.isMyLocationEnabled: ${mMap.isMyLocationEnabled}")
        //kartViewModel.updateLocation()  // Må hente lokasjon på et tidspunkt. HVOR?

        // Når bruker trykker på kartet lages det en marker
        mMap.setOnMapClickListener {
            marker?.remove()
            marker = mMap.addMarker(MarkerOptions().position(it).title("Marker on click"))
            kartViewModel.getAlert(it.latitude, it.longitude)
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
        mMap.uiSettings.isScrollGesturesEnabled = false
    }

    /* Hjelpemetode for å få tilgangsrettigheter for lokasjon */
    private fun getLocationAccess() {
        Log.d("KartActivity", "getLocation: mMap.isMyLocationEnabled: ${mMap.isMyLocationEnabled}")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationAccess = true
            mMap.isMyLocationEnabled = true
            kartViewModel.updateLocation()
        } else {
            Log.d("KartActivity", "getLocation: ber om lokasjonsrettigheter")
            hasLocationAccess = false
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    /* Metode kalles når svar ang. lokasjonstilgang kommer tilbake. Sjekker om tillatelse er innvilget */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("KartActivity", "onRequestPermissionsResult er kalt")
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("KartActivity","onRequestPermissionsResult: checkSelfPermission gir negativt svar. Har ikke tilgang.")
                } else {
                    Log.d("KartActivity", "Lokasjonstilgang innvilget!")
                    hasLocationAccess = true
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Log.i("KartActivity", "Lokasjonsrettigheter ble ikke gitt. Appen trenger tilgang til lokasjon for enkelte funksjonaliteter")
                Toast.makeText(this, "Ikke tilgang til lokasjon.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}