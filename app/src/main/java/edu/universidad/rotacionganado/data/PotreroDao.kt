package edu.universidad.rotacionganado.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PotreroDao {

    @Query("SELECT * FROM potreros ORDER BY nombre ASC")
    suspend fun obtenerPotreros(): List<Potrero>

    @Query("SELECT * FROM potreros WHERE id = :id")
    suspend fun obtenerPotreroPorId(id: Int): Potrero?

    @Insert
    suspend fun insertarPotrero(potrero: Potrero)

    @Update
    suspend fun actualizarPotrero(potrero: Potrero)

    @Delete
    suspend fun eliminarPotrero(potrero: Potrero)

    @Insert
    suspend fun insertarRotacion(rotacion: Rotacion)

    @Query("SELECT * FROM rotaciones WHERE potreroId = :potreroId ORDER BY fechaInicio DESC")
    suspend fun obtenerRotacionesPorPotrero(potreroId: Int): List<Rotacion>

    @Query("SELECT * FROM rotaciones ORDER BY fechaInicio DESC")
    suspend fun obtenerTodasLasRotaciones(): List<Rotacion>

    @Query("""DELETE FROM rotaciones WHERE id = (SELECT id FROM rotaciones WHERE potreroId = :potreroId ORDER BY fechaInicio DESC LIMIT 1)""")
    suspend fun eliminarUltimaRotacionPotrero(potreroId: Int)
}