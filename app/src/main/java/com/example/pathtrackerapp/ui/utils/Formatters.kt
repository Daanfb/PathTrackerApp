package com.example.pathtrackerapp.ui.utils

/**
 * Converts a duration in seconds to a formatted time string (HH:MM:SS).
 *
 * @param seconds The duration in seconds.
 * @return A string formatted as "HH:MM:SS".
 */
fun formatToTimeString(seconds: Long): String {
    val hours = seconds  / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

/**
 * Converts a timestamp in milliseconds to a formatted date and time string (DD/MM/YYYY, HH:MM).
 *
 * @param millis The timestamp in milliseconds.
 * @return A string formatted as "DD/MM/YYYY, HH:MM".
 */
fun formatToDateTimeString(millis: Long): String {
    val date = java.util.Date(millis)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy, HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}