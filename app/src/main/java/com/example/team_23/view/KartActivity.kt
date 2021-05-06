package com.example.team_23.view
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.team_23.R
import com.example.team_23.model.dataclasses.Bonfire
import com.example.team_23.model.dataclasses.metalerts_dataclasses.Alert
import com.example.team_23.model.dataclasses.metalerts_dataclasses.AlertColors
import com.example.team_23.utils.ViewModelProvider
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class KartActivity : AppCompatActivity(), OnMapReadyCallback {
    private val tag = "KartActivity"

    private lateinit var mMap: GoogleMap
    private lateinit var kartViewModel: KartViewModel

    private val LOCATION_PERMISSION_REQUEST = 1  // Til lokasjonsrettigheter
    private var hasLocationAccess = false

    private var marker: Marker? = null

    // ===== VARIABLER brukt av Activity =====
    // ----- Info-boks -----
    private lateinit var info: View
    private lateinit var infoButton: Button
    private lateinit var infoCloseButton: ImageButton
    private var infoSynlig = true   // Variabel som holder styr paa synligheten til info view
    // ----- Meny -----
    private lateinit var menu: View
    private lateinit var menuButton: ImageButton
    private var menuSynlig = false
    // ----- Popup-boks -----
    private lateinit var popup: View
    private lateinit var popupCloseButton: ImageButton
    private var popupSynlig = false
    // ----- Levels -----
    private lateinit var levelsButton: Button
    private lateinit var levelsPopup: View
    private lateinit var levelsPopupCloseBtn: ImageButton
    private var levelsPopupSynlig = true
    // ----- Bonfire -----
    private var showBonfireMarkers = true
    private lateinit var menuCampfireButton: Button
    private lateinit var bonfireSpots: List<Bonfire>
    private lateinit var bonfireMarkers: MutableList<Marker>
    // ----- Overlay (Alerts) -----
    private lateinit var overlayBtn: ToggleButton
    private var overlayVisible = true
    private lateinit var polygonList: MutableList<Polygon>  // Listen med polygoner


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setter KartViewModel og Kart tidlig.
        kartViewModel = ViewModelProvider.getKartViewModel(LocationServices.getFusedLocationProviderClient(applicationContext))
        // Henter SupportMapFragment og får beskjed når kartet (map) er klart til bruk. Se onMapReady-metoden.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // TODO: beskriv variablene/hvor de brukes/hva de brukes til
        // ===== SETT VIEWS =====
        // ----- Varsler Her-knapp -----
        val varslerHer = findViewById<Button>(R.id.varslerHerButton)

        // ----- Meny -----
        menu = findViewById<View>(R.id.menu)
        menuButton = findViewById<ImageButton>(R.id.menuButton)
        val rulesActivityBtn = findViewById<Button>(R.id.menuRulesButton)  // Knapp som sender bruker til reglene
        menuCampfireButton = findViewById<Button>(R.id.menuCampfireButton)
        // ----- Info-boks -----
        infoButton = findViewById<Button>(R.id.menuInfoButton)
        infoCloseButton = findViewById<ImageButton>(R.id.infoboxCloseButton)
        info = findViewById<View>(R.id.infoBox)

        // ----- Popup-boks -----
        // (varselvisning?)
        popup = findViewById<View>(R.id.popup)
        popupCloseButton = findViewById<ImageButton>(R.id.popupAlertCloseButton)
        val warningArea = findViewById<TextView>(R.id.popupAlertArea)
        val warningInfo = findViewById<TextView>(R.id.popupAlertInfoContent)
        val warningLevel = findViewById<TextView>(R.id.popupAlertLevelContent)
        val warningLevelImg = findViewById<ImageView>(R.id.warningLevelImg)
        val warningLevelColor = findViewById<View>(R.id.popupAlertLevelColor)

        // ----- Levels -----
        levelsButton = findViewById<Button>(R.id.popupAlertDescButton)
        levelsPopup = findViewById<View>(R.id.levelsDesc)
        levelsPopupCloseBtn = findViewById<ImageButton>(R.id.levelsDescCloseButton)

        // ===== (ONCLICK) LISTENERS =====
        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)
        }

        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)
            if(menuSynlig){
                menu.visibility = View.GONE
                menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                menuSynlig = !menuSynlig
            }
        }

        infoButton.setOnClickListener { toggleInfo() }      //Info knapp som endrer info sin synlighet

        infoCloseButton.setOnClickListener{ toggleInfo() }  //Info knapp som gjør info view usynelig

        menuButton.setOnClickListener{toggleMenu()}

        levelsButton.setOnClickListener { toggleLevelsPopup() }

        levelsPopupCloseBtn.setOnClickListener { toggleLevelsPopup() }

        popupCloseButton.setOnClickListener{ togglePopup() }

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


        // ===== OBSERVERS =====

        // TODO: skriv hjelpefunksjon og flytt ut av onCreate
        kartViewModel.alertAtPosition.observe(this, {
            // Observerer endringer i alertAtPosition (type LiveData<Alert>)
            val alert: Alert? = kartViewModel.alertAtPosition.value
            Log.d(tag, "Oppdatering observert i alertAtPosition. Alert: $it")
            // Løkken viser kun siste info-item siden løkken overskriver tidligere info lagt inn.
            if (alert != null) {
                val info = alert.infoNo
                warningArea.text = info.area.areaDesc
                warningInfo.text = info.instruction
                when {
                    // TODO: bruke Alert.getAlertColor for å velge farge
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
                togglePopup()
            } else {
                // Ingen varsel (alert er null)
                Toast.makeText(this, "Ingen varsler for dette området", Toast.LENGTH_SHORT).show()  // TODO: flytt streng resources
            }
        })
    }


    // ===== GOOGLE MAP READY =====
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
        // ===== VARIABLER =====
        // ----- Kart -----
        mMap = googleMap
        mMap.setPadding(0, 2000, 0, 0)

        // -- Bålplasser --
        bonfireMarkers = mutableListOf()  // Liste som holder på markørene
        showBonfireMarkers = true
      
        // -- Overlay --
        overlayVisible = true
        polygonList = mutableListOf()
        overlayBtn = findViewById<ToggleButton>(R.id.menuOverlayButton)

        //her skjules/vises baalplassene
        menuCampfireButton.setOnClickListener {
            showBonfireMarkers = !showBonfireMarkers
            if(!showBonfireMarkers) {
                for (i in bonfireMarkers) {
                    i.isVisible = false
                }
            }else{
                for(i in bonfireMarkers) {
                    i.isVisible = true
                }
            }
        }

        //viser kun baalikoner etter et angitt zoom-nivaa
        var zoom: Float
        this.mMap.setOnCameraIdleListener {
            zoom = this.mMap.cameraPosition.zoom
            if(zoom > 8.5 && showBonfireMarkers) {
                showBonfireMarkers = true
            } else {
                showBonfireMarkers = false
            }
            bonfireMarkers.forEach { it.setVisible(showBonfireMarkers) }
        }

        // ===== LOKASJON =====
        // Sjekk at tilgang til lokasjon (skal også sette mMap.isMyLocationEnabled og oppdaterer lokasjon dersom tilgang)
        getLocationAccess()
        Log.d("KartActivity.onMapReady", "mMap.isMyLocationEnabled: ${mMap.isMyLocationEnabled}")


        // ===== OBSERVERE =====
        // Observer varsel-liste fra KartViewModel
        // NB: Denne skal brukes kun til varsel-overlay, og ikke til popup-boks
        kartViewModel.allAlerts.observe(this, {alertList ->
            Log.d(tag, "Endring observert i allAlerts-liste!")
            allAlertsObserver(alertList)
        })

        // Observer path-livedata (i fra KartViewModel), tegn polyline ved oppdatering.
        kartViewModel.path.observe(this, { paths ->
            //går gjennom punktene i polyline for å skrive det ut til kartet.
            for (i in 0 until paths.size) {
                this.mMap.addPolyline(PolylineOptions().addAll(paths[i]).color(Color.RED))
            }
        })

        // ===== ON CLICK LISTENERS =====
        menuCampfireButton.setOnClickListener { toggleBonfires() }
        overlayBtn.setOnCheckedChangeListener { _, isChecked ->
            toggleOverlay(isChecked)
        }
        menuCampfireButton = findViewById<Button>(R.id.menuCampfireButton)
        menuCampfireButton.setOnClickListener {toggleBonfires()}

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
            Log.d(tag, "Klikk registrert på MyLocationButton")
            myLocationButtonOnClickMethod()
            true
        }

        // ===== INITALISER - API-kall, konfigurasjon ++ =====
        kartViewModel.getAllAlerts()  // Hent alle varsler ved oppstart av app, når kart er klart.

        // --- TEGNE BÅLPLASSER ---
        drawBonfires()

        // --- FLYTT KAMERA ---
        val oslo = LatLng(59.911491, 10.757933) //TODO: flytt dette til en konfigurasjonsfil
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))  // TODO: Flyttes nå til Oslo. Velge annet sted?
    }

    // =========================
    // ===== HJELPEMETODER =====
    // =========================

    /* Hjelpemetode for å få tilgangsrettigheter for lokasjon */
    private fun getLocationAccess() {
        Log.d(tag, "getLocation: mMap.isMyLocationEnabled: ${mMap.isMyLocationEnabled}")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationAccess = true
            mMap.isMyLocationEnabled = true
            kartViewModel.updateLocation()
        } else {
            Log.d(tag, "getLocation: ber om lokasjonsrettigheter")
            hasLocationAccess = false
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    /* Metode kalles når svar ang. lokasjonstilgang kommer tilbake. Sjekker om tillatelse er innvilget */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(tag, "onRequestPermissionsResult er kalt")
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(tag,"onRequestPermissionsResult: checkSelfPermission gir negativt svar. Har ikke tilgang.")
                } else {
                    Log.d(tag, "Lokasjonstilgang innvilget!")
                    hasLocationAccess = true
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Log.i(tag, "Lokasjonsrettigheter ble ikke gitt. Appen trenger tilgang til lokasjon for enkelte funksjonaliteter")
                Toast.makeText(this, "Ikke tilgang til lokasjon.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funksjon som endrer synligheten til info view
    private fun toggleInfo() {
        if (infoSynlig) {
            info.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
        } else {
            info.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
            if(menuSynlig){
                menu.visibility = View.GONE
                menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.menubutton,theme)
                menuSynlig = !menuSynlig
            }
        }
        infoSynlig = !infoSynlig
    }

    private fun togglePopup(){
        if (popupSynlig) {
            popup.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
        } else {
            popup.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
            if(menuSynlig) {
                menu.visibility = View.GONE
                menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.menubutton,theme)
                menuSynlig = !menuSynlig
            }
        }
        popupSynlig = !popupSynlig
    }

    private fun toggleMenu() {
        if(menuSynlig) {
            menu.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
            menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.menubutton,theme)
        } else {
            menu.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
            menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.closemenubutton,theme)
            if(infoSynlig) {
                toggleInfo()
            }
            if(popupSynlig) {
                togglePopup()
            }
        }
        menuSynlig = !menuSynlig
    }

    private fun toggleLevelsPopup() {
        if (levelsPopupSynlig){
            levelsPopup.visibility = View.VISIBLE
            popup.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
        } else {
            levelsPopup.visibility = View.GONE
            popup.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
        }
        levelsPopupSynlig = !levelsPopupSynlig
    }

    private fun toggleBonfires() {
        showBonfireMarkers = !showBonfireMarkers
        bonfireMarkers.forEach {it.isVisible = showBonfireMarkers}
    }

    private fun toggleOverlay(isChecked: Boolean) {
        overlayVisible = isChecked
        polygonList.forEach {it.isVisible = overlayVisible}
    }

    /*
    * Metode som kalles ved oppstart og henter lagret konfigurasjon (tilstand) og setter variabler deretter.
    * (eks: skal overlay og bålplasser vises?)
    */
    private fun setUpUi() {
        TODO("Implementer senere dersom vi får på plass lagring av tilstand")
    }

    /* Hjelpemetode som kalles når "MyLocation"-knapp (i kart) trykkes på */
    private fun myLocationButtonOnClickMethod() {
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
    }

    private fun allAlertsObserver(alertList: List<Alert>) {
        alertList.forEach { alert ->
            val latLngList = alert.getPolygon()
            // Hent riktig farge basert på faregrad
            val color = when (alert.getAlertColor()) {
                AlertColors.YELLOW -> getColor(R.color.alertYellowTransparent)
                AlertColors.ORANGE -> getColor(R.color.alertOrangeTransparent)
                AlertColors.RED -> getColor(R.color.alertRedTransparent)
                AlertColors.UNKOWN -> {getColor(R.color.grey); Log.w(tag, "En feil har oppstått! Ukjent farge/nivå for varsel!")}
            }
            val polygonOptions = PolygonOptions()
                    .addAll(latLngList)
                    .fillColor(color)
                    .strokeWidth(1.0f)
                    .visible(overlayVisible)
                    .clickable(false)
            // Fargelegg varsel-soner på kart
            val polygon = mMap.addPolygon(polygonOptions)
            polygonList.add(polygon)
        }
    }

    private fun drawBonfires() {
        // TODO: burde noe av dette flyttes til layout-filene?
        val bonfireIconHeight = 50   // endrer stoerrelse paa campfire ikonet
        val bonfireIconWidth = 50    // -- " ---
        val bonfireIcon = ContextCompat.getDrawable(this, R.drawable.campfire) as BitmapDrawable
        val smallBonfireMarkerBitmap = Bitmap.createScaledBitmap(bonfireIcon.bitmap, bonfireIconWidth, bonfireIconHeight, false) // Brukes når markørene lages under

        bonfireSpots = kartViewModel.getBonfireSpots()
        for (bonfire in bonfireSpots) {
            val marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(bonfire.lat, bonfire.lon))
                    .title("${bonfire.name} (${bonfire.type})")
                    .icon(BitmapDescriptorFactory.fromBitmap(smallBonfireMarkerBitmap)))
            if (marker == null)
                Log.w(tag, "onMapReady: en bonfireMarker er null! Ignorerer. Kan føre til uønsket oppførsel fra app.")
            else
                bonfireMarkers.add(marker)
        }
//viser kun baalikoner etter et angitt zoom-nivaa
        var zoom: Float
        this.mMap.setOnCameraIdleListener {
            zoom = this.mMap.cameraPosition.zoom
            if(zoom > 8.5 && showBonfireMarkers){
                for(markers in bonfireMarkers) {
                    markers.isVisible = true
                }
            }else{
                for(markers in bonfireMarkers) {
                    markers.isVisible = false
                }
            }
        }
    }
}