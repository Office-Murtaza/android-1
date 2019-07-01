//import Moya
//import RxSwift
//
//protocol RefreshCredentialsService {
//  func refresh(credentials: Account) -> Single<Account>
//}
//
//class RefreshCredentialsServiceImpl: RefreshCredentialsService {
//  
//  let networkService: NetworkRequestExecutor
//  
//  init(networkService: NetworkRequestExecutor) {
//    self.networkService = networkService
//  }
//  
//  func refresh(credentials: Account) -> Single<Account> {
//    return networkService.execute(RefreshTokenRequest(credentials: credentials))
//  }
//}
