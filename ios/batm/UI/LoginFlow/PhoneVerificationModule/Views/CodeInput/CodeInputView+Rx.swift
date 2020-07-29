import Foundation
import RxSwift
import RxCocoa

extension Reactive where Base: CodeInputView {
  var code: ControlProperty<String> {
    return controlProperty(
      editingEvents: [.editingChanged],
      getter: { view in
        return view.code },
      setter: { view, nextCode in
        view.code = nextCode })
  }
  
  var fullCode: ControlEvent<String> {
    let source = base.rx.code.filter {
      $0.count == self.base.symbolCount
    }
    return ControlEvent(events: source)
  }
}
