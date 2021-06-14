import Foundation

struct CreateWalletResponse: AccountProtocol {
    let userId: String
    let accessToken: String
    let refreshToken: String
    let expires: Date
    let coins: [CoinBalance]
    
    var account: Account {
        Account(userId: userId,
                accessToken: accessToken,
                refreshToken: refreshToken,
                expires: expires)
    }
}
