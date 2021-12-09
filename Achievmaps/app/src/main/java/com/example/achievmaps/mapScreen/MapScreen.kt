package com.example.achievmaps.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import com.example.achievmaps.R
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.achievmaps.databaseConnections.MapDB

import com.example.achievmaps.loginScreen.LoginScreen
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_maps.*

import android.util.Log

import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.main_menu_screen.*

class MapScreen : AppCompatActivity(), OnMapReadyCallback {

    private var page = 0
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var objects = ArrayList<ArrayList<String>>()
    private var lat = 0.0
    private var long = 0.0
    private var markers = ArrayList<Marker>()


    lateinit var mGoogleMap: GoogleMap
    var mapFrag: SupportMapFragment? = null
    lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    internal var mFusedLocationClient: FusedLocationProviderClient? = null


    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }

                //Place current location marker
                lat = location.latitude
                long = location.longitude

                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false)
                mGoogleMap.getUiSettings().setMapToolbarEnabled(false)
                RefreshMapButton.isEnabled = true
                MapHelpButton.isEnabled = true
                MapLoadingScreen.visibility = View.GONE
                loadData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        RefreshMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
        MapLoadingScreen.visibility = View.VISIBLE

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFrag?.getMapAsync(this)


    }

    public override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        try {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json
                )
            );

        } catch (e: Resources.NotFoundException) {
            Log.e("xd", e.toString())
        }
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 2000 // two minute interval
        mLocationRequest.fastestInterval = 2000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )
                mGoogleMap.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MapScreen,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
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
                        mGoogleMap.setMyLocationEnabled(true)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    fun myLoc(view: View) {
        getCurrLoc()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrLoc() {
        goToLocation(lat, long)
    }

    private fun goToLocation(latitude: Double, longitude: Double) {
        var latLng = LatLng(latitude, longitude)
        var cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18F)
        mGoogleMap.moveCamera(cameraUpdate)
    }

    fun loadData() {
        var mapData = "-3"
        var posData = "-3"
        val t = Thread {
            mapData =
                MapDB.getMap(LoginScreen.loggedUserNick)
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

            var marker: Marker
            for (item in objects) {
                markers.add(mGoogleMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            item[2].toDouble(),
                            item[3].toDouble()
                        )
                    ).title(item[0]).snippet(item[1])
                ))
            }
            MapLoadingScreen.visibility = View.GONE
        }
    }

    fun openHelp(view: View) {
        MapHelpLayout.visibility = View.VISIBLE
        RefreshMapButton.isEnabled = false
        MapHelpButton.isEnabled = false
    }

    fun goPreviousPage(view: View) {
        page = page - 1
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
            MapHelpNextButton.setOnClickListener(View.OnClickListener {
                goNextPage(it)
            })
        }
    }

    fun goNextPage(view: View) {
        page = page + 1
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
            MapHelpNextButton.setOnClickListener(View.OnClickListener {
                closeHelp(it)
            })
        }
    }

    fun closeHelp(view: View) {
        page = 1
        MapHelpLayout.visibility = View.GONE
        RefreshMapButton.isEnabled = true
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

    fun closeMapErrorLayout(view: View) {
        MapErrorLayout.visibility = View.GONE
        RefreshMapButton.isEnabled = true
        MapHelpButton.isEnabled = true
        this.finish()
    }
}