package com.example.proyecto.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.AssingmentDetailActivity
import com.example.proyecto.R
import com.example.proyecto.config.extranames
import com.example.proyecto.modelos.AssignmentModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_listassignments.view.*
import kotlinx.android.synthetic.main.recycler_listassignments.view.rlMensaje
import kotlinx.android.synthetic.main.recycler_listchat.view.*
import kotlinx.android.synthetic.main.recycler_listchat.view.rlNombre

class AssignmentsAdapter(private val assignments: MutableList<AssignmentModel>) :
    RecyclerView.Adapter<AssignmentsAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun init(model: AssignmentModel) {
            itemView.rlMensaje.text = model.descripcion
            itemView.rlNombre.text = model.groupNombre

            itemView.llLayoutEntregado.visibility = when(model.entregada){
                true -> View.VISIBLE
                false ->View.GONE
            }

            if(model.groupFoto.isNotBlank())
                Picasso.get()
                    .load(model.groupFoto)
                    .placeholder(R.drawable.saturn)
                    .into(itemView.ciFotoUser)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_listassignments, parent, false)
        )
    }
    override fun getItemCount(): Int = assignments.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val model = assignments[position];
        val view = holder.itemView
        holder.init(model)
        view.setOnClickListener {
            val intent = Intent(view.context, AssingmentDetailActivity::class.java).apply{
                putExtra(extranames.ToAssignmentDetail.AsssigmentId, model.id)
                putExtra(extranames.ToAssignmentDetail.GroupNombre, model.groupNombre)
                putExtra(extranames.ToAssignmentDetail.GroupFoto, model.groupFoto)
                putExtra(extranames.ToAssignmentDetail.Descripcion, model.descripcion)
                putExtra(extranames.ToAssignmentDetail.Puntos, model.puntos)
            }
            view.context.startActivity(intent)
        }
    }
}