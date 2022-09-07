package com.vectorinc.musicfirebase.exoplayer

import com.vectorinc.musicfirebase.exoplayer.State.*

class FirebaseMusicSource {

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit >()
    private var state : State = STATE_CREATED
    set(value) {
        if(value == STATE_INITIALIZED || value == STATE_ERROR){
           synchronized(onReadyListeners){
               field = value
               onReadyListeners.forEach{listeners->

                   listeners(state == STATE_INITIALIZED)
               }
           }
        }else{
            field = value
        }
    }

    fun whenReady(action : (Boolean) -> Unit): Boolean{
        if (state == STATE_CREATED || state == STATE_INITIALIZING){
            onReadyListeners += action
            return false
        }else{
            action(state==STATE_INITIALIZED)
            return true
        }

    }
}

enum class State {
    STATE_INITIALIZED,
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_ERROR
}
