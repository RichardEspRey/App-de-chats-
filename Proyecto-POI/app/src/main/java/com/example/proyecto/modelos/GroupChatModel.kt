package com.example.proyecto.modelos

data class GroupChatModel(
    var id:String = "",
    var nombre:String = "",
    var foto:String="",
    var users: List<String> = listOf(),
    var mensajes: List<MensajeModel> = listOf()
)
