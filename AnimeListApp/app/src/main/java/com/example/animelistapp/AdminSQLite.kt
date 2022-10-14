package com.example.animelistapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AdminSQLite(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {


    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("create table anime(id long primary key,nombre text, imagen text, temporada int,capitulo int, viendo int, terminado int)")
        p0?.execSQL("create table temporada(id long primary key, numero int, terminada int, anime_clave long, foreign key(anime_clave) references anime(id))")
        p0?.execSQL("create table episodio(id long primary key, numero int, visto int, temporada_clave long, foreign key(temporada_clave) references temporada(id))")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}