import Foundation
import RxSwift
import TrustWalletCore

enum CreateTransactionError: Error {
  case coinTypeNotSupported
  case cantCreate
}

protocol WalletService {
  func createWallet() -> Completable
  func recoverWallet(seedPhrase: String) -> Completable
  func getTransactionHex(for coin: BTMCoin, destination: String, amount: Double) -> Completable
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
  
  func getTransactionHex(for coin: BTMCoin, destination: String, amount: Double) -> Completable {
    switch coin.type {
    case .bitcoin, .bitcoinCash, .litecoin:
      return getBitcoinLikeTransactionHex(for: coin, to: destination, amount: amount)
    case .ethereum:
      return getEthereumTransactionHex(for: coin, to: destination, amount: amount)
    case .tron:
      return getTronTransactionHex(for: coin, to: destination, amount: amount)
    default: return .error(CreateTransactionError.coinTypeNotSupported)
    }
  }
  
  func getBitcoinLikeTransactionHex(for coin: BTMCoin, to destination: String, amount: Double) -> Completable {
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
                                                                         toAddress: destination,
                                                                         amount: amount,
                                                                         utxos: $0,
                                                                         wallet: wallet) }
          .map { (account, $0) }
      }
      .flatMapCompletable { [api] account, txhex in
        return api.submitTransaction(userId: account.userId, type: coin.type, txhex: txhex)
      }
  }
  
  private func getBitcoinLikeTransactionHex(coin: BTMCoin,
                                            toAddress: String,
                                            amount: Double,
                                            utxos: [Utxo],
                                            wallet: HDWallet) throws -> String {
    let amountInUnits = Int64(amount * Double(coin.type.unit))
    
    var input = BitcoinSigningInput.with {
      $0.hashType = coin.type.hashType
      $0.amount = amountInUnits
      $0.byteFee = Int64(coin.type.feePerByte)
      $0.changeAddress = coin.publicKey
      $0.toAddress = toAddress
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
  
  func getEthereumTransactionHex(for coin: BTMCoin, to destination: String, amount: Double) -> Completable {
    return accountStorage.get()
      .map { [unowned self] account -> (Account, String) in
        let txhex = try self.getEthereumTransactionHex(coin: coin, toAddress: destination, amount: amount)
        return (account, txhex)
      }
      .flatMapCompletable { [api] account, txhex in
        return api.submitTransaction(userId: account.userId, type: coin.type, txhex: txhex)
      }
  }
  
  private func getEthereumTransactionHex(coin: BTMCoin, toAddress: String, amount: Double) throws -> String {
    let nonce = 1 // replace with actual api request
    let million = 1_000_000
    let castedAmountMultipliedByMillion = Int(amount * Double(million))
    let oneMillionthEth = coin.type.unit / million
    let totalAmount = oneMillionthEth * castedAmountMultipliedByMillion
    
    let nonceHex = String(format: "%016llx", nonce)
    let amountHex = String(format: "%016llx", totalAmount)
    let gasLimitHex = String(format: "%016llx", coin.type.gasLimit)
    let gasPriceHex = String(format: "%016llx", coin.type.gasPrice)
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let input = EthereumSigningInput.with {
      $0.chainID = Data(hexString: "01")!
      $0.nonce = Data(hexString: nonceHex)!
      $0.gasLimit = Data(hexString: gasLimitHex)!
      $0.gasPrice = Data(hexString: gasPriceHex)!
      $0.amount = Data(hexString: amountHex)!
      $0.toAddress = toAddress
      $0.privateKey = privateKey
    }
    
    let output = EthereumSigner.sign(input: input)
    let transactionHex = "0x" + output.encoded.hexString
    
    return transactionHex
  }
  
  func getTronTransactionHex(for coin: BTMCoin, to destination: String, amount: Double) -> Completable {
    return accountStorage.get()
      .flatMap { [unowned self] account in
        return self.api.getTronBlockHeader()
          .map { try self.getTronTransactionJson(coin: coin, toAddress: destination, amount: amount, blockHeader: $0) }
          .map { (account, $0) }
      }
      .flatMapCompletable { [api] account, json in
        guard
          let data = json.data(using: .utf8),
          let jsonObject = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
        else {
          throw CreateTransactionError.cantCreate
        }
        
        
        return api.submitTronTransaction(json: jsonObject)
      }
  }
  
    private func getTronTransactionJson(coin: BTMCoin,
                                        toAddress: String,
                                        amount: Double,
                                        blockHeader: BTMTronBlockHeader) throws -> String {
    let castedAmount = Int64(amount * Double(coin.type.unit))
    
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let transfer = TronTransferContract.with {
      $0.ownerAddress = coin.publicKey
      $0.toAddress = toAddress
      $0.amount = castedAmount
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
      $0.feeLimit = 1_000_000
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
  
  private func getBinanceTransactionId(for coin: BTMCoin, destination: String, amount: Int64) throws -> String {
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    var signingInput = TW_Binance_Proto_SigningInput()
    signingInput.chainID = "Binance-Chain-Nile"
    signingInput.accountNumber = 0
    signingInput.sequence = 0
    
    signingInput.privateKey = privateKey
    
    var token = TW_Binance_Proto_SendOrder.Token()
    token.denom = "BNB"
    token.amount = amount
    
    var input = TW_Binance_Proto_SendOrder.Input()
    input.address = CosmosAddress(string: coin.publicKey)!.keyHash
    input.coins = [token]
    
    var output = TW_Binance_Proto_SendOrder.Output()
    output.address = CosmosAddress(string: destination)!.keyHash
    output.coins = [token]
    
    var sendOrder = TW_Binance_Proto_SendOrder()
    sendOrder.inputs = [input]
    sendOrder.outputs = [output]
    
    signingInput.sendOrder = sendOrder
    
    let data = BinanceSigner.sign(input: signingInput)
    let transactionId = Hash.keccak256(data: data.encoded).hexString
    
    return transactionId
  }
  
  private func getRippleTransactionId(for coin: BTMCoin, destination: String, amount: Int64) throws -> String {
    guard let privateKey = Data(hexString: coin.privateKey) else {
      throw CreateTransactionError.cantCreate
    }
    
    let input = RippleSigningInput.with {
      $0.account = coin.publicKey
      $0.destination = destination
      $0.amount = amount
      $0.privateKey = privateKey
    }
    
    let output = RippleSigner.sign(input: input)
    let transactionId = Hash.keccak256(data: output.encoded).hexString
    
    return transactionId
  }
  
}
