import Foundation
import RxSwift
import RxCocoa
import LocalAuthentication

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
            let types = self.dataFactory(phoneNumber: user.phoneNumber.phoneFormatted)
            self.types.accept(types)
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
        case .faceId: break
        case .touchId: break
        }
      })
      .disposed(by: disposeBag)
  }
    
    private func dataFactory(phoneNumber: String?) -> [SecurityCellType] {
        
        let initialTypes: [SecurityCellType] = [.updatePhone(phoneNumber: phoneNumber),
                                         .updatePassword,
                                         .updatePIN,
                                         .seedPhrase,
                                         .unlink]
        let laData = localAuthData()
        var dataTypes = [SecurityCellType]()
        
        for type in initialTypes {
            dataTypes.append(type)
            if type == .updatePIN, let laType = laData {
                dataTypes.append(laType)
            }
        }
        
        return dataTypes
    }
    
    private func localAuthData() -> SecurityCellType? {
        let laType = LAContext().supportedBioAuthType
        guard laType != .none else { return nil }
        
        return laType == .faceID ?
            .faceId(isEnabled: UserDefaultsHelper.isLocalAuthEnabled)
            : .touchId(isEnabled: UserDefaultsHelper.isLocalAuthEnabled)
    }
  
  private func fetchPinCode() {
    track(usecase.getPinCode())
      .drive(onNext: { [delegate] in delegate?.didSelectUpdatePIN($0) })
      .disposed(by: disposeBag)
  }
}
