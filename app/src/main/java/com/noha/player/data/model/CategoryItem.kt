package com.noha.player.data.model

data class CategoryItem(
    val name: String,
    val type: CategoryType,
    val count: Int
)

enum class CategoryType {
    GROUP,
    COUNTRY
}

