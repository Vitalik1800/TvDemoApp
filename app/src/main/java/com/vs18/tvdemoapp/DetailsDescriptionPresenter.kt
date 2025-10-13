package com.vs18.tvdemoapp

import androidx.leanback.widget.*
import com.vs18.tvdemoapp.core.model.*

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(vh: ViewHolder, item: Any) {
        val movie = item as Movie
        vh.title.text = movie.title
        vh.subtitle.text = movie.studio
        vh.body.text = movie.description
    }

}
