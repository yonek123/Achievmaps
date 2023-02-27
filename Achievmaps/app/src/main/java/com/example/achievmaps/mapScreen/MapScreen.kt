package com.example.achievmaps.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.DatabaseConnections
import com.example.achievmaps.loginScreen.LoginScreen
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.main_menu_screen.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor


@Suppress(
    "DEPRECATION", "NAME_SHADOWING", "UNCHECKED_CAST", "UNUSED_PARAMETER",
    "VARIABLE_WITH_REDUNDANT_INITIALIZER", "CascadeIf"
)
class MapScreen : AppCompatActivity(),
    OnMapReadyCallback,
    OnCameraMoveStartedListener,
    OnCameraIdleListener {
    private var page = 0
    private var list = listOf("0")
    private val rmObjects = ArrayList<RMObject>()
    private var achievement = ""

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("HH:mm")

    @SuppressLint("SimpleDateFormat")
    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    private var lat = 0.0
    private var long = 0.0
    private var markerlat = 10.0
    private var markerlong = 10.0
    private var movementMethod = "&mode=walking"

    //private var waypoints = "&waypoints=53.42631352136791,14.562885502732085|53.42631322136791,14.562885222732085"
    private var departureTime = "&departure_time=1669393397"

    private var originlat = 0.0
    private var originlong = 0.0
    private var targetlat = 0.0
    private var targetlong = 0.0
    private var isTrackingOn = true
    private var doTracking = true
    private var isRoute = false
    private var isRouteStatic = false
    private var isTransit = false
    private var duration = ""
    private var distance = ""
    private var markerTimeOpen = ""
    private var markerTimeClose = ""
    private var markerTimeDuration = ""
    private var yourTime = ""
    private var isMultiple = false
    private var isSimpleRM = false
    private var totalDuration = 0
    private var isRMThreadOn = false

    private val polyline: MutableList<Polyline> = ArrayList()
    private val path: MutableList<List<LatLng>> = ArrayList()
    private val waypoints: MutableList<LatLng> = ArrayList()
    private val waypointsNames: MutableList<String> = ArrayList()
    private val polyColor: MutableList<Int> = ArrayList()
    private val transitTable = ArrayList<ArrayList<String>>()
    private lateinit var mGoogleMap: GoogleMap
    private var mapFrag: SupportMapFragment? = null
    private lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }
                lat = location.latitude
                long = location.longitude
                if (doTracking)
                    updateLocation()
                checkIfClose()
                if (isRoute && !isTransit)
                    updateRoute()
            }
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        doTracking = false
    }

    override fun onCameraIdle() {
        if (isTrackingOn)
            doTracking = true
    }

    private fun checkLocationPermission() {
        val t = Thread {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            ActivityCompat.requestPermissions(
                                this@MapScreen,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                MY_PERMISSIONS_REQUEST_LOCATION
                            )
                        }
                        .create()
                        .show()


                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION
                    )
                }
            }
        }
        t.start()
        t.join()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        Looper.myLooper()?.let {
                            mFusedLocationClient?.requestLocationUpdates(
                                mLocationRequest,
                                mLocationCallback,
                                it
                            )
                        }
                        mGoogleMap.isMyLocationEnabled = true
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        TrackingMapButton.isEnabled = false
        RouteMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
        MapLoadingScreen.visibility = View.VISIBLE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFrag?.getMapAsync(this)
        TransitView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        RMView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    public override fun onPause() {
        super.onPause()
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 200 // two minute interval
        mLocationRequest.fastestInterval = 200
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        with(googleMap) {
            setOnCameraMoveStartedListener(this@MapScreen)
            setOnCameraIdleListener(this@MapScreen)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Looper.myLooper()?.let {
                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    it
                )
            }
            mGoogleMap.isMyLocationEnabled = true
        } else {
            checkLocationPermission()
        }

        mGoogleMap.uiSettings.isMyLocationButtonEnabled = false
        mGoogleMap.uiSettings.isMapToolbarEnabled = false
        loadData()
        mGoogleMap.setOnMarkerClickListener { marker ->
            TrackingMapButton.isEnabled = false
            RouteMapButton.isEnabled = false
            MapHelpButton.isEnabled = false
            markerlat = marker.position.latitude
            markerlong = marker.position.longitude
            achievement = marker.title.toString()

            checkIfClose()

            MapMarkerTitle.text = marker.title
            MapMarkerText.text = marker.snippet
            if (doTracking)
                switchTracking()
            Handler(Looper.getMainLooper()).postDelayed({
                MapMarkerLayout.visibility = View.VISIBLE
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            }, 1000)
        }

        timePicker.setIs24HourView(true)
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        try {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json
                )
            )

        } catch (e: Resources.NotFoundException) {
            Log.e("xd", e.toString())
        }
        getCurrLoc()
    }

    private fun isClose() {
        MapMarkerRouteButton.visibility = View.GONE
        MapMarkerCloseButton.text = getString(R.string.add_achievement_text)
        MapMarkerCloseButton.setOnClickListener {
            addAchievement()
        }
    }

    private fun notClose() {
        MapMarkerCloseButton.text = getString(R.string.close_button_text)
        MapMarkerCloseButton.setOnClickListener {
            closeMarkerLayout()
        }
    }

    private fun checkIfClose() {
        if (lat >= (markerlat - 0.001)
            && lat <= (markerlat + 0.001)
            && long >= (markerlong - 0.001)
            && long <= (markerlong + 0.001)
        ) {
            isClose()
        } else {
            notClose()
        }
    }

    fun tracking(view: View) {
        switchTracking()
    }

    private fun switchTracking() {
        if (!isTrackingOn) {
            TrackingMapButton.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.button_green))
            getCurrLoc()
            doTracking = true
            isTrackingOn = true
        } else {
            TrackingMapButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            doTracking = false
            isTrackingOn = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrLoc() {
        goToLocation(lat, long)
    }

    private fun goToLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18F)
        mGoogleMap.moveCamera(cameraUpdate)
    }

    private fun updateLocation() {
        val latLng = LatLng(lat, long)
        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        mGoogleMap.moveCamera(cameraUpdate)
    }

    private fun addAchievement() {
        MapLoadingScreen.visibility = View.VISIBLE
        MapMarkerCloseButton.isEnabled = false
        if (isRoute and waypoints.isEmpty()) {
            stopRoute()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            var isSuccess = "-3"
            var lines = listOf("0")
            val t = Thread {
                isSuccess = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "addAchievement.php?nickname="
                            + LoginScreen.loggedUserNick + "&achievement="
                            + achievement
                )
                lines = isSuccess.split('\n')
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                MapMarkerLayout.visibility = View.GONE
                MapErrorText.text = getString(R.string.database_conn_error3_text)
                MapErrorLayout.visibility = View.VISIBLE
                MapLoadingScreen.visibility = View.GONE
            } else if (lines[0] == "-2") {
                MapMarkerLayout.visibility = View.GONE
                MapErrorText.text = getString(R.string.database_conn_error2_text)
                MapErrorLayout.visibility = View.VISIBLE
                MapLoadingScreen.visibility = View.GONE
            } else {
                MapMarkerCloseButton.isEnabled = true
                MapMarkerCloseButton.text = getString(R.string.close_button_text)
                MapMarkerCloseButton.setOnClickListener {
                    closeMarkerLayout()
                }
                MapMarkerText.text = getString(R.string.got_achievement_text)
                mGoogleMap.clear()
                loadData()
            }
            if (isRoute and waypoints.isNotEmpty()) {
                waypoints.removeAt(0)
                waypointsNames.removeAt(0)
            }
        }, 100)
    }

    private fun loadData() {
        var mapData = "-3"
        val t = Thread {
            mapData =
                DatabaseConnections.getTables(
                    getString(R.string.url_text) + "getMap.php?nickname="
                            + LoginScreen.loggedUserNick
                )
            list = mapData.split('\n')
        }
        t.start()
        t.join()

        if (list[0] == "-3") {
            MapErrorText.text = getString(R.string.database_conn_error3_text)
            MapErrorLayout.visibility = View.VISIBLE
            MapLoadingScreen.visibility = View.GONE
        } else if (list[0] == "-2") {
            MapErrorText.text = getString(R.string.database_conn_error2_text)
            MapErrorLayout.visibility = View.VISIBLE
            MapLoadingScreen.visibility = View.GONE
        } else {
            var poz = 0
            val row = ArrayList<String>()
            val objects = ArrayList<ArrayList<String>>()
            objects.clear()
            for (item in list) {
                row.add(item)
                poz++
                if (poz > 5) {
                    poz = 0
                    objects.add(row.clone() as ArrayList<String>)
                    row.clear()
                }
            }

            for (item in objects) {
                mGoogleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            item[2].toDouble(),
                            item[3].toDouble()
                        )
                    ).title(item[0]).snippet(item[1])
                )
            }
            rmObjects.clear()
            objects.sortBy { it[0] }
            for (item in objects) {
                rmObjects.add(RMObject(item[0], item[2].toDouble(), item[3].toDouble(), false))
            }
            RMView.swapAdapter(RMAdapter(rmObjects), true)
            RMView.layoutManager = LinearLayoutManager(this)
            TrackingMapButton.isEnabled = true
            RouteMapButton.isEnabled = true
            MapHelpButton.isEnabled = true
            MapLoadingScreen.visibility = View.GONE
        }
    }

    fun openHelp(view: View) {
        MapHelpLayout.visibility = View.VISIBLE
        TrackingMapButton.isEnabled = false
        RouteMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
    }

    fun goPreviousPage(view: View) {
        page -= 1
        if (page == 1) {
            MapHelpText.text = getString(R.string.help_page1_text)
            MapHelpPreviousButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            MapHelpPreviousButton.isEnabled = false
        }
        if (page == 2) {
            MapHelpText.text = getString(R.string.help_page2_text)
        }
        if (page == 3) {
            MapHelpText.text = getString(R.string.help_page3_text)
            MapHelpNextButton.text = getString(R.string.previous_page_text)
            MapHelpNextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
            MapHelpNextButton.setOnClickListener {
                goNextPage(it)
            }
        }
    }

    fun goNextPage(view: View) {
        page += 1
        if (page == 2) {
            MapHelpPreviousButton.setBackgroundColor(getColor(R.color.button_green))
            MapHelpPreviousButton.isEnabled = true
            MapHelpText.text = getString(R.string.help_page2_text)
        }
        if (page == 3) {
            MapHelpText.text = getString(R.string.help_page3_text)
        }
        if (page == 4) {
            MapHelpText.text = getString(R.string.help_page4_text)
            MapHelpNextButton.text = getString(R.string.close_button_text)
            MapHelpNextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            MapHelpNextButton.setOnClickListener {
                closeHelp(it)
            }
        }
    }

    private fun closeHelp(view: View) {
        page = 1
        MapHelpLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
        MapHelpNextButton.text = getString(R.string.previous_page_text)
        MapHelpNextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
        MapHelpNextButton.setOnClickListener {
            goNextPage(it)
        }
        MapHelpText.text = getString(R.string.help_page1_text)
        MapHelpPreviousButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
        MapHelpPreviousButton.isEnabled = false
    }

    fun closeMarkerLayoutButton(view: View) {
        closeMarkerLayout()
    }

    private fun closeMarkerLayout() {
        page = 1
        MapMarkerRouteButton.visibility = View.VISIBLE
        MapMarkerLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun closeMapErrorLayout(view: View) {
        MapErrorLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
        this.finish()
    }

    private fun openMapNotFound() {
        MapRouteNotFoundLayout.visibility = View.VISIBLE
        TrackingMapButton.isEnabled = false
        RouteMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
        isRouteStatic = false
    }

    fun closeMapRouteNotFoundLayout(view: View) {
        MapRouteNotFoundLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun stopRouteButton(view: View) {
        stopRoute()
    }

    private fun stopRoute() {
        isRouteStatic = false
        EndRouteMapButton.visibility = View.GONE
        TransitTop.visibility = View.INVISIBLE
        val param = TrackingMapButton.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 0, 20, 50)
        TrackingMapButton.layoutParams = param

        val param2 = RouteMapButton.layoutParams as ViewGroup.MarginLayoutParams
        param2.setMargins(20, 0, 0, 50)
        RouteMapButton.layoutParams = param2

        transitTable.clear()
        TransitView.swapAdapter(TransitAdapter(transitTable), true)
        TransitView.layoutManager = LinearLayoutManager(this)

        path.clear()
        for (line in polyline) {
            line.remove()
        }
        polyColor.clear()
        isRoute = false
    }

    fun openMapTravelMethodLayout(view: View) {
        isMultiple = false
        TravelMethodWalking.isEnabled = true
        TravelMethodDriving.isEnabled = true
        TravelMethodTransit.isEnabled = true
        TravelMethodClose.isEnabled = true
        MapMarkerLayout.visibility = View.GONE
        //MapRouteLayout.visibility = View.GONE
        TravelMethodWalking.setOnClickListener {
            movementMethod = "&mode=walking"
            openMapOriginLayout()
        }
        TravelMethodDriving.setOnClickListener {
            movementMethod = "&mode=driving"
            openMapOriginLayout()
        }
        TravelMethodTransit.setOnClickListener {
            movementMethod = "&mode=transit"
            openMapOriginLayout()
        }
        MapTravelMethodLayout.visibility = View.VISIBLE
    }

    fun closeMapTravelMethodLayout(view: View) {
        MapTravelMethodLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    private fun openMapOriginLayout() {
        MapTravelMethodLayout.visibility = View.GONE
        //MapRouteLayout.visibility = View.GONE
        MapOriginLayout.visibility = View.VISIBLE
        originlat = lat
        originlong = long
    }

    fun closeMapOriginLayout(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        MapOriginLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun checkIfOriginCorrectPlace(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        var flag = false
        val tmpText = OriginField.text.replace(" ".toRegex(), "+")
        val urlDirections =
            "https://maps.googleapis.com/maps/api/geocode/json?address=" + tmpText +
                    "&key=" + getString(R.string.google_maps_key)
        val t = Thread {
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)
            val results = jsonResponse.getJSONArray("results")
            if (results.isNull(0)) {
                flag = false
            } else {
                val geometry = results.getJSONObject(0).getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                originlat = location.getString("lat").toDouble()
                originlong = location.getString("lng").toDouble()
                flag = true
            }
        }
        t.start()
        t.join()

        if (!flag) {
            MapOriginLayout.visibility = View.GONE
            openMapNotFound()
        } else {
            isRouteStatic = true
            checkIfOriginCorrectLatLong(view)
        }
    }

    fun checkIfOriginCorrectLatLong(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        var flag = false
        targetlat = markerlat
        targetlong = markerlong
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod +
                    "&key=" + getString(R.string.google_maps_key)
        val t = Thread {
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)
            val routes = jsonResponse.getJSONArray("routes")
            flag = !routes.isNull(0)
        }
        t.start()
        t.join()
        if (!flag) {
            MapOriginLayout.visibility = View.GONE
            openMapNotFound()
        } else {
            closeMapOriginLayout(view)
            if (isSimpleRM) {
                MapLoadingScreen.visibility = View.VISIBLE
                isSimpleRM = false
                departureTime = ""
                drawRouteRMSetWaypoints()
            } else
                openMapDepartureTimeLayout()
        }
    }

    private fun openMapDepartureTimeLayout() {
        getTimes()
        MapTravelMethodLayout.visibility = View.GONE
    }

    fun closeMapDepartureTimeLayout(view: View) {
        MapTimeAssuranceBox.visibility = View.GONE
        MapDepartureTimeLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    private fun getTimes() {
        MapLoadingScreen.visibility = View.VISIBLE
        TravelMethodWalking.isEnabled = false
        TravelMethodDriving.isEnabled = false
        TravelMethodTransit.isEnabled = false
        TravelMethodClose.isEnabled = false
        timePicker.isEnabled = true
        DepartureTimeAccept.isEnabled = true
        DepartureTimeClose.isEnabled = true
        Handler(Looper.getMainLooper()).postDelayed({
            var isSuccess = "-3"
            var lines = listOf("0")
            val t = Thread {
                isSuccess = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "getTimes.php?achievement="
                            + achievement
                )
                lines = isSuccess.split('\n')
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                MapMarkerLayout.visibility = View.GONE
                MapErrorText.text = getString(R.string.database_conn_error3_text)
                MapErrorLayout.visibility = View.VISIBLE
                MapLoadingScreen.visibility = View.GONE
            } else if (lines[0] == "-2") {
                MapMarkerLayout.visibility = View.GONE
                MapErrorText.text = getString(R.string.database_conn_error2_text)
                MapErrorLayout.visibility = View.VISIBLE
                MapLoadingScreen.visibility = View.GONE
            } else {
                markerTimeOpen = lines[0]
                markerTimeClose = lines[1]
                markerTimeDuration = lines[2]
                val tmpText = "Godzina otwarcia: " + markerTimeOpen +
                        "\nGodzina zamknięcia: " + markerTimeClose +
                        "\nŚredni czas zwiedzania: " + markerTimeDuration
                var formatter = DateTimeFormatter.ofPattern("HH")
                timePicker.hour = LocalDateTime.now().format(formatter).toInt()
                formatter = DateTimeFormatter.ofPattern("mm")
                timePicker.minute = LocalDateTime.now().format(formatter).toInt()
                DepartureTimeText.text = tmpText
                MapLoadingScreen.visibility = View.GONE
                MapDepartureTimeLayout.visibility = View.VISIBLE
            }
        }, 100)
    }

    fun startRoute(view: View) {
        timePicker.isEnabled = false
        DepartureTimeAccept.isEnabled = false
        DepartureTimeClose.isEnabled = false
        val currentDate = dateFormat.format(Date())
        val myDate = currentDate + " " + timePicker.hour + ":" + timePicker.minute
        val date = dateTimeFormat.parse(myDate)
        date.time = date.time + 86400000
        if (date != null) {
            departureTime = "&departure_time=" + date.time
        }
        departureTime = departureTime.dropLast(3)
        var flag = false

        val t = Thread {
            flag = isTimeOk()
        }
        t.start()
        t.join()

        if (!flag) {
            closeMapDepartureTimeLayout(view)
            openMapNotFound()
        } else {

            var timeOnPlace =
                timePicker.hour * 3600 + timePicker.minute * 60 + duration.toInt() + 3600
            while (timeOnPlace >= 86400)
                timeOnPlace -= 86400

            val timeOpen = timeFormat.parse(markerTimeOpen)!!.time / 1000 + 3600
            val timeClose = timeFormat.parse(markerTimeClose)!!.time / 1000 + 3600
            val timeDuration = timeFormat.parse(markerTimeDuration)!!.time / 1000 + 3600

            if (timeOnPlace in (timeOpen until timeClose)) {
                if ((timeOnPlace + timeDuration) > timeClose) {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(3)
                } else {
                    drawRoute()
                    closeMapDepartureTimeLayout(view)
                }
            } else {
                if (timeOnPlace < timeOpen) {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(1)
                } else {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(2)
                }
            }
        }
        /*departureTime = ""
        drawRoute()
        MapDepartureTimeLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true*/
    }

    @SuppressLint("SetTextI18n")
    private fun openTimeAssuranceBox(variant: Int) {
        when (variant) {
            1 -> {
                MapTimeAssuranceText.text = getString(R.string.time_common_text1) + " " + yourTime +
                        getString(R.string.time_too_early_text) + " " + getString(R.string.time_common_text2)
            }
            2 -> {
                MapTimeAssuranceText.text = getString(R.string.time_common_text1) + " " + yourTime +
                        getString(R.string.time_too_late_text) + " " + getString(R.string.time_common_text2)
            }
            else -> {
                MapTimeAssuranceText.text = getString(R.string.time_common_text1) + " " + yourTime +
                        getString(R.string.time_not_enough_text) + " " + getString(R.string.time_common_text2)
            }
        }
        MapTimeAssuranceBox.visibility = View.VISIBLE
    }

    fun closeTimeAssuranceBox(view: View) {
        drawRoute()
        closeMapDepartureTimeLayout(view)
    }

    fun openMapDepartureTimeButton(view: View) {
        MapTimeAssuranceBox.visibility = View.GONE
        timePicker.isEnabled = true
        DepartureTimeAccept.isEnabled = true
        DepartureTimeClose.isEnabled = true
    }

    private fun isTimeOk(): Boolean {
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod + departureTime +
                    "&key=" + getString(R.string.google_maps_key)
        //println(urlDirections)
        val apiResponse = URL(urlDirections).readText()
        val jsonResponse = JSONObject(apiResponse)
        val routes = jsonResponse.getJSONArray("routes")
        return if (routes.isNull(0))
            false
        else {
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            duration = legs.getJSONObject(0).getJSONObject("duration")
                .getString("value")
            true
        }
    }

    //początek dla multiple (można by dawać warunki w istniejących funkcjach, ale tak będzie mi prościej na prezentacji pokazać co i jak)

    fun openRouteMultipleSimpleChoiceLayout(view: View) {
        isMultiple = true
        RMSimpleChoiceLayout.visibility = View.VISIBLE
        TrackingMapButton.isEnabled = false
        RouteMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
    }

    fun setSimpleRM(view: View) {
        isSimpleRM = true
        RMSimpleChoiceLayout.visibility = View.INVISIBLE
        MapRMLayout.visibility = View.VISIBLE
    }

    fun setAdvancedRM(view: View) {
        isSimpleRM = false
        RMSimpleChoiceLayout.visibility = View.INVISIBLE
        MapRMLayout.visibility = View.VISIBLE
    }

    fun closeRouteMultipleLayout(view: View) {
        MapRMLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun openMapTravelMethodLayoutRM(view: View) {
        waypoints.clear()
        waypointsNames.clear()
        for (obj in rmObjects) {
            if (obj.isSelected) {
                waypoints.add(LatLng(obj.lat, obj.long))
                waypointsNames.add(obj.name)
            }
        }
        markerlat = waypoints[0].latitude
        markerlong = waypoints[0].longitude
        achievement = waypointsNames[0]
        TravelMethodWalking.isEnabled = true
        TravelMethodDriving.isEnabled = true
        TravelMethodTransit.isEnabled = true
        TravelMethodClose.isEnabled = true
        MapRMLayout.visibility = View.GONE
        //MapRouteLayout.visibility = View.GONE
        TravelMethodWalking.setOnClickListener {
            movementMethod = "&mode=walking"
            openMapOriginLayoutRM()
        }
        TravelMethodDriving.setOnClickListener {
            movementMethod = "&mode=driving"
            openMapOriginLayoutRM()
        }
        TravelMethodTransit.setOnClickListener {
            movementMethod = "&mode=transit"
            openMapOriginLayoutRM()
        }
        MapTravelMethodLayout.visibility = View.VISIBLE
    }

    private fun openMapOriginLayoutRM() {
        MapTravelMethodLayout.visibility = View.GONE
        //MapRouteLayout.visibility = View.GONE
        originlat = lat
        originlong = long
        MapOriginLayout.visibility = View.VISIBLE
    }

    private fun updateRoute() {
        originlat = lat
        originlong = long
        if (isMultiple)
            if (!isRMThreadOn)
                drawRouteRM()
            else
                drawRoute()
    }

    private fun drawRoute() {
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod + departureTime +
                    "&key=" + getString(R.string.google_maps_key)
        //println(urlDirections)
        val directionsRequest = object :
            StringRequest(
                Method.GET,
                urlDirections,
                Response.Listener { response ->
                    val jsonResponse = JSONObject(response)
                    // Get routes
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.isNull(0) && !isRoute)
                        openMapNotFound()
                    else if (!routes.isNull(0)) {
                        if (!isRouteStatic)
                            isRoute = true
                        isTransit = false
                        polyColor.clear()
                        transitTable.clear()
                        path.clear()
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        distance = legs.getJSONObject(0).getJSONObject("distance")
                            .getString("value")
                        duration = legs.getJSONObject(0).getJSONObject("duration")
                            .getString("value")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")
                        for (i in 0 until steps.length()) {
                            val points =
                                steps.getJSONObject(i).getJSONObject("polyline")
                                    .getString("points")
                            path.add(PolyUtil.decode(points))
                            if (steps.getJSONObject(i).getString("travel_mode") == "TRANSIT") {
                                polyColor.add(Color.BLUE)
                                isTransit = true

                                TransitTop.visibility = View.VISIBLE
                                val transitRow = ArrayList<String>()
                                val transitDetails =
                                    steps.getJSONObject(i).getJSONObject("transit_details")
                                transitRow.add(
                                    transitDetails.getJSONObject("departure_stop")
                                        .getString("name")
                                )
                                transitRow.add(
                                    transitDetails.getJSONObject("line").getString("short_name")
                                )
                                transitRow.add(
                                    transitDetails.getJSONObject("arrival_stop")
                                        .getString("name")
                                )
                                transitTable.add(transitRow.clone() as ArrayList<String>)
                                transitRow.clear()

                                val param =
                                    TrackingMapButton.layoutParams as ViewGroup.MarginLayoutParams
                                param.setMargins(0, 0, 20, 600)
                                TrackingMapButton.layoutParams = param
                                val param2 =
                                    RouteMapButton.layoutParams as ViewGroup.MarginLayoutParams
                                param2.setMargins(20, 0, 0, 600)
                                RouteMapButton.layoutParams = param2

                                TransitView.swapAdapter(TransitAdapter(transitTable), true)
                                TransitView.layoutManager = LinearLayoutManager(this)

                            } else if (steps.getJSONObject(i)
                                    .getString("travel_mode") == "WALKING"
                            )
                                polyColor.add(Color.RED)
                            else
                                polyColor.add(Color.GREEN)
                        }

                        for (line in polyline) {
                            line.remove()
                        }
                        for (i in 0 until path.size) {
                            polyline.add(
                                mGoogleMap.addPolyline(
                                    PolylineOptions().addAll(path[i]).color(polyColor[i])
                                )
                            )
                        }
                        EndRouteMapButton.visibility = View.VISIBLE
                    }
                },
                Response.ErrorListener {
                }) {}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    private fun drawRouteRM() {
        val t = Thread {
            isRMThreadOn = true
            var urlDirections: String
            for (point in 0 until waypoints.size) {
                if (point == 0)
                    urlDirections =
                        getString(R.string.map_url_text) +
                                originlat + "," + originlong +
                                "&destination=" + waypoints[point].latitude +
                                "," + waypoints[point].longitude +
                                movementMethod + departureTime +
                                "&key=" + getString(R.string.google_maps_key)
                else
                    urlDirections =
                        getString(R.string.map_url_text) +
                                waypoints[point - 1].latitude +
                                "," + waypoints[point - 1].longitude +
                                "&destination=" + waypoints[point].latitude +
                                "," + waypoints[point].longitude +
                                movementMethod + departureTime +
                                "&key=" + getString(R.string.google_maps_key)
                //println(urlDirections)
                val apiResponse = URL(urlDirections).readText()
                val jsonResponse = JSONObject(apiResponse)

                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.isNull(0) && !isRoute)
                    openMapNotFound()
                else if (!routes.isNull(0)) {
                    if (!isRouteStatic)
                        isRoute = true
                    isTransit = false
                    if (point == 0) {
                        polyColor.clear()
                        transitTable.clear()
                        path.clear()
                    }
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    distance = legs.getJSONObject(0).getJSONObject("distance")
                        .getString("value")
                    duration = legs.getJSONObject(0).getJSONObject("duration")
                        .getString("value")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points =
                            steps.getJSONObject(i).getJSONObject("polyline")
                                .getString("points")
                        path.add(PolyUtil.decode(points))
                        if (steps.getJSONObject(i)
                                .getString("travel_mode") == "TRANSIT"
                        ) {
                            if (point == 0)
                                polyColor.add(Color.BLUE)
                            else
                                polyColor.add(Color.GRAY)
                            isTransit = true

                            TransitTop.visibility = View.VISIBLE
                            val transitRow = ArrayList<String>()
                            val transitDetails =
                                steps.getJSONObject(i).getJSONObject("transit_details")
                            transitRow.add(
                                transitDetails.getJSONObject("departure_stop")
                                    .getString("name")
                            )
                            transitRow.add(
                                transitDetails.getJSONObject("line")
                                    .getString("short_name")
                            )
                            transitRow.add(
                                transitDetails.getJSONObject("arrival_stop")
                                    .getString("name")
                            )
                            transitTable.add(transitRow.clone() as ArrayList<String>)
                            transitRow.clear()

                            val param =
                                TrackingMapButton.layoutParams as ViewGroup.MarginLayoutParams
                            param.setMargins(0, 0, 20, 600)
                            TrackingMapButton.layoutParams = param
                            val param2 =
                                RouteMapButton.layoutParams as ViewGroup.MarginLayoutParams
                            param2.setMargins(20, 0, 0, 600)
                            RouteMapButton.layoutParams = param2

                            TransitView.swapAdapter(TransitAdapter(transitTable), true)
                            TransitView.layoutManager = LinearLayoutManager(this)

                        } else if (steps.getJSONObject(i)
                                .getString("travel_mode") == "WALKING"
                        )
                            if (point == 0)
                                polyColor.add(Color.RED)
                            else
                                polyColor.add(Color.GRAY)
                        else
                            if (point == 0)
                                polyColor.add(Color.GREEN)
                            else
                                polyColor.add(Color.GRAY)
                    }
                }
            }

            val mainHandler = Handler(Looper.getMainLooper())

            val myRunnable = Runnable {
                for (line in polyline) {
                    line.remove()
                }
                for (i in path.size - 1 downTo 0) {
                    polyline.add(
                        mGoogleMap.addPolyline(
                            PolylineOptions().addAll(path[i]).color(polyColor[i])
                        )
                    )
                }
            }
            mainHandler.post(myRunnable)
            isRMThreadOn = false
        }
        t.start()
    }

    private fun drawRouteRMSetWaypoints() {
        Handler(Looper.getMainLooper()).postDelayed({
            totalDuration = 0
            val t = Thread {
                var urlDirections: String
                for (point in 0 until waypoints.size) {
                    var minLegs = JSONArray()
                    var minDuration = 0
                    for (p in point until waypoints.size) {
                        if (point == 0)
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        originlat + "," + originlong +
                                        "&destination=" + waypoints[point].latitude +
                                        "," + waypoints[point].longitude +
                                        movementMethod + departureTime +
                                        "&key=" + getString(R.string.google_maps_key)
                        else
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        waypoints[point - 1].latitude +
                                        "," + waypoints[point - 1].longitude +
                                        "&destination=" + waypoints[point].latitude +
                                        "," + waypoints[point].longitude +
                                        movementMethod + departureTime +
                                        "&key=" + getString(R.string.google_maps_key)
                        //println(urlDirections)
                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        // Get routes
                        val routes = jsonResponse.getJSONArray("routes")
                        if (routes.isNull(0) && !isRoute)
                            openMapNotFound()
                        else if (!routes.isNull(0)) {
                            if (!isRouteStatic)
                                isRoute = true
                            isTransit = false
                            if (point == 0) {
                                polyColor.clear()
                                transitTable.clear()
                                path.clear()
                            }
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            distance = legs.getJSONObject(0).getJSONObject("distance")
                                .getString("value")
                            duration = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value")
                            if (duration.toInt() < minDuration || p == point) {
                                minDuration = duration.toInt()
                                val tmpWaypoint = waypoints[point]
                                waypoints[point] = waypoints[p]
                                waypoints[p] = tmpWaypoint
                                val tmpName = waypointsNames[point]
                                waypointsNames[point] = waypointsNames[p]
                                waypointsNames[p] = tmpName
                                minLegs = legs
                            }
                        }
                    }
                    totalDuration += minLegs.getJSONObject(0).getJSONObject("duration")
                        .getString("value").toInt()
                    val steps = minLegs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points =
                            steps.getJSONObject(i).getJSONObject("polyline")
                                .getString("points")
                        path.add(PolyUtil.decode(points))
                        if (steps.getJSONObject(i)
                                .getString("travel_mode") == "TRANSIT"
                        ) {
                            if (point == 0)
                                polyColor.add(Color.BLUE)
                            else
                                polyColor.add(Color.GRAY)
                            isTransit = true

                            TransitTop.visibility = View.VISIBLE
                            val transitRow = ArrayList<String>()
                            val transitDetails =
                                steps.getJSONObject(i).getJSONObject("transit_details")
                            transitRow.add(
                                transitDetails.getJSONObject("departure_stop")
                                    .getString("name")
                            )
                            transitRow.add(
                                transitDetails.getJSONObject("line")
                                    .getString("short_name")
                            )
                            transitRow.add(
                                transitDetails.getJSONObject("arrival_stop")
                                    .getString("name")
                            )
                            transitTable.add(transitRow.clone() as ArrayList<String>)
                            transitRow.clear()

                            val param =
                                TrackingMapButton.layoutParams as ViewGroup.MarginLayoutParams
                            param.setMargins(0, 0, 20, 600)
                            TrackingMapButton.layoutParams = param
                            val param2 =
                                RouteMapButton.layoutParams as ViewGroup.MarginLayoutParams
                            param2.setMargins(20, 0, 0, 600)
                            RouteMapButton.layoutParams = param2

                            TransitView.swapAdapter(TransitAdapter(transitTable), true)
                            TransitView.layoutManager = LinearLayoutManager(this)

                        } else if (steps.getJSONObject(i)
                                .getString("travel_mode") == "WALKING"
                        )
                            if (point == 0)
                                polyColor.add(Color.RED)
                            else
                                polyColor.add(Color.GRAY)
                        else
                            if (point == 0)
                                polyColor.add(Color.GREEN)
                            else
                                polyColor.add(Color.GRAY)
                    }
                }
            }
            t.start()
            t.join()

            for (line in polyline) {
                line.remove()
            }
            for (i in path.size - 1 downTo 0) {
                polyline.add(
                    mGoogleMap.addPolyline(
                        PolylineOptions().addAll(path[i]).color(polyColor[i])
                    )
                )
            }
            EndRouteMapButton.visibility = View.VISIBLE

            MapLoadingScreen.visibility = View.GONE
        }, 100)
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }
}