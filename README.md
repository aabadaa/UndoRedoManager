# UndoRedoManager
A util class that implements undo/redo on a group of states
## Setup
### Step 1 Add this in your root build.gradle at the end of repositories:
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 ```
### Step 2 Add the dependency, get the last version from [![](https://jitpack.io/v/aabadaa/undoRedoManager.svg)](https://jitpack.io/#aabadaa/undoRedoManager)
```groovy
	dependencies {
	        implementation 'com.github.aabadaa:undoRedoManager:<version>'
	}
```	
## Usage
- Define your states in the viewModel using ```SavedStateHandle``` 
```kotlin
    val text1 = savedStateHandle.getStateFlow("text1", "")
    val text2 = savedStateHandle.getStateFlow("text2", "")
```
 - Create an object of ```UndoRedoManager```
 ```kotlin
     private val undoRedoManager = UndoRedoManager(stateProvider, setOf("text1", "text2"), maxSize = 100)
```
- Use ```commit``` method to save states changes
```kotlin
    fun onText2Changed(text: String) {
        savedStateHandle.set("text2", text)
	undoRedoManager.commit()
    }
```
- Call undo/redo methods to undo or redo 
```kotlin
    undoRedoManager.undo()
    undoRedoManager.redo()
```
