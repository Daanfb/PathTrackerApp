package com.example.pathtrackerapp.ui.utils

import com.example.pathtrackerapp.BuildConfig
import com.example.pathtrackerapp.domain.model.SessionPoint

/**
 * Builds a Google Static Maps API URL to display a map with the given session points.
 *
 * @param points List of [SessionPoint] representing the path to be displayed on the map.
 * @param width Width of the map image in pixels. Default is 600.
 * @param height Height of the map image in pixels. Default is 400.
 *
 * @return A URL string that can be used to fetch the static map image.
 */
fun buildStaticMapUrl(points: List<SessionPoint>, width: Int = 600, height: Int = 600): String {
    if(points.isEmpty()) return ""

    val start = points.first()
    val end = points.last()

    val path = points.joinToString("|") { "${it.latitude},${it.longitude}" }

    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "size=${width}x${height}" +
            "&path=color:0xff0000ff|weight:5|$path" +
            "&markers=color:green|label:S|${start.latitude},${start.longitude}" +
            "&markers=color:red|label:E|${end.latitude},${end.longitude}" +
            "&key=${BuildConfig.MAPS_API_KEY}"
}