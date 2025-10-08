package com.vs18.tvdemoapp

import android.annotation.*
import android.os.*
import android.view.*
import androidx.core.content.*
import androidx.leanback.app.*

class ErrorFragment : ErrorSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
    }

    @SuppressLint("PrivateResource")
    fun setErrorContent() {
        imageDrawable = ContextCompat.getDrawable(requireActivity(), androidx.leanback.R.drawable.lb_ic_sad_cloud)
        message = resources.getString(R.string.error_fragment_message)
        setDefaultBackground(true)
        buttonText = resources.getString(R.string.dismiss_error)
        buttonClickListener = View.OnClickListener {
            fragmentManager?.beginTransaction()?.remove(this@ErrorFragment)?.commit()
        }
    }

}