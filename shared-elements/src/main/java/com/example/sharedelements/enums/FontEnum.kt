package com.example.sharedelements.enums

import androidx.annotation.Keep
import com.example.sharedelements.R

@Keep
sealed class FontEnum {
    abstract val font: Int

    object PoppinsBold : FontEnum() {
        override val font: Int = R.font.font_poppins_bold
    }

    object PoppinsRegular : FontEnum() {
        override val font: Int = R.font.font_poppins_regular
    }

    object PoppinsSemiBold : FontEnum() {
        override val font: Int = R.font.font_poppins_semibold
    }

    object PoppinsMedium : FontEnum() {
        override val font: Int = R.font.font_poppins_medium
    }

    object GaramondRegular : FontEnum() {
        override val font: Int = R.font.font_garamond
    }

    object MontserratRegular : FontEnum() {
        override val font: Int = R.font.font_montserrat_regular
    }

    object MontserratMedium : FontEnum() {
        override val font: Int = R.font.font_montserrat_medium
    }

    object MontserratSemiBold : FontEnum() {
        override val font: Int = R.font.font_montserrat_semibold
    }

    object MontserratBold : FontEnum() {
        override val font: Int = R.font.font_montserrat_bold
    }

}