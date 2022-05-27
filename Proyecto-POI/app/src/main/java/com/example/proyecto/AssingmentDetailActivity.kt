package com.example.proyecto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.proyecto.config.collections
import com.example.proyecto.config.extranames
import com.example.proyecto.modelos.AssignmentModel
import com.example.proyecto.modelos.UserInfoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_assignment_detail.*
import kotlinx.android.synthetic.main.recycler_listchat.view.*

class AssingmentDetailActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_detail)

        val ext = intent.extras!!
        setup(ext)
    }

    private fun setup(ext:Bundle){
        val id = ext.getString(extranames.ToAssignmentDetail.AsssigmentId)!!
        val foto = ext.getString(extranames.ToAssignmentDetail.GroupFoto)!!
        val nombre = ext.getString(extranames.ToAssignmentDetail.GroupNombre)!!
        val descripcion = ext.getString(extranames.ToAssignmentDetail.Descripcion)!!
        val puntos = ext.getInt(extranames.ToAssignmentDetail.Puntos)

        tvEquipoNombre.text = nombre
        tvPuntos.text = puntos.toString()
        tvDescripcionTarea.setText(descripcion)

        if(foto.isNotBlank())
            Picasso.get()
                .load(foto)
                .placeholder(R.drawable.saturn)
                .into(ciFotoDetalleTarea)

        btnEntregar.setOnClickListener {
            entregarTarea(id)
        }

        btnEntregar.visibility = View.GONE
        val ref = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.assignments)
            .child(id)

        ref.get().addOnSuccessListener {
            val tarea = it.getValue(AssignmentModel::class.java)!!
            if(!tarea.entregada){
                btnEntregar.visibility=View.VISIBLE
            }
        }
    }

    private fun entregarTarea(id:String){
        val ref = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.assignments)
            .child(id)

        ref.get().addOnSuccessListener {
            val tarea = it.getValue(AssignmentModel::class.java)!!
            ref.updateChildren(mapOf("entregada" to true)).addOnSuccessListener {
                Toast.makeText(
                    this,
                    "La tarea se ha entregado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                actualizarPuntosUsuario(tarea.puntos.toLong())
            }
        }
    }

    private fun actualizarPuntosUsuario(puntos:Long){
        val userinforef= db.getReference(collections.userinfo).child(user.displayName!!)
        userinforef.get().addOnSuccessListener {
            val userinfo = it.getValue(UserInfoModel::class.java)!!
            userinforef.updateChildren(mapOf("puntos" to userinfo.puntos+puntos)).addOnSuccessListener {
                finish()
            }
        }
    }

}