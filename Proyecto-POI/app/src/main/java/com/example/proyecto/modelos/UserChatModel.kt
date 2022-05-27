package com.example.proyecto.modelos

data class UserChatModel(
    var chatid:String = "",
    var otherUsername:String = "",
    var otherNombre:String = "",
    var otherFoto:String="",
    var lastMessageContent:String="",
    var lastMessageTimestamp:Long=0,
){
    fun format():UserChatModel{
        if(lastMessageContent.length > 15){
            lastMessageContent = lastMessageContent.substring(0, 12)+"..."
        }
        return this
    }
}