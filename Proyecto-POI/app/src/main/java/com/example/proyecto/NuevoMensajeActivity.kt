package com.example.proyecto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.proyecto.config.collections
import com.example.proyecto.config.extranames
import com.example.proyecto.databinding.ActivityNombreUsuarioBinding
import com.example.proyecto.modelos.ChatModel
import com.example.proyecto.modelos.UserChatModel
import com.example.proyecto.modelos.UserInfoModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_nuevo_mensaje.*

class NuevoMensajeActivity : AppCompatActivity() {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!

    private lateinit var binding: ActivityNombreUsuarioBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_mensaje)
        val ext = intent.extras!!

        val username = ext.getString(extranames.ToNuevoMensaje.Username)!!
        setup(username)
    }

    private fun setup(username: String){
        val ref = db.getReference(collections.userinfo).child(username);
        ref.get().addOnSuccessListener {
            val info = it.getValue(UserInfoModel::class.java)
            if(info==null){
                Toast.makeText(
                    this,
                    "Hubo un error recuperando el usuario",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }
            nmUsername.text = info.userName
            nmNombre.text = info.nombre
        }

        nmCrear.setOnClickListener{
            startChat(username)
        }
    }

    private fun startChat(username: String) {
        val inforef = db.getReference(collections.userinfo)
        val userref = db.getReference(collections.users).child(username)
        inforef.child(username).get().addOnSuccessListener {
            if(!it.exists()){
                Toast.makeText(
                    this,
                    "No se pudo encontrar al usuario especificado",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            val info = it.getValue(UserInfoModel::class.java)
            if(info==null){
                Toast.makeText(
                    this,
                    "Hubo un error obteniendo la informacion del usuario",
                    Toast.LENGTH_SHORT
                ).show()
                return@addOnSuccessListener
            }

            //now check if the chat exists
            userref.child(collections.chats).child(user.displayName!!).get()
                .addOnSuccessListener {

                if(!it.exists()){
                    crearChat(username){
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra(extranames.ToChatActivity.ChatId, it)
                            putExtra(extranames.ToChatActivity.Username, username)
                            putExtra(extranames.ToChatActivity.IsGroupChat, false)
                        }
                        finish()
                        startActivity(intent)
                    }
                }else{
                    val chatid = it.getValue(UserChatModel::class.java)!!.chatid
                    val intent = Intent(this, ChatActivity::class.java).apply {
                        putExtra(extranames.ToChatActivity.ChatId, chatid)
                        putExtra(extranames.ToChatActivity.Username, username)
                        putExtra(extranames.ToChatActivity.IsGroupChat, false)
                    }
                    finish()
                    startActivity(intent)
                }
            }

        }
    }

    private fun crearChat(other: String, onComplete: (chatid:String)->Unit) {
        val users = db.getReference(collections.users)
        val chats = db.getReference(collections.chats)
        val userinforef = db.getReference(collections.userinfo)

        // crear chat en su coleccion propia y obtener id
        val chatref = chats.push()
        val chatid = chatref.key!!

        val chat = ChatModel(listOf(other, user.displayName!!))
        chatref.setValue(chat)

        // obetener datos de ambos
        userinforef.child(other).get().addOnSuccessListener {
            val otherUserInfo = it.getValue(UserInfoModel::class.java)!!
            userinforef.child(user.displayName!!).get().addOnSuccessListener {
                val myUserInfo = it.getValue(UserInfoModel::class.java)!!

                // crear chat en las colecciones de usuarios
                val userChatMine =
                    UserChatModel(chatid, myUserInfo.userName, myUserInfo.nombre, myUserInfo.photoUrl)
                val userChatOther =
                    UserChatModel(chatid, otherUserInfo.userName, otherUserInfo.nombre, otherUserInfo.photoUrl)

                users.child(other).child(collections.chats).child(user.displayName!!)
                    .setValue(userChatMine)

                users.child(user.displayName!!).child(collections.chats).child(other)
                    .setValue(userChatOther)

                onComplete(chatid)
            }
        }
    }
}