package com.greatcan.moneysaver.configuration

import com.greatcan.moneysaver.R

enum class CurrencyEnum(val title: Int, val currency: Int) {
    DOLLAR(R.string.dollar, R.string.dollar_sign),
    RUBLE(R.string.rubl, R.string.ruble_sing),
    HRIVNIA(R.string.hrivnia, R.string.hrivnia_sign)
}