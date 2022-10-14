package com.example.animelistapp

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animelistapp.Adaptadores.AdaptadorListaEpisodio
import com.example.animelistapp.Adaptadores.ViewHolderListaEpisodio
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.Clases.Episodio
import com.example.animelistapp.databinding.ActivityEpisodeListBinding

class EpisodeList : AppCompatActivity(),ViewHolderListaEpisodio.CheckSwitch {

    private lateinit var binding: ActivityEpisodeListBinding
    private lateinit var anime: Anime
    private lateinit var adaptador: AdaptadorListaEpisodio
    private var datosMostrados: ArrayList<Pair<Int,Episodio>> = arrayListOf()
    private var posicionTemporada: Int = -1
    private var posicionVista: Int = -1
    private var textoBusqueda: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setTitle(resources.getString(R.string.episodes));
        iniciar()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        /*menuInflater.inflate(R.menu.menu_lista_episodio,menu)


        val search = menu?.findItem(R.id.buscarepisodio)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search)
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            /***
             * When the user is change the search text
             */
            override fun onQueryTextChange(p0: String?): Boolean {
                textoBusqueda = p0!!
                elegirDatos()
                adaptador.notifyDataSetChanged()
                return false
            }

            /***
             * User type and press enter at the search bar
             */
            override fun onQueryTextSubmit(p0: String?): Boolean {
                searchView.clearFocus()
                return false
            }


        })*/

        


        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> {
                var intent: Intent = Intent();
                intent.putExtra("TEMPORADA",posicionTemporada);
                intent.putExtra("DATOS",anime);
                intent.putExtra("VIEWPOS",posicionVista)
                setResult(7,intent);
                finish();
                return true;
            }


        }

        return super.onOptionsItemSelected(item)
    }

    private fun iniciar(){
        anime = intent.getParcelableExtra("DATOS")!!
        posicionTemporada = intent.getIntExtra("TEMPORADA",0)
        posicionVista = intent.getIntExtra("VIEWPOS",0)
        binding.animeEpisodeImage.setImageURI(anime.imagen)
        binding.animeEpisodeNameView.text = anime.nombre
        binding.epListTempoNumber.text = "Temporada " + anime.temporadas[posicionTemporada].numeroTemporada

        elegirDatos()

        adaptador = AdaptadorListaEpisodio(datosMostrados,this)
        val manager = LinearLayoutManager(this)
        val decoracion = DividerItemDecoration(this,manager.orientation)
        binding.episodeList.adapter = adaptador
        binding.episodeList.layoutManager = manager
        binding.episodeList.addItemDecoration(decoracion)

        binding.paraBuscar.addTextChangedListener(object: TextWatcher{
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

        findViewById<FrameLayout>(R.id.FLEpisodios).setOnTouchListener(object: View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                var imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)

                return true;
            }
        })


    }



    private fun elegirDatos(){

        datosMostrados.clear()
        for(i: Int in 0 until anime.temporadas[posicionTemporada].episodios.size){
            if(anime.temporadas[posicionTemporada].episodios[i].numeroEpisodio.toString().contains(textoBusqueda)){
                datosMostrados.add(Pair(i,anime.temporadas[posicionTemporada].episodios[i]))
                //println("i: " + i + " V: " + anime.temporadas[posicionTemporada].episodios[i].visto)
            }
        }



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

    override fun onSwitchChange(valor: Boolean, pos: Int,posView: Int) {
        anime.temporadas[posicionTemporada].episodios[pos].visto = valor
        cambiarActivo()
        val cv = ContentValues()
        cv.put("visto",valor)
        UsoBase.modificarAnime(this,"episodio",cv,"id="+anime.temporadas[posicionTemporada].episodios[pos].id)

    }
}