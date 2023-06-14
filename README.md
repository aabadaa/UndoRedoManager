# UndoRedoManager
A util class that implements undo/redo on a group of states
## Setup
### Step 1 Add this in your root build.gradle at the end of repositories:
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
### Step 2 Add the dependency, get the last version  [![](https://jitpack.io/v/aabadaa/undoRedoManager.svg)](https://jitpack.io/#aabadaa/undoRedoManager)
	dependencies {
	        implementation 'com.github.aabadaa:undoRedoManager:<version>'
	}

## Usage
- Define your states in the viewModel using ```SavedStateHandle``` 
```
    val text1 = savedStateHandle.getStateFlow("text1", "")
    val text2 = savedStateHandle.getStateFlow("text2", "")
```
 - Create an object of ```UndoRedoManager```
 ```
     private val undoRedoManager = UndoRedoManager(
        mapOf(
            "text1" to "",
            "text2" to ""
        ),
        savedStateHandle
    )
```
- Use its ```set``` method to change your states 
```
    fun onText2Changed(text: String) {
        undoRedoManager.set("text2", text)
    }
```
- Call undo/redo methods to undo or redo 
```
    undoRedoManager.undo()
    undoRedoManager.redo()
```

