package com.app.belcobtm.data.rest.wallet.request

data class CoinToCoinExchangeRequest(
    val amount: Double,
    val coinCode: String,
    val refCoinCode: String,
    val hex: String
)


//И создаешь через либку транзакцию, в которой from: твой коин адрес, to: тот адрес кошелька на сервере, и amount, который ввел юзер, потом этот хекс посылаешь в ту апишку
//
//"hex": "c1502643bcd58b52fa89cae452fa9d98df4b2155588f3b92b4ff92c30fa3b9df", - хекс из полученной транзакции
// "coinCode": "BTC", - твой коин
// "amount": 0.3, - количество твоего коина, который юзер ввел
// "refCoinCode": "LTC", - коин куда юзер хочет обменять
// "refAmount": 45.62, - это поле лишнее, я попросил Костю выпилить его
// "profitC2C": 10 - это поле тоже лишнее, ибо сервер и так знает это значение, ведь мы его у него же и получаем