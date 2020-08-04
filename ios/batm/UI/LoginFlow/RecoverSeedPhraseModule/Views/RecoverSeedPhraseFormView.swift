import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class RecoverSeedPhraseFormView: UIView, MDCChipFieldDelegate {
  
  let didUpdateSeedPhraseRelay = PublishRelay<[String]>()
  
  let chipField: MDCChipField = {
    let view = MDCChipField()
    view.textField.textColor = .slateGrey
    view.delimiter = .all
    view.contentEdgeInsets = .init(top: 20, left: 20, bottom: 20, right: 20)
    view.layer.borderColor = UIColor.whiteFive.cgColor
    view.layer.borderWidth = 1
    view.layer.cornerRadius = 4
    return view
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(chipField)
    
    chipField.delegate = self
  }
  
  private func setupLayout() {
    chipField.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func updateSeedPhrase() {
    let seedPhrase = chipField.chips.map { $0.titleLabel.text ?? "" }
    didUpdateSeedPhraseRelay.accept(seedPhrase)
  }
  
  func chipField(_ chipField: MDCChipField, didAddChip chip: MDCChipView) {
    chip.setBackgroundColor(.whiteTwo, for: .normal)
    chip.setTitleColor(.slateGrey, for: .normal)
    
    updateSeedPhrase()
  }
  
  func chipField(_ chipField: MDCChipField, didChangeInput input: String?) {
    guard let words = input?.separatedWords, words.count > 1 else { return }
    
    addChips(for: words)
  }
  
  func chipField(_ chipField: MDCChipField, didRemoveChip chip: MDCChipView) {
    updateSeedPhrase()
  }
  
  func configure(for seedPhrase: [String]) {
    chipField.chips.forEach { chipField.removeChip($0) }
    
    addChips(for: seedPhrase)
  }
  
  func addChips(for words: [String]) {
    chipField.clearTextInput()
    
    words.forEach {
      let chipView = MDCChipView()
      chipView.titleLabel.text = $0
      chipField.addChip(chipView)
    }
  }
}

extension Reactive where Base == RecoverSeedPhraseFormView {
  var seedPhrase: Driver<[String]> {
    return base.didUpdateSeedPhraseRelay.asDriver(onErrorJustReturn: [])
  }
}
