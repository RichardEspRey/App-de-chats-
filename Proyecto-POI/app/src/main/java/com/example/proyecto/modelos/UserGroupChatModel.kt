package com.example.proyecto.modelos

data class UserGroupChatModel(
    var id:String = "",
    var nombre:String = "",
    var foto:String="",
    var lastMessageContent:String="",
    var lastMessageTimestamp:Long=0,
)
