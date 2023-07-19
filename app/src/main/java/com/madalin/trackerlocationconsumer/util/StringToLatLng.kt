package com.madalin.trackerlocationconsumer.util

import com.google.android.gms.maps.model.LatLng

/**
 * Converts a given coordinates [String] of this format `123,123` to a [LatLng].
 * @param latLngString coordinates string to convert
 * @return the new coordinates as a [LatLng]
 */
fun convertStringToLatLng(latLngString: String): LatLng? {
    val latLngArray = latLngString.split(",")

    if (latLngArray.size != 2) return null // check if there are two components (latitude and longitude)

    return try {
        // convert latitude and longitude strings to Double
        val latitude = latLngArray[0].trim().toDouble()
        val longitude = latLngArray[1].trim().toDouble()

        // create and return a new LatLng object
        LatLng(latitude, longitude)
    } catch (e: NumberFormatException) {
        // invalid number format
        null
    }
}