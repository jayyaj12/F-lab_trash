package com.aos.f_lab_trash.view.ui.theme

data class Item(
    val id: Int,
    val name: String,
    val countdownSeconds: Int? = null,
    var category: Category = Category.NORMAL
)

enum class Category {
    NORMAL, DUMP;

    fun opposite(): Category = if (this == NORMAL) DUMP else NORMAL
}