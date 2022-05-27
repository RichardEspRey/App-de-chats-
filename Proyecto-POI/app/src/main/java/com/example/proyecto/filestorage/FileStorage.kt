package com.example.proyecto.filestorage

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.time.Instant

// hacer cosas de almacenamiento
class FileStorage(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val user = auth.currentUser!!
    private val storage = FirebaseStorage.getInstance()

    /**
     * @return {String} The url to the file in the storage bucket
     */
    fun newFileinStorage(file:Uri, onSuccess: (path:String)->Unit, onFailure: ()->Unit){
        val path = file.path!!
        val filename = path.substring(path.lastIndexOf('/')+1)
        val name = "${Instant.now().epochSecond}-${filename}"

        val cr = context.contentResolver
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(file))!!

        val ref = storage.getReference("$name.$ext")
        ref.putFile(file).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                onSuccess(it.toString())
            }.addOnFailureListener {
                onFailure()
            }
        }.addOnFailureListener{
            onFailure()
        }
    }

    fun downloadFile(url:String){
        val uri = Uri.parse(url)
        download(uri)
    }

    private fun download(uri: Uri){
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)

        val filename = uri.lastPathSegment
        val contentResolver = context.contentResolver
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, filename)

        downloadManager.enqueue(request)
    }
}