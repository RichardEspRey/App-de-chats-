package com.example.proyecto

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IniciarLogin.setOnClickListener {
            login()
        }

        Register_Login.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun login(){
        val email = CorreoLogin.text.toString()
        val pass = PassLogin.text.toString()

        if(email.isNullOrEmpty() || pass.isNullOrEmpty()){
            showAlert()
            return
        }

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{
            if(!it.isSuccessful){
                showAlert()
                return@addOnCompleteListener
            }

            val user = it.result.user
            if(user==null){
                showAlert()
                return@addOnCompleteListener
            }
            showStart()
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error al iniciar sesion")
        builder.setMessage("Se ha producido un error al tratar de iniciar sesion")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

   private fun showStart(){
       val intent = Intent(this, InicioActivity::class.java)
       startActivity(intent)
   }
}


