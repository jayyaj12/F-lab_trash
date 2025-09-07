package com.aos.f_lab_trash.view.ui.theme

data class Item(
    val id: Int,
    val name: String,
    var countdownSeconds: Int? = null,
    var category: Category = Category.Normal
)

sealed class Category(val type: Type) {
    object Normal : Category(Type.NORMAL)
    object Dump : Category(Type.DUMP)

    fun getCompareType(): Type {
        return when (this) {
            Normal -> Type.DUMP
            Dump -> Type.NORMAL
        }
    }
}

enum class Type {
    NORMAL, DUMP
}