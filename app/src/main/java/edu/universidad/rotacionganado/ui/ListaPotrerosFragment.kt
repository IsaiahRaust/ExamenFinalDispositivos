package edu.universidad.rotacionganado.ui

import android.app.AlertDialog
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
import edu.universidad.rotacionganado.adapter.PotreroAdapter
import edu.universidad.rotacionganado.data.AppDatabase
import edu.universidad.rotacionganado.data.Potrero
import kotlinx.coroutines.launch

class ListaPotrerosFragment : Fragment() {

    private lateinit var database: AppDatabase
    private lateinit var rvPotreros: RecyclerView
    private lateinit var btnAgregarPotrero: Button
    private lateinit var btnIrRotacion: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_lista_potreros, container, false)

        database = AppDatabase.getDatabase(requireContext())

        rvPotreros = view.findViewById(R.id.rvPotreros)
        btnAgregarPotrero = view.findViewById(R.id.btnAgregarPotrero)
        btnIrRotacion = view.findViewById(R.id.btnIrRotacion)

        rvPotreros.layoutManager = LinearLayoutManager(requireContext())

        btnAgregarPotrero.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FormularioPotreroFragment.newInstance(null))
                .addToBackStack(null)
                .commit()
        }

        btnIrRotacion.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RotacionFragment())
                .addToBackStack(null)
                .commit()
        }

        insertarPotrerosIniciales()
        cargarPotreros()

        return view
    }

    private fun insertarPotrerosIniciales() {
        lifecycleScope.launch {
            val potreros = database.potreroDao().obtenerPotreros()

            if (potreros.isEmpty()) {
                val fechaActual = System.currentTimeMillis()

                database.potreroDao().insertarPotrero(
                    Potrero(
                        nombre = "Potrero Norte",
                        metrosCuadrados = 1200.0,
                        fechaCreacion = fechaActual,
                        fotoUri = "",
                        videoUri = ""
                    )
                )

                database.potreroDao().insertarPotrero(
                    Potrero(
                        nombre = "Potrero Sur",
                        metrosCuadrados = 950.0,
                        fechaCreacion = fechaActual,
                        fotoUri = "",
                        videoUri = ""
                    )
                )

                database.potreroDao().insertarPotrero(
                    Potrero(
                        nombre = "Potrero Este",
                        metrosCuadrados = 1100.0,
                        fechaCreacion = fechaActual,
                        fotoUri = "",
                        videoUri = ""
                    )
                )

                database.potreroDao().insertarPotrero(
                    Potrero(
                        nombre = "Potrero Oeste",
                        metrosCuadrados = 800.0,
                        fechaCreacion = fechaActual,
                        fotoUri = "",
                        videoUri = ""
                    )
                )

                database.potreroDao().insertarPotrero(
                    Potrero(
                        nombre = "Potrero Central",
                        metrosCuadrados = 1500.0,
                        fechaCreacion = fechaActual,
                        fotoUri = "",
                        videoUri = ""
                    )
                )

                cargarPotreros()
            }
        }
    }

    private fun cargarPotreros() {
        lifecycleScope.launch {
            val potreros = database.potreroDao().obtenerPotreros()

            rvPotreros.adapter = PotreroAdapter(
                potreros = potreros,
                onClick = { potrero ->
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FormularioPotreroFragment.newInstance(potrero.id))
                        .addToBackStack(null)
                        .commit()
                },
                onLongClick = { potrero ->
                    confirmarEliminacion(potrero)
                }
            )
        }
    }

    private fun confirmarEliminacion(potrero: Potrero) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar potrero")
            .setMessage("¿Desea eliminar ${potrero.nombre}? También se eliminarán sus históricos.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    database.potreroDao().eliminarPotrero(potrero)
                    cargarPotreros()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}