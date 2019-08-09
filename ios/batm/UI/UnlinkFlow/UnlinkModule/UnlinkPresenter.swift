import Foundation
import RxSwift
import RxCocoa

final class UnlinkPresenter: ModulePresenter, UnlinkModule {

  struct Input {
    var back: Driver<Void>
    var unlink: Driver<Void>
  }
  
  private let usecase: SettingsUsecase

  weak var delegate: UnlinkModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }
  
  func unlink() {
    self.track(usecase.unlink())
      .drive()
      .disposed(by: disposeBag)
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishUnlink() })
      .disposed(by: disposeBag)
    
    input.unlink
      .drive(onNext: { [unowned self] in self.delegate?.didUnlink(from: self) })
      .disposed(by: disposeBag)
  }
}
