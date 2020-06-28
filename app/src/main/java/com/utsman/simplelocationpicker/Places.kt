package com.utsman.simplelocationpicker

data class Places (
    val items: List<Item>
)

data class Item (
    val title: String,
    val id: String,
    val resultType: String,
    val address: Address,
    val position: Position,
    val access: List<Position>,
    val distance: Long,
    val categories: List<Category>
)

data class Position (
    val lat: Double,
    val lng: Double
)

data class Address (
    val label: String,
    val countryCode: String,
    val countryName: String,
    val state: String,
    val county: String,
    val city: String,
    val district: String,
    val subdistrict: String,
    val street: String,
    val postalCode: String
)

data class Category (
    val id: String,
    val primary: Boolean
)