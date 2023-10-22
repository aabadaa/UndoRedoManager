package com.abada.undoredomanager

import android.os.Parcelable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.parcelize.Parcelize

@Parcelize
class ParcelableTextFieldValue constructor(
    val text: String = "",
    val selection: ParcelableTextRange = TextRange.Zero.asParcelable,
    val composition: ParcelableTextRange? = null
) : Parcelable {

    val asTextFieldValue
        get() = TextFieldValue(
            text = text,
            selection = selection.asTextRange,
            composition = composition?.asTextRange
        )

    fun isEmpty() = text.isEmpty()
    fun isNotEmpty() = text.isNotEmpty()

}

@Parcelize
class ParcelableTextRange(
    val start: Int,
    val end: Int,
) : Parcelable {
    val asTextRange get() = TextRange(start, end)
}

val TextRange.asParcelable get() = ParcelableTextRange(start, end)
val TextFieldValue.asParcelable
    get() = ParcelableTextFieldValue(
        text,
        selection.asParcelable,
        composition?.asParcelable
    )
val String.asTFV
    get() = TextFieldValue(
        this,
        TextRange(length, length)
    )

val String.asParcelableTFV get() = asTFV.asParcelable
val String.asTextFieldValue get() = asTFV.asParcelable

