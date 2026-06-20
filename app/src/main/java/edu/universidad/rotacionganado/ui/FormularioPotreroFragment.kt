package edu.universidad.rotacionganado.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import android.widget.VideoView
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.universidad.rotacionganado.R
import edu.universidad.rotacionganado.data.AppDatabase
import edu.universidad.rotacionganado.data.Potrero
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class FormularioPotreroFragment : Fragment() {

    private lateinit var database: AppDatabase

    private lateinit var tvTituloFormulario: TextView
    private lateinit var etNombre: EditText
    private lateinit var etMetros: EditText
    private lateinit var btnFecha: Button
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnSeleccionarVideo: Button

    private lateinit var imgFotoPotrero: ImageView

    private lateinit var videoPotrero: VideoView
    private lateinit var btnGuardar: Button
    private lateinit var btnRegresar: Button

    private var potreroId: Int? = null
    private var fechaSeleccionada: Long = System.currentTimeMillis()
    private var potreroActual: Potrero? = null

    private var fotoUriSeleccionada: String? = null
    private var videoUriSeleccionado: String? = null

    private val seleccionarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                guardarPermisoLectura(uri)

                fotoUriSeleccionada = uri.toString()

                btnSeleccionarFoto.text = "Foto seleccionada"

                imgFotoPotrero.visibility = View.VISIBLE
                imgFotoPotrero.setImageURI(uri)
            }
        }

    private val seleccionarVideoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                guardarPermisoLectura(uri)

                videoUriSeleccionado = uri.toString()
                btnSeleccionarVideo.text = "Video seleccionado"

                videoPotrero.visibility = View.VISIBLE
                videoPotrero.setVideoURI(uri)

                val mediaController = MediaController(requireContext())
                mediaController.setAnchorView(videoPotrero)
                videoPotrero.setMediaController(mediaController)

                videoPotrero.seekTo(1)
            }
        }

    companion object {
        fun newInstance(potreroId: Int?): FormularioPotreroFragment {
            val fragment = FormularioPotreroFragment()
            val bundle = Bundle()

            if (potreroId != null) {
                bundle.putInt("potreroId", potreroId)
            }

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_formulario_potrero, container, false)

        database = AppDatabase.getDatabase(requireContext())

        tvTituloFormulario = view.findViewById(R.id.tvTituloFormulario)
        etNombre = view.findViewById(R.id.etNombre)
        etMetros = view.findViewById(R.id.etMetros)
        btnFecha = view.findViewById(R.id.btnFecha)
        btnSeleccionarFoto = view.findViewById(R.id.btnSeleccionarFoto)
        btnSeleccionarVideo = view.findViewById(R.id.btnSeleccionarVideo)
        imgFotoPotrero = view.findViewById(R.id.imgFotoPotrero)
        videoPotrero = view.findViewById(R.id.videoPotrero)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnRegresar = view.findViewById(R.id.btnRegresar)

        potreroId = if (arguments?.containsKey("potreroId") == true) {
            arguments?.getInt("potreroId")
        } else {
            null
        }

        actualizarTextoFecha()

        if (potreroId != null) {
            cargarPotreroExistente(potreroId!!)
        } else {
            tvTituloFormulario.text = "Nuevo potrero"
        }

        btnFecha.setOnClickListener {
            mostrarDatePicker()
        }

        btnSeleccionarFoto.setOnClickListener {
            seleccionarFotoLauncher.launch(arrayOf("image/*"))
        }

        btnSeleccionarVideo.setOnClickListener {
            seleccionarVideoLauncher.launch(arrayOf("video/*"))
        }

        btnGuardar.setOnClickListener {
            guardarPotrero()
        }

        btnRegresar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun cargarPotreroExistente(id: Int) {
        lifecycleScope.launch {
            val potrero = database.potreroDao().obtenerPotreroPorId(id)

            if (potrero != null) {
                potreroActual = potrero

                tvTituloFormulario.text = "Editar potrero"
                etNombre.setText(potrero.nombre)
                etMetros.setText(potrero.metrosCuadrados.toString())

                fechaSeleccionada = potrero.fechaCreacion
                actualizarTextoFecha()

                fotoUriSeleccionada = potrero.fotoUri
                videoUriSeleccionado = potrero.videoUri

                if (!fotoUriSeleccionada.isNullOrBlank()) {
                    imgFotoPotrero.visibility = View.VISIBLE
                    imgFotoPotrero.setImageURI(Uri.parse(fotoUriSeleccionada))
                } else {
                    imgFotoPotrero.visibility = View.GONE
                }
                if (!videoUriSeleccionado.isNullOrBlank()) {
                    videoPotrero.visibility = View.VISIBLE
                    videoPotrero.setVideoURI(Uri.parse(videoUriSeleccionado))

                    val mediaController = MediaController(requireContext())
                    mediaController.setAnchorView(videoPotrero)
                    videoPotrero.setMediaController(mediaController)

                    videoPotrero.seekTo(1)
                } else {
                    videoPotrero.visibility = View.GONE
                }

                btnSeleccionarFoto.text = if (!fotoUriSeleccionada.isNullOrBlank()) {
                    "Foto seleccionada"
                } else {
                    "Seleccionar foto"
                }

                btnSeleccionarVideo.text = if (!videoUriSeleccionado.isNullOrBlank()) {
                    "Video seleccionado"
                } else {
                    "Seleccionar video"
                }
            }
        }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = fechaSeleccionada

        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val nuevaFecha = Calendar.getInstance()
                nuevaFecha.set(year, month, dayOfMonth, 0, 0, 0)
                nuevaFecha.set(Calendar.MILLISECOND, 0)

                fechaSeleccionada = nuevaFecha.timeInMillis
                actualizarTextoFecha()
            },
            anio,
            mes,
            dia
        )

        datePicker.show()
    }

    private fun actualizarTextoFecha() {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val textoFecha = formato.format(Date(fechaSeleccionada))
        btnFecha.text = "Fecha de creación: $textoFecha"
    }

    private fun guardarPotrero() {
        val nombre = etNombre.text.toString().trim()
        val metrosTexto = etMetros.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese el nombre del potrero", Toast.LENGTH_SHORT).show()
            return
        }

        if (metrosTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese las medidas en m²", Toast.LENGTH_SHORT).show()
            return
        }

        val metros = metrosTexto.toDoubleOrNull()

        if (metros == null || metros <= 0) {
            Toast.makeText(requireContext(), "Ingrese una medida válida", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (potreroActual == null) {
                val nuevoPotrero = Potrero(
                    nombre = nombre,
                    metrosCuadrados = metros,
                    fechaCreacion = fechaSeleccionada,
                    fotoUri = fotoUriSeleccionada ?: "",
                    videoUri = videoUriSeleccionado ?: ""
                )

                database.potreroDao().insertarPotrero(nuevoPotrero)
                Toast.makeText(requireContext(), "Potrero guardado", Toast.LENGTH_SHORT).show()
            } else {
                val potreroEditado = potreroActual!!.copy(
                    nombre = nombre,
                    metrosCuadrados = metros,
                    fechaCreacion = fechaSeleccionada,
                    fotoUri = fotoUriSeleccionada ?: "",
                    videoUri = videoUriSeleccionado ?: ""
                )

                database.potreroDao().actualizarPotrero(potreroEditado)
                Toast.makeText(requireContext(), "Potrero actualizado", Toast.LENGTH_SHORT).show()
            }

            parentFragmentManager.popBackStack()
        }
    }

    private fun guardarPermisoLectura(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }
    }
}