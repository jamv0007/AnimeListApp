package com.example.animelistapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.Clases.Temporada
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

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

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        val cursor: Cursor = base.rawQuery("select * from anime",null)
        val cursor2: Cursor = base.rawQuery("select * from temporada",null)
        val cursor3: Cursor = base.rawQuery("select * from episodio",null)
        val animeLista: ArrayList<Anime> = arrayListOf()
        val temporadaLista: ArrayList<Temporada> = arrayListOf()
        val episodioLista: ArrayList<Episodio> = arrayListOf()


        while (cursor3.moveToNext()){
            var boolean = false
            if(cursor3.getString(2).toInt() == 1){
                boolean = true
            }
            val episodio: Episodio = Episodio(cursor3.getString(0).toLong(), cursor3.getString(1).toInt(),boolean, cursor3.getString(3).toLong())
            episodioLista.add(episodio)

        }

        while (cursor2.moveToNext()){
            var boolean2 = false
            if(cursor2.getString(2).toInt() == 1){
                boolean2 = true
            }

            val episodios = episodioLista.filter { it.temporadaId == cursor2.getString(0).toLong() }

            val temporada: Temporada = Temporada(cursor2.getString(0).toLong(),cursor2.getString(1).toInt(),
                boolean2, episodios.toCollection(ArrayList<Episodio>()),cursor2.getString(3).toLong())
            temporadaLista.add(temporada);

        }



        while (cursor.moveToNext()) {

            var viendo = false
            if (cursor.getString(5).toInt() == 1) {
                viendo = true
            }

            var terminado = false
            if (cursor.getString(6).toInt() == 1) {
                terminado = true
            }

            val temporadas = temporadaLista.filter { it.animeId == cursor.getString(0).toLong() }

            val anime = Anime(cursor.getString(0).toLong(),cursor.getString(1),
                Uri.parse(cursor.getString(2)),cursor.getString(3).toInt(),cursor.getString(4).toInt(), viendo,terminado,
                temporadas.toCollection(ArrayList<Temporada>()))
            animeLista.add(anime)

        }


        cursor.close()
        cursor2.close()
        cursor3.close()


        return animeLista;

    }

    public fun exportarDatosCSV(context: Context,idAnime: Long, idTemporada: Long, idCapitulo: Long,localizacion: String){


        val carpeta: File = File(localizacion + "/AnimeAppList")

        var creada: Boolean = false
        if(!carpeta.exists()){
            creada = carpeta.mkdir()
        }

        val archivo: String = carpeta.toString() + "/DatosAplicacion.csv"

        val file: File = File(archivo)

        file.createNewFile()

        val filewriter: FileWriter = FileWriter(archivo)
        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        val cursor: Cursor = base.rawQuery("select * from anime",null)
        
        filewriter.append(idAnime.toString())
        filewriter.append("\n")
        while (cursor.moveToNext()){
            filewriter.append(cursor.getString(0))//id
            filewriter.append(",")
            filewriter.append(cursor.getString(1))//nombre
            filewriter.append(",")
            filewriter.append(cursor.getString(3))//temporadas
            filewriter.append(",")
            filewriter.append(cursor.getString(4))//capitulo
            filewriter.append(",")
            filewriter.append(cursor.getString(5))//viendo
            filewriter.append(",")
            filewriter.append(cursor.getString(6))//terminado
            filewriter.append("\n")
        }

        filewriter.append(idTemporada.toString())
        filewriter.append("\n")

        val cursor2: Cursor = base.rawQuery("select * from temporada",null)
        while (cursor2.moveToNext()){
            filewriter.append(cursor2.getString(0))
            filewriter.append(",")
            filewriter.append(cursor2.getString(1))
            filewriter.append(",")
            filewriter.append(cursor2.getString(2))
            filewriter.append(",")
            filewriter.append(cursor2.getString(3))
            filewriter.append("\n")
        }

        filewriter.append(idCapitulo.toString())
        filewriter.append("\n")
        val cursor3: Cursor = base.rawQuery("select * from episodio",null)
        while (cursor3.moveToNext()){
            filewriter.append(cursor3.getString(0))
            filewriter.append(",")
            filewriter.append(cursor3.getString(1))
            filewriter.append(",")
            filewriter.append(cursor3.getString(2))
            filewriter.append(",")
            filewriter.append(cursor3.getString(3))
            filewriter.append("\n")
        }

        cursor.close()
        cursor2.close()
        cursor3.close()

        filewriter.close()

        base.close()

        Toast.makeText(context,"Se ha exportado a " + archivo,Toast.LENGTH_LONG).show()

    }

    public fun importarDatosCSV(contexto: Context, ruta: Uri,localizacionImagen: String): ImportedData{

        //Hay que borrar la base y archivos. Despues se inserta en la base y se hace peticion a la base.

        //Se abre conexion con la base de datos
        val admin = AdminSQLite(contexto,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        //Se borran todos los registros
        base.delete("anime",null,null)
        base.delete("temporada",null,null)
        base.delete("episodio",null,null)

        base.close()

        //Se lee el fichero
        var linea: String = "Inicio"
        var fileReader: FileReader? = null

        copiarArchivo(ruta,localizacionImagen + "copiaArchivo.csv",contexto)

        var cont: Int = 0
        var ids: MutableList<Long> = mutableListOf(0,0,0)

        try {
            fileReader = FileReader(localizacionImagen + "copiaArchivo.csv")
            var buffer: BufferedReader = BufferedReader(fileReader)

            linea = buffer.readLine();
            while (!linea.isEmpty()){

                val separador = linea.split(",")
                if(separador.size == 1){
                    ids[cont] = separador[0].toLong()
                    cont++
                }else{
                    when(cont){
                        1 -> {
                           //Es anime
                            val content: ContentValues = ContentValues()
                            content.put("id",separador[0].toLong())
                            content.put("nombre",separador[1])
                            var uri: Uri = Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto)
                            UsoImagen.guardarImagen(localizacionImagen + separador[1],uri,contexto)
                            uri = UsoImagen.cargarImagen(localizacionImagen + separador[1])
                            content.put("imagen",uri.toString())
                            content.put("temporada",separador[2].toInt())
                            content.put("capitulo",separador[3].toInt())
                            content.put("viendo",separador[4].toInt())
                            content.put("terminado",separador[5].toInt())
                            UsoBase.insertarAnime(contexto,"anime",content)

                        }

                        2 -> {
                            //Es temporada
                            val content: ContentValues = ContentValues()
                            content.put("id",separador[0].toLong())
                            content.put("numero",separador[1].toInt())
                            content.put("terminada",separador[2].toInt())
                            content.put("anime_clave",separador[3].toLong())
                            UsoBase.insertarAnime(contexto,"temporada",content)

                        }

                        3 -> {
                            //Es capitulo
                            val content: ContentValues = ContentValues()
                            content.put("id",separador[0].toLong())
                            content.put("numero",separador[1].toInt())
                            content.put("visto",separador[2].toInt())
                            content.put("temporada_clave",separador[3].toLong())
                            UsoBase.insertarAnime(contexto,"episodio",content)
                        }
                    }
                }

                var boolean = false;
                buffer.readLine()?.let {
                    linea = it
                    boolean = true;
                }

                if(!boolean){
                    linea = "";
                }

            }
        }catch (e: java.lang.Exception){
            println(e)
        }

        borrarArchivo(localizacionImagen + "copiaArchivo.csv")

        return ImportedData(ids[0],ids[1],ids[2])
    }

    private fun copiarArchivo(ruta: Uri,directorio: String,context: Context){

        val archivo = File(directorio)
        val bytes = context.contentResolver.openInputStream(ruta)?.readBytes()!!
        archivo.writeBytes(bytes)

    }

    private fun borrarArchivo(ruta: String){
        val file: File = File(ruta)
        file.delete()
    }


}