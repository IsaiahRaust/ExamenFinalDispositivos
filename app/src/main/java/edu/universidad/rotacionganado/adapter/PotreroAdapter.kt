package edu.universidad.rotacionganado.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.universidad.rotacionganado.R
import edu.universidad.rotacionganado.data.Potrero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PotreroAdapter(
    private val potreros: List<Potrero>,
    private val onClick: (Potrero) -> Unit,
    private val onLongClick: (Potrero) -> Unit
) : RecyclerView.Adapter<PotreroAdapter.PotreroViewHolder>() {

    class PotreroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombrePotrero: TextView = itemView.findViewById(R.id.tvNombrePotrero)
        val tvDetallePotrero: TextView = itemView.findViewById(R.id.tvDetallePotrero)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PotreroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_potrero, parent, false)

        return PotreroViewHolder(view)
    }

    override fun onBindViewHolder(holder: PotreroViewHolder, position: Int) {
        val potrero = potreros[position]

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaTexto = formatoFecha.format(Date(potrero.fechaCreacion))

        holder.tvNombrePotrero.text = potrero.nombre
        holder.tvDetallePotrero.text = "${potrero.metrosCuadrados} m² | Creado: $fechaTexto"

        holder.itemView.setOnClickListener {
            onClick(potrero)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(potrero)
            true
        }
    }

    override fun getItemCount(): Int {
        return potreros.size
    }
}