package com.vs18.tvdemoapp.core.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movie(

    @PrimaryKey var id: Int,
    var title: String? = null,
    var description: String? = null,
    var backgroundImageUrl: String? = null,
    var cardImageUrl: String? = null,
    var videoUrl: String? = null,
    var studio: String? = null,
    var subtitleUrl: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        title = parcel.readString(),
        description = parcel.readString(),
        backgroundImageUrl = parcel.readString(),
        cardImageUrl = parcel.readString(),
        videoUrl = parcel.readString(),
        studio = parcel.readString(),
        subtitleUrl = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(backgroundImageUrl)
        parcel.writeString(cardImageUrl)
        parcel.writeString(videoUrl)
        parcel.writeString(studio)
        parcel.writeString(subtitleUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie = Movie(parcel)
        override fun newArray(size: Int): Array<out Movie?>? = arrayOfNulls(size)
    }
}