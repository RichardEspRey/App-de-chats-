package com.example.proyecto.adapters

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.R
import com.example.proyecto.filestorage.FileStorage
import com.example.proyecto.modelos.MensajeModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_mensajes_chat.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val listaMensajes: MutableList<MensajeModel>) :
RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileStorage = FileStorage(itemView.context)
        fun init(msj: MensajeModel) {
            itemView.tvNombre.text = msj.de
            itemView.tvMensaje.text = msj.contenido

            val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
            val date = Date(msj.timeStamp as Long)
            itemView.tvHora.text = sdf.format(date)

            itemView.tvArchivo.setOnClickListener {
                fileStorage.downloadFile(msj.archivo)
            }

            val params = itemView.llChat.layoutParams
            val newParams = FrameLayout.LayoutParams(
                params.width,
                params.height,
                when(msj.esMio) {
                        true -> Gravity.END
                        false -> Gravity.START
                    }
            )

            itemView.llChat.layoutParams = newParams

            if(msj.archivo.isBlank()){
                itemView.ivImagenMensaje.visibility=View.GONE
                itemView.tvArchivo.visibility=View.GONE
                return
            }

            if(msj.esImagen){
                itemView.ivImagenMensaje.visibility=View.VISIBLE
                itemView.tvArchivo.visibility=View.GONE
                Picasso.get()
                    .load(msj.archivo)
                    .placeholder(R.drawable.saturn)
                    .into(itemView.ivImagenMensaje)
                return
            }

            itemView.ivImagenMensaje.visibility=View.GONE
            itemView.tvArchivo.visibility=View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_mensajes_chat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msj = listaMensajes[position]
        holder.init(msj)

        val view = holder.itemView

    }

    override fun getItemCount(): Int = listaMensajes.size
}