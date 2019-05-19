package com.broads.bleep

import com.broads.bleep.entities.Bleep

interface Observer {
    fun processFinish(bleeps: List<Bleep>)
}