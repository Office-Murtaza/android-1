import Foundation
import RxSwift
import RxCocoa

class SettingsPresenter: ModulePresenter, SettingsModule {
  
  struct Input {
    var select: Driver<IndexPath>
  }
  
  let typesRelay = BehaviorRelay<[SettingsCellType]>(value: [.phone, .changePassword, .changePin, .showSeedPhrase, .unlink])
  private let usecase: SettingsUsecase
  
  weak var delegate: SettingsModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }
  
  func bind(input: Input) {
    input.select
      .asObservable()
      .withLatestFrom(typesRelay) { $1[$0.item] }
      .subscribe(onNext: { [unowned self, delegate] in
        switch $0 {
        case .phone: self.fetchPhoneNumber()
        case .changePassword: delegate?.didSelectChangePassword()
        case .changePin: delegate?.didSelectChangePin()
        case .showSeedPhrase: delegate?.didSelectShowSeedPhrase()
        case .unlink: delegate?.didSelectUnlink()
        }
      })
      .disposed(by: disposeBag)
  }
  
  private func fetchPhoneNumber() {
    track(usecase.getPhoneNumber())
      .drive(onNext: { [delegate] in delegate?.didSelectPhone($0) })
      .disposed(by: disposeBag)
  }
}
