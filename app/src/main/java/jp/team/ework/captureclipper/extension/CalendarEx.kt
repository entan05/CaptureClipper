package jp.team.ework.captureclipper.extension

import java.util.*

val Calendar.year: Int
    get() = get(Calendar.YEAR)

val Calendar.month: Int
    get() = get(Calendar.MONTH)

val Calendar.date: Int
    get() = get(Calendar.DATE)

val Calendar.hourOfDay: Int
    get() = get(Calendar.HOUR_OF_DAY)

val Calendar.minute: Int
    get() = get(Calendar.MINUTE)

val Calendar.second: Int
    get() = get(Calendar.SECOND)
