package com.tonio.albarapp.data

data class LineItem(
    var description: String = "",
    var quantity: Double = 1.0,
    var unit: String = "ud",          // e.g., ud, h, m3
    var unitPriceCents: Long = 0      // store currency in cents
) {
    val lineTotalCents: Long
        get() = (quantity * unitPriceCents).toLong()
}

