import Foundation
import RxSwift

protocol ATMUsecase {
  func getMapAddresses() -> Single<MapAddresses>
}

class ATMUsecaseImpl: ATMUsecase {
  
  let api: APIGateway
  
  init(api: APIGateway) {
    self.api = api
  }
  
  func getMapAddresses() -> Single<MapAddresses> {
    return api.getMapAddresses()
  }
  
}
