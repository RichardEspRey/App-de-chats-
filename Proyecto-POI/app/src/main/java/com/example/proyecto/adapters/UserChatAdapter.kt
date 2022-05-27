package com.example.proyecto.adapters

import android.content.Intent
import android.os.Message
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
import com.example.proyecto.modelos.UserChatModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_listchat.view.*
import kotlinx.android.synthetic.main.recycler_listchat.view.rlHora
import kotlinx.android.synthetic.main.recycler_listchat.view.rlMensaje
import kotlinx.android.synthetic.main.recycler_listchat.view.rlNombre
import kotlinx.android.synthetic.main.recycler_listequipos.view.*

class UserChatAdapter(private val userChats: MutableList<UserChatModel>) :
    RecyclerView.Adapter<UserChatAdapter.ChatViewHolder>() {


    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val encrypter =MessageEncrypter(secret.key)

        fun init(model: UserChatModel) {
            itemView.rlNombre.text = model.otherNombre
            itemView.rlMensaje.text = encrypter.decrypt(model.lastMessageContent)

            itemView.rlHora.text = when(model.lastMessageTimestamp){
                0L->""
                else -> {
                    val prettyDate = PrettyDate()
                    prettyDate.format(model.lastMessageTimestamp)
                }
            }

            if(model.otherFoto.isNotBlank())
                Picasso.get()
                    .load(model.otherFoto)
                    .placeholder(R.drawable.saturn)
                    .into(itemView.ciFotoUser)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_listchat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val model = userChats[position];
        val view = holder.itemView
        holder.init(model)
        view.setOnClickListener {
            // redirigir al item view
            val intent = Intent(view.context, ChatActivity::class.java).apply{
                putExtra(extranames.ToChatActivity.Username, model.otherUsername)
                putExtra(extranames.ToChatActivity.ChatId, model.chatid)
                putExtra(extranames.ToChatActivity.IsGroupChat, false)
            }
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userChats.size
}