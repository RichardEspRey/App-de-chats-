package com.example.proyecto

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.config.collections
import com.example.proyecto.config.extranames
import com.example.proyecto.modelos.AssignmentModel
import com.example.proyecto.modelos.GroupChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_create_assignment.*
import kotlinx.android.synthetic.main.activity_create_group.btnCreateAssignment
import kotlinx.android.synthetic.main.activity_create_group.etDescTarea
import java.text.SimpleDateFormat
import java.util.*

class CreateTareaActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_assignment)

        val ext = intent.extras!!
        setup(ext)
    }

    private fun setup(extras: Bundle){
        val groupId = extras.getString(extranames.ToCreateAssingment.GroupId)!!
        val groupNombre = extras.getString(extranames.ToCreateAssingment.GroupNombre)!!

        tvGrupoNombre.text = groupNombre

        btnCreateAssignment.setOnClickListener {
            createAssignment(groupId)
        }

        btn_date.setOnClickListener {
            handleDateButton()
        }

        btn_time.setOnClickListener {
            handleTimeButton()
        }
    }

    private fun handleTimeButton(){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this,
            { view, hourOfDay, minute ->
                val str= "${hourOfDay}:${minute}"
                editTextTime.setText(str)
            }, hour, minute, true)
        timePickerDialog.show()
    }
    private fun handleDateButton(){
        val calendar = Calendar.getInstance()
        val year =calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this,
            { view, year, month, dayOfMonth ->
                val str = "$dayOfMonth-${month+1}-$year"
                editTextDate.setText(str)
            }, year, month, day)
        datePickerDialog.show()
    }

    private fun createAssignment(id:String){
        val descripcion = etDescTarea.text.toString()
        if(descripcion.isBlank()){
            Toast.makeText(
                this,
                "La descripcion es obligatoria",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dateString = editTextDate.text.toString()
        val timeString = editTextTime.text.toString()
        if(dateString.isBlank() || timeString.isBlank()){
            Toast.makeText(
                this,
                "Ingrese Fecha y Hora",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val puntos = etPuntos.text.toString()
        if(puntos.isBlank()){
            Toast.makeText(
                this,
                "Ingrese los puntos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val str = "$dateString $timeString"
        val parsed =  simpleDateFormat.parse(str)

        val groupref = db.getReference(collections.groupchats).child(id)
        groupref.get().addOnSuccessListener{
            val nombreGrupo = it.child("nombre").getValue(String::class.java)!!
            val fotoGrupo = it.child("foto").getValue(String::class.java)!!
            val assignmentRef = db.getReference(collections.assignments).push()
            val model = AssignmentModel(
                assignmentRef.key!!,
                id,
                nombreGrupo,
                fotoGrupo,
                descripcion,
                puntos.toInt(),
                parsed.time,
                ServerValue.TIMESTAMP,
            )

            assignmentRef.setValue(model).addOnSuccessListener {
                addAssignmentToGroup(model)
                finish()
            }
        }


    }

    private fun addAssignmentToGroup(model:AssignmentModel){
        val groupRef = db.getReference(collections.groupchats)
            .child(model.groupId)
            .child(collections.users)
        groupRef.get().addOnSuccessListener {
            it.children.forEach {
                val username = it.getValue(String::class.java)!!
                addAssignmentToSingleUser(username, model)
            }
        }
    }

    private fun addAssignmentToSingleUser(username:String, model: AssignmentModel){
        val userAssignmentsRef = db.getReference(collections.users)
            .child(username)
            .child(collections.assignments)

        userAssignmentsRef.child(model.id).setValue(model)
    }
}