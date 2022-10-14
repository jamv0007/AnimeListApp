package com.example.animelistapp.Adaptadores

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.BottomSheetTemporada
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.databinding.ElementoListaEpisodioBinding

class ViewHolderListaEpisodio(view: View,act: Activity): RecyclerView.ViewHolder(view) {

    val binding = ElementoListaEpisodioBinding.bind(view)
    var listener: CheckSwitch

    init {

        var activity = act;

        try {
            listener = activity as CheckSwitch;
        }catch (e: ClassCastException){
            throw ClassCastException(act.applicationContext.toString() + "implementa la interfaz CheckedSwitch");
        }

    }

    fun render(posicion: Int,ep: Episodio,posView: Int){

        binding.switchEpisode.setOnCheckedChangeListener(null)

        binding.NumberEpisode.text = "Capitulo " + ep.numeroEpisodio

        binding.switchEpisode.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            listener.onSwitchChange(b, posicion, posView)

        })

        binding.switchEpisode.isChecked = ep.visto
    }

    public interface CheckSwitch{
        public fun onSwitchChange(valor: Boolean, pos: Int, posView: Int)
    }

}