package com.example.animelistapp.Adaptadores

import android.app.Activity
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Temporada
import com.example.animelistapp.databinding.ElementoListaTemporadaBinding

class ViewHolderListaTemporada(view: View,act: Activity): RecyclerView.ViewHolder(view) {

    val binding = ElementoListaTemporadaBinding.bind(view)
    var listener: SwitchSeasonChecked

    init {
        try {
            listener = act as SwitchSeasonChecked;
        }catch (e: ClassCastException){
            throw ClassCastException(act.applicationContext.toString() + "implementa la interfaz CheckedSeasonSwitch");
        }
    }

    fun render(posicion: Int,posView: Int, temporada: Temporada, onClick:(Temporada, Int,Int) -> Unit, onLongClick:(Temporada, Int)-> Boolean){
        binding.temporadaTerminada.setOnCheckedChangeListener(null)

        binding.numeroTemporada.text = "Temporada " + temporada.numeroTemporada
        binding.temporadaTerminada.isChecked = temporada.finalizada

        itemView.setOnClickListener{ onClick(temporada,posicion,posView) }
        itemView.setOnLongClickListener{ onLongClick(temporada,posicion) }

        binding.temporadaTerminada.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{ cb, b ->
            listener.onSwitchValueChange(b,posicion,posView)
        })

    }

    public interface SwitchSeasonChecked{
        public fun onSwitchValueChange(value: Boolean, pos: Int,posView:Int)
    }

}