package com.belcobtm.presentation.screens.wallet.trade.reserve

sealed class InputFieldState {
    object Valid: InputFieldState()
    object LessThanNeedError: InputFieldState()
    object MoreThanNeedError: InputFieldState()
    object NotEnoughETHError: InputFieldState()
}