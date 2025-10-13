package com.vs18.tvdemoapp

import android.graphics.drawable.Drawable
import android.view.*
import androidx.core.content.*
import androidx.leanback.widget.*
import com.bumptech.glide.*
import com.vs18.tvdemoapp.core.model.Movie
import kotlin.properties.*

class CardPresenter : Presenter() {

    private var defaultCardImage: Drawable? = null
    private var selectedBackgroundColor: Int by Delegates.notNull()
    private var defaultBackgroundColor: Int by Delegates.notNull()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        defaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        selectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background)
        defaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.movie)

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val movie = item as Movie
        val cardView = viewHolder.view as ImageCardView

        if (movie.cardImageUrl != null) {
            cardView.titleText = movie.title
            cardView.contentText = movie.studio
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            Glide.with(viewHolder.view.context)
                .load(movie.cardImageUrl)
                .centerCrop()
                .error(defaultCardImage)
                .into(cardView.mainImageView!!)
        }

        cardView.setOnFocusChangeListener { _, hasFocus ->
            cardView.alpha = if (hasFocus) 1.0f else 0.7f
            cardView.scaleX = if (hasFocus) 1.1f else 1.0f
            cardView.scaleY = if (hasFocus) 1.1f else 1.0f
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) selectedBackgroundColor else defaultBackgroundColor
        view.setBackgroundColor(color)
        view.setInfoAreaBackgroundColor(color)
    }

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

}