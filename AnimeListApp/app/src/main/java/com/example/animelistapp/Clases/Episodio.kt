package com.example.animelistapp.Clases

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

class Episodio(
    id: Long,
    numeroEpisodio: Int,
    visto: Boolean
): Parcelable {

    var id: Long = id
                get() = field
                set(value) { field = value}
    var numeroEpisodio: Int = numeroEpisodio
                get() = field
                set(value) { field = value }
    var visto: Boolean = visto
                get() = field
                set(value) { field = value}



    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(parcel.readLong(),parcel.readInt(), parcel.readBoolean()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(numeroEpisodio)
        parcel.writeByte(if (visto) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Episodio> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Episodio {
            return Episodio(parcel)
        }

        override fun newArray(size: Int): Array<Episodio?> {
            return arrayOfNulls(size)
        }
    }


}