package com.example.proyecto.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.ChatActivity
import com.example.proyecto.R
import com.example.proyecto.config.extranames
import com.example.proyecto.config.secret
import com.example.proyecto.date.PrettyDate
import com.example.proyecto.encryption.MessageEncrypter
import com.example.proyecto.modelos.UserGroupChatModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.recycler_listequipos.view.*

class EquipoChatAdapter (private val grupos: MutableList<UserGroupChatModel>) :
    RecyclerView.Adapter<EquipoChatAdapter.ChatViewHolder>() {
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val encrypter = MessageEncrypter(secret.key)
        fun init(model: UserGroupChatModel) {
            itemView.rlNombre.text = model.nombre
            if(model.lastMessageContent.isNotBlank())
                itemView.rlMensaje.text = encrypter.decrypt(model.lastMessageContent)
            else
                itemView.rlMensaje.text = ""

            val prettyDate = PrettyDate()
            itemView.rlHora.text = when(model.lastMessageTimestamp){
                0L->""
                else -> {
                    val prettyDate = PrettyDate()
                    prettyDate.format(model.lastMessageTimestamp)
                }
            }

            // "https://m.media-amazon.com/images/I/91UrvnMGKoL._AC_SL1500_.jpg"
            if(model.foto.isNotBlank())
                Picasso.get()
                    .load(model.foto)
                    .placeholder(R.drawable.saturn)
                    .into(itemView.ciFotoGrupo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_listequipos, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val view = holder.itemView
        val groupChatModel = grupos[position]
        holder.init(groupChatModel)

        view.setOnClickListener {
            val intent = Intent(view.context, ChatActivity::class.java).apply {
                putExtra(extranames.ToChatActivity.ChatId,groupChatModel.id)
                putExtra(extranames.ToChatActivity.GroupChatName,groupChatModel.nombre)
                putExtra(extranames.ToChatActivity.IsGroupChat, true)
            }
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = grupos.size
}