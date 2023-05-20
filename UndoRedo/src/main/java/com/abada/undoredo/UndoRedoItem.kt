package com.abada.undoredo

data class UndoRedoItem(
    val key: String,
    val value: Any?,
    val prevValue: Any?
)