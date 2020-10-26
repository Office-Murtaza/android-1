import Foundation
import Swinject
import Moya
import CoreData

final class AppAssembly: Assembly {
  
  enum Keys: String {
    case pinCodeModule
    case testApiUrl
    case prodApiUrl
    case apiUrl
  }
  
  func assemble(container: Container) {
    assembleNetwork(container: container)
    assembleStorages(container: container)
    assembleServices(container: container)
    assembleUsecases(container: container)
  }
  
  private func assembleNetwork(container: Container) {
    container.register(URL.self, name: Keys.testApiUrl.rawValue) { _ in URL(string: "http://test.belcobtm.com/api/v1")! }
    container.register(URL.self, name: Keys.prodApiUrl.rawValue) { _ in URL(string: "https://prod.belcobtm.com/api/v1")! }
    container.register(URL.self, name: Keys.apiUrl.rawValue) { ioc in ioc.resolve(URL.self, name: Keys.testApiUrl.rawValue)! }
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
      let errorService = ioc.resolve(ErrorService.self)!
      return BTMNetworkService(networkService: network,
                               accountStorage: accountStorage,
                               logoutUsecase: logoutUsecase,
                               pinCodeService: pinCodeService,
                               refreshCredentialsService: refreshCredentialsService,
                               errorService: errorService)
      }
      .inObjectScope(.transient)
    container.register(RefreshCredentialsService.self) { ioc in
      let apiUrl = ioc.resolve(URL.self, name: Keys.apiUrl.rawValue)!
      let networkService = ioc.resolve(NetworkService.self, argument: apiUrl)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let logoutUsecase = ioc.resolve(LogoutUsecase.self)!
      return RefreshCredentialsServiceImpl(networkService: networkService,
                                           accountStorage: accountStorage,
                                           logoutUsecase: logoutUsecase)
      }.inObjectScope(.container)
    container.register(APIGateway.self) { ioc in
      let apiUrl = ioc.resolve(URL.self, name: Keys.apiUrl.rawValue)!
      let networkService = ioc.resolve(NetworkRequestExecutor.self, argument: apiUrl)!
      let errorService = ioc.resolve(ErrorService.self)!
      return APIGatewayImpl(networkProvider: networkService,
                            errorService: errorService)
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
    container.register(LocationUpdateDateStorage.self) { ioc in
      let executor = ioc.resolve(StorageTransactionExecutor.self)!
      let storageRegistry = ioc.resolve(LogoutStorageRegistry.self)!
      let storage = LocationUpdateDateStorageImpl(transactionExecutor: executor)
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
      let apiUrl = ioc.resolve(URL.self, name: Keys.apiUrl.rawValue)!
      let networkService = ioc.resolve(NetworkService.self, argument: apiUrl)!
      let accountService = ioc.resolve(AccountStorage.self)!
      return LogoutUsecaseImpl(storageRegistry: registry,
                               networkService: networkService,
                               accountStorage: accountService)
      }
      .inObjectScope(.container)
    container.register(WalletUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return WalletUsecaseImpl(api: api,
                               accountStorage: accountStorage,
                               walletStorage: walletStorage)
      }.inObjectScope(.container)
    container.register(ATMUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      return ATMUsecaseImpl(api: api)
      }.inObjectScope(.container)
    container.register(ManageWalletsUsecase.self) { ioc in
      let walletStorage = ioc.resolve(BTMWalletStorage.self)!
      return ManageWalletsUsecaseImpl(walletStorage: walletStorage)
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
    container.register(TradesUsecase.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      return TradesUsecaseImpl(api: api,
                               accountStorage: accountStorage)
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
    container.register(ErrorService.self) { ioc in
      let getModule = { return ioc.resolve(Module<ErrorModule>.self)! }
      return ErrorServiceImpl(getModule: getModule)
      }
      .inObjectScope(.container)
      .implements(ErrorModuleDelegate.self)
    container.register(LocationService.self) { ioc in
      let api = ioc.resolve(APIGateway.self)!
      let accountStorage = ioc.resolve(AccountStorage.self)!
      let locationUpdateDateStorage = ioc.resolve(LocationUpdateDateStorage.self)!
      return LocationServiceImpl(api: api,
                                 accountStorage: accountStorage,
                                 locationUpdateDateStorage: locationUpdateDateStorage)
    }
      .inObjectScope(.container)
    container.register(Module<PinCodeModule>.self, name: Keys.pinCodeModule.rawValue) { resolver in
      let viewController = PinCodeViewController()
      let usecase = resolver.resolve(PinCodeUsecase.self)!
      let presenter = PinCodePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(PinCodeVerificationModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<PinCodeModule>(controller: viewController, input: presenter)
    }
    container.register(Module<ErrorModule>.self) { resolver in
      let viewController = ErrorViewController()
      let presenter = ErrorPresenter()
      
      presenter.delegate = resolver.resolve(ErrorModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ErrorModule>(controller: viewController, input: presenter)
    }
  }
}
// swiftlint:enable type_body_length
