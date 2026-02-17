package com.blank.commutetrack.core.common.extension

import kotlinx.datetime.*

fun LocalDateTime.formatTime(): String {
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun LocalDate.formatDate(): String {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${months[monthNumber - 1]} $dayOfMonth, $year"
}

fun Int.formatDuration(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

fun Double.formatDistance(useKm: Boolean = true): String =
    if (useKm) "%.1f km".format(this)
    else "%.1f mi".format(this * 0.621371)
