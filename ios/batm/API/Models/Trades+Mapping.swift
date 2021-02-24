import ObjectMapper

extension Trade: ImmutableMappable {
    init(map: Map) throws {
        id = try map.value("id")
        type = try map.value("type")
        coin = try map.value("coin")
        status = try map.value("status")
        timestamp = try map.value("timestamp")
        price = try map.value("price")
        minLimit = try map.value("minLimit")
        maxLimit = try map.value("maxLimit")
        paymentMethods = try map.value("paymentMethods")
        terms = try map.value("terms")
        openOrders = try map.value("openOrders")
        makerId = try map.value("makerId")
        makerPublicId = try map.value("makerPublicId")
        makerLatitude = try map.value("makerLatitude")
        makerLongitude = try map.value("makerLongitude")
        makerTotalTrades = try map.value("makerTotalTrades")
        makerTradingRate = try map.value("makerTradingRate")
    }
}

//extension Order: ImmutableMappable {
//    init(map: Map) throws {
//        id = try map.value("id")
//        tradeId = try map.value("tradeId")
//        coin = try map.value("coin")
//        status = try map.value("status")
//        timestamp = try map.value("timestamp")
//        price = try map.value("price")
//        cryptoAmount = try map.value("cryptoAmount")
//        fiatAmount = try map.value("fiatAmount")
//        terms = try map.value("terms")
//        openOrders = try map.value("openOrders")
//        makerId = try map.value("makerId")
//        makerPublicId = try map.value("makerPublicId")
//        makerLatitude = try map.value("makerLatitude")
//        makerLongitude = try map.value("makerLongitude")
//        makerTotalTrades = try map.value("makerTotalTrades")
//        makerTradingRate = try map.value("makerTradingRate")
//        takerId = try map.value("takerId")
//        takerPublicId = try map.value("takerPublicId")
//        takerLatitude = try map.value("takerLatitude")
//        takerLongitude = try map.value("takerLongitude")
//        takerTotalTrades = try map.value("takerTotalTrades")
//        takerTradingRate = try map.value("takerTradingRate")
//    }
//}

extension Trades: ImmutableMappable {
    init(map: Map) throws {
        publicId = try map.value("publicId")
        status = try map.value("status")
        totalTrades = try map.value("totalTrades")
        tradingRate = try map.value("tradingRate")
        trades = try map.value("trades")
//        orders = try map.value("orders")
    }
}

