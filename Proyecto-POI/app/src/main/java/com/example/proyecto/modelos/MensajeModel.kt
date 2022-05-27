package com.example.proyecto.modelos

import com.google.firebase.database.Exclude

data class MensajeModel(
    var id: String = "",
    var contenido: String = "",
    var archivo: String = "",
    var esImagen: Boolean = false,
    var de: String = "",
    var timeStamp : Any? = null
){
    @Exclude
    var esMio = false
}