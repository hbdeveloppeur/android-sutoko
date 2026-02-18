package com.purpletear.core.presentation.services

import android.content.Context
import android.widget.Toast
import javax.inject.Inject


class MakeToastService @Inject constructor(private val context: Context) {

    operator fun invoke(message: Int, vararg formatArgs: Any?) {
        Toast.makeText(context, context.getString(message, *formatArgs), Toast.LENGTH_SHORT).show()
    }
}