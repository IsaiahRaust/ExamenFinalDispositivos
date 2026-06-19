package edu.universidad.rotacionganado.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rotaciones",
    foreignKeys = [
        ForeignKey(
            entity = Potrero::class,
            parentColumns = ["id"],
            childColumns = ["potreroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("potreroId")]
)
data class Rotacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potreroId: Int,
    val fechaInicio: Long
)