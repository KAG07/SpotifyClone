package com.example.spotifyclone.others

open  class Event<out T>(private val data:T){
    var hasbeenhandled=false
    private set

    fun getcontentifnothandled():T?{
        return if(hasbeenhandled) {
            null
        } else {
            hasbeenhandled = true
            data
        }
    }
}