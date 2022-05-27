package com.example.proyecto.modelos

data class ChatModel(
    var users: List<String> = listOf(),
    var mensajes: List<MensajeModel> = listOf()
)
