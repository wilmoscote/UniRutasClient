package com.uniguajira.unirutasclient

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var map: GoogleMap
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("Rutas")
        startActivity(Intent(this,MapsClientActivity::class.java))

    }

    fun transm(view: View){
        startActivity(Intent(this,MapsClientActivity::class.java))
    }

    private fun setUpClient(){
        checkPermission()
                //map.mapType = GoogleMap.MAP_TYPE_HYBRID   //Cambiar Estilo de Mapa
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
                location->
            if(location != null){
                lastLocation = location
                val latlong :HashMap<String, Any> = HashMap()
                latlong.put("Latitud",location.latitude)
                latlong.put("Longitud",location.longitude)

                dbReference.push().setValue(latlong)
                //val currentLatLong = LatLng(location.latitude, location.longitude)
                //placeMarker(currentLatLong)
                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,12f))
            }
        }
    }

    private fun checkPermission(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }
}
