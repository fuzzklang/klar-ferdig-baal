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
import com.example.team_23.R
import com.example.team_23.model.api.metalerts_dataclasses.Alert
import com.example.team_23.model.api.metalerts_dataclasses.Info
import com.example.team_23.utils.ViewModelProvider
import com.example.team_23.viewmodel.KartViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class KartActivity : AppCompatActivity(), OnMapReadyCallback {
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
        val varslerHer = findViewById<Button>(R.id.varsler_her)

        // ----- Meny -----
        menu = findViewById<View>(R.id.menu)
        menuButton = findViewById<ImageButton>(R.id.menuButton)
        val rulesActivityBtn = findViewById<Button>(R.id.send_rules)  // Knapp som sender bruker til reglene

        // ----- Info-boks -----
        infoButton = findViewById<Button>(R.id.info_button)
        infoCloseButton = findViewById<ImageButton>(R.id.info_close_button)
        info = findViewById<View>(R.id.infoBox)

        // ----- Popup-boks -----
        // (varselvisning?)
        popup = findViewById<View>(R.id.popup)
        popupCloseButton = findViewById<ImageButton>(R.id.popupCloseButton)
        val warningArea = findViewById<TextView>(R.id.warningArea)
        val warningInfo = findViewById<TextView>(R.id.warningInfo)
        val warningLevel = findViewById<TextView>(R.id.warningLevel)
        val warningLevelImg = findViewById<ImageView>(R.id.warningLevelImg)
        val warningLevelColor = findViewById<View>(R.id.warningLevelColor)

        // ----- Levels -----
        levelsButton = findViewById<Button>(R.id.levelsButton)
        levelsPopup = findViewById<View>(R.id.levelsPopup)
        levelsPopupCloseBtn = findViewById<ImageButton>(R.id.levelsCloseButton)


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

        infoButton.setOnClickListener {toggleInfo()}      //Info knapp som endrer info sin synlighet

        infoCloseButton.setOnClickListener{toggleInfo()}  //Info knapp som gjør info view usynelig

        menuButton.setOnClickListener{toggleMenu()}

        levelsButton.setOnClickListener { toggleLevelsPopup() }

        levelsPopupCloseBtn.setOnClickListener { toggleLevelsPopup() }

        popupCloseButton.setOnClickListener{togglePopup()}

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
        // Observer path-livedata (i fra KartViewModel), tegn polyline ved oppdatering.
        kartViewModel.path.observe(this, { paths ->
            //går gjennom punktene i polyline for å skrive det ut til kartet.
            for (i in 0 until paths.size) {
                this.mMap.addPolyline(PolylineOptions().addAll(paths[i]).color(Color.RED))
            }
        })

        // Observer varsel-liste fra KartViewModel
        // NB: Denne skal brukes kun til varsel-overlay, og ikke til popup-boks
        kartViewModel.allAlerts.observe(this, {
            Log.d("KartActivity", "Endring skjedd i alerts-liste!")
            // TODO: implementere overlay
        })

        // TODO: skriv hjelpefunksjon og flytt ut av onCreate
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


        kartViewModel.getAllAlerts()  // Hent alle varsler ved oppstart av app
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


        val baalplassKnapp = findViewById<Button>(R.id.baalplass_button)
        //endrer stoerrelse paa campfire ikonet
        val height = 50
        val width = 50
        val baalikon = ContextCompat.getDrawable(this, R.drawable.campfire) as BitmapDrawable
        val b = baalikon.bitmap
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val oslo = LatLng(59.911491, 10.757933) // Oslo

        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))

        // Sjekk at tilgang til lokasjon
        // hashmap av alle baalplasser i oslo med navn og koordinater
        //TODO legg inn i egen fil eller strukturer et annet sted
        val baalmap = HashMap<String, Array<Double>>()
        baalmap["Rundvann (Ildsted)"] = arrayOf(59.851, 10.875)
        baalmap["Askevann (Ildsted)"] = arrayOf(59.833, 10.901)
        baalmap["Setertjern (Ildsted)"] = arrayOf(59.828, 10.894)
        baalmap["Skjelbreia (Ildsted)"] = arrayOf(59.825, 10.950)
        baalmap["Vangen (Ildsted)"] = arrayOf(59.818, 11.004)
        baalmap["Bråten ved Nøklevann (Ildsted)"] = arrayOf(59.876, 10.862)
        baalmap["Katissa (Ildsted)"] = arrayOf(59.873, 10.874)
        baalmap["Bremsrud (Ildsted)"] = arrayOf(59.876, 10.881)
        baalmap["Nord Elvåga (Ildsted)"] = arrayOf(59.901, 10.911)
        baalmap["Ulsrudvann (Ildsted)"] = arrayOf(59.889, 10.868)
        baalmap["Lutvann (Ildsted)"] = arrayOf(59.918, 10.881)
        baalmap["Bogstadvannet nordøst (Ildsted)"] = arrayOf(59.976, 10.622)
        baalmap["Jegersborgdammen (Ildsted)"] = arrayOf(59.975, 10.631)
        baalmap["Sognsvann Sør (Ildsted)"] = arrayOf(59.971, 10.724)
        baalmap["Sognsvann Øst (Ildsted)"] = arrayOf(59.974, 10.732)
        baalmap["Nedre blanksjø (Ildsted)"] = arrayOf(59.981, 10.739)
        baalmap["Sognsvann nord (Ildsted)"] = arrayOf(59.980, 10.726)
        baalmap["Øvresetertjern (Ildsted)"] = arrayOf(59.982, 10.670)
        baalmap["Lille Åklungen (Ildsted)"] = arrayOf(59.988, 10.714)
        baalmap["Trollvann (Ildsted)"] = arrayOf(59.962, 10.808)
        baalmap["Vesletjern (Ildsted)"] = arrayOf(59.960, 10.862)
        baalmap["Steinbruvann sør (Ildsted)"] = arrayOf(59.975, 10.881)
        baalmap["Steinbruvann nord (Ildsted)"] = arrayOf(59.980, 10.882)
        baalmap["Finnerud (Ildsted)"] = arrayOf(60.030, 10.638)
        baalmap["Store Åklungen (Ildsted)"] = arrayOf(60.001, 10.724)
        baalmap["Lille Tryvannet (Ildsted)"] = arrayOf(60.000, 10.677)
        baalmap["Skjennungen (Ildsted)"] = arrayOf(60.005, 10.683)
        baalmap["Øyungen (Ildsted)"] = arrayOf(60.041, 10.752)
        baalmap["Øyungen Damstokksletta (Ildsted)"] = arrayOf(60.042, 10.755)
        baalmap["Kapteinsputten (Ildsted)"] = arrayOf(59.969, 10.815)
        baalmap["Hvernvenbukta1 (Fastgrill)"] = arrayOf(59.831, 10.772)
        baalmap["Hvernvenbukta2 (Fastgrill)"] = arrayOf(59.834, 10.773)
        baalmap["Asperuddumpa (Fastgrill)"] = arrayOf(59.836, 10.799)
        baalmap["Stensrudtjern (Fastgrill)"] = arrayOf(59.823, 10.869)
        baalmap["Nordseterparken (Fastgrill)"] = arrayOf(59.873, 10.795)
        baalmap["Manglerud Friområde (Fastgrill)"] = arrayOf(59.894, 10.817)
        baalmap["Trolldalen (Fastgrill)"] = arrayOf(59.911, 10.855)
        baalmap["Tveitaparken (Fastgrill)"] = arrayOf(59.918, 10.844)
        baalmap["Haugerud friområde (Fastgrill)"] = arrayOf(59.919, 10.864)
        baalmap["Haugerud friområde (Fastgrill)"] = arrayOf(59.920, 10.863)
        baalmap["Ammerudgrenda Turvei D9 (Fastgrill)"] = arrayOf(59.962, 10.878)
        baalmap["Teglverksdammen Nedre (Fastgrill)"] = arrayOf(59.923, 10.796)
        baalmap["Teglverksdammen Øvre (Fastgrill)"] = arrayOf(59.923, 10.797)
        baalmap["Kampen Trykkbassenget (Fastgrill)"] = arrayOf(59.915, 10.781)
        baalmap["Sofienbergparken (Fastgrill)"] = arrayOf(59.923, 10.765)
        baalmap["St.Hanshaugen park (Fastgrill)"] = arrayOf(59.926, 10.741)
        baalmap["Frognerparken (Fastgrill)"] = arrayOf(59.924, 10.707)
        baalmap["Hukodden (Fastgrill)"] = arrayOf(59.895, 10.675)
        baalmap["Årvolldammen (Fastgrill)"] = arrayOf(59.948, 10.819)
        baalmap["Svarttjern (Fastgrill)"] = arrayOf(59.968, 10.898)
        baalmap["Smedstuaparken (Fastgrill)"] = arrayOf(59.954, 10.915)
        baalmap["Furuset kulturpark (Fastgrill)"] = arrayOf(59.943, 10.889)
        baalmap["Verdensparken (Fastgrill)"] = arrayOf(59.943, 10.895)
        baalmap["Verdensparken (Fastgrill)"] = arrayOf(59.944, 10.896)
        baalmap["Verdensparken (Fastgrill)"] = arrayOf(59.945, 10.896)
        baalmap["Veitvettparken (Fastgrill)"] = arrayOf(59.942, 10.848)
        baalmap["Tokerudbekken (Fastgrill)"] = arrayOf(59.972, 10.922)
        baalmap["Jesperudjordet (Fastgrill)"] = arrayOf(59.962, 10.930)
        baalmap["Alnaparken (Fastgrill)"] = arrayOf(59.942, 10.876)
        val baalMarkers = mutableListOf<Marker>()
        for ((k, v) in baalmap) {
            baalMarkers.add(this.mMap.addMarker(MarkerOptions().position(LatLng(v[0],v[1])).title(k).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))))
        }
        //her skjules/vises baalplassene
        var status = true
        baalplassKnapp.setOnClickListener {
            status = !status
            if(!status) {
                for (i in baalMarkers) {
                    i.isVisible = false
                }
            }else{
                for(i in baalMarkers) {
                    i.isVisible = true
                }
            }
        }

        //viser kun baalikoner etter et angitt zoom-nivaa
        var zoom: Float = -1f
        this.mMap.setOnCameraIdleListener {
            zoom = this.mMap.cameraPosition.zoom
            if(zoom > 8.5 && status){
                for(i in baalMarkers) {
                    i.setVisible(true)
                }
            }else{
                for(i in baalMarkers) {
                    i.setVisible(false)
                }
            }
        }
       this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 6f))

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
    }

    // ===== HJELPEMETODER =====

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

    // Funksjon som endrer synligheten til info view
    private fun toggleInfo() {
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

    private fun togglePopup(){
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

    private fun toggleMenu() {
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

    private fun toggleLevelsPopup(){
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
}