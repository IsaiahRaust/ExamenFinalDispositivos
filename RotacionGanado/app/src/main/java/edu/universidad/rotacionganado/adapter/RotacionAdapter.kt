package edu.universidad.rotacionganado.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.universidad.rotacionganado.R
import edu.universidad.rotacionganado.data.Potrero

class RotacionAdapter(
    private val items: List<Pair<Potrero, String>>,
    private val seleccionados: MutableSet<Int>,
    private val onLongClick: (Potrero) -> Unit
) : RecyclerView.Adapter<RotacionAdapter.RotacionViewHolder>() {

    class RotacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardPotrero: MaterialCardView = itemView.findViewById(R.id.cardPotrero)
        val checkSeleccion: CheckBox = itemView.findViewById(R.id.checkSeleccion)
        val textNombre: TextView = itemView.findViewById(R.id.textNombre)
        val textEstado: TextView = itemView.findViewById(R.id.textEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotacion, parent, false)

        return RotacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotacionViewHolder, position: Int) {
        val potrero = items[position].first
        val estado = items[position].second
        val context = holder.itemView.context

        holder.textNombre.text = potrero.nombre

        when (estado) {
            "ROJO" -> {
                holder.cardPotrero.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.rojo_estado)
                )
                holder.textEstado.text = "Con ganado (rojo)"
                holder.checkSeleccion.visibility = View.GONE
            }

            "ANARANJADO" -> {
                holder.cardPotrero.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.anaranjado_estado)
                )
                holder.textEstado.text = "En recuperación"
                holder.checkSeleccion.visibility = View.GONE
            }

            else -> {
                holder.cardPotrero.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.verde_principal)
                )
                holder.textEstado.text = "Disponible (verde)"
                holder.checkSeleccion.visibility = View.VISIBLE
            }
        }

        holder.checkSeleccion.setOnCheckedChangeListener(null)
        holder.checkSeleccion.isChecked = seleccionados.contains(potrero.id)

        holder.itemView.alpha = if (seleccionados.contains(potrero.id)) {
            0.85f
        } else {
            1.0f
        }

        holder.itemView.setOnClickListener {
            if (estado == "VERDE") {
                if (seleccionados.contains(potrero.id)) {
                    seleccionados.remove(potrero.id)
                } else {
                    seleccionados.add(potrero.id)
                }

                notifyItemChanged(position)
            }
        }

        holder.checkSeleccion.setOnClickListener {
            if (estado == "VERDE") {
                if (seleccionados.contains(potrero.id)) {
                    seleccionados.remove(potrero.id)
                } else {
                    seleccionados.add(potrero.id)
                }

                notifyItemChanged(position)
            }
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(potrero)
            true
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}