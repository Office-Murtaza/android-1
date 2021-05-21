import Foundation
import RxSwift
import RxCocoa

class SettingsPresenter: ModulePresenter, SettingsModule {
  struct Input {
    var select: Driver<IndexPath>
  }
    
  let types = SettingsCellType.allCases
  private let usecase: SettingsUsecase
  
  weak var delegate: SettingsModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }
  
  func bind(input: Input) {
    input.select
      .asObservable()
      .map { [types] in types[$0.item] }
      .subscribe(onNext: { [delegate] in
        switch $0 {
        case .wallet: delegate?.didSelectWallet()
        case .security: delegate?.didSelectSecurity()
        case .kyc: delegate?.didSelectKYC()
        case .notifications: delegate?.didSelectNotifications()
        case .support: delegate?.didSelectSupport()
        case .about: delegate?.didSelectAbout()
        }
      })
      .disposed(by: disposeBag)
  }
}
