package com.abada.undoredo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UndoRedoManager(
    private val stateProvider: StateProvider,
    private val observedKeys: Set<String>,
    private val maxSize: Int = 10,
) {
    private val managerScope = CoroutineScope(Job())
    private val stack = MutableStateFlow(listOf<Commit>())
    private val index: MutableStateFlow<Int> = MutableStateFlow(-1)
    val canUndoFlow =
        index.map { it > 0 }.stateIn(managerScope, SharingStarted.WhileSubscribed(1000), false)
    val canRedoFlow = combine(stack, index) { stack, index ->
        (index + 1) in stack.indices
    }.stateIn(managerScope, SharingStarted.WhileSubscribed(1000), false)

    init {
        if (maxSize < 5) throw IllegalArgumentException("The minimum size is 5; you passed $maxSize")
        restoreState()
    }

    fun undo() {
        index.value.takeIf { canUndoFlow.value }?.let {
            val commit = stack.value[it]
            commit.revertedChanges.forEach { (key, value) ->
                if (key in observedKeys) {
                    stateProvider[key] = value
                }
            }
            index.value = (it - 1)
        }
    }

    fun redo() {
        index.value.takeIf { canRedoFlow.value }?.let {
            if (it + 1 in stack.value.indices) {
                index.value = it + 1
                val commit = stack.value[it + 1]
                commit.changes.forEach { (key, value) ->
                    stateProvider[key] = value
                }
            }
        }
    }

    fun commit() {
        val currentState = stateProvider.getAll(observedKeys)
        val previousCommit = stack.value.getOrNull(index.value)
        val previousState = previousCommit?.state ?: mapOf<String, Any>()
        val changes = currentState.filter { (key, value) ->
            key in observedKeys && (!previousState.containsKey(key) || value != previousState[key])
        }
        if (changes.isNotEmpty()) {
            val commit = Commit(currentState, changes, previousState)
            if (index.value != stack.value.lastIndex) {
                stack.value = stack.value.take(index.value + 1)
            }
            stack.value += commit
            if (stack.value.size > maxSize) {
                stack.value = stack.value.drop(1)
            }
            index.value = stack.value.lastIndex
        }
    }

    private fun restoreState() {
        val commitStack = stateProvider.get<List<Commit>>(COMMIT_STACK_KEY) ?: emptyList()
        stack.value = commitStack
        if (stack.value.isNotEmpty()) {
            index.value = stack.value.lastIndex
        } else initObservedStates()
    }

    private fun initObservedStates() {
        val initialState = stateProvider.getAll(observedKeys)
        val initialCommit = Commit(initialState, mapOf<String, Any>(), mapOf<String, Any>())
        stack.value = listOf(initialCommit)
        index.value = 0
    }

    companion object {
        private const val COMMIT_STACK_KEY = "commit stack"
    }
}