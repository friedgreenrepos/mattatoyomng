package com.example.mattatoyomng

import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime,
    val img: Int)
