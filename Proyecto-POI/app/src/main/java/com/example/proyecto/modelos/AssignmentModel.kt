package com.example.proyecto.modelos

data class AssignmentModel(
    var id: String ="",
    var groupId: String ="",
    var groupNombre: String="",
    var groupFoto: String = "",
    var descripcion: String="",
    var puntos: Int = 0,
    var fechaEntrega: Long = 0,
    var fechaCreada: Any? = null,
    var entregada:Boolean=false,
)