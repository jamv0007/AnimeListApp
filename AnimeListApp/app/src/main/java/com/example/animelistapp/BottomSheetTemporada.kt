package com.example.animelistapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.animelistapp.Clases.Anime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetTemporada(private var anime: Anime, private var pos: Int): BottomSheetDialogFragment() {

    private lateinit var listener: ModifiedSeason
    private val modifySeasonResponderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == 6){
            val number = result.data?.getIntExtra("CHAPTERNUMBER",0)
            listener.returnModifiedData(anime, pos,number!!)
            dismiss()

        }else if(result.resultCode == Activity.RESULT_CANCELED){
            println("AQui")
            dismiss()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view: View = inflater.inflate(R.layout.bottom_sheet_temporada,container,false);
        val change = view.findViewById<Button>(R.id.modifySeason);
        change.setOnClickListener{
            val intent = Intent(context, AddSeason::class.java)
            intent.putExtra("MODIFIED",true)
            intent.putExtra("NUMBER",anime.temporadas[pos].episodios.size)
            modifySeasonResponderLauncher.launch(intent)

        }

        val delete = view.findViewById<Button>(R.id.deleteSeason);
        delete.setOnClickListener {
            var dialog: AlertDialog.Builder = AlertDialog.Builder(context)

            dialog.setMessage(resources.getString(R.string.deleteItem));
            dialog.setTitle(resources.getString(R.string.deleteTitle));

            dialog.setNegativeButton(
                Html.fromHtml("<font color='#109DFA'>"+resources.getString(R.string.cancel)+"</font>",
                    Html.FROM_HTML_MODE_LEGACY), DialogInterface.OnClickListener{ dialogInterface, i ->
                dismiss();
            })

            dialog.setPositiveButton(
                Html.fromHtml("<font color='#EF280F'>"+resources.getString(R.string.confirmDelete)+"</font>",
                    Html.FROM_HTML_MODE_LEGACY), DialogInterface.OnClickListener{ dialogInterface, i ->
                    listener.returnDeletedData(pos)
                dismiss();
            })

            dialog.show();
        }

        val name = view.findViewById<TextView>(R.id.nameSeason);
        name.text = "Temporada " + anime.temporadas[pos].numeroTemporada

        return view;
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        var activity = activity;

        try {
            listener = activity as ModifiedSeason;
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() + "implementa la interfaz ModifiedSeason");
        }

    }

    public interface ModifiedSeason{
        public fun returnModifiedData(anime: Anime,season: Int,number: Int);
        public fun returnDeletedData(number: Int);
    }

}