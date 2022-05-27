package com.example.proyecto.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.proyecto.CreateGroupActivity
import com.example.proyecto.R
import com.example.proyecto.adapters.EquipoChatAdapter
import com.example.proyecto.config.collections
import com.example.proyecto.modelos.UserGroupChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_equipo.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Equipo.newInstance] factory method to
 * create an instance of this fragment.
 */
class Equipo : Fragment(), AdapterView.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!

    private val groupChats = mutableListOf<UserGroupChatModel>()
    private val filteredList = mutableListOf<UserGroupChatModel>()
    private var currentFilter = ""

    private lateinit var adapter : EquipoChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        subscribeData()
    }

    private val lv:ListView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_equipo, container, false)
        setup(view)
        return view
    }

    private fun setup(view: View){
        adapter = EquipoChatAdapter(filteredList)
        view.rvAssignments.adapter = adapter
        view.ibCrearGrupo.setOnClickListener {
            val intent = Intent(context, CreateGroupActivity::class.java)
            startActivity(intent)
        }

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
    }

    private fun subscribeData(){
        val ref = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.groupchats)

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.children.map{it.getValue(UserGroupChatModel::class.java)!!}
                groupChats.clear()
                groupChats.addAll(temp)
                groupChats.sortWith(compareByDescending{it.lastMessageTimestamp})
                filterData()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun filterData(){
        filteredList.clear()
        val filter = groupChats.filter {
            it.nombre.contains(currentFilter)
        }
        filteredList.addAll(filter)
        adapter.notifyDataSetChanged()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Equipo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Equipo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var items:String = parent?.getItemAtPosition(position) as String
    }
}