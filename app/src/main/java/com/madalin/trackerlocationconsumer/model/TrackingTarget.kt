package com.madalin.trackerlocationconsumer.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackingTarget(
    var id: String = "",
    var currentPosition: LatLng? = null,
    var path: MutableList<LatLng> = mutableListOf()
) : Parcelable