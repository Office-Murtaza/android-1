import Foundation
import RxSwift
import RxCocoa

class SecurityPresenter: ModulePresenter, SecurityModule {
  struct Input {
    var select: Driver<IndexPath>
  }
      
  var types = BehaviorRelay<[SecurityCellType]>(value: [])
  private var userPhoneNumber: String?
  private let usecase: SettingsUsecase
  
  weak var delegate: SecurityModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }
  
  func bind(input: Input) {
    track(usecase.getPhoneNumber())
        .drive(onNext: { [weak self] user in
            guard let self = self else { return }
            self.userPhoneNumber = user.phoneNumber
            self.types.accept([.updatePhone(phoneNumber: user.phoneNumber.phoneFormatted),
                               .updatePassword,
                               .updatePIN,
                               .seedPhrase,
                               .unlink])
        })
        .disposed(by: disposeBag)
    
    input.select
      .asObservable()
      .withLatestFrom(types) { $1[$0.item] }
      .subscribe(onNext: { [unowned self, delegate] in
        switch $0 {
        case .updatePhone: delegate?.didSelectUpdatePhone(self.userPhoneNumber ?? "")
        case .updatePassword: delegate?.didSelectUpdatePassword()
        case .updatePIN: self.fetchPinCode()
        case .seedPhrase: delegate?.didSelectSeedPhrase()
        case .unlink: delegate?.didSelectUnlink()
        }
      })
      .disposed(by: disposeBag)
  }
  
  private func fetchPinCode() {
    track(usecase.getPinCode())
      .drive(onNext: { [delegate] in delegate?.didSelectUpdatePIN($0) })
      .disposed(by: disposeBag)
  }
}
