package com.example.proyecto.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.example.proyecto.adapters.UserChatAdapter
import com.example.proyecto.config.collections
import com.example.proyecto.modelos.UserChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chats.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Chats.newInstance] factory method to
 * create an instance of this fragment.14
 */
class Chats : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val user = auth.currentUser!!

    private lateinit var adapter : UserChatAdapter

    private val chatList = mutableListOf<UserChatModel>()
    private val filteredList = mutableListOf<UserChatModel>()

    private var currentFilter = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        subscribeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        adapter = UserChatAdapter(filteredList)
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
        val ref = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.chats)

        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "No se pudo recuperar a los chats",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for( child in snapshot.children){
                    chatList.add(child.getValue(UserChatModel::class.java)!!)
                }
                chatList.sortWith(compareByDescending{it.lastMessageTimestamp})
                filterData()
            }
        })
    }

    private fun filterData(){
        filteredList.clear()
        val filter = chatList.filter {
            it.otherNombre.contains(currentFilter) || it.otherUsername.contains(currentFilter)
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
         * @return A new instance of fragment Chats.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Chats().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}