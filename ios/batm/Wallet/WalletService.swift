import Foundation
import RxSwift
import TrustWalletCore
import BigNumber

enum CreateTransactionError: Error {
  case coinTypeNotSupported
  case cantCreate
}

protocol WalletService {
  func createWallet() -> Completable
  func recoverWallet(seedPhrase: String) -> Completable
  func getTransactionHex(for coin: BTMCoin,
                         with coinSettings: CoinSettings,
                         destination: String,
                         amount: Double) -> Single<String>
}

class WalletServiceImpl: WalletService {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
  }

  func createWallet() -> Completable {
    let hdWallet = HDWallet(strength: 128, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
  func recoverWallet(seedPhrase: String) -> Completable {
    let hdWallet = HDWallet(mnemonic: seedPhrase, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
  func getTransactionHex(for coin: BTMCoin,
                         with coinSettings: CoinSettings,
                         destination: String,
                         amount: Double) -> Single<String> {
    switch coin.type {
    case .bitcoin, .bitcoinCash, .litecoin:
      return getBitcoinLikeTransactionHex(for: coin, with: coinSettings, to: destination, amount: amount)
    case .ethereum:
      return getEthereumTransactionHex(for: coin, with: coinSettings, to: destination, amount: amount)
    case .tron:
      return getTronTransactionHex(for: coin, with: coinSettings, to: destination, amount: amount)
    case .binance:
      return getBinanceTransactionHex(for: coin, to: destination, amount: amount)
    case .xrp:
      return getRippleTransactionHex(for: coin, with: coinSettings, to: destination, amount: amount)
    default: return .error(CreateTransactionError.coinTypeNotSupported)
    }
  }
  
  func getBitcoinLikeTransactionHex(for coin: BTMCoin,
                                    with coinSettings: CoinSettings,
                                    to destination: String,
                                    amount: Double) -> Single<String> {
    return accountStorage.get()
      .flatMap { [walletStorage] account in
        return walletStorage.get()
          .map { HDWallet(mnemonic: $0.seedPhrase, passphrase: "") }
          .map { ($0.getExtendedPubKey(purpose: coin.type.customPurpose, coin: coin.type, version: coin.type.customVersion), $0) }
          .map { (account, $0, $1) }
      }
      .flatMap { [api] account, xpub, wallet in
        return api.getUtxos(userId: account.userId, type: coin.type, xpub: xpub)
          .map { [unowned self] in try self.getBitcoinLikeTransactionHex(coin: coin,
                                                                         coinSettings: coinSettings,
                                                                         toAddress: destination,
                                                                         amount: amount,
                                                                         utxos: $0,
                                                                         wallet: wallet) }
      }
  }
  
  private func getBitcoinLikeTransactionHex(coin: BTMCoin,
                                            coinSettings: CoinSettings,
                                            toAddress: String,
                                            amount: Double,
                                            utxos: [Utxo],
                                            wallet: HDWallet) throws -> String {
    let amountInUnits = Int64(amount * Double(coin.type.unit))
    
    var input = BitcoinSigningInput.with {
      $0.hashType = coin.type.hashType
      $0.amount = amountInUnits
      $0.byteFee = coin.transactionFee(fee: coinSettings.byteFee ?? 0)
      $0.changeAddress = coin.publicKey
      $0.toAddress = toAddress
      $0.coinType = coin.type.rawValue
    }
    
    utxos.compactMap { DerivationPath($0.path) }.forEach {
      let privateKey = wallet.getKey(at: $0)
      input.privateKey.append(privateKey.data)
    }
    
    utxos.forEach {
      let redeemScript = BitcoinScript.buildForAddress(address: $0.address, coin: coin.type)
      let keyHash: Data?
      
      if (redeemScript.isPayToWitnessScriptHash) {
        keyHash = redeemScript.matchPayToWitnessPublicKeyHash()
      } else {
        keyHash = redeemScript.matchPayToPubkeyHash()
      }
      
      if let key = keyHash?.hexString {
        input.scripts[key] = redeemScript.data
      }
    }
    
    try utxos.enumerated().forEach { (currentIndex, utxo) in
      guard let hash = Data(hexString: utxo.txid) else {
        throw CreateTransactionError.cantCreate
      }
      let reversedHash = Data(hash.reversed())
      let index = UInt32(utxo.vout)
      let sequence = UInt32.max - UInt32(utxos.count) + UInt32(currentIndex)
      
      let outpoint = BitcoinOutPoint.with {
        $0.hash = reversedHash
        $0.index = index
        $0.sequence = sequence
      }
      
      guard let amount = Int64(utxo.value) else {
        throw CreateTransactionError.cantCreate
      }
      let redeemScript = BitcoinScript.buildForAddress(address: utxo.address, coin: coin.type)
      
      let utxo0 = BitcoinUnspentTransaction.with {
        $0.script = redeemScript.data
        $0.amount = amount
        $0.outPoint = outpoint
      }
      input.utxo.append(utxo0)
    }
    
    let signer = BitcoinTransactionSigner(input: input)
    let result = signer.sign()
    
    guard let output = try? BitcoinSigningOutput(unpackingAny: result.objects[0]) else {
      throw CreateTransactionError.cantCreate
    }
    
    return output.encoded.hexString
  }
  
  func getEthereumTransactionHex(for coin: BTMCoin,
                                 with coinSettings: CoinSettings,
                                 to destination: String,
                                 amount: Double) -> Single<String> {
    return accountStorage.get()
      .flatMap { [api] in api.getNonce(userId: $0.userId, type: coin.type, address: coin.publicKey) }
      .map { [unowned self] in try self.getEthereumTransactionHex(coin: coin,
                                                                  coinSettings: coinSettings,
                                                                  toAddress: destination,
                                                                  amount: amount,
                                                                  nonce: $0.nonce) }
  }
  
  private func getEthereumTransactionHex(coin: BTMCoin,
                                         coinSettings: CoinSettings,
                                         toAddress: String,
                                         amount: Double,
                                         nonce: Int) throws -> String {
    let divider: Int64 = Int64(10.pow(CoinType.maxNumberOfFractionDigits))
    
    let dividerthUnit = coin.type.unit / divider
    let amountMultipliedByDivider = Int64(amount * Double(divider))
    
    let bigUnit = BInt(dividerthUnit)
    let bigAmount = BInt(amountMultipliedByDivider)
    let bigAmountInUnits = bigUnit * bigAmount
    let hexAmountInUnits = bigAmountInUnits.asString(radix: 16).leadingZeros(64)
    
    let hexNonce = BInt(nonce).asString(radix: 16).leadingZeros(64)
    let hexGasLimit = BInt(coinSettings.gasLimit ?? 0).asString(radix: 16).leadingZeros(64)
    let hexGasPrice = BInt(coinSettings.gasPrice ?? 0).asString(radix: 16).leadingZeros(64)
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let input = EthereumSigningInput.with {
      $0.chainID = Data(hexString: "01")!
      $0.nonce = Data(hexString: hexNonce)!
      $0.gasLimit = Data(hexString: hexGasLimit)!
      $0.gasPrice = Data(hexString: hexGasPrice)!
      $0.amount = Data(hexString: hexAmountInUnits)!
      $0.toAddress = toAddress
      $0.privateKey = privateKey
    }
    
    let output = EthereumSigner.sign(input: input)
    let transactionHex = "0x" + output.encoded.hexString
    
    return transactionHex
  }
  
  func getTronTransactionHex(for coin: BTMCoin,
                             with coinSettings: CoinSettings,
                             to destination: String,
                             amount: Double) -> Single<String> {
    return accountStorage.get()
      .flatMap { [unowned self] in self.api.getTronBlockHeader(userId: $0.userId, type: coin.type) }
      .map { [unowned self] in try self.getTronTransactionJson(coin: coin,
                                                               coinSettings: coinSettings,
                                                               toAddress: destination,
                                                               amount: amount,
                                                               blockHeader: $0) }
  }
  
    private func getTronTransactionJson(coin: BTMCoin,
                                        coinSettings: CoinSettings,
                                        toAddress: String,
                                        amount: Double,
                                        blockHeader: BTMTronBlockHeader) throws -> String {
    let amountInUnits = Int64(amount * Double(coin.type.unit))
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let transfer = TronTransferContract.with {
      $0.ownerAddress = coin.publicKey
      $0.toAddress = toAddress
      $0.amount = amountInUnits
    }
    
    let timestamp = Int64(Date().timeIntervalSince1970 * 1000)
    let expiration = Int64(Date(timeIntervalSinceNow: 60 * 60 * 10).timeIntervalSince1970 * 1000)
    
    let blockHeader = TronBlockHeader.with {
      $0.timestamp = Int64(blockHeader.timestamp)
      $0.txTrieRoot = Data(hexString: blockHeader.txTrieRoot)!
      $0.parentHash = Data(hexString: blockHeader.parentHash)!
      $0.number = Int64(blockHeader.number)
      $0.witnessAddress = Data(hexString: blockHeader.witnessAddress)!
      $0.version = Int32(blockHeader.version)
    }
    
    let transaction = TronTransaction.with {
      $0.transfer = transfer
      $0.timestamp = timestamp
      $0.expiration = expiration
      $0.feeLimit = coin.transactionFee(fee: coinSettings.txFee)
      $0.blockHeader = blockHeader
    }
    
    let input = TronSigningInput.with {
      $0.transaction = transaction
      $0.privateKey = privateKey
    }
    
    let output = TronSigner.sign(input: input)
    let transactionHex = output.json
    
    return transactionHex
  }
  
  func getBinanceTransactionHex(for coin: BTMCoin, to destination: String, amount: Double) -> Single<String> {
    return accountStorage.get()
      .flatMap { [unowned self] in self.api.getBinanceAccountInfo(userId: $0.userId, type: coin.type) }
      .map { [unowned self] in try self.getBinanceTransactionHex(coin: coin,
                                                                 toAddress: destination,
                                                                 amount: amount,
                                                                 accountInfo: $0) }
  }
  
  private func getBinanceTransactionHex(coin: BTMCoin,
                                        toAddress: String,
                                        amount: Double,
                                        accountInfo: BinanceAccountInfo) throws -> String {
    let amountInUnits = Int64(amount * Double(coin.type.unit))
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    var signingInput = BinanceSigningInput()
    signingInput.chainID = accountInfo.chainId
    signingInput.accountNumber = Int64(accountInfo.accountNumber)
    signingInput.sequence = Int64(accountInfo.sequence)
    
    signingInput.privateKey = privateKey
    
    var token = BinanceSendOrder.Token()
    token.denom = "BNB"
    token.amount = amountInUnits
    
    var input = BinanceSendOrder.Input()
    input.address = CosmosAddress(string: coin.publicKey)!.keyHash
    input.coins = [token]
    
    var output = BinanceSendOrder.Output()
    output.address = CosmosAddress(string: toAddress)!.keyHash
    output.coins = [token]
    
    var sendOrder = BinanceSendOrder()
    sendOrder.inputs = [input]
    sendOrder.outputs = [output]
    
    signingInput.sendOrder = sendOrder
    
    let data = BinanceSigner.sign(input: signingInput)
    let transactionHex = data.encoded.hexString
    
    return transactionHex
  }
  
  func getRippleTransactionHex(for coin: BTMCoin,
                               with coinSettings: CoinSettings,
                               to destination: String,
                               amount: Double) -> Single<String> {
    return accountStorage.get()
      .flatMap { [unowned self] in self.api.getRippleSequence(userId: $0.userId, type: coin.type) }
      .map { [unowned self] in try self.getRippleTransactionHex(coin: coin,
                                                                coinSettings: coinSettings,
                                                                toAddress: destination,
                                                                amount: amount,
                                                                sequence: $0.sequence) }
  }
  
  private func getRippleTransactionHex(coin: BTMCoin,
                                       coinSettings: CoinSettings,
                                       toAddress: String,
                                       amount: Double,
                                       sequence: Int) throws -> String {
    let amountInUnits = Int64(amount * Double(coin.type.unit))
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let input = RippleSigningInput.with {
      $0.account = coin.publicKey
      $0.destination = toAddress
      $0.amount = amountInUnits
      $0.fee = coin.transactionFee(fee: coinSettings.txFee)
      $0.sequence = Int32(sequence)
      $0.privateKey = privateKey
    }
    
    let output = RippleSigner.sign(input: input)
    let transactionHex = output.encoded.hexString
    
    return transactionHex
  }
  
}
