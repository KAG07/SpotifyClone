package com.example.spotifyclone.others

data class Resource<out T>(val status: Status, val data:T?, val msg:String?) {
// a general type resource
    companion object{
        fun < T>success(data:T?)=
            Resource(Status.SUCCESS,data,null)// fun called by var of type t and return type resouce have a status to check it state
    fun <T>error(msg:String,data:T?)= Resource(Status.ERROR,data,msg)
    fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)


    }


}

enum class Status{
    SUCCESS,
    ERROR,
    LOADING
}
