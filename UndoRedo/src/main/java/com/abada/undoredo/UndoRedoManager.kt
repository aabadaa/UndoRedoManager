package com.abada.undoredo

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


/**
 * Manages the undo and redo functionality for changes in an application's state.
 *
 * This class allows you to track and manage changes made to a set of observed keys in the application's state.
 * It provides methods for recording, undoing, and redoing changes, as well as initializing and resetting
 * the commit stack.
 *
 * @param stateProvider An object or interface responsible for providing access to the application's state.
 * @param observedKeys A set of keys representing the state properties to be observed and recorded.
 * @param maxSize The maximum number of changes to keep in the undo/redo stack (default is 100).
 * @param triggerKeys A set of keys that trigger the recording of a change when their values change.
 *                   These keys should be a subset of observedKeys.
 * @param comparator A function used to compare values associated with keys to determine whether a change should
 *                  be recorded (default checks for equality).
 *
 * @throws IllegalArgumentException If maxSize is less than 5.
 * @throws AssertionError If triggerKeys contain keys not present in observedKeys.
 */
class UndoRedoManager(
    private val stateProvider: StateProvider,
    private val observedKeys: Set<String>,
    private val maxSize: Int = 100,
    private val triggerKeys: Set<String> = observedKeys,
    private val comparator: (String, Any?, Any?) -> Boolean = { _, a, b -> a == b },
) {
    private var isInitialized = false
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
        assert((triggerKeys - observedKeys).isEmpty()) {
            "this keys: ${triggerKeys- observedKeys} are not included in the observed key"
        }
    }
    /**
     * Undo the most recent change in the application's state.
     * If the undo operation is possible, it reverts the last committed changes.
     */
    fun undo() {
        index.value.takeIf { canUndoFlow.value }?.let {
            val commit = stack.value[it]
            Log.i(javaClass.name, "undo:${commit.revertedChanges} ")
            commit.revertedChanges.forEach { (key, value) ->
                if (key in observedKeys) {
                    stateProvider[key] = value
                }
            }
            commit.onRevert?.invoke(commit.revertResult)
            index.value = (it - 1)
        }
    }

    /**
     * Redo a previously undone change in the application's state.
     * If the redo operation is possible, it re-applies the previously undone changes.
     */
    fun redo() {
        index.value.takeIf { canRedoFlow.value }?.let {
            if (it + 1 in stack.value.indices) {
                index.value = it + 1
                val commit = stack.value[it + 1]
                commit.changes.forEach { (key, value) ->
                    stateProvider[key] = value
                }
                commit.revertResult = commit.commitAction?.invoke()
            }
        }
    }

    /**
     * Record a change in the application's state and add it to the commit stack.
     *
     * @param actionCommit An optional lambda function to perform the change action. It should return the result
     *                    of the action. If not provided, no action is performed.
     * @param onUndo An optional lambda function to specify actions to perform when undoing this change. It accepts
     *               the result of the `actionCommit` as a parameter.
     */
    fun commit(
        actionCommit: (() -> Any?)? = null,
        onUndo: ((Any?) -> Unit)? = null,
    ) {
        if (!isInitialized)
            throw IllegalStateException("Please call init() before commit()")
        val result = actionCommit?.invoke()
        val currentState = stateProvider.getAll(observedKeys)
        val previousCommit = stack.value.getOrNull(index.value)
        val previousState = previousCommit?.state ?: mapOf()
        val changes = currentState.filter { (key, value) ->
            key in triggerKeys && (!previousState.containsKey(key) ||
                    !comparator.invoke(key,value, previousState[key])
                    )
        }
        if (changes.isNotEmpty()) {
            val commit = Commit(currentState, changes, previousState, onUndo, actionCommit, result)
            Log.i(javaClass.name, "commit: $commit")

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


    fun init() {
        isInitialized = true
        val commitStack = stateProvider.get<List<Commit>>(COMMIT_STACK_KEY) ?: emptyList()
        stack.value = commitStack
        if (stack.value.isNotEmpty()) {
            index.value = stack.value.lastIndex
        } else reset()
    }

    fun reset() {
        val initialState = stateProvider.getAll(observedKeys)
        val initialCommit = Commit(initialState, mapOf(), mapOf(), null, null)
        stack.value = listOf(initialCommit)
        index.value = 0
    }

    companion object {
        private const val COMMIT_STACK_KEY = "commit stack"
    }
}