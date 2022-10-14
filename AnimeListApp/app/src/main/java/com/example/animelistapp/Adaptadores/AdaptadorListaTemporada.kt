package com.example.animelistapp.Adaptadores

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Temporada
import com.example.animelistapp.R

class AdaptadorListaTemporada(private var listaTemporada: ArrayList<Pair<Int,Temporada>>, private val onClick:(Temporada, Int, Int) -> Unit, private val onLongClick: (Temporada, Int) -> Boolean,private var act: Activity) : RecyclerView.Adapter<ViewHolderListaTemporada>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderListaTemporada {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolderListaTemporada(layoutInflater.inflate(R.layout.elemento_lista_temporada,parent,false),act)
    }

    override fun onBindViewHolder(holder: ViewHolderListaTemporada, position: Int) {
        val elemento = listaTemporada[position]
        holder.render(elemento.first,position,elemento.second,onClick,onLongClick)
    }

    override fun getItemCount(): Int {
        return listaTemporada.size
    }


}