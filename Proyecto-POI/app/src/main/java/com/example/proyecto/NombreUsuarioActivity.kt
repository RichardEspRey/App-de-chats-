package com.example.proyecto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.example.proyecto.config.collections
import com.example.proyecto.databinding.ActivityNombreUsuarioBinding
import com.example.proyecto.filestorage.FileStorage
import com.example.proyecto.modelos.UserInfoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_nombre_usuario.*

class NombreUsuarioActivity : ImagePickerActivity() {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!
    private val fileStorage = FileStorage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nombre_usuario)

        profile_imageinfo.setOnClickListener {
            startFileChooser()
        }

        infoAcept.setOnClickListener {
            enviarInfo()
        }
    }

    private fun enviarInfo(){
        val nombre = nombreUsuarioInfo.text.toString()
        val username = userNameInfo.text.toString()

        if(nombre.isEmpty() || username.isEmpty()){
            Toast.makeText(
                this,
                "Faltan llenar campos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // comprobar que no existe el username
        if(imageUri!= Uri.EMPTY){
            fileStorage.newFileinStorage(imageUri,
            {
                val uri = Uri.parse(it)
                enviarInfoDbConFoto(username,nombre, uri)
            },
            {
                Toast.makeText(
                    this,
                    "No se pudo subir la foto de usuario",
                    Toast.LENGTH_SHORT
                ).show()
            })
            return
        }

        enviarInfoDb(username, nombre)
    }

    private fun enviarInfoDb(username:String, nombre: String){
        val ref = db.getReference(collections.userinfo)
        val usersref = db.getReference(collections.users)
        ref.child(username).get().addOnSuccessListener {
            if(it.exists()){
                Toast.makeText(
                    this,
                    "El usuario ya existe, prueba con otro nombre",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            // agregar info al usuario en auth
            val builder = UserProfileChangeRequest.Builder()
            builder.displayName = username
            val request = builder.build()
            user.updateProfile(request).addOnSuccessListener {
                // agregar info a db
                val info = UserInfoModel(username, nombre, user.displayName!!   , false, user.uid)
                ref.child(username).setValue(info)
                usersref.child(username).setValue(null)

                val intent = Intent(this, InicioActivity::class.java)
                finish()
                startActivity(intent)
            }
        }
    }

    private fun enviarInfoDbConFoto(username:String, nombre: String, foto:Uri){
        val ref = db.getReference(collections.userinfo)
        val usersref = db.getReference(collections.users)
        ref.child(username).get().addOnSuccessListener {
            if(it.exists()){
                Toast.makeText(
                    this,
                    "El usuario ya existe, prueba con otro nombre",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            // agregar info al usuario en auth
            val builder = UserProfileChangeRequest.Builder()
            builder.displayName = username
            builder.photoUri=foto
            val request = builder.build()
            user.updateProfile(request).addOnSuccessListener {
                // agregar info a db
                val info = UserInfoModel(username, nombre, user.displayName!!, false, user.uid)
                info.photoUrl=foto.toString()
                ref.child(username).setValue(info)
                usersref.child(username).setValue(null)

                val intent = Intent(this, InicioActivity::class.java)
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data== null || resultCode!= Activity.RESULT_OK){
            return;
        }
        imageUri = data.data!!
        Picasso.get()
            .load(imageUri)
            .placeholder(R.drawable.saturn)
            .into(profile_imageinfo)
    }
}