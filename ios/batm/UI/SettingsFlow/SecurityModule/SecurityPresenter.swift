import Foundation
import RxSwift
import RxCocoa

class SecurityPresenter: ModulePresenter, SecurityModule {
  
  struct Input {
    var select: Driver<IndexPath>
  }
  
  let types = SecurityCellType.allCases
  private let usecase: SettingsUsecase
  
  weak var delegate: SecurityModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }
  
  func bind(input: Input) {
    input.select
      .asObservable()
      .map { [types] in types[$0.item] }
      .subscribe(onNext: { [unowned self, delegate] in
        switch $0 {
        case .updatePhone: self.fetchPhoneNumber()
        case .updatePassword: delegate?.didSelectUpdatePassword()
        case .updatePIN: delegate?.didSelectUpdatePIN()
        case .seedPhrase: delegate?.didSelectSeedPhrase()
        case .unlinkWallet: delegate?.didSelectUnlinkWallet()
        }
      })
      .disposed(by: disposeBag)
  }
  
  private func fetchPhoneNumber() {
    track(usecase.getPhoneNumber())
      .drive(onNext: { [delegate] in delegate?.didSelectUpdatePhone($0) })
      .disposed(by: disposeBag)
  }
}
