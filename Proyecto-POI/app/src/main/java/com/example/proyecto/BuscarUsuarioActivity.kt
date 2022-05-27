package com.example.proyecto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.proyecto.config.collections
import com.example.proyecto.modelos.UserInfoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_buscar_usuario.*

class BuscarUsuarioActivity : AppCompatActivity() {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_usuario)
        buttonBuscar.setOnClickListener {
            busqueda()
        }
    }

    private fun busqueda(){
        val username = UserBusqueda.text.toString()
        if( username.isNullOrEmpty()){
            Toast.makeText(
                this,
                "Porfavor ingrese el usuario a buscar",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uid = auth.currentUser!!.uid
        val ref = db.getReference(collections.userinfo)
        ref.child(username).get().addOnSuccessListener {
            if( !it.exists() ){
                Toast.makeText(
                    this,
                    "No se encontro un usuario con ese username",
                    Toast.LENGTH_SHORT)
                    .show()
                return@addOnSuccessListener
            }

            val info = it.getValue(UserInfoModel::class.java)
            if(info==null){
                Toast.makeText(
                    this,
                    "Hubo un error recuperando la informacion del usuario",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            if(info.uid==uid){
                Toast.makeText(
                    this,
                    "El usuario buscado es usted",
                    Toast.LENGTH_SHORT)
                    .show()
                return@addOnSuccessListener
            }

            val intent = Intent(this, NuevoMensajeActivity::class.java)
            intent.putExtra("username",info.userName)
            startActivity(intent)
        }
    }
}