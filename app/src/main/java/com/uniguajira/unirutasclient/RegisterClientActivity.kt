package com.uniguajira.unirutasclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern
import kotlin.concurrent.timerTask

class RegisterClientActivity : AppCompatActivity() {
    private lateinit var txtName:EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPass: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPass = findViewById(R.id.txtPassword2)

        progressBar= findViewById(R.id.progressBar)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("Rutas")

    }



    fun register(view:View){
        createNewAccount()
    }

    private fun createNewAccount() {
        val name: String = txtName.text.toString()
        val email: String = txtEmail.text.toString()
        val password: String = txtPassword.text.toString()
        val confirmPass: String = txtConfirmPass.text.toString()

        val partes: List<String> = email.split("@")

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(
                password
            ) && validEmail(email)
        ){

            if(password == confirmPass){
                if (partes[1] != "") {
                    progressBar.visibility = View.VISIBLE

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            if (task.isSuccessful) {
                                val user: FirebaseUser? = auth.currentUser

                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build()
                                user?.updateProfile(profileUpdates)
                                verifyEmail(user)
                                val userBD = dbReference.child(user?.uid!!)
                                userBD.child("Latitud").setValue(0f)
                                userBD.child("Longitud").setValue(0f)
                                userBD.child("Nombre").setValue(name)
                                userBD.child("Estado").setValue(0)

                                progressBar.visibility = View.GONE
                                action()
                            }else{

                                Toast.makeText(this, "Error 404.",
                                    Toast.LENGTH_SHORT).show()

                            }
                        }
                }else{
                    Toast.makeText(this, "Coloque un correo valido", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Las Contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }else {
            if (!validEmail(email)) {
                Toast.makeText(this, "Ingrese Un Correo Valido.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Llene todos los campos, por favor.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun action(){
        startActivity(Intent(this,LoginClientActivity::class.java))
    }
    private fun verifyEmail(user:FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this){
                    task->
                if(task.isSuccessful){
                    Toast.makeText(this,"Ruta Registrada Correctamente",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"Error al Contactar Email",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validEmail(email:String) : Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}


