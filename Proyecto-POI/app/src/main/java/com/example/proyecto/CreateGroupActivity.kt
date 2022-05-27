package com.example.proyecto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.adapters.MultiSelectionAdapter
import com.example.proyecto.config.collections
import com.example.proyecto.modelos.GroupChatModel
import com.example.proyecto.modelos.UserGroupChatModel
import com.example.proyecto.modelos.UserInfoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_group.*

class CreateGroupActivity : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!

    private lateinit var adapter : MultiSelectionAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        setup()
    }

    private fun setup(){
        val ref = db.getReference(collections.userinfo);
        ref.get().addOnSuccessListener {

            val finalArr = it.children
                .map{
                    it.getValue(UserInfoModel::class.java)!!.userName
                }
                .filter{
                    !it.equals(user.displayName!!)
                }

            adapter = MultiSelectionAdapter<String>(this, finalArr)
            lvTareaCrear.adapter = adapter
        }

        btnCreateAssignment.setOnClickListener {
            createGroupChat()
        }
    }

    private fun createGroupChat(){
        val checkedUsers = adapter.getCheckedItems()
        if(checkedUsers.size < 2){
            Toast.makeText(
                this,
                "No se puede crear un grupo con menos de 2 personas",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nombreDelGrupo = etDescTarea.text.toString()
        if(nombreDelGrupo.isBlank()){
            Toast.makeText(
                this,
                "No se puede crear un grupo sin nombre",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // TODO: desarrollar logica de filestorage y crear modelo con la url
        val fotoUrl = subirFoto()

        // create groupchat
        val groupchatref = db.getReference(collections.groupchats).push()
        val grouchatid = groupchatref.key!!

        val usersInGroupChat = mutableListOf<String>()
        usersInGroupChat.addAll(checkedUsers)
        usersInGroupChat.add(user.displayName!!)

        val groupChatModel = GroupChatModel(
            grouchatid,
            nombreDelGrupo,
            fotoUrl,
            usersInGroupChat
        )

        val userGroupChatModel = UserGroupChatModel(
            grouchatid,
            nombreDelGrupo,
            fotoUrl
        )
        groupchatref.setValue(groupChatModel)

        addGroupChatToUsers(userGroupChatModel, usersInGroupChat)
        finish()
    }

    private fun subirFoto():String{
        return ""
    }

    private fun addGroupChatToUsers(model: UserGroupChatModel, usersInGroupChat: List<String>){
        addGroupChatToSingleUser(user.displayName!!, model)
        usersInGroupChat.forEach{
            addGroupChatToSingleUser(it, model)
        }
    }

    private fun addGroupChatToSingleUser(username:String, model: UserGroupChatModel){
        val ref= db.getReference(collections.users)
        val groupChatRef = ref.child(username).child(collections.groupchats)
        groupChatRef.child(model.id).setValue(model)
    }
}