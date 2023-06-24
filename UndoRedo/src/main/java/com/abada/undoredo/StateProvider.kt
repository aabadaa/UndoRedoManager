package com.abada.undoredo

interface StateProvider {
    operator fun <T> get(key: String): T?
    operator fun <T> set(key: String, value: T)
    fun remove(key: String)
    fun  getAll(observedKeys: Set<String>): Map<String, Any>
}

