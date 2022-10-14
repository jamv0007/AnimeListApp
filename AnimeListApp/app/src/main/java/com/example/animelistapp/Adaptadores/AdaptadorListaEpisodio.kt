package com.example.animelistapp.Adaptadores

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.R

class AdaptadorListaEpisodio(private var lista: ArrayList<Pair<Int,Episodio>>,private var act: Activity): RecyclerView.Adapter<ViewHolderListaEpisodio>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderListaEpisodio {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolderListaEpisodio(layoutInflater.inflate(R.layout.elemento_lista_episodio,parent,false),act)
    }

    override fun onBindViewHolder(holder: ViewHolderListaEpisodio, position: Int) {
        val elemento = lista[position]
        holder.render(elemento.first,elemento.second,position)


    }

    override fun getItemCount(): Int {
        return lista.size
    }

}