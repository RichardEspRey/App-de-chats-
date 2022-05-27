package com.example.proyecto.modelos

import com.google.firebase.database.Exclude

data class UserInfoModel(
    var userName : String = "",
    var nombre : String = "",
    var mail : String = "" ,
    var info : Boolean = false,
    var uid :String = "",
    var photoUrl: String = "",
    var puntos: Long=0
    )