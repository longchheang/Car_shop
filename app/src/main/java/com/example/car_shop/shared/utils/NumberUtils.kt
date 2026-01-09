package com.example.car_shop.shared.utils

/**
 * Extension function to format integers with locale-specific thousand separators
 */
fun Int.toLocaleString(): String {
    return "%,d".format(this)
}

