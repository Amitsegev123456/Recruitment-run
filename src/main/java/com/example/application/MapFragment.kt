package com.example.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
    }

    // הפונקציה שה-Activity יקרא לה כשלוחצים על שורה בטבלה
    fun zoomToLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)
        googleMap?.clear() // מחיקת סימונים קודמים
        googleMap?.addMarker(MarkerOptions().position(location).title("Score Location"))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}