package com.abada.undoredo

import androidx.lifecycle.SavedStateHandle
class SavedStateProvider(private val savedStateHandle: SavedStateHandle) : StateProvider {
    override fun <T> get(key: String): T? {
        return savedStateHandle.get<T>(key)
    }

    override fun <T> set(key: String, value: T) {
        savedStateHandle[key] = value
    }

    override fun remove(key: String) {
        savedStateHandle.remove<Any>(key)
    }

    override fun getAll(observedKeys: Set<String>): Map<String, Any> {
        return observedKeys.filter { it in savedStateHandle.keys() }
            .associateWith { savedStateHandle.get<Any>(it)!! }.toMap()
    }

    companion object {
        val SavedStateHandle.asStateProvider get() = SavedStateProvider(this)
    }
}