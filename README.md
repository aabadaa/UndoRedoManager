# UndoRedoManager
A util class that implements undo/redo on a group of states
## Demo
[undo-redo-manager.demo.webm](https://github.com/aabadaa/UndoRedoManager/assets/54916786/c8a0bf0b-c965-4d73-8546-1038981cc953)
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

# License
```xml
Designed and developed by 2023 AbdulAleem Alsayed

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
