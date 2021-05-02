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
import androidx.constraintlayout.widget.ConstraintLayout
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

        /* Observer varsel-liste fra KartViewModel */
        kartViewModel.alerts.observe(this, {
            Log.d("KartActivity", "Endring skjedd i alerts-liste!")
            kartViewModel.alerts.value?.forEach {
                Log.d("KartActivity", "Alert: $it")
                it.infoItemsNo.forEach{
                    warningArea.text = it.area.areaDesc
                    warningInfo.text = it.instruction
                    if (it.severity.toString() == "Moderate") {
                        warningLevel.text = "Moderat skogbrannfare"
                        warningLevelImg.background = resources.getDrawable(R.drawable.yellowwarning,theme)
                        warningLevelColor.background = resources.getDrawable(R.color.yellow,theme)
                    }else if(it.severity.toString() == "Severe"){
                        warningLevel.text = "Betydelig skogbrannfare"
                        warningLevelImg.background = resources.getDrawable(R.drawable.orangewarning,theme)
                        warningLevelColor.background = resources.getDrawable(R.color.orange,theme)
                    }else{
                        warningLevel.text = "?"
                    }
                }
            }
        })

        //knapp som sender bruker til reglene
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
        var infoSynlig = true //Variabel som holder styr paa synligheten til info view
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

        //Funksjon som endrer synligheten til info view
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
            } else{
                popup.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
                if(menuSynlig){
                    menu.visibility = View.GONE
                    menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
                    menuSynlig = !menuSynlig
                }
            }
            popupSynlig = !popupSynlig
        }

        popupButton.setOnClickListener{togglePopup()}
        popupCloseButton.setOnClickListener{togglePopup()}

        fun toggleMenu(){
            if(menuSynlig){
                menu.visibility = View.GONE
                mMap.uiSettings.isScrollGesturesEnabled = true
                menuButton.background = resources.getDrawable(R.drawable.menubutton,theme)
            }else{
                menu.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = false
                menuButton.background = resources.getDrawable(R.drawable.closemenubutton,theme)
                if(infoSynlig){
                    toggleInfo()
                }
                if(popupSynlig){
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
        baalplassKnapp.setOnClickListener(){
            status = !status
            if(!status) {
                for (i in baalMarkers) {
                    i.setVisible(false)
                }
            }else{
                for(i in baalMarkers) {
                    i.setVisible(true)
                }
            }
        }


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
        mMap.uiSettings.isScrollGesturesEnabled = false
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