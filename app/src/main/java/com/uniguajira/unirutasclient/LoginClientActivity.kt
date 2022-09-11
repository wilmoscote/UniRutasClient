package com.uniguajira.unirutasclient

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginClientActivity : AppCompatActivity() {
    private lateinit var txtUser: EditText
    private lateinit var txtPass: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_client)
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        val user = FirebaseAuth.getInstance().currentUser

        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
        txtUser = findViewById(R.id.txtUser)
        txtPass = findViewById(R.id.txtPass)

        progressBar= findViewById(R.id.progressBar2)
        auth=FirebaseAuth.getInstance()

    }

    fun register(view: View){
        startActivity(Intent(this,RegisterClientActivity::class.java))
    }

    fun forgotPassword(view: View){

    }

    fun info (view: View){
        startActivity(Intent(this,InfoClientActivity::class.java))
    }

    fun login(view: View){
        loginUser()
    }

    private fun loginUser(){
        val user:String=txtUser.text.toString()
        val pass:String=txtPass.text.toString()

        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)){
            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(user,pass)
                .addOnCompleteListener(this){ task->
                    if(task.isSuccessful){
                        progressBar.visibility = View.GONE
                        val usuario = auth.currentUser
                        action(usuario)
                    }else{
                        progressBar.visibility = View.GONE
                        Toast.makeText(this,"Correo o Contraseña incorrectos",Toast.LENGTH_SHORT).show()
                    }
                }
        }else{
            Toast.makeText(this,"Ingrese un Correo y una Contraseña",Toast.LENGTH_SHORT).show()
        }
    }

    private fun action(usuario: FirebaseUser?){
        startActivity(Intent(this, MapsClientActivity::class.java))
    }
}
