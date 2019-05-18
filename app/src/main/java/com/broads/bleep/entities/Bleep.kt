package com.broads.bleep.entities

import java.io.File

data class Bleep(val title: String, val publisher: String = "", val popularity: Int = 0,
                 val bleepRef: String = "", var bleep: File? = null, var url: String = "")