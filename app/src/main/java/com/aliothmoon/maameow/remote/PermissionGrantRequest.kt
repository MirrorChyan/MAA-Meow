package com.aliothmoon.maameow.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PermissionGrantRequest(
    val packageName: String,
    val uid: Int,
    val accessibilityServiceId: String = "",
) : Parcelable
