package com.example.animelistapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.net.toUri
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.Clases.Temporada

object UsoBase {

    public fun insertarAnime(context: Context,tabla: String,content: ContentValues){

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.insert(tabla,null,content)
        base.close()

    }

    public fun borrarAnime(context: Context,tabla: String,clausula: String){

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.delete(tabla,clausula,null)
        base.close()

    }

    public fun modificarAnime(context: Context,tabla: String,content: ContentValues,clausula: String){
        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.update(tabla,content,clausula,null)
        base.close()
    }



    public fun cargarDatos(context: Context): ArrayList<Anime>{

        val datos: ArrayList<Anime> = arrayListOf()

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        val cursor: Cursor = base.rawQuery("select * from anime",null)

        while (cursor.moveToNext()){

            var viendo = false
            if (cursor.getString(5).toInt() == 1){
                viendo = true
            }

            var terminado = false
            if (cursor.getString(6).toInt() == 1){
                terminado = true
            }

            val anime = Anime(cursor.getString(0).toLong(),cursor.getString(1),
                Uri.parse(cursor.getString(2)),cursor.getString(3).toInt(),cursor.getString(4).toInt(),
                viendo,terminado,
                arrayListOf())
            val cursorTemporada: Cursor = base.rawQuery("select * from temporada where anime_clave=" + anime.id,null)
            while (cursorTemporada.moveToNext()){
                var boolean2 = false
                if(cursorTemporada.getString(2).toInt() == 1){
                    boolean2 = true
                }
                val temporada: Temporada = Temporada(cursorTemporada.getString(0).toLong(),cursorTemporada.getString(1).toInt(),
                    boolean2,
                    arrayListOf())
                val cursorEpisodio: Cursor = base.rawQuery("select * from episodio where temporada_clave=" + temporada.id,null)
                while (cursorEpisodio.moveToNext()){
                    var boolean = false
                    if(cursorEpisodio.getString(2).toInt() == 1){
                        boolean = true
                    }
                    val episodio: Episodio = Episodio(cursorEpisodio.getString(0).toLong(), cursorEpisodio.getString(1).toInt(),boolean)

                    temporada.addEpisode(episodio)
                }
                anime.addSeason(temporada)
            }
            datos.add(anime)

        }

        cursor.close()

        return datos

    }


}