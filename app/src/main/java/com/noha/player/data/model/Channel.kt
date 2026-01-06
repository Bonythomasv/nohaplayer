package com.noha.player.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val country: String? = null,
    val language: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null
) : Parcelable

