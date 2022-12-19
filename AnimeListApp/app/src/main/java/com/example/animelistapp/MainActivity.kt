package com.example.animelistapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animelistapp.Adaptadores.AdaptadorListaAnime
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Temporada
import com.example.animelistapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*


enum class Mostrado{
    TODO,
    VIENDO,
    PENDIENTE,
    VISTO
}

class MainActivity : AppCompatActivity(), BottomSheet.BottomSheetListener{

    private var datos: ArrayList<Anime> = arrayListOf()//Datos de la aplicacion
    private var nombres: ArrayList<String> = arrayListOf()//Lista de nombres de los datos para que sean unicos
    private var datosMostrados: ArrayList<Pair<Int,Anime>> = arrayListOf()//Lista de datos mostrados con un pair de posicion en los originales y el dato
    private var mostrando: Mostrado = Mostrado.TODO//Indica que datos esta mostrando
    private lateinit var adaptador: AdaptadorListaAnime//Adaptador para el Recycler view que muestra los datos
    private lateinit var binding: ActivityMainBinding//Binding para conectar con los elementos de la vista
    private var textoBusqueda: String = ""//Texto de busqueda actual
    private var archivoSeleccionadoImportar: String = ""//Archivo a importar
    private var contadorId: Long = 0//Contador del id del anime
    private var temporadaId: Long = 0//Contador del id de temporada
    private var capituloId: Long = 0//Contador del id de capitulo

    //Responder al añadir anime
    private val addResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 2){
            //Se escoge el nombre y la imagen
            var name: String = result.data?.getStringExtra("ANIME_NAME").orEmpty();
            var image: Uri = Uri.parse(result.data?.getStringExtra("ANIME_IMAGE").orEmpty());
            //Se copia la imagen a archivos internos y se carga la url
            UsoImagen.guardarImagen(filesDir.toString() + name,image,applicationContext)
            image = UsoImagen.cargarImagen(filesDir.toString() + name)

            //Se crea el objeto y se añade
            var nuevo: Anime = Anime(contadorId,name,image,0,0,false,false, arrayListOf())
            datos.add(nuevo)

            //Se almacena en la base de datos
            val contentValues = ContentValues()
            contentValues.put("id",nuevo.id)
            contentValues.put("nombre",nuevo.nombre)
            contentValues.put("imagen",nuevo.imagen.toString())
            contentValues.put("temporada",nuevo.temporadaActual)
            contentValues.put("capitulo",nuevo.episodioActual)
            contentValues.put("viendo",nuevo.viendo)
            contentValues.put("terminado",nuevo.terminado)
            UsoBase.insertar(this,"anime",contentValues)

            //Se aumenta la id
            contadorId += 1

            //Se guandan los estados de id
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPreferences.edit()
            editor.putLong("ID_ANIME",contadorId)
            editor.commit()

            //Se recargan los datos de la vista
            selecionarDatos()
            getNombres()

        }
    }

    //Responder al ir a las vista de temporadas
    private val temporadaResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 4){
            //Se obtiene el dato y la posicion
            var anime: Anime = result.data?.getParcelableExtra<Anime>("ELEMENT")!!
            var posicion: Int = result.data?.getIntExtra("POSITION",0)!!
            //Se actualiza
            datos[posicion] = anime
            //Se actualiza la vista
            selecionarDatos()

        }
    }

    //Responder al importar un fichero de datos
    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.N)
    private val importResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK){
            //Se comprueba que sea del formato csv
            val uri: Uri = result.data!!.data!!
            archivoSeleccionadoImportar = uri.toString()
            val separador = archivoSeleccionadoImportar.split("/")
            val extension = separador[separador.size-1].split(".")
            if(extension[extension.size-1] !="csv"){
                archivoSeleccionadoImportar = ""
                Toast.makeText(applicationContext,"El archivo no tiene extension csv",Toast.LENGTH_LONG).show()
            }else{
                //Muestra el alert dialog de confirmacion
                var dialog: AlertDialog.Builder = AlertDialog.Builder(this)
                var acept: Boolean = false;

                //Al aceptar y una vez que desaparezca el mensaje
                dialog.setOnDismissListener {
                    if(acept) {
                        //Se muestra el mensaje de importando
                        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
                        loadMessaje.setCancelable(false)
                        loadMessaje.setView(R.layout.load_bar)
                        val alert = loadMessaje.create()
                        alert.show()

                        //Se lanza la corutina para realizar la importacion en otro hilo
                       GlobalScope.launch(Dispatchers.Main) {

                           withContext(Dispatchers.IO){
                               importarYCargarDatos(uri)
                           }
                           //Una vez que termina se quita el mensaje y se actualiza la UI
                           alert.dismiss()
                           selecionarDatos()
                           adaptador.notifyDataSetChanged()
                       }
                        acept = false;
                    }
                }

                dialog.setMessage(resources.getString(R.string.textImport));
                dialog.setTitle(resources.getString(R.string.impor));

                dialog.setNegativeButton(
                    Html.fromHtml("<font color='#109DFA'>"+resources.getString(R.string.cancel)+"</font>",
                        Html.FROM_HTML_MODE_LEGACY),
                    DialogInterface.OnClickListener{ dialogInterface, i ->
                })

                dialog.setPositiveButton(
                    Html.fromHtml("<font color='#EF280F'>"+resources.getString(R.string.impor)+"</font>",
                        Html.FROM_HTML_MODE_LEGACY),
                    DialogInterface.OnClickListener{ dialogInterface, b ->
                    acept = true;

                })

                dialog.show();



            }

        }
    }

    //Funcion que importa los datos
    private fun importarYCargarDatos(uri: Uri){

        //Se borran la imagenes almacenadas de los datos actuales
        for (i: Int in 0..datos.size - 1) {
            UsoImagen.borrarArchivo(filesDir.toString() + datos[i].nombre)
        }

        //Se importa los datos
        UsoBase.importarDatosCSV(applicationContext, uri, filesDir.toString())

        datos.clear()

        //Se obtienen de la base de datos
        datos = UsoBase.cargarDatos(applicationContext).clone() as ArrayList<Anime>
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        //Se instancia la conexion con la vista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.GONE
        val context: Context = this

        //Se muestra el mensaje de carga
        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
        loadMessaje.setCancelable(false)

        loadMessaje.setView(R.layout.load_data_bar)

        val alert = loadMessaje.create()
        alert.show()

        //Se lanza la subroutina para que cargue los datos de la base de datos
        GlobalScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO){
                datos = UsoBase.cargarDatos(context).clone() as ArrayList<Anime>
            }

            alert.dismiss()

            getNombres()

            selecionarDatos()

        }

        //Se asigna el adaptador
        adaptador = AdaptadorListaAnime(datosMostrados, { anime,pos -> accederAnime(anime,pos) },{anime, pos -> menuDesplegable(anime,pos) })
        //Se inicia la vista
        iniciarRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //Se añade el menu
        var inflater: MenuInflater = menuInflater

        inflater.inflate(R.menu.menu_export_import,menu)
        inflater.inflate(R.menu.menu_lista_anime,menu)

        return super.onCreateOptionsMenu(menu)

    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //se realiza una accion para cada elemento del menu
        val id = item.itemId

        when(id){

            R.id.todo -> {
                mostrando = Mostrado.TODO
                selecionarDatos()
            }
            R.id.pendientes -> {
                mostrando = Mostrado.PENDIENTE
                selecionarDatos()
            }

            R.id.viendo -> {
                mostrando = Mostrado.VIENDO
                selecionarDatos()
            }

            R.id.visto -> {
                mostrando = Mostrado.VISTO
                selecionarDatos()
            }

            R.id.exportar -> {
                UsoBase.exportarDatosCSV(applicationContext,contadorId,temporadaId,capituloId)
            }

            R.id.importar -> {


                var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = "*/*"
                chooseFile = Intent.createChooser(chooseFile, "Choose a file")
                importResponderLauncher.launch(chooseFile)


            }

        }



        return super.onOptionsItemSelected(item)
    }

    //Al poner la app en segundo plano
    override fun onPause() {
        super.onPause()
        //Se almacena el id por si se cierra
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putLong("ID_ANIME",contadorId)
        editor.commit()


    }

    //Al volver a poner la app en primer plano
    override fun onResume() {
        super.onResume()
        //Se recuperan los id
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        contadorId = sharedPreferences.getLong("ID_ANIME",0)
        temporadaId = sharedPreferences.getLong("ID_TEMPORADA",0)
        capituloId = sharedPreferences.getLong("ID_EPISODIO",0)

    }

    //Funcion para acceder dentro de un elemento
    fun accederAnime(anime: Anime,pos: Int){
        //Se lanza el intent
        val intent: Intent = Intent(applicationContext, SeasonList::class.java)
        intent.putExtra("POSITION",pos)
        intent.putExtra("ELEMENT",anime)
        temporadaResponderLauncher.launch(intent)
    }

    //Funcion para mostrar el bottom sheet al manterner presionado un elemento
    fun menuDesplegable(anime: Anime,pos: Int): Boolean{

        val bottomSheet = BottomSheet(anime,pos,nombres,filesDir)
        bottomSheet.show(supportFragmentManager,"TAG");

        return true
    }

    //Funcion para inicializar el recycler view
    private fun iniciarRecyclerView(){

        //Se inician los valores de la vista
        val manager = LinearLayoutManager(this)
        val decoracion = DividerItemDecoration(this,manager.orientation)
        binding.listaAnime.layoutManager = manager
        binding.listaAnime.adapter = adaptador
        binding.listaAnime.addItemDecoration(decoracion)
        binding.addnuevoanime.setOnClickListener{
            val intent = Intent(this, AddAnime::class.java)
            intent.putExtra("MODIFICANDO",false)
            intent.putExtra("NOMBRES",nombres);
            addResponderLauncher.launch(intent)
        }

        //Se asignan los listener al cuadro de busqueda
        //Al escribir
        binding.paraBuscar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textoBusqueda = p0!!.toString()
                selecionarDatos()
                adaptador.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        //Al tocar en el boton x para vaciar el texto
        binding.paraBuscar.setOnTouchListener(object: View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if(p1!!.action == MotionEvent.ACTION_UP){
                    if(p1!!.rawX >= binding.paraBuscar.right - binding.paraBuscar.compoundDrawables[2].bounds.width()){
                        textoBusqueda = ""
                        binding.paraBuscar.setText("")
                        selecionarDatos()
                        adaptador.notifyDataSetChanged()
                        return true
                    }
                }
                return false
            }
        })

        //Al pulsar fuera del cuadro
        findViewById<FrameLayout>(R.id.FLAnime).setOnTouchListener(object: View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                var imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)

                return true;
            }
        })


    }

    //Funcion que selecciona los datos segun lo que este marcado para mostrar
    private fun selecionarDatos(){

        when(mostrando){

            Mostrado.TODO->{
                copiarTodo()
            }
            Mostrado.PENDIENTE->{
                copiarPendientes()
            }
            Mostrado.VIENDO->{
                copiarViendo()
            }
            Mostrado.VISTO->{
                copiarVisto()
            }
        }

        adaptador.notifyDataSetChanged()

    }

    //Funcion que muestra todos los registros y filtra por el texto de busqueda
    private fun copiarTodo(){

        datosMostrados.clear()
        for((ind, i: Anime) in datos.withIndex()){
            if(i.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                datosMostrados.add(Pair(ind, i))
            }
        }

    }

    //Funcion que muestra los registros pendiente y filtra por el texto de busqueda
    private fun copiarPendientes(){

        datosMostrados.clear()
        for((ind,i: Anime) in datos.withIndex()){
            if(!i.viendo && !i.terminado) {
                if(i.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                    datosMostrados.add(Pair(ind, i))
                }
            }
        }
    }

    //Funcion que muestra los registros de viendo y filtra por el texto de busqueda
    private fun copiarViendo(){

        datosMostrados.clear()
        for((ind,i: Anime) in datos.withIndex()){
            if(i.viendo && !i.terminado) {
                if(i.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                    datosMostrados.add(Pair(ind, i))
                }
            }
        }
    }
    //Funcion que muestra los registros de visto y filtra por el texto de busqueda
    private fun copiarVisto(){

        datosMostrados.clear()
        for((ind,i: Anime) in datos.withIndex()){
            if(i.terminado) {
                if(i.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                    datosMostrados.add(Pair(ind, i))
                }
            }
        }
    }

    //Funcion que itera por los elementos y rellena el vector de nombres con los valores
    private fun getNombres(){
        nombres.clear()
        for(i: Anime in datos){
            nombres.add(i.nombre)
        }

    }

    //Funcion sobrescrita de la interfaz Bottom sheet que se llama cuando se pulsa en el boton de modificar un anime
    override fun onChangeButtonClicked(anime: Anime, pos: Int) {
        //Se actualizan los valores
        datos[pos].nombre = anime.nombre
        datos[pos].imagen = Uri.parse(anime.imagen.toString())
        //Se actualiza la vista
        selecionarDatos()
        getNombres()
        //Se actualiza la base de datos
        val contentValues = ContentValues()
        contentValues.put("nombre",anime.nombre)
        contentValues.put("imagen",anime.imagen.toString())
        UsoBase.modificar(this,"anime",contentValues,"id="+anime.id)

        binding.listaAnime.adapter = null
        binding.listaAnime.adapter = adaptador

        adaptador.notifyDataSetChanged()
    }

    //Funcion sobrescrita de la interfaz Bottom Sheet que se llama cuando se pulsa el boton de borrar un anime
    override fun onDeleteButtonClicked(anime: Anime, pos: Int) {

        //Se borran los episodios de cada temporada de la base de datos
        for(i: Temporada in anime.temporadas){
            UsoBase.borrar(this,"episodio","temporada_clave="+i.id)
        }
        //Se borran las temporadas y el registro del anime
        UsoBase.borrar(this,"temporada","anime_clave="+anime.id)
        UsoBase.borrar(this,"anime","id="+datos[pos].id)
        //Se borra el archivo de la imagen de los ficheros internos
        UsoImagen.borrarArchivo(filesDir.toString() + datos[pos].nombre)
        //Se elimina y se actualiza la vista
        datos.removeAt(pos)
        selecionarDatos()
        getNombres()

    }


}