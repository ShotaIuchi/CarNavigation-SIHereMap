package com.sihuocthai.map.here

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapviewlite.MapScene.LoadSceneCallback
import com.here.sdk.mapviewlite.MapSceneConfig
import com.here.sdk.mapviewlite.MapStyle
import com.here.sdk.mapviewlite.MapViewLite
import com.here.sdk.search.SearchCallback
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.sihuocthai.map.here.PermissionsRequestor.ResultListener


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var mapView: MapViewLite
    private lateinit var permissionsRequestor: PermissionsRequestor
    private lateinit var searchEngine: SearchEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById<MapViewLite>(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        try {
            searchEngine = SearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of SearchEngine failed: " + e.error.name)
        }

        handleAndroidPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun handleAndroidPermissions() {
        permissionsRequestor = PermissionsRequestor(this)
        permissionsRequestor.request(object : ResultListener {
            override fun permissionsGranted() {
                loadMapScene()
            }

            override fun permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun loadMapScene() {
        mapView.mapScene.loadScene(
            MapStyle.NORMAL_DAY,
            MapSceneConfig().apply {
                //this.mainLanguageCode = LanguageCode.JA_JP
                //this.fallbackLanguageCode = LanguageCode.JA_JP
            },
            LoadSceneCallback { errorCode ->
                if (errorCode == null) {
                    //mapView.camera.target = GeoCoordinates(35.12243151268302, 135.94515931399008)
                    mapView.camera.zoomLevel = 14.0
                } else {
                    Log.d(TAG, "onLoadScene failed: $errorCode");
                }
            })
        mapView.gestures.longPressListener = LongPressListener { gestureState, touchPoint ->
            val geoCoordinates = mapView.camera.viewToGeoCoordinates(touchPoint)
            if (gestureState == GestureState.BEGIN) {
                Log.d(TAG, "LongPress detected at: $geoCoordinates")
            }
            if (gestureState == GestureState.UPDATE) {
                Log.d(TAG, "LongPress update at: $geoCoordinates")
            }
            if (gestureState == GestureState.END) {
                Log.d(TAG, "LongPress finger lifted at: $geoCoordinates")
            }
        }
        mapView.gestures.tapListener = TapListener {
            val geoCoordinates = mapView.camera.viewToGeoCoordinates(it)
            Log.d(TAG, "Tap at: $geoCoordinates")
            getAddressForCoordinates(geoCoordinates)
        }
    }

    private fun getAddressForCoordinates(geoCoordinates: GeoCoordinates) {
        val maxItems = 1
        val reverseGeocodingOptions = SearchOptions(LanguageCode.EN_GB, maxItems)
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, SearchCallback { searchError, list ->
            if (searchError != null) {
                Log.d(TAG, "getAddressForCoordinates: NG: $searchError")
                return@SearchCallback
            }

            Log.d(TAG, "getAddressForCoordinates: OK: ${list!![0].address.addressText}|${list!![0].type}")
        })
    }
}