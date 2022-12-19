package com.example.animelistapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.animelistapp.Clases.Anime
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class BottomSheet(private var anime: Anime,private var pos: Int, private var nombres: ArrayList<String>,dir: File): BottomSheetDialogFragment() {

    private lateinit var listener: BottomSheetListener//Listener de la clase que implementa BottomSheetListener (Main)
    //RResult de modificar elemento
    private var modifyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //If save, modified the data and close the bottom sheet
        if (result.resultCode == 3) {
            var name: String = result.data?.getStringExtra("ANIME_NAME").orEmpty();
            var image: Uri = Uri.parse(result.data?.getStringExtra("ANIME_IMAGE").orEmpty());

            UsoImagen.guardarImagen(dir.toString() + name,image,this.requireContext())
            image = UsoImagen.cargarImagen(dir.toString() + name)

            anime.imagen = image
            anime.nombre = name

            listener.onChangeButtonClicked(anime,pos)
            dismiss()

        }else if(result.resultCode == Activity.RESULT_CANCELED){
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        var activity = activity;

        try {
            listener = activity as BottomSheetListener
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() + "implementa la interfaz BottomSheetListener");
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view: View = inflater.inflate(R.layout.botton_sheet_anime,container,false);
        val change = view.findViewById<Button>(R.id.modify);
        change.setOnClickListener{
            var intent: Intent = Intent(context,AddAnime::class.java);
            intent.putExtra("DATOS",anime);
            intent.putExtra("NOMBRES",nombres);
            intent.putExtra("MODIFICAR",true);
            modifyLauncher.launch(intent);
        }

        val delete = view.findViewById<Button>(R.id.delete);
        delete.setOnClickListener {
            var dialog: AlertDialog.Builder = AlertDialog.Builder(context)



            dialog.setMessage(resources.getString(R.string.deleteItem));
            dialog.setTitle(resources.getString(R.string.deleteTitle));

            dialog.setNegativeButton(Html.fromHtml("<font color='#109DFA'>"+resources.getString(R.string.cancel)+"</font>",Html.FROM_HTML_MODE_LEGACY),DialogInterface.OnClickListener{ dialogInterface, i ->
                dismiss();
            })

            dialog.setPositiveButton(Html.fromHtml("<font color='#EF280F'>"+resources.getString(R.string.confirmDelete)+"</font>",Html.FROM_HTML_MODE_LEGACY),DialogInterface.OnClickListener{ dialogInterface, i ->
                listener!!.onDeleteButtonClicked(anime,pos)
                dismiss();
            })

            dialog.show();
        }

        val name = view.findViewById<TextView>(R.id.name);
        name.text = anime.nombre

        return view;
    }

    //Interfaz para enviar a la vista anterior
    public interface BottomSheetListener{
        fun onChangeButtonClicked(anime: Anime,pos: Int);
        fun onDeleteButtonClicked(anime: Anime,pos: Int);
    }


}