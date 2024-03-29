package com.example.animelistapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.Clases.Temporada
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object UsoBase {

    /***
     * Funcion para insertar en la base de datos
     * @param context Contexto de la aplicacion
     * @param tabla Tabla
     * @param content Content Values con los datos de cada columna
     */
    public fun insertar(context: Context, tabla: String, content: ContentValues){

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.insert(tabla,null,content)
        base.close()
        admin.close()

    }

    /***
     * Funcion para borrar de una tabla
     * @param tabla String con la tabla donde borrar
     * @param clausula String con la clausula para borrar en esa tabla
     */
    public fun borrar(context: Context, tabla: String, clausula: String){

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.delete(tabla,clausula,null)
        base.close()
        admin.close()
    }

    /***
     * Funcion para modificar contenido de una tabla
     * @param context Contexto de la aplicacion
     * @param tabla Tabla para modificar
     * @param content Content Value con los datos de las columnas a modificar
     * @param clausula Clausula para modificar los datos
     */
    public fun modificar(context: Context, tabla: String, content: ContentValues, clausula: String){
        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase
        base.update(tabla,content,clausula,null)
        base.close()
        admin.close()
    }


    /***
     * Funcion que carga los datos desde la base de datos
     * @param context Contexto de la aplicacion
     * @throws ArrayList<Anime> Datos de la base de datos
     */
    public fun cargarDatos(context: Context): ArrayList<Anime>{

        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        //Se extraen los datos
        val cursor: Cursor = base.rawQuery("select * from anime",null)
        val cursor2: Cursor = base.rawQuery("select * from temporada",null)
        val cursor3: Cursor = base.rawQuery("select * from episodio",null)
        val animeLista: ArrayList<Anime> = arrayListOf()
        val temporadaLista: ArrayList<Temporada> = arrayListOf()
        val episodioLista: ArrayList<Episodio> = arrayListOf()

        //Para cada episodio se añade a la lista
        while (cursor3.moveToNext()){
            var boolean = false
            if(cursor3.getString(2).toInt() == 1){
                boolean = true
            }
            val episodio: Episodio = Episodio(cursor3.getString(0).toLong(), cursor3.getString(1).toInt(),boolean, cursor3.getString(3).toLong())
            episodioLista.add(episodio)

        }

        //Para cada temporada se añade a la lista
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


        //Se añade a la lista cada anime
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
        admin.close()

        return animeLista;

    }

    /***
     * Funcion que exporta los datos a csv
     * @param context Contexto de la aplicacion
     * @param idAnime El id actual por el que esta el anime añadiendo
     * @param idTemporada El id actual por el que esta la temporada añadiendo
     * @param idCapitulo El id por el que esta el capitulo añadiendo
     */
    public fun exportarDatosCSV(context: Context,idAnime: Long, idTemporada: Long, idCapitulo: Long){

        //Se crea el archivo en documentos
        var root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        var file = File(root,"DatosApp.csv")
        val filewriter: FileWriter = FileWriter(file)

        //Se accede a la base
        val admin = AdminSQLite(context,"administracion",null,1)
        val base: SQLiteDatabase = admin.writableDatabase

        val cursor: Cursor = base.rawQuery("select * from anime",null)

        //Se itera y se añade el id y los datos en filas
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
        admin.close()



        Toast.makeText(context,"Se ha exportado al directorio de Documentos",Toast.LENGTH_LONG).show()

    }

    /***
     * Funcion para importar los datos de csv
     * @param contexto Contexto aplicacion
     * @param ruta Ruta donde esta el csv
     * @param localizacionImagen Lugar donde se guardan las imagenes
     * @throws ImportedData Los ids de los elementos
     */
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
        admin.close()

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
                            UsoBase.insertar(contexto,"anime",content)

                        }

                        2 -> {
                            //Es temporada
                            val content: ContentValues = ContentValues()
                            content.put("id",separador[0].toLong())
                            content.put("numero",separador[1].toInt())
                            content.put("terminada",separador[2].toInt())
                            content.put("anime_clave",separador[3].toLong())
                            UsoBase.insertar(contexto,"temporada",content)

                        }

                        3 -> {
                            //Es capitulo
                            val content: ContentValues = ContentValues()
                            content.put("id",separador[0].toLong())
                            content.put("numero",separador[1].toInt())
                            content.put("visto",separador[2].toInt())
                            content.put("temporada_clave",separador[3].toLong())
                            UsoBase.insertar(contexto,"episodio",content)
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

    /***
     * Funcion que copia un archivo de un lugar a otro
     * @param ruta Ruta del archivo
     * @param directorio Directorio a copiar
     * @param context Contexto de la aplicacion
     */
    private fun copiarArchivo(ruta: Uri,directorio: String,context: Context){

        val archivo = File(directorio)
        val bytes = context.contentResolver.openInputStream(ruta)?.readBytes()!!
        archivo.writeBytes(bytes)

    }

    /***
     * Funcion que borra un archivo
     * @param ruta Ruta del archivo
     */
    private fun borrarArchivo(ruta: String){
        val file: File = File(ruta)
        file.delete()
    }


}