package com.greatcan.moneysaver.models

data class UserMoneyModel(
        var income: Double = 0.0,
        var expense: Double = 0.0,
        var monthBalance: Double = 0.0,
        var balance: Double = 0.0
)