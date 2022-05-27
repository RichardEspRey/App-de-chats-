package com.example.proyecto.adapters

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.recycler_usuarios_crear_grupo.view.*


class MultiSelectionAdapter<T>(
 private val context: Context,
 private val list: List<T>
 ) : BaseAdapter() {

 private val layoutInflater = LayoutInflater.from(context)
 private var mSparseBooleanArray = SparseBooleanArray()

  fun getCheckedItems():List<T>{
   return list.filterIndexed { i, t ->  mSparseBooleanArray[i]}
  }

 override fun getCount(): Int {
  return list.size
 }

 override fun getItem(position: Int): T {
  return list[position]
 }

 override fun getItemId(position: Int): Long {
  return position.toLong()
 }

 override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
  var convertView = convertView
  if (convertView == null) {
   convertView = layoutInflater.inflate(com.example.proyecto.R.layout.recycler_usuarios_crear_grupo, null)
  }
  val tvTitle = convertView!!.tvTitle
  tvTitle.text = list[position].toString()
  val mCheckBox = convertView.chkEnable
  mCheckBox.tag = position
  mCheckBox.isChecked = mSparseBooleanArray[position]
  mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener)
  return convertView
 }

 private val mCheckedChangeListener: CompoundButton.OnCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
  override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
   if (buttonView != null) {
    mSparseBooleanArray.put((buttonView.tag as Int), isChecked)
   }
  }
 }
}

