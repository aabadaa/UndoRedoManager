package com.abada.undoredo

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

class UndoRedoManager(
    private val initialStates: Map<String, Any>,
    private val savedStateHandle: SavedStateHandle,
    private val maxSize: Int = 10,
) {

    private val stack = MutableStateFlow(listOf<UndoRedoItem>())
    private var index: Int = -1

    init {
        if (maxSize < 5)
            throw IllegalArgumentException("minimum size is 5 you passed $maxSize")
        restoreState()
    }

    fun undo() {
        index.takeIf { it >= 0 }?.let {
            val UndoRedoItem = stack.value[it]
            savedStateHandle[UndoRedoItem.key] = UndoRedoItem.prevValue
            index = (it - 1)
        }
    }

    fun redo() = index.let {
        if (it + 1 in stack.value.indices) {
            index = it + 1
            val currentState = stack.value[it + 1]
            savedStateHandle[currentState.key] = currentState.value
        }
    }

    fun <T : Any> set(key: String, value: T) {
        if (!initialStates.containsKey(key))
            throw IllegalArgumentException("the key $key is not exist in initial states")
        val currentValue = savedStateHandle.get<T>(key)
        val UndoRedoItem = UndoRedoItem(key, value, currentValue)

        if (index != stack.value.lastIndex) {
            stack.value = stack.value.take(index + 1)
        }
        stack.value += UndoRedoItem
        if (stack.value.size > maxSize + initialStates.size)
            stack.value = stack.value - stack.value.first()
        index = stack.value.lastIndex
        savedStateHandle[key] = value
    }


    private fun restoreState() {
        val undoStack =
            savedStateHandle.get<List<UndoRedoItem>>(UNDO_STACK_KEY) ?: emptyList()
        stack.value = undoStack
        if (stack.value.isNotEmpty()) {
            index = stack.value.lastIndex
        } else {
            initialStates.forEach {
                savedStateHandle[it.key] = it.value
            }
        }
    }

    companion object {
        private const val UNDO_STACK_KEY = "undo stack"
    }
}