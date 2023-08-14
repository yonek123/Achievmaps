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
import kotlin.system.measureTimeMillis


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

    private var departureTime = "1703292800"

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

    private val TSPwaypoints: MutableList<Pair<Int, MutableList<LatLng>>> = ArrayList()
    val tagArr: ArrayList<TagObj> = arrayListOf()

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
                if (poz > 9) {
                    poz = 0
                    objects.add(row.clone() as ArrayList<String>)
                    row.clear()
                }
            }

            var counter = 0
            for (item in objects) {
                if (counter < 50 && (item[8].contains("indoors")
                            && item[9].toInt() >=3 )
                ) {
                    //if (true) {
                    counter += 1
                    mGoogleMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                item[2].toDouble(),
                                item[3].toDouble()
                            )
                        ).title(item[0]).snippet(item[1])
                    )
                }
            }
            rmObjects.clear()
            objects.sortBy { it[0] }
            for (item in objects) {
                if (item[4].equals(""))
                    item[4] = "00:00"
                if (item[5].equals(""))
                    item[5] = "24:00"
                val timeOpen = timeFormat.parse(item[4])!!.time / 1000 + 3600
                val timeClose = timeFormat.parse(item[5])!!.time / 1000 + 3600
                val timeDuration = timeFormat.parse(item[6])!!.time / 1000 + 3600
                rmObjects.add(
                    RMObject(
                        item[0],
                        item[2].toDouble(),
                        item[3].toDouble(),
                        timeOpen,
                        timeClose,
                        timeDuration,
                        item[7],
                        item[8],
                        item[9].toInt(),
                        false
                    )
                )
            }
            tagArr.add(TagObj("outdoors", false))
            tagArr.add(TagObj("indoors", false))
            tagArr.add(TagObj("museum", false))
            tagArr.add(TagObj("church", false))
            tagArr.add(TagObj("castle", false))
            tagArr.add(TagObj("cementary", false))
            tagArr.add(TagObj("statue", false))
            tagArr.add(TagObj("park", false))
            tagArr.add(TagObj("other", false))
            RMView.swapAdapter(RMAdapter(tagArr), true)
            RMView.layoutManager = LinearLayoutManager(this)
            TrackingMapButton.isEnabled = true
            RouteMapButton.isEnabled = true
            MapHelpButton.isEnabled = true
            MapLoadingScreen.visibility = View.GONE
        }
    }

    fun openHelp(view: View) {
        TSPAlgo()
        /*MapHelpLayout.visibility = View.VISIBLE
        TrackingMapButton.isEnabled = false
        RouteMapButton.isEnabled = false
        MapHelpButton.isEnabled = false*/
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
        isRoute = false
        val t = Thread {
            while (isRMThreadOn) {
                isRouteStatic = true
                isRoute = false
                Thread.sleep(1000)
            }
            val mainHandler = Handler(Looper.getMainLooper())

            val myRunnable = Runnable {
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
                polyColor.clear()

                for (line in polyline) {
                    line.remove()
                }
            }
            mainHandler.post(myRunnable)
        }
        t.start()
    }

    fun openMapTravelMethodLayout(view: View) {
        isMultiple = true
        isSimpleRM = false
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
                isSimpleRM = false
                departureTime = ""
                mechanizmWnioskujacy(
                    "Szczecin",
                    mutableListOf("statue", "castle", "museum"),
                    1703292800,
                    3
                )
            } else if (isMultiple) {
                openMapDepartureTimeRMLayout(view)
            } else
                openMapDepartureTimeLayout(view)
        }
    }

    private fun openMapDepartureTimeLayout(view: View) {
        getTimes(view)
        MapTravelMethodLayout.visibility = View.GONE
    }

    private fun openMapDepartureTimeRMLayout(view: View) {
        getTimesRM(view)
        MapTravelMethodLayout.visibility = View.GONE
        TrackingMapButton.visibility = View.GONE
        RouteMapButton.visibility = View.GONE
    }

    fun closeMapDepartureTimeLayout(view: View) {
        MapTimeAssuranceBox.visibility = View.GONE
        MapDepartureTimeLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    private fun getTimes(view: View) {
        MapLoadingScreen.visibility = View.VISIBLE
        TravelMethodWalking.isEnabled = false
        TravelMethodDriving.isEnabled = false
        TravelMethodTransit.isEnabled = false
        TravelMethodClose.isEnabled = false
        timePicker.isEnabled = true
        DepartureTimeAccept.setOnClickListener() { startRoute(view) }
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
                MapLoadingScreen.visibility = View.GONE
                MapDepartureTimeLayout.visibility = View.VISIBLE
            }
        }, 100)
    }

    private fun getTimesRM(view: View) {
        MapLoadingScreen.visibility = View.VISIBLE
        TravelMethodWalking.isEnabled = false
        TravelMethodDriving.isEnabled = false
        TravelMethodTransit.isEnabled = false
        TravelMethodClose.isEnabled = false
        timePicker.isEnabled = true
        DepartureTimeAccept.setOnClickListener() { startRouteRM(view) }
        DepartureTimeAccept.isEnabled = true
        DepartureTimeClose.isEnabled = true

        var formatter = DateTimeFormatter.ofPattern("HH")
        timePicker.hour = LocalDateTime.now().format(formatter).toInt()
        formatter = DateTimeFormatter.ofPattern("mm")
        timePicker.minute = LocalDateTime.now().format(formatter).toInt()
        MapLoadingScreen.visibility = View.GONE
        MapDepartureTimeLayout.visibility = View.VISIBLE
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
            departureTime = date.time.toString()
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
            timeOnPlace = timeOnPlace.mod(86400) + 1

            val timeOpen = timeFormat.parse(markerTimeOpen)!!.time / 1000 + 3600
            val timeClose = timeFormat.parse(markerTimeClose)!!.time / 1000 + 3600
            val timeDuration = timeFormat.parse(markerTimeDuration)!!.time / 1000 + 3600

            if (timeOnPlace in (timeOpen until timeClose)) {
                if ((timeOnPlace + timeDuration) > timeClose) {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(3, view)
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
                    openTimeAssuranceBox(1, view)
                } else {
                    val hours = floor((timeOnPlace + timeDuration) / 3600.0).toInt()
                    val minutes = floor((timeOnPlace + timeDuration) % 3600 / 60.0).toInt()
                    val timeString = String.format("%02d:%02d", hours, minutes)
                    yourTime = timeString + "\n"
                    openTimeAssuranceBox(2, view)
                }
            }
        }
    }

    fun startRouteRM(view: View) {
        timePicker.isEnabled = false
        DepartureTimeAccept.isEnabled = false
        DepartureTimeClose.isEnabled = false
        val currentDate = dateFormat.format(Date())
        val myDate = currentDate + " " + timePicker.hour + ":" + timePicker.minute
        val date = dateTimeFormat.parse(myDate)
        date.time = date.time + 86400000
        if (date != null) {
            departureTime = date.time.toString()
        }
        departureTime = departureTime.dropLast(3)

        var timeOnPlace =
            timePicker.hour * 3600 + timePicker.minute * 60 + 3600
        timeOnPlace = timeOnPlace.mod(86400) + 1

        val sortedTimes: MutableList<RMObject> = ArrayList()

        var tmpText = "Nie starczy czasu na zwiedzenie obiektów: "
        var tmpFlag = true;
        for (obj in rmObjects) {
            if (obj.isSelected) {
                if (obj.close > timeOnPlace)
                    sortedTimes.add(obj)
                else {
                    tmpText += obj.name + ", "
                    tmpFlag = false
                }
            }
        }

        if (!sortedTimes.isEmpty()) {
            sortedTimes.sortBy { it.close }

            waypoints.clear()
            var tmpDepTime = departureTime
            sortedTimes.forEachIndexed { index, obj ->
                if (index > 0) {
                    tmpDepTime = routeMultipleCheckTimes(obj, sortedTimes[index - 1], tmpDepTime)
                }
                if (obj.isSelected)
                    waypoints.add(LatLng(obj.lat, obj.long))
                else {
                    tmpText += obj.name + ", "
                    tmpFlag = false
                }

                tmpDepTime = (tmpDepTime.toLong() + obj.duration).toString()
            }

            tmpText.dropLast(2)
            tmpText += ".\nObiekty zostały usunięte z listy. Jeśli koniecznie chcesz je odwiedzić ponownie wybierz obiekty i wyznacz trasę od innej godziny lub z innego miejsca."

            if (!tmpFlag) {
                MapTimeAssuranceText.text = tmpText
                TimeAssuranceAccept.visibility = View.GONE
                TimeAssuranceClose.text = "OK"
                TimeAssuranceClose.setOnClickListener() {
                    MapLoadingScreen.visibility = View.VISIBLE
                    drawRouteRMSetWaypoints()
                    closeMapDepartureTimeLayout(view)
                }
                MapTimeAssuranceBox.visibility = View.VISIBLE
            } else {
                MapLoadingScreen.visibility = View.VISIBLE
                drawRouteRMSetWaypoints()
                MapDepartureTimeLayout.visibility = View.GONE
                TrackingMapButton.isEnabled = true
                RouteMapButton.isEnabled = true
                MapHelpButton.isEnabled = true
            }
        } else {
            MapTimeAssuranceText.text =
                "Wszystkie obiekty są zamknięte albo brak obiektów na liście"
            TimeAssuranceAccept.visibility = View.GONE
            TimeAssuranceClose.text = "OK"
            TimeAssuranceClose.setOnClickListener() { closeMapDepartureTimeLayout(view) }
            MapTimeAssuranceBox.visibility = View.VISIBLE
            MapDepartureTimeLayout.visibility = View.GONE
            TrackingMapButton.isEnabled = true
            RouteMapButton.isEnabled = true
            MapHelpButton.isEnabled = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openTimeAssuranceBox(variant: Int, view: View) {
        TimeAssuranceAccept.visibility = View.VISIBLE
        TimeAssuranceClose.text = "Nie"
        TimeAssuranceClose.setOnClickListener() { closeTimeAssuranceBox(view) }
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
                    movementMethod + "&departure_time=" + departureTime +
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
        rmObjects.forEach { obj ->
            obj.isSelected = false
        }
        tagArr.forEach { obj ->
            obj.isSelected = false
        }
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
        for (obj in rmObjects)
            for (tag in tagArr)
                if (tag.isSelected)
                    if (obj.tags.contains(tag.name)) {
                        obj.isSelected = true
                        break
                    }

        if (rmObjects.isEmpty())
            closeRouteMultipleLayout(view)
        else {
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
        /*if (isMultiple)
            if (!isRMThreadOn)
                drawRouteRM()
            else
                drawRoute()*/
    }

    private fun drawRoute() {
        val urlDirections =
            getString(R.string.map_url_text) +
                    originlat + "," + originlong +
                    "&destination=" + targetlat + "," + targetlong +
                    movementMethod + "&departure_time=" + departureTime +
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
            for (point in 0 until waypoints.size + 1) {
                if (point == 0)
                    urlDirections =
                        getString(R.string.map_url_text) +
                                originlat + "," + originlong +
                                "&destination=" + waypoints[point].latitude +
                                "," + waypoints[point].longitude +
                                movementMethod + "&departure_time=" + departureTime +
                                "&key=" + getString(R.string.google_maps_key)
                else if (point == waypoints.size)
                    urlDirections =
                        getString(R.string.map_url_text) +
                                waypoints[point - 1].latitude + "," + waypoints[point - 1].longitude +
                                "&destination=" + originlat +
                                "," + originlong +
                                movementMethod + "&departure_time=" + departureTime +
                                "&key=" + getString(R.string.google_maps_key)
                else
                    urlDirections =
                        getString(R.string.map_url_text) +
                                waypoints[point - 1].latitude +
                                "," + waypoints[point - 1].longitude +
                                "&destination=" + waypoints[point].latitude +
                                "," + waypoints[point].longitude +
                                movementMethod + "&departure_time=" + departureTime +
                                "&key=" + getString(R.string.google_maps_key)
                //println(urlDirections)
                val apiResponse = URL(urlDirections).readText()
                val jsonResponse = JSONObject(apiResponse)

                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.isNull(0) && !isRoute)
                    println(urlDirections)
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
                                polyColor.add(Color.GREEN)
                            else if(point== waypoints.size)
                                polyColor.add(Color.RED)
                            else
                                polyColor.add(Color.GRAY)
                        else
                            if (point == 0)
                                polyColor.add(Color.GREEN)
                            else if(point== waypoints.size)
                                polyColor.add(Color.RED)
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
                                        "&destination=" + waypoints[p].latitude +
                                        "," + waypoints[p].longitude +
                                        movementMethod + "&departure_time=" + departureTime +
                                        "&key=" + getString(R.string.google_maps_key)
                        else
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        waypoints[point - 1].latitude +
                                        "," + waypoints[point - 1].longitude +
                                        "&destination=" + waypoints[p].latitude +
                                        "," + waypoints[p].longitude +
                                        movementMethod + "&departure_time=" + departureTime +
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

    private fun routeMultipleCheckTimes(
        obj: RMObject,
        prevObj: RMObject,
        tmpDepTime: String
    ): String {
        var returnVal = tmpDepTime
        val t = Thread {
            val urlDirections =
                getString(R.string.map_url_text) +
                        originlat + "," + originlong +
                        "&destination=" + prevObj.lat +
                        "," + prevObj.long +
                        movementMethod + tmpDepTime +
                        "&key=" + getString(R.string.google_maps_key)

            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            if (routes.isNull(0)) {
                obj.isSelected = true
            } else {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val tmpDuration = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value")
                if ((tmpDepTime.toLong() + tmpDuration.toLong()).mod(86400) < obj.close &&
                    (tmpDepTime.toLong() + tmpDuration.toLong()).mod(86400) > obj.open
                ) {
                    obj.isSelected = true
                    returnVal = (tmpDepTime.toLong() + tmpDuration.toLong()).toString()
                } else {
                    obj.isSelected = false
                    returnVal = tmpDepTime
                }
            }
        }
        t.start()
        t.join()
        return returnVal
    }


    fun mechanizmWnioskujacy(
        city: String,
        tags: MutableList<String>,
        tripDate: Long,
        minScore: Int
    ) {
        var counter = 0
        for (obj in rmObjects) {
            if (counter > 7)
                break
            if (obj.city == city)
                for (t in tags)
                    if (t in obj.tags && obj.score >= minScore) {
                        waypoints.add(LatLng(obj.lat, obj.long))
                        counter += 1
                        break
                    }
        }
    }

    var numa = 0
    private fun TSPAlgo() {
        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800
        var points: MutableList<LatLng> = ArrayList()
        val algo = 5
        val test = 3
        if (test == 0) {
            for (i in rmObjects) {
                if (i.tags.contains("outdoors"))
                    points.add(LatLng(i.lat, i.long))
            }
            V = 11
            N = 11
            movementMethod = "&mode=walking"
        } else if (test == 1) {
            for (i in rmObjects) {
                if (i.tags.contains("museum"))
                    points.add(LatLng(i.lat, i.long))
            }
            V = 6
            N = 6
            movementMethod = "&mode=walking"
        } else if (test == 2) {
            for (i in rmObjects) {
                if (i.tags.contains("church"))
                    points.add(LatLng(i.lat, i.long))
            }
            V = 26
            N = 26
            movementMethod = "&mode=driving"
        } else if (test == 3) {
            for (i in rmObjects) {
                if ((i.tags.contains("indoors")) &&
                    i.score >= 3
                )
                    points.add(LatLng(i.lat, i.long))
            }
            println(points.size + 1)
            V = points.size + 1
            N = points.size + 1
            movementMethod = "&mode=walking"
            println(V)
        }
        var wp = ""
        for (i in points) {
            wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
        }
        wp.dropLast(1)

        var urlDirections =
            getString(R.string.map_url_text) +
                    tmpLat + "," + tmpLong +
                    "&waypoints=" + wp +
                    "&destination=" + tmpLat + "," + tmpLong +
                    movementMethod + "&departure_time=" + timeStamp +
                    "&key=" + getString(R.string.google_maps_key)

        if (algo == 0) {
            points.clear()
            for (i in rmObjects) {
                if (i.tags.contains("museum"))
                    points.add(LatLng(i.lat, i.long))
            }
            wp = ""
            for (i in points) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)
            var urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val timeInMillis = measureTimeMillis {
                bruteForceIterate(0, points)
            }
            var result = TSPwaypoints[0]
            for (i in TSPwaypoints)
                if (i.first > 0 && i.first < result.first)
                    result = i
            println("Bruteforce")
            println(urlDirections)
            println("Result time: " + result)
            println("Work time: " + timeInMillis)
        } else if (algo == 1) {
            val timeInMillis = measureTimeMillis {
                branchNBound(points)
            }
            println("B&B")
            println("Minimum cost : " + final_res)
            println("Path Taken : ")
            for (i in 0..N) {
                print(final_path[i])
            }
            println()
            println(timeInMillis)
        } else if (algo == 2) {
            val timeInMillis = measureTimeMillis {
                nearestNeighbour(points)
            }
            println("Nearest neighbour")
            println("Work time: " + timeInMillis)
        } else if (algo == 3) {
            val timeInMillis = measureTimeMillis {
                cheapestInsertion(points)
            }
            println("Cheapest Insert")
            println("Work time: " + timeInMillis)
        } else if (algo == 4) {
            val timeInMillis = measureTimeMillis {
                farthestInsertion(points)
            }
            println("Farthest Insert")
            println("Work time: " + timeInMillis)
        } else if (algo == 5) {
            val timeInMillis = measureTimeMillis {
                christofides(points)
            }
            println("Christofides")
            println("Work time: " + timeInMillis)
        } else if (algo == 6) {
            val timeInMillis = measureTimeMillis {
                cheapestConvex(points, points.size)
            }
            println("cheapestConvex")
            println("Work time: " + timeInMillis)
        } else if (algo == 7) {
            val timeInMillis = measureTimeMillis {
                farthestConvex(points, points.size)
            }
            println("farthestConvex")
            println("Work time: " + timeInMillis)
        }
    }

    /// Brute force search
    private fun bruteForceIterate(index: Int, points: MutableList<LatLng>) {
        if (index == 1) {
            println(numa)
            numa += 1
        }
        if (index == points.size - 1) {
            val tmpLat = lat
            val tmpLong = long
            val timeStamp = 1703292800
            var wp = ""
            var resultTime = -1
            for (i in points) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)
            val t = Thread {
                var urlDirections =
                    getString(R.string.map_url_text) +
                            tmpLat + "," + tmpLong +
                            "&waypoints=" + wp +
                            "&destination=" + tmpLat + "," + tmpLong +
                            movementMethod + "&departure_time=" + timeStamp +
                            "&key=" + getString(R.string.google_maps_key)
                val apiResponse = URL(urlDirections).readText()
                val jsonResponse = JSONObject(apiResponse)

                val routes = jsonResponse.getJSONArray("routes")
                if (!routes.isNull(0)) {
                    resultTime += 1
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    resultTime = legs.getJSONObject(0).getJSONObject("duration")
                        .getString("value").toInt()
                }
            }
            t.start()
            t.join()
            TSPwaypoints.add(Pair(resultTime, points.toMutableList()))
        } else {
            for (i in index until points.size) {
                if (i == index)
                    bruteForceIterate(index + 1, points)
                else {
                    var tmpPoints = points.toMutableList()
                    var tmp = tmpPoints[i]
                    tmpPoints[i] = tmpPoints[index]
                    tmpPoints[index] = tmp
                    bruteForceIterate(index + 1, tmpPoints)
                }
            }
        }
    }


    ///Branch and bound
    private var N = 11
    private var final_path = IntArray(N + 1)
    private var visited = BooleanArray(N)
    private var final_res = Int.MAX_VALUE
    private fun copyToFinal(curr_path: IntArray) {
        for (i in 0 until N) final_path[i] = curr_path[i]
        final_path[N] = curr_path[0]
    }

    private fun firstMin(adj: Array<IntArray>, i: Int): Int {
        var min = Int.MAX_VALUE
        for (k in 0 until N) if (adj[i][k] < min && i != k) min = adj[i][k]
        return min
    }

    private fun secondMin(adj: Array<IntArray>, i: Int): Int {
        var first = Int.MAX_VALUE
        var second = Int.MAX_VALUE
        for (j in 0 until N) {
            if (i == j) continue
            if (adj[i][j] <= first) {
                second = first
                first = adj[i][j]
            } else if (adj[i][j] <= second &&
                adj[i][j] != first
            ) second = adj[i][j]
        }
        return second
    }

    private fun TSPRec(
        adj: Array<IntArray>, curr_bound: Int, curr_weight: Int,
        level: Int, curr_path: IntArray
    ) {
        var curr_bound = curr_bound
        var curr_weight = curr_weight
        if (level == N) {
            if (adj[curr_path[level - 1]][curr_path[0]] != 0) {
                val curr_res = curr_weight +
                        adj[curr_path[level - 1]][curr_path[0]]

                if (curr_res < final_res) {
                    copyToFinal(curr_path)
                    final_res = curr_res
                }
            }
            return
        }

        for (i in 0 until N) {
            if (adj[curr_path[level - 1]][i] != 0 &&
                visited[i] == false
            ) {
                val temp = curr_bound
                curr_weight += adj[curr_path[level - 1]][i]

                curr_bound -= if (level == 1) (firstMin(adj, curr_path[level - 1]) +
                        firstMin(adj, i)) / 2 else (secondMin(adj, curr_path[level - 1]) +
                        firstMin(adj, i)) / 2

                if (curr_bound + curr_weight < final_res) {
                    curr_path[level] = i
                    visited[i] = true

                    TSPRec(
                        adj, curr_bound, curr_weight, level + 1,
                        curr_path
                    )
                }

                curr_weight -= adj[curr_path[level - 1]][i]
                curr_bound = temp

                Arrays.fill(visited, false)
                for (j in 0..level - 1) visited[curr_path[j]] = true
            }
        }
    }

    private fun branchNBound(points: MutableList<LatLng>) {
        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800
        points.add(0, LatLng(tmpLat, tmpLong))
        var adj: Array<IntArray> = Array(N) { IntArray(N) }
        for (i in 0 until points.size) {
            for (j in 0 until points.size) {
                if (i != j) {
                    val t = Thread {
                        var urlDirections =
                            getString(R.string.map_url_text) +
                                    points[i].latitude + "," + points[i].longitude +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)
                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            adj[i][j] = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                        }
                    }
                    t.start()
                    t.join()
                }
            }
        }
        val curr_path = IntArray(N + 1)

        var curr_bound = 0
        Arrays.fill(curr_path, -1)
        Arrays.fill(visited, false)

        for (i in 0 until N) curr_bound += firstMin(adj, i) +
                secondMin(adj, i)

        curr_bound = if (curr_bound == 1) curr_bound / 2 + 1 else curr_bound / 2

        visited[0] = true
        curr_path[0] = 0

        TSPRec(adj, curr_bound, 0, 1, curr_path)
    }


    ///Nearest neighbour
    private fun nearestNeighbour(points: MutableList<LatLng>) {
        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800

        var urlDirections = ""

        val t = Thread {
            for (i in -1 until points.size - 2) {
                var currMinVal = 0
                var currMinIndex = 0
                for (j in i + 1 until points.size) {
                    if (i == -1)
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    tmpLat + "," + tmpLong +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)
                    else
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    points[j - 1].latitude + "," + points[j - 1].longitude +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)

                    val apiResponse = URL(urlDirections).readText()
                    val jsonResponse = JSONObject(apiResponse)

                    val routes = jsonResponse.getJSONArray("routes")
                    if (!routes.isNull(0)) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                            .getString("value").toInt()
                        if (resultTime < currMinVal || j == i + 1) {
                            currMinVal = resultTime
                            currMinIndex = j
                        }
                    }
                }
                val tmp = points[i + 1]
                points[i + 1] = points[currMinIndex]
                points[currMinIndex] = tmp
            }
            var wp = ""
            for (i in points) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)

            var res = 0
            urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                res = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value").toInt()
            }
            println(urlDirections)
            println(res)
        }
        t.start()
        t.join()
    }


    ///Cheapest Insertion
    private fun cheapestInsertion(points: MutableList<LatLng>) {
        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800

        var urlDirections = ""

        val t = Thread {
            var currRoute: MutableList<LatLng> = mutableListOf()
            var currBestRoute: MutableList<LatLng> = mutableListOf()
            for (i in -1 until points.size - 1) {
                println(currRoute.joinToString())
                var currMinVal = 0
                var currMinIndex = 0

                for (j in i + 1 until points.size) {
                    if (i == -1) {
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    tmpLat + "," + tmpLong +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)

                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                            if (resultTime < currMinVal || j == i + 1) {
                                currMinVal = resultTime
                                currMinIndex = j
                            }
                        }
                    } else {
                        for (k in 0 until currRoute.size + 1) {
                            var currRouteTmp = currRoute.toMutableList()
                            if (k == currRoute.size)
                                currRouteTmp.add(points[j])
                            else
                                currRouteTmp.add(k, points[j])
                            var wp = ""
                            for (p in currRouteTmp) {
                                wp =
                                    wp + "via:" + p.latitude + "," + p.longitude + "|"
                            }
                            wp.dropLast(1)
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        tmpLat + "," + tmpLong +
                                        "&waypoints=" + wp +
                                        "&destination=" + tmpLat + "," + tmpLong +
                                        movementMethod + "&departure_time=" + timeStamp +
                                        "&key=" + getString(R.string.google_maps_key)

                            val apiResponse = URL(urlDirections).readText()
                            val jsonResponse = JSONObject(apiResponse)

                            val routes = jsonResponse.getJSONArray("routes")
                            if (!routes.isNull(0)) {
                                val legs = routes.getJSONObject(0).getJSONArray("legs")
                                val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                    .getString("value").toInt()
                                if (resultTime < currMinVal || j == i + 1) {
                                    currMinVal = resultTime
                                    currMinIndex = j
                                    currBestRoute = currRouteTmp.toMutableList()
                                }
                            }
                        }
                    }
                }
                if (i == -1) {
                    val tmp = points[i + 1]
                    points[i + 1] = points[currMinIndex]
                    points[currMinIndex] = tmp
                    currRoute.add(points[i + 1])
                } else {
                    val tmp = points[i + 1]
                    points[i + 1] = points[currMinIndex]
                    points[currMinIndex] = tmp
                    currRoute = currBestRoute.toMutableList()
                    println(currRoute.joinToString())
                }
            }
            var wp = ""
            for (i in currRoute) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)

            var res = 0
            urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                res = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value").toInt()
            }
            println(urlDirections)
            println(res)
        }
        t.start()
        t.join()
    }


    ///Farthest Insertion
    private fun farthestInsertion(points: MutableList<LatLng>) {
        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800

        var urlDirections = ""

        val t = Thread {
            var currRoute: MutableList<LatLng> = mutableListOf()
            var currBestRoute: MutableList<LatLng> = mutableListOf()
            for (i in -1 until points.size - 1) {
                println(currRoute.joinToString())
                var currMinVal = 0
                var currMinIndex = 0
                var currFarthestVal = 0
                var currFarthestIndex = 0

                for (j in i + 1 until points.size) {
                    if (i == -1) {
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    tmpLat + "," + tmpLong +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)

                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                            if (resultTime > currFarthestVal) {
                                currFarthestVal = resultTime
                                currFarthestIndex = j
                            }
                        }
                    } else {
                        for (k in 0 until currRoute.size + 1) {
                            if (k == currRoute.size)
                                urlDirections =
                                    getString(R.string.map_url_text) +
                                            tmpLat + "," + tmpLong +
                                            "&destination=" + points[j].latitude + "," + points[j].longitude +
                                            movementMethod + "&departure_time=" + timeStamp +
                                            "&key=" + getString(R.string.google_maps_key)
                            else
                                urlDirections =
                                    getString(R.string.map_url_text) +
                                            currRoute[k].latitude + "," + currRoute[k].longitude +
                                            "&destination=" + points[j].latitude + "," + points[j].longitude +
                                            movementMethod + "&departure_time=" + timeStamp +
                                            "&key=" + getString(R.string.google_maps_key)


                            val apiResponse = URL(urlDirections).readText()
                            val jsonResponse = JSONObject(apiResponse)

                            val routes = jsonResponse.getJSONArray("routes")
                            if (!routes.isNull(0)) {
                                val legs = routes.getJSONObject(0).getJSONArray("legs")
                                val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                    .getString("value").toInt()
                                if (resultTime > currFarthestVal) {
                                    currFarthestVal = resultTime
                                    currFarthestIndex = j
                                }
                            }
                        }
                    }
                }
                if (i == -1) {
                    val tmp = points[i + 1]
                    points[i + 1] = points[currFarthestIndex]
                    points[currFarthestIndex] = tmp
                    currRoute.add(points[i + 1])
                } else {
                    val tmp = points[i + 1]
                    points[i + 1] = points[currFarthestIndex]
                    points[currFarthestIndex] = tmp
                    for (k in 0 until currRoute.size + 1) {
                        var currRouteTmp = currRoute.toMutableList()
                        if (k == currRoute.size)
                            currRouteTmp.add(points[i + 1])
                        else
                            currRouteTmp.add(k, points[i + 1])
                        var wp = ""
                        for (p in currRouteTmp) {
                            wp =
                                wp + "via:" + p.latitude + "," + p.longitude + "|"
                        }
                        wp.dropLast(1)
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    tmpLat + "," + tmpLong +
                                    "&waypoints=" + wp +
                                    "&destination=" + tmpLat + "," + tmpLong +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)

                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                            if (resultTime < currMinVal || k == 0) {
                                currMinVal = resultTime
                                currMinIndex = k
                                currBestRoute = currRouteTmp.toMutableList()
                            }
                        }
                    }
                    currRoute = currBestRoute.toMutableList()
                    println(currRoute.joinToString())
                }
            }
            var wp = ""
            for (i in currRoute) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)

            var res = 0
            urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                res = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value").toInt()
            }
            println(urlDirections)
            println(res)
        }
        t.start()
        t.join()
    }


    ///Christofides
    private var V = 11

    fun minKey(key: MutableList<Int>, mstSet: MutableList<Boolean>): Int {
        var min = Int.MAX_VALUE
        var min_index = -1
        for (v in 0 until V) {
            if (mstSet[v] == false && key[v] < min) {
                min = key[v]
                min_index = v
            }
        }
        return min_index
    }

    fun christofides(points: MutableList<LatLng>) {
        val tmpLat = lat
        val tmpLong = long
        originlat = tmpLat
        originlong = tmpLong
        val timeStamp = 1703292800
        val tmpPoints = points.toMutableList()
        points.add(0, LatLng(tmpLat, tmpLong))
        var graph: Array<IntArray> = Array(V) { IntArray(V) }
        val t = Thread {
            for (i in 0 until points.size) {
                for (j in 0 until points.size) {
                    if (i != j) {
                        var urlDirections =
                            getString(R.string.map_url_text) +
                                    points[i].latitude + "," + points[i].longitude +
                                    "&destination=" + points[j].latitude + "," + points[j].longitude +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)
                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            graph[i][j] = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                        }
                        else
                            println(apiResponse)
                    }
                }
            }

            for (i in graph) {
                for (j in i)
                    print(j.toString() + " ")
                println()
            }

            val parent = IntArray(V)
            val key: MutableList<Int> = arrayListOf()
            val mstSet: MutableList<Boolean> = arrayListOf()
            val edgeCounter: MutableList<Int> = arrayListOf()

            for (i in 0 until V) {
                key.add(Int.MAX_VALUE)
                mstSet.add(false)
                edgeCounter.add(0)
            }

            key[0] = 0
            parent[0] = -1

            for (count in 0 until (V - 1)) {
                val u = minKey(key, mstSet)
                mstSet[u] = true
                for (v in 0 until V) {
                    if ((graph[u][v] != 0) &&
                        (mstSet[v] == false) &&
                        (graph[u][v] < key[v])
                    ) {
                        parent[v] = u
                        key[v] = graph[u][v]
                    }
                }
            }

            val edgePairs: MutableList<Pair<Int, Int>> = mutableListOf()
            for (i in 1 until V) {
                edgeCounter[i] += 1
                edgeCounter[parent[i]] += 1
                edgePairs.add(Pair(i, parent[i]))
            }

            for (i in 0 until V) {
                if (edgeCounter[i] % 2 == 1) {
                    var minEdge = Int.MAX_VALUE
                    var minEdgeIndex = -1
                    for (j in i + 1 until V)
                        if (edgeCounter[j] % 2 == 1 &&
                            graph[i][j] < minEdge &&
                            (Pair(i, j) !in edgePairs || Pair(j, i) !in edgePairs)
                        ) {
                            minEdge = graph[i][j]
                            minEdgeIndex = j
                        }
                    edgeCounter[i] += 1
                    edgeCounter[minEdgeIndex] += 1
                    edgePairs.add(Pair(i, minEdgeIndex))
                }
            }

            val sortedList: MutableList<Pair<Int, Int>> =
                edgePairs.sortedWith(compareBy({ graph[it.first][it.second] })).toMutableList()

            for (i in 0 until sortedList.size) {
                if (sortedList[i].first == 0 || sortedList[i].second == 0) {
                    var tmp = sortedList[i]
                    sortedList.removeAt(i)
                    sortedList.add(0, tmp)
                    break
                }
            }

            val usedList: MutableList<Int> = mutableListOf()
            var next = 0
            if (sortedList[0].first == 0) {
                next = sortedList[0].second
                usedList.add(sortedList[0].first)
                usedList.add(sortedList[0].second)
            } else {
                next = sortedList[0].first
                usedList.add(sortedList[0].second)
                usedList.add(sortedList[0].first)
            }
            sortedList.removeAt(0)

            while (usedList.size < V) {
                for (i in 0 until sortedList.size) {
                    if (sortedList[i].first in usedList && sortedList[i].second in usedList) {
                        sortedList.removeAt(i)
                        break
                    }
                    if (sortedList[i].first == next && sortedList[i].second !in usedList) {
                        next = sortedList[i].second
                        usedList.add(sortedList[i].second)
                        sortedList.removeAt(i)
                        break
                    }
                    if (sortedList[i].second == next && sortedList[i].first !in usedList) {
                        next = sortedList[i].first
                        usedList.add(sortedList[i].first)
                        sortedList.removeAt(i)
                        break
                    }
                }
                if (sortedList[0].first !in usedList) {
                    next = sortedList[0].first
                    usedList.add(sortedList[0].first)
                }
                if (sortedList[0].second !in usedList) {
                    next = sortedList[0].second
                    usedList.add(sortedList[0].second)
                }
                sortedList.removeAt(0)
            }
            usedList.remove(0)

            var wp = ""
            waypoints.clear()
            var counter = 0
            for (p in usedList) {
                println(tmpPoints[p - 1].latitude.toString() + " " + tmpPoints[p - 1].longitude)
                wp =
                    wp + "via:" + tmpPoints[p - 1].latitude + "," + tmpPoints[p - 1].longitude + "|"
                waypoints.add(LatLng(tmpPoints[p - 1].latitude, tmpPoints[p - 1].longitude))
                counter+=1
                if(counter>=14)
                    break
            }
            wp.dropLast(1)
            var urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            println(urlDirections)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                println(
                    "Duration: " + legs.getJSONObject(0).getJSONObject("duration")
                        .getString("value")
                )
            }
            isRoute = true
            drawRouteRM()
        }
        t.start()
        t.join()
    }


    ///Convexhull
    open fun orientation(p: LatLng, q: LatLng, r: LatLng): Int {
        val `val`: Double = (q.longitude - p.longitude) * (r.latitude - q.latitude) -
                (q.latitude - p.latitude) * (r.longitude - q.longitude)
        if (`val` == 0.000) return 0
        return if (`val` > 0) 1 else 2
    }

    fun cheapestConvex(points: MutableList<LatLng>, n: Int) {
        if (n < 3) return
        val hull: Vector<LatLng> = Vector<LatLng>()
        var l = 0
        for (i in 1 until n) if (points[i].latitude < points[l].latitude) l = i

        var p = l
        var q: Int
        do {
            hull.add(points[p])
            q = (p + 1) % n
            for (i in 0 until n) {
                if (orientation(points[p], points[i], points[q])
                    == 2
                ) q = i
            }
            p = q
        } while (p != l)

        val tmpList: MutableList<Int> = mutableListOf()
        for (i in hull) {
            if (i in points) {
                points.remove(i)
            }
        }

        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800

        val newPoints = hull.toMutableList()
        newPoints.addAll(points)
        var urlDirections = ""

        val t = Thread {
            var currRoute = hull.toMutableList()
            var currBestRoute = hull.toMutableList()

            var currMinVal = 0
            var currMinIndex = 0
            var wp = ""
            for (i in 0 until hull.size) {
                for (j in 0 until hull.size) {
                    currRoute[j] = hull[(i + j) % hull.size]
                    wp =
                        wp + "via:" + currRoute[j].latitude + "," + currRoute[j].longitude + "|"
                }
                wp.dropLast(1)

                urlDirections =
                    getString(R.string.map_url_text) +
                            tmpLat + "," + tmpLong +
                            "&waypoints=" + wp +
                            "&destination=" + tmpLat + "," + tmpLong +
                            movementMethod + "&departure_time=" + timeStamp +
                            "&key=" + getString(R.string.google_maps_key)

                val apiResponse = URL(urlDirections).readText()
                val jsonResponse = JSONObject(apiResponse)

                val routes = jsonResponse.getJSONArray("routes")
                if (!routes.isNull(0)) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                        .getString("value").toInt()
                    if (resultTime < currMinVal || i == 0) {
                        currMinVal = resultTime
                        currBestRoute = currRoute.toMutableList()
                    }
                }
            }
            currRoute = currBestRoute.toMutableList()

            for (i in hull.size until newPoints.size - 1) {
                currMinVal = 0
                currMinIndex = 0
                println(currRoute.joinToString())
                for (j in i + 1 until newPoints.size) {
                    for (k in 0 until currRoute.size + 1) {
                        var currRouteTmp = currRoute.toMutableList()
                        if (k == currRoute.size)
                            currRouteTmp.add(newPoints[j])
                        else
                            currRouteTmp.add(k, newPoints[j])
                        wp = ""
                        for (p in currRouteTmp) {
                            wp =
                                wp + "via:" + p.latitude + "," + p.longitude + "|"
                        }
                        wp.dropLast(1)
                        urlDirections =
                            getString(R.string.map_url_text) +
                                    tmpLat + "," + tmpLong +
                                    "&waypoints=" + wp +
                                    "&destination=" + tmpLat + "," + tmpLong +
                                    movementMethod + "&departure_time=" + timeStamp +
                                    "&key=" + getString(R.string.google_maps_key)

                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                            if (resultTime < currMinVal || j == i + 1) {
                                currMinVal = resultTime
                                currMinIndex = j
                                currBestRoute = currRouteTmp.toMutableList()
                            }
                        }
                    }
                }
                val tmp = newPoints[i + 1]
                newPoints[i + 1] = newPoints[currMinIndex]
                newPoints[currMinIndex] = tmp
                currRoute = currBestRoute.toMutableList()
                println(currRoute.joinToString())
            }
            wp = ""
            for (i in currRoute) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)

            var res = 0
            urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                res = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value").toInt()
            }
            println(urlDirections)
            println(res)
        }
        t.start()
        t.join()
    }


    ///farthest hull
    fun farthestConvex(points: MutableList<LatLng>, n: Int) {
        if (n < 3) return
        val hull: Vector<LatLng> = Vector<LatLng>()
        var l = 0
        for (i in 1 until n) if (points[i].latitude < points[l].latitude) l = i

        var p = l
        var q: Int
        do {
            hull.add(points[p])
            q = (p + 1) % n
            for (i in 0 until n) {
                if (orientation(points[p], points[i], points[q])
                    == 2
                ) q = i
            }
            p = q
        } while (p != l)

        val tmpList: MutableList<Int> = mutableListOf()
        for (i in hull) {
            if (i in points) {
                points.remove(i)
            }
        }

        val tmpLat = lat
        val tmpLong = long
        val timeStamp = 1703292800

        val newPoints = hull.toMutableList()
        newPoints.addAll(points)
        var urlDirections = ""

        val t = Thread {
            var currRoute = hull.toMutableList()
            var currBestRoute = hull.toMutableList()

            var currMinVal = 0
            var currMinIndex = 0
            var wp = ""
            for (i in 0 until hull.size) {
                for (j in 0 until hull.size) {
                    currRoute[j] = hull[(i + j) % hull.size]
                    wp =
                        wp + "via:" + currRoute[j].latitude + "," + currRoute[j].longitude + "|"
                }
                wp.dropLast(1)

                urlDirections =
                    getString(R.string.map_url_text) +
                            tmpLat + "," + tmpLong +
                            "&waypoints=" + wp +
                            "&destination=" + tmpLat + "," + tmpLong +
                            movementMethod + "&departure_time=" + timeStamp +
                            "&key=" + getString(R.string.google_maps_key)

                val apiResponse = URL(urlDirections).readText()
                val jsonResponse = JSONObject(apiResponse)

                val routes = jsonResponse.getJSONArray("routes")
                if (!routes.isNull(0)) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                        .getString("value").toInt()
                    if (resultTime < currMinVal || i == 0) {
                        currMinVal = resultTime
                        currBestRoute = currRoute.toMutableList()
                    }
                }
            }
            currRoute = currBestRoute.toMutableList()

            for (i in hull.size until newPoints.size - 1) {
                currMinVal = 0
                currMinIndex = 0
                var currFarthestVal = 0
                var currFarthestIndex = 0
                println(currRoute.joinToString())
                for (j in i + 1 until newPoints.size) {
                    for (k in 0 until currRoute.size + 1) {
                        if (k == currRoute.size)
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        tmpLat + "," + tmpLong +
                                        "&destination=" + newPoints[j].latitude + "," + newPoints[j].longitude +
                                        movementMethod + "&departure_time=" + timeStamp +
                                        "&key=" + getString(R.string.google_maps_key)
                        else
                            urlDirections =
                                getString(R.string.map_url_text) +
                                        currRoute[k].latitude + "," + currRoute[k].longitude +
                                        "&destination=" + newPoints[j].latitude + "," + newPoints[j].longitude +
                                        movementMethod + "&departure_time=" + timeStamp +
                                        "&key=" + getString(R.string.google_maps_key)


                        val apiResponse = URL(urlDirections).readText()
                        val jsonResponse = JSONObject(apiResponse)

                        val routes = jsonResponse.getJSONArray("routes")
                        if (!routes.isNull(0)) {
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                                .getString("value").toInt()
                            if (resultTime > currFarthestVal) {
                                currFarthestVal = resultTime
                                currFarthestIndex = j
                            }
                        }
                    }
                }
                val tmp = newPoints[i + 1]
                newPoints[i + 1] = newPoints[currFarthestIndex]
                newPoints[currFarthestIndex] = tmp
                for (k in 0 until currRoute.size + 1) {
                    var currRouteTmp = currRoute.toMutableList()
                    if (k == currRoute.size)
                        currRouteTmp.add(newPoints[i + 1])
                    else
                        currRouteTmp.add(k, newPoints[i + 1])
                    var wp = ""
                    for (p in currRouteTmp) {
                        wp =
                            wp + "via:" + p.latitude + "," + p.longitude + "|"
                    }
                    wp.dropLast(1)
                    urlDirections =
                        getString(R.string.map_url_text) +
                                tmpLat + "," + tmpLong +
                                "&waypoints=" + wp +
                                "&destination=" + tmpLat + "," + tmpLong +
                                movementMethod + "&departure_time=" + timeStamp +
                                "&key=" + getString(R.string.google_maps_key)

                    val apiResponse = URL(urlDirections).readText()
                    val jsonResponse = JSONObject(apiResponse)

                    val routes = jsonResponse.getJSONArray("routes")
                    if (!routes.isNull(0)) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val resultTime = legs.getJSONObject(0).getJSONObject("duration")
                            .getString("value").toInt()
                        if (resultTime < currMinVal || k == 0) {
                            currMinVal = resultTime
                            currMinIndex = k
                            currBestRoute = currRouteTmp.toMutableList()
                        }
                    }
                }
                currRoute = currBestRoute.toMutableList()
                println(currRoute.joinToString())
            }
            wp = ""
            for (i in currRoute) {
                wp = wp + "via:" + i.latitude + "," + i.longitude + "|"
            }
            wp.dropLast(1)

            var res = 0
            urlDirections =
                getString(R.string.map_url_text) +
                        tmpLat + "," + tmpLong +
                        "&waypoints=" + wp +
                        "&destination=" + tmpLat + "," + tmpLong +
                        movementMethod + "&departure_time=" + timeStamp +
                        "&key=" + getString(R.string.google_maps_key)
            val apiResponse = URL(urlDirections).readText()
            val jsonResponse = JSONObject(apiResponse)

            val routes = jsonResponse.getJSONArray("routes")
            if (!routes.isNull(0)) {
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                res = legs.getJSONObject(0).getJSONObject("duration")
                    .getString("value").toInt()
            }
            println(urlDirections)
            println(res)
        }
        t.start()
        t.join()
    }
}
