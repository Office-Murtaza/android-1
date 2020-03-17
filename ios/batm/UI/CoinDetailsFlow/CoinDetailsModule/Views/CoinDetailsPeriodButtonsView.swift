import UIKit
import RxSwift
import RxCocoa

final class CoinDetailsPeriodButtonsView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 10
    return stackView
  }()
  
  var commonAttributes: [NSAttributedString.Key: Any] {
    return [.font: UIFont.poppinsSemibold12]
  }
  
  var inactiveAttributes: [NSAttributedString.Key: Any] {
    return commonAttributes.merging([.foregroundColor: UIColor.warmGrey], uniquingKeysWith: { $1 })
  }
  
  var activeAttributes: [NSAttributedString.Key: Any] {
    return commonAttributes.merging([.foregroundColor: UIColor.white], uniquingKeysWith: { $1 })
  }
  
  var defaultButton: UIButton {
    let button = UIButton()
    button.backgroundColor = .whiteTwo
    button.layer.cornerRadius = 8
    return button
  }
  
  lazy var oneDayPeriodButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.oneDay), attributes: inactiveAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var oneWeekPeriodButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.oneWeek), attributes: inactiveAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var oneMonthPeriodButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.oneMonth), attributes: inactiveAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var threeMonthsPeriodButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.threeMonths), attributes: inactiveAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var oneYearPeriodButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.oneYear), attributes: inactiveAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  var buttons: [UIButton] {
    return [oneDayPeriodButton,
            oneWeekPeriodButton,
            oneMonthPeriodButton,
            threeMonthsPeriodButton,
            oneYearPeriodButton]
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
    
    addSubview(stackView)
    stackView.addArrangedSubviews(buttons)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    buttons.forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(26)
        $0.width.equalTo(38)
      }
    }
  }
  
  func configure(for selectedPeriod: SelectedPeriod) {
    let selectedButton: UIButton
    
    switch selectedPeriod {
    case .oneDay: selectedButton = oneDayPeriodButton
    case .oneWeek: selectedButton = oneWeekPeriodButton
    case .oneMonth: selectedButton = oneMonthPeriodButton
    case .threeMonths: selectedButton = threeMonthsPeriodButton
    case .oneYear: selectedButton = oneYearPeriodButton
    }
    
    buttons.forEach {
      let text = $0.currentAttributedTitle!.string
      let newAttributedTitle: NSAttributedString
      
      if $0 === selectedButton {
        $0.backgroundColor = .ceruleanBlue
        newAttributedTitle = NSAttributedString(string: text, attributes: activeAttributes)
      } else {
        $0.backgroundColor = .whiteTwo
        newAttributedTitle = NSAttributedString(string: text, attributes: inactiveAttributes)
      }
      
      $0.setAttributedTitle(newAttributedTitle, for: .normal)
    }
  }
}

extension Reactive where Base == CoinDetailsPeriodButtonsView {
  var selectedPeriod: Driver<SelectedPeriod> {
    return Driver.merge(base.oneDayPeriodButton.rx.tap.asDriver().map { .oneDay },
                        base.oneWeekPeriodButton.rx.tap.asDriver().map { .oneWeek },
                        base.oneMonthPeriodButton.rx.tap.asDriver().map { .oneMonth },
                        base.threeMonthsPeriodButton.rx.tap.asDriver().map { .threeMonths },
                        base.oneYearPeriodButton.rx.tap.asDriver().map { .oneYear })
  }
}
