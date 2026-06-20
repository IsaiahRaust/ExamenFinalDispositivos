package edu.universidad.rotacionganado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potreros")
data class Potrero(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val metrosCuadrados: Double,
    val fechaCreacion: Long,
    val fotoUri: String?,
    val videoUri: String?
)