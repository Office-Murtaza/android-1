import UIKit
import RxSwift
import RxCocoa

class CoinSellDetailsInstructionsView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 20
    return stackView
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
    
    addSubview(stackView)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(instructions: [String]) {
    instructions.enumerated().forEach { index, instruction in
      let instructionView = CoinSellDetailsInstructionView()
      instructionView.configure(number: index + 1, text: instruction)
      
      stackView.addArrangedSubview(instructionView)
    }
  }
}
