import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class SeedPhraseFormView: UIView, MDCChipFieldDelegate {
  
  let chipField: MDCChipField = {
    let view = MDCChipField()
    view.isUserInteractionEnabled = false
    view.textField.textColor = .slateGrey
    view.contentEdgeInsets = .init(top: 20, left: 20, bottom: 20, right: 20)
    view.layer.borderColor = UIColor.clear.cgColor
    view.layer.borderWidth = 0
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
  
  func chipField(_ chipField: MDCChipField, didAddChip chip: MDCChipView) {
    chip.setBackgroundColor(.white, for: .normal)
    chip.setBorderColor(.whiteFive, for: .normal)
    chip.setBorderWidth(1, for: .normal)
    chip.setTitleColor(.slateGrey, for: .normal)
  }
  
  func configure(for seedPhrase: [String]) {
    chipField.clearTextInput()
    chipField.chips.forEach { chipField.removeChip($0) }
    
    seedPhrase.forEach {
      let chipView = MDCChipView()
      chipView.titleLabel.text = $0
      chipField.addChip(chipView)
    }
  }
}
