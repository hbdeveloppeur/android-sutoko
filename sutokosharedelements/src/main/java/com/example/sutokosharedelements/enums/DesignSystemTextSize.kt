package com.example.sutokosharedelements.enums

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

sealed class DesignSystemTextSize {
    abstract val size: TextUnit

    object XLarge : DesignSystemTextSize() {
        override val size: TextUnit = 26.sp
    }

    object Large : DesignSystemTextSize() {
        override val size: TextUnit = 18.sp
    }

    object Medium : DesignSystemTextSize() {
        override val size: TextUnit = 16.sp
    }


    object Normal : DesignSystemTextSize() {
        override val size: TextUnit = 14.sp
    }

    object Small : DesignSystemTextSize() {
        override val size: TextUnit = 12.sp
    }

    object XSmall : DesignSystemTextSize() {
        override val size: TextUnit = 10.sp
    }


}