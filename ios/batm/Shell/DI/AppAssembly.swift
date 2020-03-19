import Foundation
import Swinject
import Moya
import CoreData

final class AppAssembly: Assembly {
  
  enum Keys: String {
    case pinCodeModule
    case testApiUrl
    case prodApiUrl
  }
  
  func assemble(container: Container) {
    assembleNetwork(container: container)
    assembleStorages(container: container)
    assembleServices(container: container)
    assembleUsecases(container: container)
  }
  
  private func assembleNetwork(container: Container) {
    container.register(URL.self, name: Keys.testApiUrl.rawValue) { _ in URL(string: "https://test.belcobtm.com/api/v1")! }
    container.register(URL.self, name: Keys.prodApiUrl.rawValue) { _ in URL(string: "https://prod.belcobtm.com/api/v1")! }
    container.register(NetworkService.self) { (ioc, baseUrl: URL) in
      let provider = MoyaProvider<MultiTarget>()
      return NetworkService(baseApiUrl: baseUrl, provider: provider)
      }.inObjectScope(.transient)
    container.register(NetworkRequestExecutor.self) { (ioc, baseUrl: URL) in
      let network = ioc.resolve(NetworkService.self, argument: baseUrl)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let logoutUsecase = ioc.resolve(LogoutUsecase.self)!
      let pinCodeService = ioc.resolve(PinCodeService.self)!
      let refreshCredentialsService = ioc.resolve(RefreshCredentialsService.self)!
      return BTMNetworkService(networkService: network,
                               accountStorage: accountStorage,
                               logoutUsecase: logoutUsecase,
                               pinCodeService: pinCodeService,
                               refreshCredentialsService: refreshCredentialsService)
      }
      .inObjectScope(.transient)
    container.register(RefreshCredentialsService.self) { ioc in
      let apiUrl = ioc.resolve(URL.self, name: Keys.prodApiUrl.rawValue)!
      let networkService = ioc.resolve(NetworkService.self, argument: apiUrl)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let logoutUsecase = ioc.resolve(LogoutUsecase.self)!
      return RefreshCredentialsServiceImpl(networkService: networkService,
                                           accountStorage: accountStorage,
                                           logoutUsecase: logoutUsecase)
      }.inObjectScope(.container)
    container.register(APIGateway.self) { ioc in
      let apiUrl = ioc.resolve(URL.self, name: Keys.prodApiUrl.rawValue)!
      let networkService = ioc.resolve(NetworkRequestExecutor.self, argument: apiUrl)!
      return APIGatewayImpl(networkProvider: networkService)
      } .inObjectScope(.container)
  }
  
  fileprivate func assembleStorages(container: Container) {
    container.register(LogoutStorageRegistry.self) { _ in
      return LogoutStorageRegistryImpl()
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
    container.register(PinCodeStorage.self) { ioc in
      let executor = ioc.resolve(StorageTransactionExecutor.self)!
      let storageRegistry = ioc.resolve(LogoutStorageRegistry.self)!
      let storage = PinCodeStorageImpl(transactionExecutor: executor)
      storageRegistry.add(storage: storage)
      return storage
      }.inObjectScope(.container)
  }
  
  fileprivate func assembleServices(container: Container) {
    container.register(WalletService.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return WalletServiceImpl(api: api,
                               accountStorage: accountStorage,
                               walletStorage: walletStorage)
      }.inObjectScope(.container)
  }
  
  fileprivate func assembleUsecases(container: Container) {
    container.register(LoginUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      let pinCodeStorage = ioc.resolve(PinCodeStorage.self)!
      let walletService = ioc.resolve(WalletService.self)!
      return LoginUsecaseImpl(api: api,
                              accountStorage: accountStorage,
                              walletStorage: walletStorage,
                              pinCodeStorage: pinCodeStorage,
                              walletService: walletService)
      }.inObjectScope(.container)
    container.register(LogoutUsecase.self) { ioc in
      let registry = ioc.resolve(LogoutStorageRegistry.self)!
      return LogoutUsecaseImpl(storageRegistry: registry)
      }
      .inObjectScope(.container)
    container.register(CoinsBalanceUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return CoinsBalanceUsecaseImpl(api: api,
                                     accountStorage: accountStorage,
                                     walletStorage: walletStorage)
      }.inObjectScope(.container)
    container.register(ATMUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      return ATMUsecaseImpl(api: api)
      }.inObjectScope(.container)
    container.register(FilterCoinsUsecase.self) { ioc in
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return FilterCoinsUsecaseImpl(walletStorage: walletStorage)
      }.inObjectScope(.container)
    container.register(CoinDetailsUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      let walletService = ioc.resolve(WalletService.self)!
      return CoinDetailsUsecaseImpl(api: api,
                                    accountStorage: accountStorage,
                                    walletStorage: walletStorage,
                                    walletService: walletService)
      }.inObjectScope(.container)
    container.register(SettingsUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      let refreshCredentialsService = ioc.resolve(RefreshCredentialsService.self)!
      let logoutUsecase = ioc.resolve(LogoutUsecase.self)!
      let pinCodeUsecase = ioc.resolve(PinCodeUsecase.self)!
      return SettingsUsecaseImpl(api: api,
                                 accountStorage: accountStorage,
                                 walletStorage: walletStorage,
                                 refreshCredentialsService: refreshCredentialsService,
                                 logoutUsecase: logoutUsecase,
                                 pinCodeUsecase: pinCodeUsecase)
      }.inObjectScope(.container)
    container.register(PinCodeUsecase.self) { ioc in
      let pinCodeStorage = ioc.resolve(PinCodeStorage.self)!
      let refreshService = ioc.resolve(RefreshCredentialsService.self)!
      return PinCodeUsecaseImpl(pinCodeStorage: pinCodeStorage, refreshService: refreshService)
      }.inObjectScope(.container)
    container.register(PinCodeService.self) { ioc in
      let pinCodeStorage = ioc.resolve(PinCodeStorage.self)!
      let getModule = { () -> Module<PinCodeModule> in
        let module = ioc.resolve(Module<PinCodeModule>.self, name: Keys.pinCodeModule.rawValue)!
        module.input.setup(for: .verification)
        return module
      }
      return PinCodeServiceImpl(pinCodeStorage: pinCodeStorage, getModule: getModule)
      }
      .inObjectScope(.container)
      .implements(PinCodeVerificationModuleDelegate.self)
    container.register(Module<PinCodeModule>.self, name: Keys.pinCodeModule.rawValue) { resolver in
      let viewController = PinCodeViewController()
      let usecase = resolver.resolve(PinCodeUsecase.self)!
      let presenter = PinCodePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(PinCodeVerificationModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<PinCodeModule>(controller: viewController, input: presenter)
    }
  }
}
// swiftlint:enable type_body_length
