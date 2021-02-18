import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import LocalAuthentication

class PinCodeKeyboardView: UIView {
  
  let mainStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.distribution = .fillEqually
    return stackView
  }()
  
  var defaultHorizontalStackView: UIStackView {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.distribution = .fillEqually
    return stackView
  }

  lazy var firstStackView = defaultHorizontalStackView
  lazy var secondStackView = defaultHorizontalStackView
  lazy var thirdStackView = defaultHorizontalStackView
  lazy var fourthStackView = defaultHorizontalStackView
  
  let digitButton_1 = PinCodeKeyboardDigitButton(digit: 1)
  let digitButton_2 = PinCodeKeyboardDigitButton(digit: 2)
  let digitButton_3 = PinCodeKeyboardDigitButton(digit: 3)
  let digitButton_4 = PinCodeKeyboardDigitButton(digit: 4)
  let digitButton_5 = PinCodeKeyboardDigitButton(digit: 5)
  let digitButton_6 = PinCodeKeyboardDigitButton(digit: 6)
  let digitButton_7 = PinCodeKeyboardDigitButton(digit: 7)
  let digitButton_8 = PinCodeKeyboardDigitButton(digit: 8)
  let digitButton_9 = PinCodeKeyboardDigitButton(digit: 9)
  let digitButton_0 = PinCodeKeyboardDigitButton(digit: 0)
  let backButton = PinCodeKeyboardBackButton()
  
    lazy var localAuthButton: MDCButton = {
        let button = MDCButton()
        button.setBackgroundColor(.whiteTwo, for: .normal)
        let type = LAContext().supportedBioAuthType
        if UserDefaultsHelper.isLocalAuthEnabled,
           UserDefaultsHelper.pinCodeWasEntered {
            button.setImage(UIImage(named: type.rawValue), for: .normal)
        }
        return button
    }()
  
   
  var digitButtons: [PinCodeKeyboardDigitButton] {
    return [
      digitButton_0,
      digitButton_1,
      digitButton_2,
      digitButton_3,
      digitButton_4,
      digitButton_5,
      digitButton_6,
      digitButton_7,
      digitButton_8,
      digitButton_9,
    ]
  }
  
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
    backgroundColor = .whiteTwo
    
    addSubview(mainStackView)
    
    mainStackView.addArrangedSubviews(firstStackView,
                                      secondStackView,
                                      thirdStackView,
                                      fourthStackView)
    
    firstStackView.addArrangedSubviews(digitButton_1,
                                       digitButton_2,
                                       digitButton_3)
    
    secondStackView.addArrangedSubviews(digitButton_4,
                                        digitButton_5,
                                        digitButton_6)
    
    thirdStackView.addArrangedSubviews(digitButton_7,
                                       digitButton_8,
                                       digitButton_9)
    
    fourthStackView.addArrangedSubviews(localAuthButton,
                                        digitButton_0,
                                        backButton)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(safeAreaLayoutGuide)
    }
    digitButtons.forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(60)
      }
    }
  }
}

extension Reactive where Base == PinCodeKeyboardView {
  var digitTapped: Driver<String> {
    return Driver.merge(base.digitButtons.map { $0.rx.digitTapped })
  }
  var backTapped: Driver<Void> {
    return base.backButton.rx.tap.asDriver()
  }
    
    var laAuthTapped: Driver<Void> {
        return base.localAuthButton.rx.tap.asDriver()
    }
}
