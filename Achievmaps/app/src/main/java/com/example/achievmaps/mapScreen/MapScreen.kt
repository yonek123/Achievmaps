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
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor


@Suppress(
    "DEPRECATION", "NAME_SHADOWING", "UNCHECKED_CAST", "UNUSED_PARAMETER",
    "VARIABLE_WITH_REDUNDANT_INITIALIZER"
)
class MapScreen : AppCompatActivity(),
    OnMapReadyCallback,
    OnCameraMoveStartedListener,
    OnCameraIdleListener {
    private var page = 0
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var objects = ArrayList<ArrayList<String>>()
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
    private var isTransit = false
    private var duration = ""
    private var distance = ""
    private var markerTimeOpen = ""
    private var markerTimeClose = ""
    private var markerTimeDuration = ""
    private var yourTime = ""

    private val polyline: MutableList<Polyline> = ArrayList()
    private val path: MutableList<List<LatLng>> = ArrayList()
    private val polyColor: MutableList<Int> = ArrayList()
    private var transitRow = ArrayList<String>()
    private var transitTable = ArrayList<ArrayList<String>>()
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
            achievement = marker.title

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
        if (isRoute) {
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
                markerlat += 1
                MapMarkerCloseButton.text = getString(R.string.close_button_text)
                MapMarkerCloseButton.setOnClickListener {
                    closeMarkerLayout()
                }
                MapMarkerText.text = getString(R.string.got_achievement_text)
                mGoogleMap.clear()
                loadData()
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
            val t = Thread {
                var poz = 0
                row.clear()
                objects.clear()
                for (item in list) {
                    row.add(item)
                    poz++
                    if (poz > 3) {
                        poz = 0
                        objects.add(row.clone() as ArrayList<String>)
                        row.clear()
                    }
                }
            }
            t.start()
            t.join()

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
        isRoute = false
    }

    fun openMapTravelMethodLayout(view: View) {
        TravelMethodWalking.isEnabled = true
        TravelMethodDriving.isEnabled = true
        TravelMethodTransit.isEnabled = true
        TravelMethodClose.isEnabled = true
        MapMarkerLayout.visibility = View.GONE
        //MapRouteLayout.visibility = View.GONE
        MapTravelMethodLayout.visibility = View.VISIBLE
    }

    fun closeMapTravelMethodLayout(view: View) {
        MapTravelMethodLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun setWalking(view: View) {
        movementMethod = "&mode=walking"
        openMapOriginLayout()
    }

    fun setDriving(view: View) {
        movementMethod = "&mode=driving"
        openMapOriginLayout()
    }

    fun setTransit(view: View) {
        movementMethod = "&mode=transit"
        openMapOriginLayout()
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
        println(urlDirections)
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
        println(urlDirections)
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
            openMapDepartureTimeLayout()
        }
    }

    private fun openMapDepartureTimeLayout() {
        getTimes()
        MapTravelMethodLayout.visibility = View.GONE
    }

    fun closeMapDepartureTimeLayout(view: View) {
        closeMapDepartureTimeLayout()
    }

    private fun closeMapDepartureTimeLayout() {
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
            closeMapDepartureTimeLayout()
            openMapNotFound()
        } else {

            var timeOnPlace =
                timePicker.hour * 3600 + timePicker.minute * 60 + duration.toInt() + 3600
            while (timeOnPlace >= 86400)
                timeOnPlace -= 86400

            val timeOpen = timeFormat.parse(markerTimeOpen)!!.time / 1000 + 3600
            val timeClose = timeFormat.parse(markerTimeClose)!!.time / 1000 + 3600
            val timeDuration = timeFormat.parse(markerTimeDuration)!!.time / 1000 + 3600
            println(timeOnPlace)

            if (timeOnPlace in (timeOpen until timeClose)) {
                if ((timeOnPlace + timeDuration) > timeClose) {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(3)
                } else {
                    updateRoute()
                    MapDepartureTimeLayout.visibility = View.GONE
                    TrackingMapButton.isEnabled = true
                    RouteMapButton.isEnabled = true
                    MapHelpButton.isEnabled = true
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
        updateRoute()
        MapDepartureTimeLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
        closeMapDepartureTimeLayout()
    }

    fun openMapDepartureTimeButton(view: View) {
        MapTimeAssuranceBox.visibility = View.GONE
        timePicker.isEnabled = true
        DepartureTimeAccept.isEnabled = true
        DepartureTimeClose.isEnabled = true
    }

    private fun isTimeOk(): Boolean {
        originlat = lat
        originlong = long
        targetlat = markerlat
        targetlong = markerlong
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod + departureTime +
                    "&key=" + getString(R.string.google_maps_key)
        println(urlDirections)
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

    fun routeMultipleTarget(view: View) {
        updateRoute()
    }

    private fun updateRoute() {
        originlat = lat
        originlong = long
        targetlat = markerlat
        targetlong = markerlong
        drawRoute()
    }

    private fun drawRoute() {
        transitTable.clear()
        path.clear()
        //"https://maps.googleapis.com/maps/api/directions/json?origin=53.45237680317154,14.537924413421782&destination=53.388689987076354,14.515383062995541&mode=transit&key=AIzaSyDi6Eaj-EWJX3Mt6eu1PfNRglnP6GVZLC0"
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod + departureTime +
                    "&key=" + getString(R.string.google_maps_key)
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
                        isRoute = true
                        isTransit = false
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
                                    transitDetails.getJSONObject("arrival_stop").getString("name")
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

                            } else if (steps.getJSONObject(i).getString("travel_mode") == "WALKING")
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
}