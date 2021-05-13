package com.example.team_23.view
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.team_23.R
import com.example.team_23.model.dataclasses.Campfire
import com.example.team_23.model.dataclasses.Candidates
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
    private var menuVisible = false
    // ----- Alert Popup-box -----
    private lateinit var popup: View
    private lateinit var popupCloseButton: ImageButton
    private var popupVisible = false
    // ----- Travel here -------
    private lateinit var travelHereButton: ImageButton
    private lateinit var travelPolylineList: MutableList<Polyline>
    // ----- Alert Levels Description -----
    private lateinit var alertLevelsDescButton: Button
    private lateinit var alertLevelsDescPopup: View
    private lateinit var alertLevelsDescCloseBtn: ImageButton
    private var alertLevelsDescVisible = true
    // ----- Campfire -----
    private var menuCampfireButtonIsChecked = true  // Erstatt med direkte aksess til Switch
    private val ZOOM_LEVEL_SHOW_CAMPFIRES = 8.5
    private lateinit var menuCampfireButton: ToggleButton
    private lateinit var campfireSpots: List<Campfire>
    private lateinit var campfireMarkers: MutableList<Marker>
    // ----- Overlay (Alerts) -----
    private lateinit var menuOverlayBtn: ToggleButton
    private var overlayVisible = true
    private lateinit var overlayPolygonList: MutableList<Polygon>  // Listen med polygoner
    //----- Search-function ------
    private lateinit var searchButton: Button
    private lateinit var searchBar: EditText




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
        menuCampfireButton = findViewById<ToggleButton>(R.id.menuCampfireButton)
        // ----- Info-boks -----
        infoButton = findViewById<Button>(R.id.menuInfoButton)
        infoCloseButton = findViewById<ImageButton>(R.id.infoboxCloseButton)
        info = findViewById<View>(R.id.infoBox)

        // ----- Alert Popup-boks -----
        popup = findViewById<View>(R.id.popup)
        popupCloseButton = findViewById<ImageButton>(R.id.popupAlertCloseButton)
        val warningArea = findViewById<TextView>(R.id.popupAlertArea)
        val warningInfo = findViewById<TextView>(R.id.popupAlertInfoContent)
        val warningLevel = findViewById<TextView>(R.id.popupAlertLevelContent)
        val warningLevelImg = findViewById<ImageView>(R.id.warningLevelImg)
        val warningLevelColor = findViewById<View>(R.id.popupAlertLevelColor)
        travelHereButton = findViewById<ImageButton>(R.id.popupDraHitButton)

        // ----- Levels -----
        alertLevelsDescButton = findViewById<Button>(R.id.popupAlertDescButton)
        alertLevelsDescPopup = findViewById<View>(R.id.levelsDesc)
        alertLevelsDescCloseBtn = findViewById<ImageButton>(R.id.levelsDescCloseButton)

        // ------- Travel here -------
        travelPolylineList = mutableListOf()

        //--------- Search-function ----------
        searchBar = findViewById(R.id.searchBar)
        searchButton = findViewById(R.id.searchLocation)

        // ===== (ONCLICK) LISTENERS =====
        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)
        }

        rulesActivityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)
            if(menuVisible){
                menu.visibility = View.GONE
                menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                menuVisible = !menuVisible
            }
        }

        infoButton.setOnClickListener { toggleInfo() }      //Info knapp som endrer info sin synlighet

        infoCloseButton.setOnClickListener{ toggleInfo() }  //Info knapp som gjør info view usynelig

        menuButton.setOnClickListener{toggleMenu()}

        alertLevelsDescButton.setOnClickListener { toggleLevelsPopup() }

        alertLevelsDescCloseBtn.setOnClickListener { toggleLevelsPopup() }

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
            val warningText: String
            val background: Drawable
            val colorLevel : Drawable
            if (alert != null) {
                val info = alert.infoNo
                warningArea.text = info.area.areaDesc
                warningInfo.text = info.instruction
                val alertColorLevel = alert.getAlertColor()

                // TODO: tekst burde hentes fra resources
                when (alertColorLevel) {
                    AlertColors.YELLOW -> {
                        warningText = "Moderat skogbrannfare"
                        background = resources.getDrawable(R.drawable.yellowwarning,theme)
                        colorLevel = resources.getDrawable(R.color.alertYellow, theme)}
                    AlertColors.ORANGE -> {
                        warningText = "Betydelig skogbrannfare"
                        background = resources.getDrawable(R.drawable.orangewarning,theme)
                        colorLevel = resources.getDrawable(R.color.alertOrange, theme)}
                    AlertColors.RED    -> {
                        warningText = "Moderat skogbrannfare"
                        background = resources.getDrawable(R.drawable.orangewarning,theme)  // TODO: hent rød varsel fra Githuben til YR!
                        colorLevel = resources.getDrawable(R.color.alertRed, theme)
                        Log.w(tag, "Returnert alertColor er RED. Ikke forventet. Fortsetter kjøring.")
                    }
                    AlertColors.UNKNOWN -> {
                        Log.w(tag, "Returnert alertColor er Unkown.")
                        warningText = "?"
                        colorLevel = resources.getDrawable(R.color.black, theme)
                        background = resources.getDrawable(R.drawable.nivaaer,theme)
                    }
                }
            } else {
                // Ingen varsel (alert er null)
                warningText = "Ingen varsel funnet"
                background = resources.getDrawable(R.drawable.shape,theme)
                warningArea.text = ""
                warningInfo.text = "Ingen varsel i dette området"
                colorLevel = resources.getDrawable(R.color.black, theme)

            }
            warningLevel.text = warningText
            warningLevelImg.background = background
            warningLevelColor.background = colorLevel
            togglePopup()  // TODO: endre toggling til 'showPopup'.
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
        // Setter padding på toppen til kartet slik at kartet ikke havner bak den øverste fanen i appen.
        mMap.setPadding(0, 2000, 0, 0)

        // -- Bålplasser --
        campfireMarkers = mutableListOf()  // Liste som holder på markørene
      
        // -- Overlay --
        overlayVisible = true
        overlayPolygonList = mutableListOf()
        menuOverlayBtn = findViewById<ToggleButton>(R.id.menuOverlayButton)

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
        kartViewModel.path.observe(this, { paths -> drawDirectionsPath(paths) })

        //Observerer places-livedata (i fra KartViewModel), plasserer marker på søkt sted ved oppdatering
        kartViewModel.places.observe(this, { places -> placeMarker(places[0]) })


        // ===== ON CLICK LISTENERS =====
        menuCampfireButton.setOnCheckedChangeListener {_, isChecked -> toggleCampfires(isChecked) }
        menuOverlayBtn.setOnCheckedChangeListener { _, isChecked -> toggleOverlay(isChecked) }

        mMap.setOnCameraIdleListener { toogleCampfireZoomVisibility() }

        // Når bruker trykker på kartet lages det en marker
        mMap.setOnMapClickListener {
            marker?.remove()
            marker = mMap.addMarker(MarkerOptions().position(it))
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


        // Ved klikk på "Dra hit"-knappen (i popup-menyen):
        travelHereButton.setOnClickListener{ getAndShowDirections() }

        //Ved klikk på "Søk"-knappen
        searchButton.setOnClickListener {
            val input = searchBar.text.toString()
            kartViewModel.findPlace(input)

        }

        // ===== INITALISER - API-kall, konfigurasjon ++ =====
        kartViewModel.getAllAlerts()  // Hent alle varsler ved oppstart av app, når kart er klart.

        // --- TEGNE BÅLPLASSER ---
        drawCampfires()

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

    private fun placeMarker(latlng: LatLng){
        marker?.remove()
        marker = mMap.addMarker(MarkerOptions().position(latlng))
        togglePopup()

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
            if(menuVisible){
                menu.visibility = View.GONE
                menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.menubutton,theme)
                menuVisible = !menuVisible
            }
        }
        infoSynlig = !infoSynlig
    }

    private fun togglePopup(){
        if (popupVisible) {
            popup.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
        } else {
            popup.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
            if(menuVisible) {
                menu.visibility = View.GONE
                menuButton.background = ResourcesCompat.getDrawable(resources, R.drawable.menubutton,theme)
                menuVisible = !menuVisible
            }
        }
        popupVisible = !popupVisible
    }

    private fun toggleMenu() {
        if(menuVisible) {
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
            if(popupVisible) {
                togglePopup()
            }
        }
        menuVisible = !menuVisible
    }

    private fun toggleLevelsPopup() {
        if (alertLevelsDescVisible){
            alertLevelsDescPopup.visibility = View.VISIBLE
            popup.visibility = View.GONE
            mMap.uiSettings.isScrollGesturesEnabled = true
        } else {
            alertLevelsDescPopup.visibility = View.GONE
            popup.visibility = View.VISIBLE
            mMap.uiSettings.isScrollGesturesEnabled = false
        }
        alertLevelsDescVisible = !alertLevelsDescVisible
    }

    private fun toggleCampfires(isChecked: Boolean) {
        menuCampfireButtonIsChecked = isChecked
        campfireMarkers.forEach {it.isVisible = isChecked}
    }

    private fun toggleOverlay(isChecked: Boolean) {
        overlayVisible = isChecked
        overlayPolygonList.forEach {it.isVisible = overlayVisible}
    }

    /*
    * Metode som kalles ved oppstart og henter lagret konfigurasjon (tilstand) og setter variabler deretter.
    * (eks: skal overlay og bålplasser vises?)
    */
    /*private fun setUpUi() {
        TODO("Implementer senere dersom vi får på plass lagring av tilstand")
    }*/

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
                AlertColors.RED    -> getColor(R.color.alertRedTransparent)
                AlertColors.UNKNOWN -> {getColor(R.color.grey); Log.w(tag, "En feil har oppstått! Ukjent farge/nivå for varsel!")}
            }
            val polygonOptions = PolygonOptions()
                    .addAll(latLngList)
                    .fillColor(color)
                    .strokeWidth(1.0f)
                    .visible(overlayVisible)
                    .clickable(false)
            // Fargelegg varsel-soner på kart
            val polygon = mMap.addPolygon(polygonOptions)
            overlayPolygonList.add(polygon)
        }
    }

    private fun drawCampfires() {
        // TODO: burde noe av dette flyttes til layout-filene?
        val campfireIconHeight = 50   // endrer stoerrelse paa campfire ikonet
        val campfireIconWidth = 50    // -- " ---
        val campfireIcon = ContextCompat.getDrawable(this, R.drawable.campfire) as BitmapDrawable
        val smallCampfireMarkerBitmap = Bitmap.createScaledBitmap(campfireIcon.bitmap, campfireIconWidth, campfireIconHeight, false) // Brukes når markørene lages under

        campfireSpots = kartViewModel.getCampfireSpots()
        for (campfire in campfireSpots) {
            val marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(campfire.lat, campfire.lon))
                    .title("${campfire.name} (${campfire.type})")
                    .icon(BitmapDescriptorFactory.fromBitmap(smallCampfireMarkerBitmap)))
            if (marker == null)
                Log.w(tag, "onMapReady: en campfireMarker er null! Ignorerer. Kan føre til uønsket oppførsel fra app.")
            else
                campfireMarkers.add(marker)
        }
    }

    private fun toogleCampfireZoomVisibility() {
        //viser kun baalikoner etter et angitt zoom-nivaa
        val zoom = this.mMap.cameraPosition.zoom
        if (zoom > ZOOM_LEVEL_SHOW_CAMPFIRES && menuCampfireButtonIsChecked)
            campfireMarkers.forEach { it.isVisible = true }  // Vis bålplasser dersom Zoom langt inne nok og visning av bålplasser aktivert
        else
            campfireMarkers.forEach { it.isVisible = false }
    }


    private fun getAndShowDirections() {
        travelPolylineList.forEach{ it.remove() }   // Fjern tidligere tidligere tegnet rute fra kart.
        travelPolylineList.clear()                  // Nullstill liste

        togglePopup()
        getLocationAccess()

        if (hasLocationAccess) {
            val current_location = kartViewModel.getLocation()
            current_location.observe(this, {
                Log.d(tag, "getAndShowDirections: endring observert i lokasjon")
                val originLat = it?.latitude
                val originLon = it?.longitude
                val destinationLat = marker?.position?.latitude
                val destinationLon = marker?.position?.longitude
                Log.d(tag, "getAndShowDirections: originLat: $originLat, originLon: $originLon, destinationLat: $destinationLat, destinationLon: $destinationLon")
                if (originLat == null || originLon == null || destinationLat == null || destinationLon == null) {
                    Log.w(tag,"getAndShowDirections: minst en av posisjonsverdiene er null. Kan ikke hente Directions")
                    Toast.makeText(this, "Feil: mangler posisjon", Toast.LENGTH_SHORT).show()
                } else {
                    // Har tilgang til alle posisjoner: hent rute
                    Log.d(tag, "getAndShowDirections: kaller på kartViewModel.findRoute()")
                    kartViewModel.findRoute(originLat, originLon, destinationLat, destinationLon)
                }
            })
        }
    }

    private fun drawDirectionsPath(paths: MutableList<List<LatLng>>) {
        Log.d(tag, "drawDirectionsPath: Tegner rute")
        if (paths.size == 0) {
            Toast.makeText(this, "Fant ingen rute", Toast.LENGTH_SHORT).show()  // TODO: flytt streng resources
        }
        //går gjennom punktene i polyline for å skrive det ut til kartet.
        for (i in 0 until paths.size) {
            val polylineOptions = PolylineOptions().addAll(paths[i]).color(Color.RED)
            val polyline = this.mMap.addPolyline(polylineOptions)
            travelPolylineList.add(polyline)
            Log.d(tag, "drawDirectionsPath: travelPolylineList: $polyline")
        }
    }
}