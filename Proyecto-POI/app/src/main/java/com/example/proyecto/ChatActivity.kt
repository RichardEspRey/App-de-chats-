package com.example.proyecto

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.example.proyecto.adapters.ChatAdapter
import com.example.proyecto.config.collections
import com.example.proyecto.config.extranames
import com.example.proyecto.config.secret
import com.example.proyecto.encryption.MessageEncrypter
import com.example.proyecto.filestorage.FileStorage
import com.example.proyecto.modelos.MensajeModel
import com.example.proyecto.modelos.UserChatModel
import com.example.proyecto.modelos.UserGroupChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : ImagePickerActivity() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser!!
    private val fileStorage = FileStorage(this)

    private val encrypter = MessageEncrypter(secret.key)
    private var mensajes = mutableListOf<MensajeModel>()

    private lateinit var groupchatid:String

    private lateinit var adapter: ChatAdapter
    private lateinit var other:String
    private lateinit var progressDialog:ProgressDialog

    private var imagenEsMensaje=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val ext = intent.extras!!
        val isGroupChat = ext.getBoolean(extranames.ToChatActivity.IsGroupChat)

        setup(isGroupChat, ext)
    }

    private fun setup(isGroupChat:Boolean, extras: Bundle){
        adapter= ChatAdapter(mensajes)
        rvMsj.adapter = adapter

        volverchat.setOnClickListener {
            finish()
        }

        btn_newTarea.visibility = when(isGroupChat){
            true -> View.VISIBLE
            false ->View.GONE
        }

        btnSubirImagen.setOnClickListener {
            imagenEsMensaje=true
            startImageChooser()
        }

        btnSubirArchivo.setOnClickListener {
            startFileChooser()
        }

        btnCancelarArchivo.setOnClickListener {
            tvArchivoSubido.visibility=View.GONE
            btnCancelarArchivo.visibility=View.GONE

            fileUri = Uri.EMPTY
            imageUri= Uri.EMPTY
        }

        val chatid = extras.getString(extranames.ToChatActivity.ChatId)!!
        if(isGroupChat){
            val other = extras.getString(extranames.ToChatActivity.GroupChatName)!!
            setupGroup(other, chatid)
        }
        else{
            val nombre = extras.getString(extranames.ToChatActivity.Username)!!
            setupPrivado(nombre, chatid)
        }

    }

    private fun setupPrivado(username: String, chatid: String ) {
        chatUsername.text = username
        other = username

        val userschatref = db.getReference(collections.users)
            .child(username)
            .child(collections.chats)
            .child(user.displayName!!)

        userschatref.get().addOnSuccessListener {
            subscribirMensajesPrivado(it)
        }

        Btn_Enviar_Mensaje.setOnClickListener {
            enviarMensajePrivado(chatid)
        }

        rvMensajes.setOnKeyListener { v, keyCode, event ->
            if(keyCode== KeyEvent.KEYCODE_ENTER){
                enviarMensajePrivado(chatid)
            }
            keyCode == KeyEvent.KEYCODE_ENTER
        }

        val ref = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.chats)
            .child(username)

        ref.get().addOnSuccessListener {
            val foto = it.child("otherFoto").getValue(String::class.java)!!
            if(foto.isNotBlank())
                Picasso.get()
                    .load(foto)
                    .placeholder(R.drawable.saturn)
                    .into(chatPicture)
        }

    }

    private fun setupGroup(nombre:String, chatid:String){
        groupchatid = chatid
        chatUsername.text = nombre

        val usersGroupChatRef = db.getReference(collections.users)
            .child(user.displayName!!)
            .child(collections.groupchats)
            .child(chatid)

        usersGroupChatRef.get().addOnSuccessListener {
            subscribirMensajesGrupo(it)
            val foto = it.child("foto").getValue(String::class.java)!!
            if(foto.isNotBlank())
                Picasso.get()
                    .load(foto)
                    .placeholder(R.drawable.saturn)
                    .into(chatPicture)
        }

        Btn_Enviar_Mensaje.setOnClickListener {
            enviarMensajeGrupo(chatid)
        }

        rvMensajes.setOnKeyListener { v, keyCode, event ->
            if(keyCode== KeyEvent.KEYCODE_ENTER){
                enviarMensajeGrupo(chatid)
            }
            keyCode == KeyEvent.KEYCODE_ENTER
        }
        btn_newTarea.setOnClickListener{
            val intent = Intent(this, CreateTareaActivity::class.java).apply {
                putExtra(extranames.ToCreateAssingment.GroupId, chatid)
                putExtra(extranames.ToCreateAssingment.GroupNombre, nombre)
            }
            startActivity(intent)
        }
        chatPicture.setOnClickListener {
            imagenEsMensaje=false
            startImageChooser()
        }
    }

    private fun subscribirMensajesPrivado(snapshot: DataSnapshot){
        val chatref = db.getReference(collections.chats)
        val userchat =  snapshot.getValue(UserChatModel::class.java)!!
        chatref.child(userchat.chatid).child(collections.mensajes)
            .addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                System.out.println("Chat Activity : error on db connection");
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val arr =  snapshot.children.map{
                    val msj = it.getValue(MensajeModel::class.java)!!
                    if(msj.de == user.displayName!!){
                        msj.esMio = true
                    }
                    val decrypted = encrypter.decrypt(msj.contenido)
                    msj.contenido = decrypted

                    msj
                }

                mensajes.clear()
                mensajes.addAll(arr as MutableList<MensajeModel>)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun subscribirMensajesGrupo(snapshot: DataSnapshot){
        val chatref = db.getReference(collections.groupchats)
        val userchat =  snapshot.getValue(UserGroupChatModel::class.java)!!
        chatref.child(userchat.id).child(collections.mensajes)
            .addValueEventListener(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    System.out.println("Chat Activity : error on db connection");
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val arr =  snapshot.children.map{
                        val msj = it.getValue(MensajeModel::class.java)!!
                        if(msj.de == user.displayName!!){
                            msj.esMio = true
                        }
                        val decrypted = encrypter.decrypt(msj.contenido)
                        msj.contenido = decrypted

                        msj
                    }

                    mensajes.clear()
                    mensajes.addAll(arr as MutableList<MensajeModel>)
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun enviarMensajePrivado(chatid: String){
        val contenido = rvMensajes.text.toString()
        rvMensajes.text.clear()
        if(contenido.isEmpty()&&fileUri!=Uri.EMPTY&&imageUri!= Uri.EMPTY){
            return;
        }

        val chatref = db.getReference(collections.chats)
        val msjref = chatref.child(chatid).child(collections.mensajes).push()

        val msj = MensajeModel(
            msjref.key!!,
            encrypter.encrypt(contenido.toByteArray()),
            "",
            false,
            user.displayName!!,
            ServerValue.TIMESTAMP
        )

        if(imageUri!=Uri.EMPTY||fileUri!=Uri.EMPTY){
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Enviando Mensaje...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            subirArchivo(
                {
                    tvArchivoSubido.visibility=View.GONE
                    btnCancelarArchivo.visibility=View.GONE
                    msj.archivo = it
                    msj.esImagen = imageUri!=Uri.EMPTY
                    msjref.setValue(msj).addOnSuccessListener {
                        progressDialog.dismiss()
                        msjref.get().addOnSuccessListener {
                            val dbmensaje = it.getValue(MensajeModel::class.java)!!
                            actualizarUltimoMensajePrivado(dbmensaje)
                        }
                    }
                },
                {
                    Toast.makeText(
                        this,
                        "No se pudo subir la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            return
        }

        msjref.setValue(msj).addOnSuccessListener {
            msjref.get().addOnSuccessListener {
                val dbmensaje = it.getValue(MensajeModel::class.java)!!
                actualizarUltimoMensajePrivado(dbmensaje)
            }
        }

    }

    private fun actualizarUltimoMensajePrivado(msj: MensajeModel){
        actualizarUltimoMensajeEnUserChatPrivado(user.displayName!!, other, msj)
        actualizarUltimoMensajeEnUserChatPrivado(other, user.displayName!!, msj)
    }

    private fun actualizarUltimoMensajeEnUserChatPrivado(username:String, other:String, msj:MensajeModel){
        val userchatref = db.getReference(collections.users)
            .child(username)
            .child(collections.chats)
            .child(other)

        userchatref.updateChildren(
            mapOf(
                "lastMessageContent" to msj.contenido,
                "lastMessageTimestamp" to msj.timeStamp as Long
            )
        )
    }

    private fun enviarMensajeGrupo(chatid: String){
        val contenido = rvMensajes.text.toString()
        rvMensajes.text.clear()
        if(contenido.isEmpty()&&imageUri==Uri.EMPTY&&fileUri==Uri.EMPTY){
            return;
        }

        val chatref = db.getReference(collections.groupchats)
        val msjref = chatref.child(chatid).child(collections.mensajes).push()

        val msj = MensajeModel(
            msjref.key!!,
            encrypter.encrypt(contenido.toByteArray()),
            "",
            false,
            user.displayName!!,
            ServerValue.TIMESTAMP
        )

        if(imageUri!=Uri.EMPTY||fileUri!=Uri.EMPTY){
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Enviando Mensaje...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            subirArchivo(
                {
                    tvArchivoSubido.visibility=View.GONE
                    btnCancelarArchivo.visibility=View.GONE
                    msj.archivo = it
                    msj.esImagen = imageUri!=Uri.EMPTY
                    msjref.setValue(msj).addOnSuccessListener {
                        progressDialog.dismiss()
                        msjref.get().addOnSuccessListener {
                            val dbmensaje = it.getValue(MensajeModel::class.java)!!
                            actualizarUltimoMensajeGrupo(chatid, dbmensaje)
                        }
                    }
                },
                {
                    Toast.makeText(
                        this,
                        "No se pudo subir la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            return
        }

        msjref.setValue(msj).addOnSuccessListener {
            msjref.get().addOnSuccessListener {
                val dbmensaje = it.getValue(MensajeModel::class.java)!!
                actualizarUltimoMensajeGrupo(chatid, dbmensaje)
            }
        }
    }

    private fun actualizarUltimoMensajeGrupo(chatid:String, msj: MensajeModel){
        val chatref = db.getReference(collections.groupchats)
            .child(chatid)
            .child(collections.users)

        actualizarUltimoMensajeEnUserChatGrupo(user.displayName!!, chatid, msj)
        chatref.get().addOnSuccessListener {
            it.children.forEach {
                val username = it.getValue(String::class.java)!!
                actualizarUltimoMensajeEnUserChatGrupo(username,chatid, msj)
            }
        }
    }

    private fun actualizarUltimoMensajeEnUserChatGrupo(username:String, chatId: String, msj:MensajeModel){
        val userchatref = db.getReference(collections.users)
            .child(username)
            .child(collections.groupchats)
            .child(chatId)

        userchatref.updateChildren(
            mapOf(
                "lastMessageContent" to msj.contenido,
                "lastMessageTimestamp" to msj.timeStamp as Long
            )
        )
    }

    private fun subirArchivo(onSuccess: (String)->Unit, onFailure: ()->Unit){
        val uri = when(imageUri==Uri.EMPTY){
            true-> fileUri
            false->imageUri
        }
        fileStorage.newFileinStorage( uri, onSuccess, onFailure)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data== null || resultCode!=Activity.RESULT_OK){
            return;
        }

        val filedata = data.data!!
        when(requestCode){
            REQUEST_CODE_IMAGE->{
                imageUri = filedata
                fileUri = Uri.EMPTY
            }
            REQUEST_CODE_FILE->{
                fileUri = filedata
                imageUri = Uri.EMPTY
            }
        }

        tvArchivoSubido.visibility=View.VISIBLE
        btnCancelarArchivo.visibility=View.VISIBLE

        tvArchivoSubido.text = when(requestCode){
            REQUEST_CODE_IMAGE->"Imagen"
            else->"Archivo"
        }

        if(imagenEsMensaje || fileUri!=Uri.EMPTY){
            return
        }

        Picasso.get()
            .load(filedata)
            .into(chatPicture)

        fileStorage.newFileinStorage(filedata,{
            Toast.makeText(
                this,
                "Imagen de chat subida exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            agregarImagenAChatGrupoUsuarios(groupchatid, it)
        },{
            Toast.makeText(
                this,
                "No se pudo subir imagen de chat",
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun agregarImagenAChatGrupoUsuarios(id:String,uri:String){
        val ref = db.getReference(collections.groupchats)
            .child(id)
            .child(collections.users)

        ref.get().addOnSuccessListener{
            it.children
                .map{it.getValue(String::class.java)!!}
                .forEach{
                    val userGroupChatRef = db.getReference(collections.users)
                        .child(it)
                        .child(collections.groupchats)
                        .child(id)

                    userGroupChatRef.updateChildren(mapOf("foto" to uri))
                }
        }

    }
}