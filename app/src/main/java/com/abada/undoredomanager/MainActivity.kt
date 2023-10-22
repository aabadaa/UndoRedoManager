package com.abada.undoredomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.abada.undoredo.SavedStateProvider.Companion.asStateProvider
import com.abada.undoredo.UndoRedoManager
import com.abada.undoredomanager.ui.theme.UndoRedoManagerTheme
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UndoRedoManagerTheme {
                // A surface container using the 'background' color from the theme
                val text1 by viewModel.text1.collectAsState("")
                val text2 by viewModel.text2.collectAsState("")
                val count by viewModel.count.collectAsState(0)
                val canUndo by viewModel.canUndo.collectAsState(false)
                val canRedo by viewModel.canRedo.collectAsState(false)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(value = text1, onValueChange = viewModel::setText1)
                        TextField(value = text2, onValueChange = viewModel::setText2)
                        Button(onClick = viewModel::incrementCount) {
                            Text(count.toString())
                        }
                        Row {
                            Button(onClick = viewModel::undo, enabled = canUndo) {
                                Text("Undo")
                            }
                            Button(onClick = viewModel::redo, enabled = canRedo) {
                                Text("redo")
                            }
                        }
                        Button(onClick = viewModel::commit) {
                            Text("Commit")
                        }
                    }
                }
            }
        }
    }
}


class MyViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val stateProvider = savedStateHandle.asStateProvider
    val text1: StateFlow<String> = savedStateHandle.getStateFlow("text1", "")
    val text2: StateFlow<String> = savedStateHandle.getStateFlow("text2", "")
    val count: StateFlow<Int> = savedStateHandle.getStateFlow("count", 0)

    private val undoRedoManager =
        UndoRedoManager(
            stateProvider, setOf("text1", "text2"), maxSize = 100,
            triggerKeys = setOf("text1"),
        )
    val canUndo: StateFlow<Boolean> = undoRedoManager.canUndoFlow
    val canRedo: StateFlow<Boolean> = undoRedoManager.canRedoFlow

    init {
        undoRedoManager.init()
    }

    fun set(key: String, value: Any) {
        stateProvider[key] = value
    }

    fun commit() = undoRedoManager.commit()
    fun setText1(value: String) {
        set("text1", value)
        commit()
    }


    fun setText2(value: String) {
        set("text2", value)
        commit()
    }


    fun incrementCount() = set("count", count.value + 1)


    fun undo() = undoRedoManager.undo()


    fun redo() = undoRedoManager.redo()

}