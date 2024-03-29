package com.abada.undoredo


data class Commit(
    val state: Map<String, Any>,
    val changes: Map<String, Any>,
    val revertedChanges: Map<String, Any>,
    val onRevert: ((Any?) -> Unit)?,
    val commitAction: (() -> Any?)?,
    var revertResult: Any? = null,
)