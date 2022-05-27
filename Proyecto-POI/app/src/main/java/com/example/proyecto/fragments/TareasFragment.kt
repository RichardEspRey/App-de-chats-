package com.example.proyecto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.example.proyecto.adapters.AssignmentsAdapter
import com.example.proyecto.config.collections
import com.example.proyecto.modelos.AssignmentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_tareas.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tareas : Fragment() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!

    private val assignments = mutableListOf<AssignmentModel>()
    private val filteredList = mutableListOf<AssignmentModel>()
    private var currentFilter = ""

    private lateinit var adapter: AssignmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tareas, container, false)

        adapter = AssignmentsAdapter(filteredList)
        view.rvAssignments.adapter = adapter

        view.aceptarBusqueda.setOnClickListener {
            val filtertemp = view.Busqueda.text.toString()
            currentFilter = when(filtertemp.isBlank()){
                true-> {
                    view.Busqueda.text.clear()
                    ""
                }
                false->filtertemp
            }
            filterData()
        }

        return view
    }

    private fun subscribeData(){
        val assignmentsRef = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.assignments)

        assignmentsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val assignmentsTemp =
                    snapshot.children.map{ it.getValue(AssignmentModel::class.java)!! }
                assignments.clear()
                assignments.addAll(assignmentsTemp)
                assignments.sortWith(compareByDescending { it.fechaCreada as Long })
                filterData()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun filterData(){
        filteredList.clear()
        val filter = assignments.filter {
            it.groupNombre.contains(currentFilter) || it.descripcion.contains(currentFilter)
        }
        filteredList.addAll(filter)
        adapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tareas().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}