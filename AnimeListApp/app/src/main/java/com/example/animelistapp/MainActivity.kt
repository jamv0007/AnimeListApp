package com.example.animelistapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animelistapp.Adaptadores.AdaptadorListaAnime
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Temporada
import com.example.animelistapp.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.Manifest


enum class Mostrado{
    TODO,
    VIENDO,
    PENDIENTE,
    VISTO
}

class MainActivity : AppCompatActivity(), BottomSheet.BottomSheetListener{

    private var datos: ArrayList<Anime> = arrayListOf()
    private var nombres: ArrayList<String> = arrayListOf()
    private var datosMostrados: ArrayList<Pair<Int,Anime>> = arrayListOf()
    private var mostrando: Mostrado = Mostrado.TODO
    private lateinit var adaptador: AdaptadorListaAnime
    private lateinit var binding: ActivityMainBinding
    private var textoBusqueda: String = ""
    private var archivoSeleccionadoImportar: String = ""
    private var contadorId: Long = 0
    private var temporadaId: Long = 0
    private var capituloId: Long = 0
    private val addResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 2){
            var name: String = result.data?.getStringExtra("ANIME_NAME").orEmpty();
            var image: Uri = Uri.parse(result.data?.getStringExtra("ANIME_IMAGE").orEmpty());

            UsoImagen.guardarImagen(filesDir.toString() + name,image,applicationContext)
            image = UsoImagen.cargarImagen(filesDir.toString() + name)



            var nuevo: Anime = Anime(contadorId,name,image,0,0,false,false, arrayListOf())
            datos.add(nuevo)


            val contentValues = ContentValues()
            contentValues.put("id",nuevo.id)
            contentValues.put("nombre",nuevo.nombre)
            contentValues.put("imagen",nuevo.imagen.toString())
            contentValues.put("temporada",nuevo.temporadaActual)
            contentValues.put("capitulo",nuevo.episodioActual)
            contentValues.put("viendo",nuevo.viendo)
            contentValues.put("terminado",nuevo.terminado)


            UsoBase.insertarAnime(this,"anime",contentValues)

            contadorId += 1

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPreferences.edit()
            editor.putLong("ID_ANIME",contadorId)
            editor.commit()

            selecionarDatos()
            getNombres()

        }
    }

    private val temporadaResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 4){
            var anime: Anime = result.data?.getParcelableExtra<Anime>("ELEMENT")!!
            var posicion: Int = result.data?.getIntExtra("POSITION",0)!!

            datos[posicion] = anime

            selecionarDatos()

        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.N)
    private val importResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK){
            val uri: Uri = result.data!!.data!!
            archivoSeleccionadoImportar = uri.toString()
            val separador = archivoSeleccionadoImportar.split("/")
            val extension = separador[separador.size-1].split(".")
            if(extension[extension.size-1] !="csv"){
                archivoSeleccionadoImportar = ""
                Toast.makeText(applicationContext,"El archivo no tiene extension csv",Toast.LENGTH_LONG).show()
            }else{

                var dialog: AlertDialog.Builder = AlertDialog.Builder(this)
                var acept: Boolean = false;

                dialog.setOnDismissListener {
                    if(acept) {

                        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
                        loadMessaje.setCancelable(false)
                        loadMessaje.setView(R.layout.load_bar)
                        val alert = loadMessaje.create()
                        alert.show()

                       GlobalScope.launch(Dispatchers.Main) {

                           withContext(Dispatchers.IO){
                               importarYCargarDatos(uri)
                           }

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


    private fun importarYCargarDatos(uri: Uri){

        for (i: Int in 0..datos.size - 1) {
            UsoImagen.borrarArchivo(filesDir.toString() + datos[i].nombre)
        }

        UsoBase.importarDatosCSV(applicationContext, uri, filesDir.toString())

        datos.clear()

        datos = UsoBase.cargarDatos(applicationContext).clone() as ArrayList<Anime>
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.GONE
        val context: Context = this

        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
        loadMessaje.setCancelable(false)

        loadMessaje.setView(R.layout.load_data_bar)

        val alert = loadMessaje.create()
        alert.show()

        GlobalScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO){
                datos = UsoBase.cargarDatos(context).clone() as ArrayList<Anime>
            }

            alert.dismiss()

            getNombres()

            selecionarDatos()

        }

        adaptador = AdaptadorListaAnime(datosMostrados, { anime,pos -> accederAnime(anime,pos) },{anime, pos -> menuDesplegable(anime,pos) })
        iniciarRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        var inflater: MenuInflater = menuInflater

        inflater.inflate(R.menu.menu_export_import,menu)
        inflater.inflate(R.menu.menu_lista_anime,menu)

        return super.onCreateOptionsMenu(menu)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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

    override fun onPause() {
        super.onPause()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putLong("ID_ANIME",contadorId)
        editor.commit()


    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        contadorId = sharedPreferences.getLong("ID_ANIME",0)
        temporadaId = sharedPreferences.getLong("ID_TEMPORADA",0)
        capituloId = sharedPreferences.getLong("ID_EPISODIO",0)

    }

    fun accederAnime(anime: Anime,pos: Int){
        val intent: Intent = Intent(applicationContext, SeasonList::class.java)
        intent.putExtra("POSITION",pos)
        intent.putExtra("ELEMENT",anime)
        temporadaResponderLauncher.launch(intent)
    }

    fun menuDesplegable(anime: Anime,pos: Int): Boolean{

        val bottomSheet = BottomSheet(anime,pos,nombres,filesDir)
        bottomSheet.show(supportFragmentManager,"TAG");

        return true
    }

    private fun iniciarRecyclerView(){

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

        findViewById<FrameLayout>(R.id.FLAnime).setOnTouchListener(object: View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                var imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)

                return true;
            }
        })


    }

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

    private fun copiarTodo(){

        datosMostrados.clear()
        for((ind, i: Anime) in datos.withIndex()){
            if(i.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                datosMostrados.add(Pair(ind, i))
            }
        }

    }

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

    private fun getNombres(){
        nombres.clear()
        for(i: Anime in datos){
            nombres.add(i.nombre)
        }

    }





    override fun onChangeButtonClicked(anime: Anime, pos: Int) {
        datos[pos].nombre = anime.nombre
        datos[pos].imagen = Uri.parse(anime.imagen.toString())


        //UsoImagen.guardarImagen(filesDir.toString() + anime.nombre,anime.imagen,applicationContext)
        selecionarDatos()
        getNombres()
        val contentValues = ContentValues()
        contentValues.put("nombre",anime.nombre)
        contentValues.put("imagen",anime.imagen.toString())
        UsoBase.modificarAnime(this,"anime",contentValues,"id="+anime.id)

        binding.listaAnime.adapter = null
        binding.listaAnime.adapter = adaptador

        adaptador.notifyDataSetChanged()
    }

    override fun onDeleteButtonClicked(anime: Anime, pos: Int) {

        for(i: Temporada in anime.temporadas){
            UsoBase.borrarAnime(this,"episodio","temporada_clave="+i.id)
        }
        UsoBase.borrarAnime(this,"temporada","anime_clave="+anime.id)
        UsoBase.borrarAnime(this,"anime","id="+datos[pos].id)
        UsoImagen.borrarArchivo(filesDir.toString() + datos[pos].nombre)
        datos.removeAt(pos)
        selecionarDatos()
        getNombres()

    }


}