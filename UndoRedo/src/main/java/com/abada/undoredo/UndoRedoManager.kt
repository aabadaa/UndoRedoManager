package com.abada.undoredo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UndoRedoManager(
    private val initialStates: Map<String, Any>,
    private val savedStateHandle: StateProvider,
    private val maxSize: Int = 10,
) {
    private val managerScope = CoroutineScope(Job())
    private val stack = MutableStateFlow(listOf<UndoRedoItem>())
    private val index: MutableStateFlow<Int> = MutableStateFlow(-1)
    val canUndoFlow = index.map { it >= 0 }
        .stateIn(managerScope, SharingStarted.WhileSubscribed(1000), false)
    val canRedoFlow = combine(stack, index) { stack, index ->
        (index + 1) in stack.indices
    }.stateIn(managerScope, SharingStarted.WhileSubscribed(1000), false)

    init {
        if (maxSize < 5)
            throw IllegalArgumentException("minimum size is 5 you passed $maxSize")
        restoreState()
    }

    fun undo() {
        index.value.takeIf { canUndoFlow.value }?.let {
            val undoRedoItem = stack.value[it]
            savedStateHandle[undoRedoItem.key] = undoRedoItem.prevValue
            index.value = (it - 1)
        }
    }

    fun redo() = index.value.takeIf { canRedoFlow.value }?.let {
        if (it + 1 in stack.value.indices) {
            index.value = it + 1
            val currentState = stack.value[it + 1]
            savedStateHandle[currentState.key] = currentState.value
        }
    }

    fun <T : Any> set(key: String, value: T) {
        if (!initialStates.containsKey(key))
            throw IllegalArgumentException("the key $key is not exist in initial states")
        val currentValue = savedStateHandle.get<T>(key)
        val undoRedoItem = UndoRedoItem(key, value, currentValue)

        if (index.value != stack.value.lastIndex) {
            stack.value = stack.value.take(index.value + 1)
        }
        stack.value += undoRedoItem
        if (stack.value.size > maxSize + initialStates.size)
            stack.value = stack.value - stack.value.first()
        index.value = stack.value.lastIndex
        savedStateHandle[key] = value
    }


    private fun restoreState() {
        val undoStack =
            savedStateHandle.get<List<UndoRedoItem>>(UNDO_STACK_KEY) ?: emptyList()
        stack.value = undoStack
        if (stack.value.isNotEmpty()) {
            index.value = stack.value.lastIndex
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