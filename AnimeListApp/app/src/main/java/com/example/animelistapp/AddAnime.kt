package com.example.animelistapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.animelistapp.Clases.Anime
import com.example.animelistapp.databinding.ActivityAddAnimeBinding
import com.example.animelistapp.databinding.ActivityMainBinding

class AddAnime : AppCompatActivity() {

    private lateinit var binding: ActivityAddAnimeBinding
    private lateinit var nombres: ArrayList<String>
    private lateinit var anime: Anime
    private var ruta: Uri = Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto)
    private var modificando: Boolean = false
    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK && result.data?.data != null){
            val image: Uri = result.data!!.data!!;
            binding.animeImagen.setImageURI(image)
            ruta = image
        }else{
            binding.animeImagen.setImageURI(Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto))

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setTitle(resources.getString(R.string.addAnime));
        iniciar()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        var inflater: MenuInflater = menuInflater;
        inflater.inflate(R.menu.add_menu,menu);

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            android.R.id.home -> {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }

            R.id.confirm -> {
                if(!binding.animeNombre.text.isEmpty()){
                    var intent: Intent = Intent();
                    val text = binding.animeNombre.text.toString();

                    if(modificando){
                        //Si se ha modificado, si se ha cambiado
                        if(text != anime.nombre){
                            if(isUsed(text,anime.nombre)){
                                //Si el nuevo no es diferente
                                Toast.makeText(this,"El nombre ya esta en uso",Toast.LENGTH_LONG).show();
                            }else{
                                val parseUrl = ruta.toString();
                                intent.putExtra("ANIME_NAME", text);
                                intent.putExtra("ANIME_IMAGE", parseUrl);

                                setResult(3,intent);

                                finish();
                            }
                        }else{
                            val parseUrl = ruta.toString();
                            intent.putExtra("ANIME_NAME", text);
                            intent.putExtra("ANIME_IMAGE", parseUrl);

                            setResult(3,intent);

                            finish();
                        }
                    }else{
                        if(isUsed(text,"")){
                            //Si es nuevo pero esta en uso
                            Toast.makeText(this,"El nombre ya esta en uso",Toast.LENGTH_LONG).show();
                        }else{
                            val parseUrl = ruta.toString();
                            intent.putExtra("ANIME_NAME", text);
                            intent.putExtra("ANIME_IMAGE", parseUrl);

                            setResult(2, intent);
                            finish();
                        }
                    }

                }else{
                    Toast.makeText(this,"El nombre debe estar relleno",Toast.LENGTH_LONG).show();
                }

                return true;
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun iniciar(){

        binding.animeImagen.setImageURI(Uri.parse("android.resource://com.example.animelistapp/drawable/"+R.drawable.defaultphoto))
        binding.animeImagen.setOnClickListener{
            var intent: Intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        }

        findViewById<ConstraintLayout>(R.id.addLayout).setOnTouchListener(object: View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                var imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
                imm.hideSoftInputFromWindow(currentFocus?.windowToken,0)

                return true;
            }
        })

        modificando = intent.getBooleanExtra("MODIFICAR",false)
        nombres = intent.getStringArrayListExtra("NOMBRES")!!;

        if(modificando){
            supportActionBar!!.setTitle(resources.getString(R.string.modifyanime))
            anime = intent.getParcelableExtra("DATOS")!!
            binding.animeNombre.setText(anime.nombre)
            binding.animeImagen.setImageURI(anime.imagen)
            ruta = anime.imagen
        }


    }

    private fun isUsed(n: String, other: String): Boolean{
        for(i: String in nombres){
            if(n == i && i != other){
                return true;
            }
        }
        return false;
    }


}