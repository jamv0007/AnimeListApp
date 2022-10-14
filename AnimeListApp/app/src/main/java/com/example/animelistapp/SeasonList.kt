package com.example.animelistapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.Toast
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

class SeasonList : AppCompatActivity(),BottomSheetTemporada.ModifiedSeason,ViewHolderListaTemporada.SwitchSeasonChecked {

    private lateinit var binding: ActivitySeasonListBinding
    private lateinit var adaptador: AdaptadorListaTemporada
    private lateinit var anime: Anime
    private var datosMostrados: ArrayList<Pair<Int,Temporada>> = arrayListOf()
    private var posicionAnime: Int = -1
    private var textoBusqueda: String = ""
    private var temporadaId: Long = 0
    private var capituloId: Long = 0
    private val addSeasonResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == 5){
            val number = result.data?.getIntExtra("CHAPTERNUMBER",0)
            var episodios: ArrayList<Episodio> = arrayListOf()
            val cv = ContentValues()
            cv.put("id",temporadaId)
            cv.put("numero",anime.temporadas.size+1)
            cv.put("terminada",false)
            cv.put("anime_clave",anime.id)
            UsoBase.insertarAnime(this,"temporada",cv)


            for(i: Int in 0 until number!!){
                episodios.add(Episodio(capituloId,i+1,false))
                val contentValues = ContentValues()
                contentValues.put("id",capituloId)
                contentValues.put("numero",episodios[episodios.size-1].numeroEpisodio)
                contentValues.put("visto",episodios[episodios.size-1].visto)
                contentValues.put("temporada_clave",temporadaId)
                UsoBase.insertarAnime(this,"episodio",contentValues)
                capituloId++
            }

            anime.addSeason(Temporada(temporadaId,anime.temporadas.size+1,false,episodios))
            temporadaId++

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPreferences.edit()
            editor.putLong("ID_TEMPORADA",temporadaId)
            editor.putLong("ID_EPISODIO",capituloId)
            editor.commit()

            elegirDatos()
            cambiarActivo()

            adaptador.notifyDataSetChanged()

        }

    }
    private val episodeListResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == 7){
            val posicionTemporada = result.data?.getIntExtra("TEMPORADA",0)
            val datos = result.data?.getParcelableExtra<Anime>("DATOS")
            val viewPos = result.data?.getIntExtra("VIEWPOS",0)

            anime.temporadas[posicionTemporada!!] = datos!!.temporadas[posicionTemporada]

            cambiarActivo()
            elegirDatos()

            adaptador.notifyItemChanged(viewPos!!)

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeasonListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setTitle(resources.getString(R.string.season));
        posicionAnime = intent.getIntExtra("POSITION", -1)
        anime = intent.getParcelableExtra("ELEMENT")!!
        elegirDatos()
        iniciar()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putLong("ID_TEMPORADA",temporadaId)
        editor.putLong("ID_EPISODIO",capituloId)
        editor.commit()
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        temporadaId = sharedPreferences.getLong("ID_TEMPORADA",0)
        capituloId = sharedPreferences.getLong("ID_EPISODIO",0)
    }

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
            UsoBase.modificarAnime(this,"temporada",cvS,"id="+anime.temporadas[i].id)
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

        UsoBase.modificarAnime(this,"anime",cv,"id="+anime.id)


    }

    fun accederTemporada(temporada: Temporada,pos: Int,viewPos: Int){
        val intent = Intent(this,EpisodeList::class.java)
        intent.putExtra("DATOS",anime)
        intent.putExtra("TEMPORADA",pos)
        intent.putExtra("VIEWPOS",viewPos)
        episodeListResponderLauncher.launch(intent)
    }

    fun mostrarMenu(temporada: Temporada,pos: Int): Boolean{
        val bottomSheet = BottomSheetTemporada(anime,pos)
        bottomSheet.show(supportFragmentManager,"TAG");
        return true
    }

    private fun elegirDatos(){
        datosMostrados.clear()
        for(i:Int in 0 until anime.temporadas.size){
            if(anime.temporadas[i].numeroTemporada.toString().contains(textoBusqueda)){
                datosMostrados.add(Pair(i,anime.temporadas[i]))
            }
        }
    }

    override fun returnModifiedData(anime: Anime, season: Int, number: Int) {
        var currentNumber = this.anime.temporadas[season].episodios.size

        if(number > currentNumber){
            var difference = number - currentNumber
            for (i: Int in 0 until difference){
                var ep: Episodio = Episodio(capituloId,this.anime.temporadas[season].episodios.size + 1,false);
                val contentValues = ContentValues()
                contentValues.put("id",ep.id)
                contentValues.put("numero",ep.numeroEpisodio)
                contentValues.put("visto",ep.visto)
                contentValues.put("temporada_clave",this.anime.temporadas[season].id)
                UsoBase.insertarAnime(this,"episodio",contentValues)
                this.anime.temporadas[season].addEpisode(ep);
                capituloId++
            }
        }else if(number < currentNumber){
            var difference = currentNumber - number
            println(difference)
            this.anime.temporadas[season].removeRangeEpisode((currentNumber) - difference,this);
        }

        elegirDatos()
        cambiarActivo()
        adaptador.notifyDataSetChanged()

    }

    override fun returnDeletedData(number: Int) {
        this.anime.removeRangeSeason(number,this)
        elegirDatos()
        adaptador.notifyDataSetChanged()
    }

    override fun onSwitchValueChange(value: Boolean, pos: Int,posView:Int) {

        val contentValues = ContentValues()
        contentValues.put("terminada",value)
        UsoBase.modificarAnime(this,"temporada",contentValues,"id="+anime.temporadas[pos].id)
        val contentV = ContentValues()
        contentV.put("visto",value)
        UsoBase.modificarAnime(this,"episodio",contentV,"temporada_clave="+anime.temporadas[pos].id)
        anime.temporadas[pos].finalizada = value
        for (i: Int in 0 until anime.temporadas[pos].episodios.size){
            anime.temporadas[pos].episodios[i].visto = value
        }

        cambiarActivo()


    }
}