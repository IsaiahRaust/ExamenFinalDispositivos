package edu.universidad.rotacionganado.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.universidad.rotacionganado.R
import edu.universidad.rotacionganado.adapter.RotacionAdapter
import edu.universidad.rotacionganado.data.AppDatabase
import edu.universidad.rotacionganado.data.Potrero
import edu.universidad.rotacionganado.data.Rotacion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RotacionFragment : Fragment() {

    private lateinit var database: AppDatabase
    private lateinit var rvRotacion: RecyclerView
    private lateinit var btnFechaRotacion: Button
    private lateinit var btnCargarGanado: Button
    private lateinit var btnRegresarRotacion: Button

    private val seleccionados = mutableSetOf<Int>()
    private var fechaConsulta: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_rotacion, container, false)

        database = AppDatabase.getDatabase(requireContext())

        rvRotacion = view.findViewById(R.id.rvRotacion)
        btnFechaRotacion = view.findViewById(R.id.btnFechaRotacion)
        btnCargarGanado = view.findViewById(R.id.btnCargarGanado)
        btnRegresarRotacion = view.findViewById(R.id.btnRegresarRotacion)

        rvRotacion.layoutManager = LinearLayoutManager(requireContext())

        actualizarTextoFecha()
        cargarRotacion()

        btnFechaRotacion.setOnClickListener {
            mostrarDatePicker()
        }

        btnCargarGanado.setOnClickListener {
            cargarGanado()
        }

        btnRegresarRotacion.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = fechaConsulta

        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val nuevaFecha = Calendar.getInstance()
                nuevaFecha.set(year, month, dayOfMonth, 0, 0, 0)
                nuevaFecha.set(Calendar.MILLISECOND, 0)

                fechaConsulta = nuevaFecha.timeInMillis
                seleccionados.clear()

                actualizarTextoFecha()
                cargarRotacion()
            },
            anio,
            mes,
            dia
        ).show()
    }

    private fun actualizarTextoFecha() {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        btnFechaRotacion.text = "Fecha: ${formato.format(Date(fechaConsulta))}"
    }

    private fun cargarRotacion() {
        lifecycleScope.launch {
            val potreros = database.potreroDao().obtenerPotreros()
            val items = mutableListOf<Pair<Potrero, String>>()

            for (potrero in potreros) {
                val estado = calcularEstadoPotrero(potrero.id, fechaConsulta)
                items.add(Pair(potrero, estado))
            }

            rvRotacion.adapter = RotacionAdapter(
                items = items,
                seleccionados = seleccionados,
                onLongClick = { potrero ->
                    val estado = items.firstOrNull { it.first.id == potrero.id }?.second

                    if (estado == "ROJO") {
                        confirmarQuitarGanado(potrero)
                    } else {
                        mostrarHistorial(potrero)
                    }
                }
            )
        }
    }

    private suspend fun calcularEstadoPotrero(potreroId: Int, fecha: Long): String {
        val rotaciones = database.potreroDao().obtenerRotacionesPorPotrero(potreroId)

        if (rotaciones.isEmpty()) {
            return "VERDE"
        }

        val rotacionValida = rotaciones
            .filter { it.fechaInicio <= fecha }
            .maxByOrNull { it.fechaInicio }

        if (rotacionValida == null) {
            return "VERDE"
        }

        val dias = TimeUnit.MILLISECONDS.toDays(
            fecha - rotacionValida.fechaInicio
        )

        return when {
            dias < 5 -> "ROJO"
            dias < 20 -> "ANARANJADO"
            else -> "VERDE"
        }
    }

    private fun cargarGanado() {
        if (seleccionados.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Sin selección")
                .setMessage("Seleccione uno o más potreros verdes para cargar ganado.")
                .setPositiveButton("Aceptar", null)
                .show()
            return
        }

        lifecycleScope.launch {
            for (potreroId in seleccionados) {
                database.potreroDao().insertarRotacion(
                    Rotacion(
                        potreroId = potreroId,
                        fechaInicio = fechaConsulta
                    )
                )
            }

            seleccionados.clear()
            cargarRotacion()
        }
    }

    private fun mostrarHistorial(potrero: Potrero) {
        lifecycleScope.launch {
            val rotaciones = database.potreroDao().obtenerRotacionesPorPotrero(potrero.id)

            if (rotaciones.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(potrero.nombre)
                    .setMessage("Este potrero no tiene históricos de rotación.")
                    .setPositiveButton("Aceptar", null)
                    .show()
                return@launch
            }

            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val texto = StringBuilder()

            for (rotacion in rotaciones) {
                val inicioRojo = rotacion.fechaInicio
                val finRojo = sumarDias(rotacion.fechaInicio, 4)
                val inicioAnaranjado = sumarDias(rotacion.fechaInicio, 5)
                val finAnaranjado = sumarDias(rotacion.fechaInicio, 19)

                texto.append("Inicio rojo: ${formato.format(Date(inicioRojo))}\n")
                texto.append("Fin rojo: ${formato.format(Date(finRojo))}\n")
                texto.append("Inicio anaranjado: ${formato.format(Date(inicioAnaranjado))}\n")
                texto.append("Fin anaranjado: ${formato.format(Date(finAnaranjado))}\n")
                texto.append("\n")
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Historial de ${potrero.nombre}")
                .setMessage(texto.toString())
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }

    private fun sumarDias(fecha: Long, dias: Int): Long {
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = fecha
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return calendario.timeInMillis
    }
    private fun confirmarQuitarGanado(potrero: Potrero) {
        AlertDialog.Builder(requireContext())
            .setTitle("Quitar ganado")
            .setMessage("¿Desea quitar el ganado de ${potrero.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    database.potreroDao().eliminarUltimaRotacionPotrero(potrero.id)
                    seleccionados.clear()
                    cargarRotacion()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}