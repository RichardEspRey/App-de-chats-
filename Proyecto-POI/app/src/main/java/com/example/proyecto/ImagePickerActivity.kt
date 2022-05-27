package com.example.proyecto

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class ImagePickerActivity() : AppCompatActivity() {

    companion object{
        const val PERMISSION_CODE=1001
        const val REQUEST_CODE_IMAGE=1000
        const val REQUEST_CODE_FILE=1002
    }

    protected var imageUri = Uri.EMPTY
    protected var fileUri = Uri.EMPTY

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
    }

    fun startImageChooser()
    {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE)
            return
        }

        pickImageFromGallery()
    }

    private fun pickImageFromGallery()
    {
        val intent  =  Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        ActivityCompat.startActivityForResult(
            this,
            Intent.createChooser(intent, "Seleccionar Imagen"),
            REQUEST_CODE_IMAGE,
            null
        )
    }

    fun startFileChooser(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE)
            return
        }

        pickFileFromFiles()
    }

    private fun pickFileFromFiles(){
        val intent  =  Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        ActivityCompat.startActivityForResult(
            this,
            Intent.createChooser(intent, "Seleccionar Archivo"),
            REQUEST_CODE_FILE,
            null
        )
    }
}