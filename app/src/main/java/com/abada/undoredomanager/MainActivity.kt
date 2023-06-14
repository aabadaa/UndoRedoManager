package com.abada.undoredomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.abada.undoredo.SavedStateHandleProvider.Companion.asStateProvider
import com.abada.undoredo.UndoRedoManager
import com.abada.undoredomanager.ui.theme.UndoRedoManagerTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UndoRedoManagerTheme {
                // A surface container using the 'background' color from the theme
                val text1 by viewModel.text1.collectAsState("")
                val text2 by viewModel.text2.collectAsState("")

                val canUndo by viewModel.canUndo.collectAsState()
                val canRedo by viewModel.canRedo.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(value = text1, onValueChange = viewModel::onText1Changed)
                        TextField(value = text2, onValueChange = viewModel::onText2Changed)
                        Button(onClick = viewModel::undo, enabled = canUndo) {
                            Text("Undo")
                        }
                        Button(onClick = viewModel::redo, enabled = canRedo) {
                            Text("redo")
                        }
                    }
                }
            }
        }
    }
}

class MyViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val undoRedoManager = UndoRedoManager(
        mapOf(
            "text1" to "",
            "text2" to ""
        ),
        savedStateHandle.asStateProvider
    )

    val canUndo = undoRedoManager.canUndoFlow
    val canRedo = undoRedoManager.canRedoFlow
    val text1 = savedStateHandle.getStateFlow("text1", "")
    val text2 = savedStateHandle.getStateFlow("text2", "")
    fun onText1Changed(text: String) {
        undoRedoManager.set("text1", text)
    }

    fun onText2Changed(text: String) {
        undoRedoManager.set("text2", text)
    }

    fun undo() = undoRedoManager.undo()

    fun redo() = undoRedoManager.redo()
}