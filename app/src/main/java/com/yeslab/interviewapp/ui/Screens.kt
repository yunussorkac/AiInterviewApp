package com.yeslab.interviewapp.ui

import kotlinx.serialization.Serializable

sealed class Screens {

    @Serializable
    data object Home : Screens()

    @Serializable
    data object Interview : Screens()

    @Serializable
    data object History : Screens()

    @Serializable
    data class Detail(val id: Long) : Screens()

}