package com.example.animelistapp.Clases

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.animelistapp.UsoBase

class Temporada(
    id: Long,
    numeroTemporada: Int,
    finalizada: Boolean,
    episodios: ArrayList<Episodio>
): Parcelable {

    var id: Long = id
                get() = field
                set(value) { field = value}
    var numeroTemporada: Int = numeroTemporada
                get() = field
                set(value) { field = value }
    var finalizada: Boolean = finalizada
                get() = field
                set(value) { field = value }
    var episodios: ArrayList<Episodio> = episodios
                get() = field
                set(value) { field = value }



    fun addEpisode(episodio: Episodio){
        episodios.add(episodio);
    }

    fun removeRangeEpisode(ini: Int,context: Context){

        for(i: Int in episodios.size-1 downTo ini){
            UsoBase.borrarAnime(context,"episodio","id="+episodios[episodios.size-1].id)
            episodios.removeAt(episodios.size-1);
        }

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(parcel.readLong(),parcel.readInt(), parcel.readBoolean(),parcel.createTypedArrayList(Episodio.CREATOR)!!) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(numeroTemporada)
        parcel.writeByte(if (finalizada) 1 else 0)
        parcel.writeTypedArray(episodios.toTypedArray(),0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Temporada> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Temporada {
            return Temporada(parcel)
        }

        override fun newArray(size: Int): Array<Temporada?> {
            return arrayOfNulls(size)
        }
    }


}