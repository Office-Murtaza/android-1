import Foundation
import RxSwift
import TrustWalletCore
import BigNumber

enum CreateTransactionError: Error {
  case cantCreate
}

protocol WalletService {
  func createWallet() -> Completable
  func recoverWallet(seedPhrase: String) -> Completable
  func getTransactionHex(for coin: BTMCoin,
                         with coinDetails: CoinDetails,
                         destination: String,
                         amount: Decimal,
                         stakingType: TransactionType?) -> Single<String>
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
    let hdWallet = HDWallet(strength: BTMWallet.seedPhraseStrength, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
  func recoverWallet(seedPhrase: String) -> Completable {
    let hdWallet = HDWallet(mnemonic: seedPhrase, passphrase: "")
    let btmWallet = BTMWallet(hdWallet: hdWallet)
    
    return walletStorage.save(wallet: btmWallet)
  }
  
  func getTransactionHex(for coin: BTMCoin,
                         with coinDetails: CoinDetails,
                         destination: String,
                         amount: Decimal,
                         stakingType: TransactionType? = nil) -> Single<String> {
    switch coin.type {
    case .bitcoin, .bitcoinCash, .litecoin, .dash, .doge:
      return getBitcoinLikeTransactionHex(for: coin, with: coinDetails, to: destination, amount: amount)
    case .ethereum, .catm, .usdt:
      return getEthereumTransactionHex(for: coin, with: coinDetails, to: destination, amount: amount, stakingType: stakingType)
    case .tron:
      return getTronTransactionHex(for: coin, with: coinDetails, to: destination, amount: amount)
    case .binance:
      return getBinanceTransactionHex(for: coin, to: destination, amount: amount)
    case .ripple:
      return getRippleTransactionHex(for: coin, with: coinDetails, to: destination, amount: amount)
    }
  }
  
  func getBitcoinLikeTransactionHex(for coin: BTMCoin,
                                    with coinDetails: CoinDetails,
                                    to destination: String,
                                    amount: Decimal) -> Single<String> {
    return walletStorage.get()
      .map { HDWallet(mnemonic: $0.seedPhrase, passphrase: "") }
      .map { ($0.getExtendedPublicKey(purpose: coin.type.customPurpose,
                                      coin: coin.type.defaultCoinType,
                                      version: coin.type.customVersion), $0) }
      .flatMap { [api] xpub, wallet in
        return api.getUtxos(type: coin.type, xpub: xpub)
          .map { [unowned self] in try self.getBitcoinLikeTransactionHex(coin: coin,
                                                                         coinDetails: coinDetails,
                                                                         toAddress: destination,
                                                                         amount: amount,
                                                                         utxos: $0,
                                                                         wallet: wallet) }
    }
  }
  
  private func getBitcoinLikeTransactionHex(coin: BTMCoin,
                                            coinDetails: CoinDetails,
                                            toAddress: String,
                                            amount: Decimal,
                                            utxos: [Utxo],
                                            wallet: HDWallet) throws -> String {
    guard let amountInUnits = (amount * Decimal(coin.type.unit)).int64Value else {
      throw CreateTransactionError.cantCreate
    }
    
    var input = BitcoinSigningInput.with {
      $0.hashType = coin.type.hashType
      $0.amount = amountInUnits
      $0.byteFee = Int64(coinDetails.byteFee ?? 0)
      $0.changeAddress = coin.address
      $0.toAddress = toAddress
      $0.coinType = coin.type.defaultCoinType.rawValue
    }
    
    utxos.compactMap { DerivationPath($0.path) }.forEach {
      let privateKey = wallet.getKey(coin: coin.type.defaultCoinType, derivationPath: $0.description)
      input.privateKey.append(privateKey.data)
    }
    
    utxos.forEach {
      let redeemScript = BitcoinScript.lockScriptForAddress(address: $0.address, coin: coin.type.defaultCoinType)
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
      let redeemScript = BitcoinScript.lockScriptForAddress(address: utxo.address, coin: coin.type.defaultCoinType)
      
      let utxo0 = BitcoinUnspentTransaction.with {
        $0.script = redeemScript.data
        $0.amount = amount
        $0.outPoint = outpoint
      }
      input.utxo.append(utxo0)
    }
    
    let output: BitcoinSigningOutput = AnySigner.sign(input: input, coin: coin.type.defaultCoinType)
    
    return output.encoded.hexString
  }
  
  func getEthereumTransactionHex(for coin: BTMCoin,
                                 with coinDetails: CoinDetails,
                                 to destination: String,
                                 amount: Decimal,
                                 stakingType: TransactionType? = nil) -> Single<String> {
    return api.getNonce(type: coin.type, address: coin.address)
      .map { [unowned self] in try self.getEthereumTransactionHex(coin: coin,
                                                                  coinDetails: coinDetails,
                                                                  toAddress: destination,
                                                                  amount: amount,
                                                                  nonce: $0.nonce,
                                                                  stakingType: stakingType) }
  }
  
  private func getEthereumTransactionHex(coin: BTMCoin,
                                         coinDetails: CoinDetails,
                                         toAddress: String,
                                         amount: Decimal,
                                         nonce: Int,
                                         stakingType: TransactionType? = nil) throws -> String {
    let divider: Int64 = Int64(10.pow(CustomCoinType.maxNumberOfFractionDigits))
    
    let dividerthUnit = coin.type.unit / divider
    guard let amountMultipliedByDivider = (amount * Decimal(divider)).int64Value else {
      throw CreateTransactionError.cantCreate
    }
    
    let bigUnit = BInt(dividerthUnit)
    let bigAmount = BInt(amountMultipliedByDivider)
    let bigAmountInUnits = bigUnit * bigAmount
    let hexAmountInUnits = bigAmountInUnits.asString(radix: 16).leadingZeros(64)
    let dataAmount = Data(hexString: hexAmountInUnits)!
    
    let hexNonce = BInt(nonce).asString(radix: 16).leadingZeros(64)
    let hexGasLimit = BInt(coinDetails.gasLimit ?? 0).asString(radix: 16).leadingZeros(64)
    let hexGasPrice = BInt(coinDetails.gasPrice ?? 0).asString(radix: 16).leadingZeros(64)
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    var input = EthereumSigningInput.with {
      $0.chainID = Data(hexString: "01")!
      $0.nonce = Data(hexString: hexNonce)!
      $0.gasLimit = Data(hexString: hexGasLimit)!
      $0.gasPrice = Data(hexString: hexGasPrice)!
      $0.privateKey = privateKey
    }
    
    if coin.type.isETHBased {
      let function: EthereumAbiFunction
      
      if let stakingType = stakingType {
        switch stakingType {
        case .createStake:
          function = EthereumAbiFunction(name: "createStake")
          function.addParamUInt256(val: dataAmount, isOutput: false)
        case .cancelStake:
          function = EthereumAbiFunction(name: "cancelStake")
        case .withdrawStake:
          function = EthereumAbiFunction(name: "withdrawStake")
        default:
          fatalError("Unsupported transaction type for staking")
        }
      } else {
        function = EthereumAbiFunction(name: "transfer")
        function.addParamAddress(val: Data(hexString: toAddress)!, isOutput: false)
        function.addParamUInt256(val: dataAmount, isOutput: false)
      }
      
      input.payload = EthereumAbi.encode(fn: function)
      input.toAddress = coinDetails.contractAddress ?? ""
    } else {
      input.amount = dataAmount
      input.toAddress = toAddress
    }
    
    let output: EthereumSigningOutput = AnySigner.sign(input: input, coin: coin.type.defaultCoinType)
    let transactionHex = "0x" + output.encoded.hexString
    
    return transactionHex
  }
    
  func getTronTransactionHex(for coin: BTMCoin,
                             with coinDetails: CoinDetails,
                             to destination: String,
                             amount: Decimal) -> Single<String> {
    return api.getTronBlockHeader(type: coin.type)
      .map { [unowned self] in try self.getTronTransactionJson(coin: coin,
                                                               coinDetails: coinDetails,
                                                               toAddress: destination,
                                                               amount: amount,
                                                               blockHeader: $0) }
  }
  
  private func getTronTransactionJson(coin: BTMCoin,
                                      coinDetails: CoinDetails,
                                      toAddress: String,
                                      amount: Decimal,
                                      blockHeader: BTMTronBlockHeader) throws -> String {
    guard let amountInUnits = (amount * Decimal(coin.type.unit)).int64Value else {
      throw CreateTransactionError.cantCreate
    }
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let transfer = TronTransferContract.with {
      $0.ownerAddress = coin.address
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
      $0.blockHeader = blockHeader
    }
    
    let input = TronSigningInput.with {
      $0.transaction = transaction
      $0.privateKey = privateKey
    }
    
    let output: TronSigningOutput = AnySigner.sign(input: input, coin: coin.type.defaultCoinType)
    let transactionHex = output.json
    
    return transactionHex
  }
  
  func getBinanceTransactionHex(for coin: BTMCoin, to destination: String, amount: Decimal) -> Single<String> {
    return api.getBinanceAccountInfo(type: coin.type, address: coin.address)
      .map { [unowned self] in try self.getBinanceTransactionHex(coin: coin,
                                                                 toAddress: destination,
                                                                 amount: amount,
                                                                 accountInfo: $0) }
  }
  
  private func getBinanceTransactionHex(coin: BTMCoin,
                                        toAddress: String,
                                        amount: Decimal,
                                        accountInfo: BinanceAccountInfo) throws -> String {
    guard let amountInUnits = (amount * Decimal(coin.type.unit)).int64Value else {
      throw CreateTransactionError.cantCreate
    }
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let token = BinanceSendOrder.Token.with {
      $0.denom = "BNB"
      $0.amount = amountInUnits
    }
    
    let orderInput = BinanceSendOrder.Input.with {
      $0.address = AnyAddress(string: coin.address, coin: .binance)!.data
      $0.coins = [token]
    }
    
    let orderOutput = BinanceSendOrder.Output.with {
      $0.address = AnyAddress(string: toAddress, coin: .binance)!.data
      $0.coins = [token]
    }
    
    let sendOrder = BinanceSendOrder.with {
      $0.inputs = [orderInput]
      $0.outputs = [orderOutput]
    }
    
    let input = BinanceSigningInput.with {
      $0.chainID = accountInfo.chainId
      $0.accountNumber = Int64(accountInfo.accountNumber)
      $0.sequence = Int64(accountInfo.sequence)
      $0.privateKey = privateKey
      $0.sendOrder = sendOrder
    }
    
    let output: BinanceSigningOutput = AnySigner.sign(input: input, coin: coin.type.defaultCoinType)
    let transactionHex = output.encoded.hexString
    
    return transactionHex
  }
  
  func getRippleTransactionHex(for coin: BTMCoin,
                               with coinDetails: CoinDetails,
                               to destination: String,
                               amount: Decimal) -> Single<String> {
    return api.getRippleSequence(type: coin.type, address: coin.address)
      .map { [unowned self] in try self.getRippleTransactionHex(coin: coin,
                                                                coinDetails: coinDetails,
                                                                toAddress: destination,
                                                                amount: amount,
                                                                sequence: $0.sequence) }
  }
    
    
  
  private func getRippleTransactionHex(coin: BTMCoin,
                                       coinDetails: CoinDetails,
                                       toAddress: String,
                                       amount: Decimal,
                                       sequence: Int) throws -> String {
    guard let amountInUnits = (amount * Decimal(coin.type.unit)).int64Value else {
      throw CreateTransactionError.cantCreate
    }
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let input = RippleSigningInput.with {
      $0.account = coin.address
      $0.destination = toAddress
      $0.amount = amountInUnits
      $0.fee = coin.transactionFee(fee: coinDetails.txFee)
      $0.sequence = Int32(sequence)
      $0.privateKey = privateKey
    }
    
    let output: RippleSigningOutput = AnySigner.sign(input: input, coin: coin.type.defaultCoinType)
    let transactionHex = output.encoded.hexString
    
    return transactionHex
  }
}
