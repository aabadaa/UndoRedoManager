package com.abada.undoredomanager

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

class UndoRedoManager(
    private val savedStateHandle: SavedStateHandle,
    private val maxSize: Int = 10,
) {
    data class RedoState(
        val key: String, val value: Any, val previous: Any? = null
    )

    private val stack = MutableStateFlow(listOf<RedoState>())
    private var index: Int? = null
        set(value) {
            if (value != null && value in stack.value.indices) {
                val undoState = stack.value[value]
                savedStateHandle[undoState.key] = undoState.value
            }
            field = value
        }

    init {
        if (maxSize < 5)
            throw IllegalArgumentException("minimum size is 5 you passed $maxSize")
        restoreState()
    }

    fun undo() {
        index = index?.minus(1).takeIf { it in stack.value.indices } ?: index
    }

    fun redo() {
        index = index?.plus(1)
    }

    fun <T : Any> set(key: String, value: T) {
        val prev = savedStateHandle.get<T>(key)

        val redoState = RedoState(key, value, prev)
        if (index != null && index != stack.value.lastIndex) {
            stack.value = stack.value.subList(0, index!!)
        }
        stack.value = stack.value + redoState
        if (stack.value.size > maxSize)
            stack.value = stack.value - stack.value.first()
        index = stack.value.lastIndex
    }


    private fun restoreState() {
        val undoStack = savedStateHandle.get<List<RedoState>>(UNDO_STACK_KEY) ?: emptyList()
        stack.value = undoStack
        index = stack.value.lastIndex
    }

    companion object {
        private const val UNDO_STACK_KEY = "undo stack"
    }
}
