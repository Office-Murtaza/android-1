import Foundation
import Swinject
import Moya
import CoreData

final class AppAssembly: Assembly {
  
  func assemble(container: Container) {
    assembleNetwork(container: container)
    assembleStorages(container: container)
    assembleServices(container: container)
  }
  
  private func assembleNetwork(container: Container) {
    container.register(NetworkService.self) { (ioc, url: URL) in
      let provider = MoyaProvider<MultiTarget>()
      return NetworkService(baseApiUrl: url, provider: provider)
      }.inObjectScope(.transient)
    container.register(NetworkRequestExecutor.self) { (ioc, url: URL) -> NetworkRequestExecutor in
      let network = ioc.resolve(NetworkService.self, argument: url)!
      let credentials = ioc.resolve(AccountStorage.self)!
      return BTMNetworkService(networkService: network, credentials: credentials)
      }.inObjectScope(.transient)
    container.register(APIGateway.self) { ioc in
      let baseURL = URL(string: "http://206.189.204.44:8080/api/v1")!
      let networkService = ioc.resolve(NetworkRequestExecutor.self, argument: baseURL)!
      return APIGatewayImpl(networkProvider: networkService)
      } .inObjectScope(.container)
  }
  
  fileprivate func assembleStorages(container: Container) {
    container.register(LogoutStorageRegistry.self) { _ in
      return StorageRegistry()
      }.inObjectScope(.container)
    container.register(AccountStorage.self) { ioc in
      let executor = ioc.resolve(StorageTransactionExecutor.self)!
      let storageRegistry = ioc.resolve(LogoutStorageRegistry.self)!
      let storage = AccountStorageImpl(transactionExecutor: executor)
      storageRegistry.add(storage: storage)
      return storage
      }.inObjectScope(.container)
    container.register(BTMWalletStorage.self) { ioc in
      let executor = ioc.resolve(StorageTransactionExecutor.self)!
      let storageRegistry = ioc.resolve(LogoutStorageRegistry.self)!
      let storage = BTMWalletStorageImpl(transactionExecutor: executor)
      storageRegistry.add(storage: storage)
      return storage
      }.inObjectScope(.container)
  }
  
  fileprivate func assembleServices(container: Container) {
    container.register(WalletService.self) { ioc in
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return WalletServiceImpl(walletStorage: walletStorage)
      }.inObjectScope(.container)
  }
}
// swiftlint:enable type_body_length
