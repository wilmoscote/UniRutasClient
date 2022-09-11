package com.uniguajira.unirutasclient

import android.app.AlertDialog
import android.content.Intent
import kotlin.concurrent.schedule
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MapsClientActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var lastLocation: Location
    private var estado:Boolean = false
    private lateinit var btn:Button
    private lateinit var btnOut:Button
    companion object{
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_client)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        btn = findViewById(R.id.botonon)
        btnOut = findViewById(R.id.btnSalir)
        val actualizar = Handler(Looper.getMainLooper())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("Rutas")
/*
        Timer("Localizacion",false).schedule(2000){
            enviarPosicion()
        }*/
            actualizar.post(object : Runnable {
                override fun run() {
                    if(estado){
                        enviarPosicion()
                    }
                    actualizar.postDelayed(this, 250)
                }
            })

        /*
        Handler().postDelayed({

            //Toast.makeText(this,"Llamado!",Toast.LENGTH_SHORT).show()
        },2000)*/
        btn.setOnClickListener {
            val user: FirebaseUser? = auth.currentUser
            if(!estado){
                estado = true
                dbReference.child(user?.uid!!).child("Estado").setValue(1)
                btn.setBackgroundResource(R.color.btnOff)
                btn.setText("Apagar")
                Toast.makeText(this,"Encendido",Toast.LENGTH_SHORT).show()
                btnOut.setBackgroundResource(R.color.btnDisabled)
                btnOut.isEnabled = false

            }else{
                estado = false
                dbReference.child(user?.uid!!).child("Estado").setValue(0)
                btn.setBackgroundResource(R.color.btnOn)
                btn.setText("Encender")
                Toast.makeText(this,"Apagado",Toast.LENGTH_SHORT).show()
                btnOut.setBackgroundResource(R.color.out)
                btnOut.isEnabled = true

            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera

        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true


        setUpMap()
    }

    fun on(view:View){


    }

    fun off(view:View){

    }

    private fun placeMarker(location: LatLng){
        val user: FirebaseUser? = auth.currentUser
        val markerOptions = MarkerOptions().position(location).title(user?.displayName!!)

        map.addMarker(markerOptions)
    }

    fun salir(view: View){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Seguro que Desea Salir?")

        builder.setMessage("Cerrar Sesión")

        builder.setPositiveButton("Si"){dialog, which->
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginClientActivity::class.java))
            Toast.makeText(this,"Sesión Cerrada.", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No"){_,_->}
        val dialog: AlertDialog = builder.create()

        dialog.show()
    }


    private fun setUpMap(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        //map.mapType = GoogleMap.MAP_TYPE_HYBRID   //Cambiar Estilo de Mapa
        if(estado){
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    val user: FirebaseUser? = auth.currentUser
                    lastLocation = location
                    val currentLatLong = LatLng(location.latitude, location.longitude)
                    placeMarker(currentLatLong)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 13f))
                    dbReference.child(user?.uid!!).child("Latitud").setValue(location.latitude)
                    dbReference.child(user?.uid!!).child("Longitud").setValue(location.longitude)

                }
            }
        }
    }

    private fun enviarPosicion(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.clear()
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
                location->
            if(location != null){
                val user: FirebaseUser? = auth.currentUser
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarker(currentLatLong)
                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,13f))
                dbReference.child(user?.uid!!).child("Latitud").setValue(location.latitude)
                dbReference.child(user?.uid!!).child("Longitud").setValue(location.longitude)


            }
        }
    }


}
