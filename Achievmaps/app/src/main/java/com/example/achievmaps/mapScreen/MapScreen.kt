package com.example.achievmaps.mapScreen

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
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
import java.lang.reflect.InvocationTargetException
import kotlin.concurrent.thread


class MapScreen : AppCompatActivity(),
    OnMapReadyCallback,
    OnCameraMoveStartedListener,
    OnCameraIdleListener {
    private var page = 0
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var objects = ArrayList<ArrayList<String>>()
    private var achievement = ""

    private var lat = 0.0
    private var long = 0.0
    private var markerlat = 10.0
    private var markerlong = 10.0
    private var isTrackingOn = true
    private var doTracking = true
    private var isRoute = false
    private var isTransit = false
    private var movementMethod = "&mode=walking"
    //private var waypoints = "&waypoints=53.42631352136791,14.562885502732085|53.42631322136791,14.562885222732085"
    private var departureTime = "&departure_time=1669393397"
    private var originlat = 0.0
    private var originlong = 0.0
    private var targetlat = 0.0
    private var targetlong = 0.0

    private var duration = ""
    private var distance = ""
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

                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
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
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
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
            TrackingMapButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.button_green)))
            getCurrLoc()
            doTracking = true
            isTrackingOn = true
        } else {
            TrackingMapButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY))
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

    fun closeHelp(view: View) {
        page = 1
        MapHelpLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
        MapHelpNextButton.text = getString(R.string.previous_page_text)
        MapHelpNextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
        MapHelpNextButton.setOnClickListener(View.OnClickListener {
            goNextPage(it)
        })
        MapHelpText.text = getString(R.string.help_page1_text)
        MapHelpPreviousButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
        MapHelpPreviousButton.isEnabled = false
    }

    private fun closeMarkerLayoutButton(view: View) {
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

    fun openMapNotFound() {
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

    fun stopRoute() {
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
        openMapDepartureTimeLayout()
    }

    fun setDriving(view: View) {
        movementMethod = "&mode=driving"
        openMapDepartureTimeLayout()
    }

    fun setTransit(view: View) {
        movementMethod = "&mode=transit"
        openMapDepartureTimeLayout()
    }

    fun openMapDepartureTimeLayout() {
        MapTravelMethodLayout.visibility = View.GONE
        //MapDepartureTimeLayout.visibility = View.VISIBLE
    }

    fun closeMapDepartureTimeLayout(view: View) {
        //MapDepartureTimeLayout.visibility = View.GONE
        TrackingMapButton.isEnabled = true
        RouteMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
    }

    fun routeOneTarget(view: View) {
        updateRoute()
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
        //"https://maps.googleapis.com/maps/api/directions/json?origin=53.45237680317154,14.537924413421782&destination=53.388689987076354,14.515383062995541&mode=transit&key=AIzaSyDi6Eaj-EWJX3Mt6eu1PfNRglnP6GVZLC0"
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat + "," + long + "&destination=53.388689987076354,14.515383062995541&mode=transit&key=AIzaSyDi6Eaj-EWJX3Mt6eu1PfNRglnP6GVZLC0"

        val directionsRequest = object :
            StringRequest(
                Request.Method.GET,
                urlDirections,
                Response.Listener<String> { response ->
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
                            .getString("text")
                        duration = legs.getJSONObject(0).getJSONObject("duration")
                            .getString("text")
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

                                val transit_details =
                                    steps.getJSONObject(i).getJSONObject("transit_details")
                                transitRow.add(
                                    transit_details.getJSONObject("departure_stop")
                                        .getString("name")
                                )
                                transitRow.add(
                                    transit_details.getJSONObject("line").getString("short_name")
                                )
                                transitRow.add(
                                    transit_details.getJSONObject("arrival_stop").getString("name")
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
                Response.ErrorListener { _ ->
                }) {}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }
}