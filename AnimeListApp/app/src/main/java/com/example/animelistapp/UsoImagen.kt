package com.example.animelistapp

import android.content.Context
import android.net.Uri
import java.io.File

object UsoImagen {


    /***
     * Funcion que guarda una imagen en en otra localizacion
     * @param id Localizacion + nombre unico
     * @param uri Localizacion de la imagen original
     * @param context Contexto
     */
     fun guardarImagen(id: String, uri: Uri,context: Context){
        val archivo = File(id)
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()!!
        archivo.writeBytes(bytes)
    }

    /***
     * Funcion que carga la imagen dada la id
     * @param id Id con el que se guardo la imagen
     * @throws Uri Una Uri con la localizacion de la imagen
     */
    fun cargarImagen(id: String): Uri {
        val archivo = File(id)

        //Si existe devuelve la imagen sino la de por defecto
        return if(archivo.exists()) Uri.fromFile(archivo)
        else Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto)

    }

    /***
     * Funcion que borra la imagen
     * @param id Id de la imagen
     * @throws Boolean Si se ha borrado o no
     */
    fun borrarArchivo(id: String): Boolean{
        val archivo = File(id)
        return archivo.delete()
    }
}