import Foundation
import RxSwift
import RxCocoa

final class UnlinkPresenter: ModulePresenter, UnlinkModule {

  struct Input {
    var unlink: Driver<Void>
  }
  
  private let usecase: SettingsUsecase

  weak var delegate: UnlinkModuleDelegate?
  
  init(usecase: SettingsUsecase) {
    self.usecase = usecase
  }

  func bind(input: Input) {
//    input.unlink
//      .asObservable()
//      .flatMap { [unowned self] in self.track(self.unlink()) }
//      .subscribe()
//      .disposed(by: disposeBag)
  }
  
}
