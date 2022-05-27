package com.example.proyecto

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Btn_Registrar.setOnClickListener{
            register()
        }
    }

    private fun register(){
        val email = CorreoRegistro.text.toString()
        val nombre = NombreRegistro.text.toString()
        val pass = PassRegistro.text.toString()
        val passConf = PassConfirRegistro.text.toString()

        if( email.isEmpty()  ||
            nombre.isEmpty() ||
            pass.isEmpty()   ||
            passConf.isEmpty()
        ){
            showAlert()
            return;
        }

        if(pass.compareTo(passConf) != 0){
            showPassAlert()
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                showAlert();
                return@addOnCompleteListener
            }

            Toast.makeText(this, "Registro Exitoso", Toast.LENGTH_SHORT).show()
            showLogin()
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun showPassAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Las password no son iguales, introduzca pass iguales")
        builder.setPositiveButton("Aceptar", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogin(){
        finish()
    }
}