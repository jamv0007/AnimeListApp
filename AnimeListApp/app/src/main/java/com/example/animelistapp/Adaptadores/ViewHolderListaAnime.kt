package com.example.animelistapp.Adaptadores

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.databinding.ElementoListaAnimeBinding
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.R

class ViewHolderListaAnime(view:View): RecyclerView.ViewHolder(view) {

    val binding = ElementoListaAnimeBinding.bind(view)

    fun render(posicion: Int,anime: Anime,onClick:(Anime,Int) -> Unit,onLongClick:(Anime,Int)-> Boolean){
        binding.nombreAnime.text = anime.nombre
        binding.imagenAnime.setImageURI(anime.imagen)

        if(anime.terminado){
            binding.capituloActual.text = "Terminado"
            binding.capituloActual.setTextColor(Color.parseColor("#ff4040"))
        }else{
            if(anime.viendo){
                binding.capituloActual.text = "Temporada " + anime.temporadaActual + " Capitulo " + anime.episodioActual
                binding.capituloActual.setTextColor(Color.parseColor("#ffa040"))
            }else{
                binding.capituloActual.text = "Pendiente"
                binding.capituloActual.setTextColor(Color.parseColor("#00b347"))
            }
        }

        itemView.setOnClickListener{ onClick(anime,posicion) }

        itemView.setOnLongClickListener{ onLongClick(anime,posicion) }



    }

}