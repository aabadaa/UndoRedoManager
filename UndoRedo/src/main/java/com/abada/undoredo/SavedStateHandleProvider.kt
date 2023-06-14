package com.abada.undoredo

import androidx.lifecycle.SavedStateHandle

class SavedStateHandleProvider(
    private val savedStateHandle: SavedStateHandle
) : StateProvider {
    override fun <T : Any> get(key: String): T? = savedStateHandle[key]
    override fun <T : Any> set(key: String, value: T?) = savedStateHandle.set(key, value)

    companion object {
        val SavedStateHandle.asStateProvider get() = SavedStateHandleProvider(this)
    }
}