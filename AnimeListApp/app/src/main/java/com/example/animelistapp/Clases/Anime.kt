package com.example.animelistapp.Clases

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.animelistapp.UsoBase

class Anime(
    id: Long,
    nombre: String,
    imagen: Uri,
    temporadaActual: Int,
    episodioActual: Int,
    viendo: Boolean,
    terminado: Boolean,
    temporadas: ArrayList<Temporada>

): Parcelable {

    var id: Long = id
                get() = field
                set(value) { field = value}
    var nombre: String = nombre
                get() = field
                set(value) { field = value}
    var imagen: Uri = imagen
                get() = field
                set(value) { field = value}
    var temporadaActual: Int = temporadaActual
                get() = field
                set(value) { field = value}
    var episodioActual: Int = episodioActual
                get() = field
                set(value) { field = value}
    var viendo: Boolean = viendo
                get() = field
                set(value) { field = value}
    var terminado: Boolean = terminado
                get() = field
                set(value) { field = value}
    var temporadas: ArrayList<Temporada> = temporadas
                get() = field
                set(value) { field = value}

    @RequiresApi(Build.VERSION_CODES.Q)
    private constructor(parcel: Parcel): this(parcel.readLong(),parcel.readString()!!,parcel.readParcelable(Uri::class.java.classLoader)!!,parcel.readInt(),parcel.readInt(),parcel.readBoolean(),
        parcel.readBoolean(),parcel.createTypedArrayList(Temporada.CREATOR)!!){


    }


    fun addSeason(season: Temporada){
        temporadas.add(season);
    }


    fun removeRangeSeason(ini: Int,context: Context){

        for(i: Int in temporadas.size-1 downTo ini){
            UsoBase.borrarAnime(context,"episodio","temporada_clave="+temporadas[temporadas.size-1].id)
            UsoBase.borrarAnime(context,"temporada","id="+temporadas[temporadas.size-1].id)
            temporadas.removeAt(temporadas.size-1)
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(nombre)
        parcel.writeParcelable(imagen, 0)
        parcel.writeInt(temporadaActual)
        parcel.writeInt(episodioActual)
        parcel.writeByte(if (viendo) 1 else 0)
        parcel.writeByte(if (terminado) 1 else 0)
        parcel.writeTypedArray(temporadas.toTypedArray(),0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Anime> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Anime {
            return Anime(parcel)
        }

        override fun newArray(size: Int): Array<Anime?> {
            return arrayOfNulls(size)
        }
    }


}