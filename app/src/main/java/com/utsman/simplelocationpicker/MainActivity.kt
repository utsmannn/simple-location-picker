package com.utsman.simplelocationpicker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val jakartaLatLng = LatLng(-6.21462, 106.84513)
    private val retrofitInstance = RetrofitInstance.create()

    private var hasFetch = false
    private var animateMarker = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomSheet = BottomSheetBehavior.from(bottom_sheet)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        progress_circular.visibility = View.GONE

        (map_view as SupportMapFragment).getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(jakartaLatLng, 18f))

            val oldPosition = map.cameraPosition.target

            map.setOnCameraMoveStartedListener {
                // drag started
                if (animateMarker) {
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

                    icon_marker.animate().translationY(-50f).start()
                    icon_marker_shadow.animate().withStartAction {
                        icon_marker_shadow.setPadding(10)
                    }.start()
                }

                hasFetch = false
            }

            map.setOnCameraIdleListener {
                val newPosition = map.cameraPosition.target
                if (newPosition != oldPosition) {
                    // drag ended
                    icon_marker.animate().translationY(0f).start()
                    icon_marker_shadow.animate().withStartAction {
                        icon_marker_shadow.setPadding(0)
                    }.start()

                    getLocation(newPosition) { item ->
                        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                        val position = item.position
                        val findLocation = LatLng(position.lat, position.lng)

                        map.animateCamera(CameraUpdateFactory.newLatLng(findLocation), 200,
                            object : GoogleMap.CancelableCallback {
                                override fun onFinish() {
                                    hasFetch = true
                                    animateMarker = true
                                }

                                override fun onCancel() {
                                    animateMarker = true
                                }

                            })

                        val titlePlace = item.title
                        val address = item.address.label

                        text_title.text = titlePlace
                        text_address.text = address
                    }
                }
            }
        }
    }

    private fun getLocation(latLng: LatLng, done: (Item) -> Unit) {
        val at = "${latLng.latitude},${latLng.longitude}"
        if (!hasFetch) {
            animateMarker = false
            progress_circular.visibility = View.VISIBLE
            GlobalScope.launch {
                try {
                    val places = retrofitInstance.getLocation(at).items
                    runOnUiThread {
                        if (places.isNotEmpty()) {
                            progress_circular.visibility = View.GONE
                            done.invoke(places.first())
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}