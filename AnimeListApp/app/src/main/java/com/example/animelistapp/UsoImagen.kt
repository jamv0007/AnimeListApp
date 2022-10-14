package com.example.animelistapp

import android.content.Context
import android.net.Uri
import java.io.File

object UsoImagen {

     fun guardarImagen(id: String, uri: Uri,context: Context){
        val archivo = File(id)
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()!!
        archivo.writeBytes(bytes)
    }

    fun cargarImagen(id: String): Uri {
        val archivo = File(id)

        return if(archivo.exists()) Uri.fromFile(archivo)
        else Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto)

    }

    fun borrarArchivo(id: String): Boolean{
        val archivo = File(id)
        return archivo.delete()
    }
}