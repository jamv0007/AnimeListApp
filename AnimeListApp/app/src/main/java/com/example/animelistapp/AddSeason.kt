package com.example.animelistapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.animelistapp.databinding.ActivityAddSeasonBinding
import com.example.animelistapp.databinding.ActivitySeasonListBinding

class AddSeason : AppCompatActivity(),AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityAddSeasonBinding
    private var numberList = arrayOf("0","1","2","3","4","5","6","7","8","9");//List of numbers for spinner
    private var spinners: ArrayList<Spinner> = arrayListOf<Spinner>();//Array List of Spinners
    private var adapter: ArrayAdapter<String>? = null;//Spinner adapter
    private var modificando: Boolean = false
    private var seasonNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSeasonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(resources.getString(R.string.addSeason))

        //Connect spinners with layout
        var one:Spinner = findViewById(R.id.spinner);
        var two:Spinner  = findViewById(R.id.spinner2);
        var three:Spinner  = findViewById(R.id.spinner3);
        var four:Spinner  = findViewById(R.id.spinner4);

        //Add spinners to arraylist
        spinners.addAll(listOf(one,two,three,four));

        //Create the adapter
        adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,numberList);

        //Add for each spinner the adapter and listener
        for(i:Spinner in spinners){
            i.adapter = adapter;
            i.onItemSelectedListener = this;
        }

        iniciar()

    }

    private fun iniciar(){

        modificando = intent.getBooleanExtra("MODIFIED",false);

        if(modificando){
            supportActionBar!!.setTitle(resources.getString(R.string.modifyseason))
            seasonNumber = intent.getIntExtra("NUMBER",0)
            binding.currentCapSeason.text = seasonNumber.toString()

        }

        //Show current number from chapters
        convertFromNumber();
    }

    /***
     * Function that get number of spinners
     */
    private fun convertToNumber(){

        var limit: Boolean = false;
        var number: String = "";

        //For each spinner, if spinner have a 0 before, is ignore, else if is other number, add, or is a 0 but before has a number, it save too
        for(i: Spinner in spinners){
            if(i.selectedItem.toString() != "0" || limit ){
                limit = true;
                number += i.selectedItem.toString();
            }
        }

        seasonNumber = number.toInt();

    }

    /***
     * Function that select a number at the spinners
     */
    private fun convertFromNumber(){

        var str: String = seasonNumber.toString();

        var n: Int = str.length;

        //If number not have 4 digits, its add before for the 4 digits
        if(n < 4){
            while(str.length != 4){
                str = "0" + str;
            }
            n = str.length;
        }

        //Change each spinner for the digit
        for(i: Int in n-1 downTo 0){
            var index: Int = adapter!!.getPosition(str[i].toString());
            spinners[i].setSelection(index);
        }

    }

    /***
     * Change the menu with others buttons
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        var inflater: MenuInflater = menuInflater;
        inflater.inflate(R.menu.add_menu,menu);

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            //Return to cancel
            android.R.id.home -> {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }

            R.id.confirm -> {
                var intent: Intent = Intent();
                convertToNumber();
                intent.putExtra("CHAPTERNUMBER",seasonNumber);
                if(modificando){
                    setResult(6,intent);

                }else {
                    setResult(5, intent);
                }
                finish();
                return true;
            }

        }

        return super.onOptionsItemSelected(item);

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}