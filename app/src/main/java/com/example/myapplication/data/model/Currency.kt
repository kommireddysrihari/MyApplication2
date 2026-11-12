package com.example.myapplication.data.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String
) {
    companion object {
        val SUPPORTED_CURRENCIES = listOf(
            Currency("INR", "Indian Rupee", "₹"),
            Currency("USD", "US Dollar", "$"),
            Currency("EUR", "Euro", "€"),
            Currency("GBP", "British Pound", "£"),
            Currency("JPY", "Japanese Yen", "¥"),
            Currency("AUD", "Australian Dollar", "A$"),
            Currency("CAD", "Canadian Dollar", "C$")
        )

        fun getByCode(code: String): Currency {
            return SUPPORTED_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
                ?: Currency(code, code, code)
        }
    }
}
