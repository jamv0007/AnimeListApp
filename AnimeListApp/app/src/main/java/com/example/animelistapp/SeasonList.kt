package com.example.animelistapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animelistapp.Adaptadores.AdaptadorListaTemporada
import com.example.animelistapp.Adaptadores.ViewHolderListaTemporada
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.Clases.Temporada
import com.example.animelistapp.databinding.ActivitySeasonListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonList : AppCompatActivity(),BottomSheetTemporada.ModifiedSeason,ViewHolderListaTemporada.SwitchSeasonChecked {

    private lateinit var binding: ActivitySeasonListBinding//Binding a los elementos de la interfaz
    private lateinit var adaptador: AdaptadorListaTemporada//Adaptador del recycler view
    private lateinit var anime: Anime//Datos pasados del main
    private var datosMostrados: ArrayList<Pair<Int,Temporada>> = arrayListOf()//Datos mostrados en el recycler view
    private var posicionAnime: Int = -1//Posicion del dato en los datos globales del main
    private var textoBusqueda: String = ""//Texto de busqueda
    private var temporadaId: Long = 0//Id actual de la temporada
    private var capituloId: Long = 0//Id actual del capitulo
    //Responder al añadir temporada
    private val addSeasonResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == 5){
            //Se recoge numero de episodios  y se actualiza la base de datos
            val number = result.data?.getIntExtra("CHAPTERNUMBER",0)
            var episodios: ArrayList<Episodio> = arrayListOf()
            val cv = ContentValues()
            cv.put("id",temporadaId)
            cv.put("numero",anime.temporadas.size+1)
            cv.put("terminada",false)
            cv.put("anime_clave",anime.id)

            val context: Context = this
            //Se realiza inserccion en base de datos
            val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
            loadMessaje.setCancelable(false)

            loadMessaje.setView(R.layout.load_add_season_bar)

            val alert = loadMessaje.create()
            alert.show()

            GlobalScope.launch(Dispatchers.Main) {

                withContext(Dispatchers.IO){
                    UsoBase.insertar(context,"temporada",cv)


                    for(i: Int in 0 until number!!){
                        episodios.add(Episodio(capituloId,i+1,false,temporadaId))
                        val contentValues = ContentValues()
                        contentValues.put("id",capituloId)
                        contentValues.put("numero",episodios[episodios.size-1].numeroEpisodio)
                        contentValues.put("visto",episodios[episodios.size-1].visto)
                        contentValues.put("temporada_clave",temporadaId)
                        UsoBase.insertar(context,"episodio",contentValues)
                        capituloId++
                    }
                }

                alert.dismiss()

                anime.addSeason(Temporada(temporadaId,anime.temporadas.size+1,false,episodios,anime.id))
                temporadaId++

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = sharedPreferences.edit()
                editor.putLong("ID_TEMPORADA",temporadaId)
                editor.putLong("ID_EPISODIO",capituloId)
                editor.commit()

                elegirDatos()
                cambiarActivo()

                adaptador.notifyDataSetChanged()
            }

        }

    }
    //Responder de la lista de episodios
    private val episodeListResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 7){
            //Se recibe la posicion, los datos y la posicion en el view
            val posicionTemporada = result.data?.getIntExtra("TEMPORADA",0)
            val datos = result.data?.getParcelableExtra<Anime>("DATOS")
            val viewPos = result.data?.getIntExtra("VIEWPOS",0)

            //Se actualiza y se recarga la vista
            anime.temporadas[posicionTemporada!!] = datos!!.temporadas[posicionTemporada]

            cambiarActivo()
            elegirDatos()

            adaptador.notifyItemChanged(viewPos!!)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Se instancia el binding
        binding = ActivitySeasonListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setTitle(resources.getString(R.string.season));
        //Se reciben los datos de la actividad anterior(main)
        posicionAnime = intent.getIntExtra("POSITION", -1)
        anime = intent.getParcelableExtra("ELEMENT")!!
        elegirDatos()
        iniciar()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Al pulsar la flecha ahacia atras regresa los datos
        when(item.itemId){
            android.R.id.home -> {
                var intent: Intent = Intent();
                intent.putExtra("POSITION",posicionAnime);
                intent.putExtra("ELEMENT",anime);
                setResult(4,intent);
                finish();
                return true;
            }


        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        //Al poner en segundo plano la app se guardan los id
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putLong("ID_TEMPORADA",temporadaId)
        editor.putLong("ID_EPISODIO",capituloId)
        editor.commit()
    }

    override fun onResume() {
        super.onResume()
        //Al volver a primer plano se rescatan los id
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        temporadaId = sharedPreferences.getLong("ID_TEMPORADA",0)
        capituloId = sharedPreferences.getLong("ID_EPISODIO",0)
    }

    //Funcion que inicia el recycler view
    private fun iniciar(){

        val manager = LinearLayoutManager(this)
        val decoracion = DividerItemDecoration(this,manager.orientation)

        adaptador = AdaptadorListaTemporada(datosMostrados,{temporada, i, v ->  accederTemporada(temporada,i,v)},{temporada, i -> mostrarMenu(temporada,i) },this)

        binding.listaTemporadas.layoutManager = manager
        binding.listaTemporadas.adapter = adaptador
        binding.listaTemporadas.addItemDecoration(decoracion)

        binding.animeImagen.setImageURI(anime.imagen)
        binding.animeNombreView.text = anime.nombre
        binding.barraProgreso.isVisible = false

        binding.addTemporada.setOnClickListener {
            var intent: Intent = Intent(this, AddSeason::class.java)
            addSeasonResponderLauncher.launch(intent)
        }

        binding.paraBuscar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textoBusqueda = p0!!.toString()
                elegirDatos()
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
                        elegirDatos()
                        adaptador.notifyDataSetChanged()
                        return true
                    }
                }
                return false
            }
        })

        findViewById<FrameLayout>(R.id.FLTemporada).setOnTouchListener(object: View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                var imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)

                return true;
            }
        })


    }

    //Si se pulsa el switch de un elemento se modifica los el que se esta viendo actual de temporada y capitulo
    private fun cambiarActivo(){

        val cv = ContentValues()
        val cvS = ContentValues()

        var ultimoCapitulo: Int = -1
        var ultimaTemporada: Int = -1
        var contadorTemporadas: Int = 0
        for(i: Int in 0 until anime.temporadas.size){
            var contadorCapitulos: Int = 0

            cvS.put("terminada",false)
            anime.temporadas[i].finalizada = false
            cv.put("terminado",false)
            cv.put("viendo",false)
            anime.terminado = false
            anime.viendo = false

            for (j: Int in 0 until anime.temporadas[i].episodios.size){
                if(anime.temporadas[i].episodios[j].visto){
                    ultimoCapitulo = anime.temporadas[i].episodios[j].numeroEpisodio
                    ultimaTemporada = anime.temporadas[i].numeroTemporada
                    contadorCapitulos++
                }
            }
            if(contadorCapitulos == anime.temporadas[i].episodios.size){
                anime.temporadas[i].finalizada = true
                cvS.put("terminada",true)
                contadorTemporadas++
            }
            UsoBase.modificar(this,"temporada",cvS,"id="+anime.temporadas[i].id)
        }

        if(ultimaTemporada != -1 && ultimoCapitulo != -1){
            anime.temporadaActual = ultimaTemporada
            anime.episodioActual = ultimoCapitulo
            anime.viendo = true
            cv.put("temporada",ultimaTemporada)
            cv.put("capitulo",ultimoCapitulo)
            cv.put("viendo",true)
        }

        if(contadorTemporadas == anime.temporadas.size){
            anime.terminado = true
            cv.put("terminado",true)
        }

        UsoBase.modificar(this,"anime",cv,"id="+anime.id)


    }

    //Funcion que se llama al acceder a un elemento
    fun accederTemporada(temporada: Temporada,pos: Int,viewPos: Int){
        val intent = Intent(this,EpisodeList::class.java)
        intent.putExtra("DATOS",anime)
        intent.putExtra("TEMPORADA",pos)
        intent.putExtra("VIEWPOS",viewPos)
        episodeListResponderLauncher.launch(intent)
    }

    //Funcion que muestra el bottom sheet
    fun mostrarMenu(temporada: Temporada,pos: Int): Boolean{
        val bottomSheet = BottomSheetTemporada(anime,pos)
        bottomSheet.show(supportFragmentManager,"TAG");
        return true
    }

    //Funcion que cambia los datos mostrados segun la barra dse busqueda
    private fun elegirDatos(){
        datosMostrados.clear()
        for(i:Int in 0 until anime.temporadas.size){
            if(anime.temporadas[i].numeroTemporada.toString().contains(textoBusqueda)){
                datosMostrados.add(Pair(i,anime.temporadas[i]))
            }
        }
    }

    //Se llama al volver tras modificar una temporada
    override fun returnModifiedData(anime: Anime, season: Int, number: Int) {
        var currentNumber = this.anime.temporadas[season].episodios.size

        val context: Context = this
        val sl: SeasonList = this

        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
        loadMessaje.setCancelable(false)

        loadMessaje.setView(R.layout.load_modify_season_bar)

        val alert = loadMessaje.create()
        alert.show()

        GlobalScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO){
                if(number > currentNumber){
                    var difference = number - currentNumber
                    for (i: Int in 0 until difference){
                        var ep: Episodio = Episodio(capituloId,sl.anime.temporadas[season].episodios.size + 1,false,sl.anime.temporadas[season].id);
                        val contentValues = ContentValues()
                        contentValues.put("id",ep.id)
                        contentValues.put("numero",ep.numeroEpisodio)
                        contentValues.put("visto",ep.visto)
                        contentValues.put("temporada_clave",sl.anime.temporadas[season].id)
                        UsoBase.insertar(context,"episodio",contentValues)
                        sl.anime.temporadas[season].addEpisode(ep);
                        capituloId++
                    }
                }else if(number < currentNumber){
                    var difference = currentNumber - number
                    sl.anime.temporadas[season].removeRangeEpisode((currentNumber) - difference,context);
                }
            }

            alert.dismiss()
            elegirDatos()
            cambiarActivo()
            adaptador.notifyDataSetChanged()

        }





    }

    //Al pulsar el boton de atras
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            var intent: Intent = Intent();
            intent.putExtra("POSITION",posicionAnime);
            intent.putExtra("ELEMENT",anime);
            setResult(4,intent);
            finish();
        }

        return super.onKeyDown(keyCode, event)
    }

    //Al borrar un elemento
    override fun returnDeletedData(number: Int) {

        val context: Context = this
        val sl: SeasonList = this

        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
        loadMessaje.setCancelable(false)

        loadMessaje.setView(R.layout.load_delete_season_bar)

        val alert = loadMessaje.create()
        alert.show()

        GlobalScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO) {
                sl.anime.removeRangeSeason(number,context)
            }
            alert.dismiss()
            elegirDatos()
            adaptador.notifyDataSetChanged()
        }

    }

    //Al cambiar un switch de la temporada
    override fun onSwitchValueChange(value: Boolean, pos: Int,posView:Int) {

        var contexto: Context = this

        val loadMessaje: AlertDialog.Builder = AlertDialog.Builder(this)
        loadMessaje.setCancelable(false)

        loadMessaje.setView(R.layout.load_modify_season_bar)

        val alert = loadMessaje.create()
        alert.show()

        GlobalScope.launch(Dispatchers.Main) {

            withContext(Dispatchers.IO){
                val contentValues = ContentValues()
                contentValues.put("terminada",value)
                UsoBase.modificar(contexto,"temporada",contentValues,"id="+anime.temporadas[pos].id)
                val contentV = ContentValues()
                contentV.put("visto",value)
                UsoBase.modificar(contexto,"episodio",contentV,"temporada_clave="+anime.temporadas[pos].id)
                anime.temporadas[pos].finalizada = value
                for (i: Int in 0 until anime.temporadas[pos].episodios.size){
                    anime.temporadas[pos].episodios[i].visto = value
                }

                cambiarActivo()
            }

            alert.dismiss()

        }

    }
}