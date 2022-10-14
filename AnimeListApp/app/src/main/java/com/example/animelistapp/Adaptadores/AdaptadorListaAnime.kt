package com.example.animelistapp.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.R

class AdaptadorListaAnime(private var lista: ArrayList<Pair<Int,Anime>>, private val onClick:(Anime, Int) -> Unit, private val onLongClick: (Anime, Int) -> Boolean) : RecyclerView.Adapter<ViewHolderListaAnime>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderListaAnime {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolderListaAnime(layoutInflater.inflate(R.layout.elemento_lista_anime,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolderListaAnime, position: Int) {
        val elemento = lista[position]

        holder.render(elemento.first,elemento.second,onClick,onLongClick)
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}