package com.example.proyecto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.proyecto.config.collections
import com.example.proyecto.filestorage.FileStorage
import com.example.proyecto.fragments.Chats
import com.example.proyecto.fragments.Equipo
import com.example.proyecto.fragments.Tareas
import com.example.proyecto.modelos.UserInfoModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_inicio.*
import kotlinx.android.synthetic.main.header_drawer.*

class InicioActivity : ImagePickerActivity() {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!
    private val fileStorage = FileStorage(this)

    private val chatsFragment = Chats()
    private val equipoFragment = Equipo()
    private val tareasFragment = Tareas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        setup()
    }

    private fun setup(){
        replaceFragment(chatsFragment)

        bottomNav.setOnItemSelectedListener {item->
            when(item.itemId){
                R.id.ic_chat -> replaceFragment(chatsFragment)
                R.id.ic_equipo -> replaceFragment(equipoFragment)
                R.id.ic_tareas ->replaceFragment(tareasFragment)
            }
            true
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title =""

        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            0,
            0
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item->
            when(item.itemId){
                R.id.nuevoMensaje->{
                    val intent = Intent(this, BuscarUsuarioActivity::class.java)
                    startActivity(intent)
                }
                R.id.logOutButton->{
                    auth.signOut()
                    finish()
                }
            }
            true
        }

        val inforef = db.getReference(collections.userinfo)

        if(user.displayName==null){
            finish()
            val intent = Intent(this, NombreUsuarioActivity::class.java)
            startActivity(intent)
            return
        }

        inforef.child(user.displayName!!).get().addOnSuccessListener {
            val info = it.getValue(UserInfoModel::class.java)
            if(info==null){
                finish()
                val intent = Intent(this, NombreUsuarioActivity::class.java)
                startActivity(intent)
                return@addOnSuccessListener
            }
            menuname.text = info.nombre
            menusername.text = info.userName
            tvPuntosDrawer.text = info.puntos.toString()
            setupProfileImage()

            //subscribir cambios en puntos
            val ref = db.getReference(collections.userinfo)
                .child(user.displayName!!)
                .child("puntos")

            ref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val puntos = snapshot.getValue(Long::class.java)!!
                    tvPuntosDrawer.text = puntos.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@InicioActivity,
                        "Error actualizando los puntos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun setupProfileImage(){
        if(user.photoUrl!=null){
            Picasso.get()
                .load(user.photoUrl)
                .placeholder(R.drawable.saturn)
                .into(profile_image)
        }

        profile_image.setOnClickListener {
            startImageChooser()
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
            .into(profile_image)
        actualizarPfp()
    }

    private fun actualizarPfp(){
        val builder = UserProfileChangeRequest.Builder()
        builder.photoUri=imageUri
        val request = builder.build()
        user.updateProfile(request).addOnSuccessListener {
            fileStorage.newFileinStorage(imageUri,{url->
                val ref = db.getReference(collections.userinfo)
                    .child(user.displayName!!)
                ref.updateChildren(mapOf("photoUrl" to url)).addOnSuccessListener {
                    actualizarPfpInOther(url)
                }
            },{
                Toast.makeText(
                    this,
                    "No se pudo subir la imagen",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun actualizarPfpInOther(url:String){
        val ref= db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.chats)

        ref.get().addOnSuccessListener{
            it.children.forEach {
                val chatref = db.getReference(collections.users)
                    .child(it.key!!)
                    .child(collections.chats)
                    .child(user.displayName!!)

                chatref.updateChildren(mapOf("otherFoto" to url))
            }
        }
    }

    private fun replaceFragment (fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_conteiner, fragment)
        transaction.commit()
    }
}
