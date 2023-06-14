package com.abada.undoredo

interface StateProvider {
    operator fun <T : Any> get(key: String): T?
    operator fun <T : Any> set(key: String, value: T?)
}