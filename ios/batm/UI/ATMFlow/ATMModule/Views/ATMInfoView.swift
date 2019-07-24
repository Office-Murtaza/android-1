import UIKit

class ATMInfoView: UIView {
  
  let rootView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.layer.cornerRadius = 16
    view.layer.shadowColor = UIColor.black.cgColor
    view.layer.shadowOffset = CGSize(width: 0, height: 7)
    view.layer.shadowRadius = 15
    view.layer.shadowOpacity = 0.2
    return view
  }()
  
  let container = UIView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold12
    return label
  }()
  
  let subtitleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGreyFour
    label.font = .poppinsMedium9
    return label
  }()
  
  let openHoursStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 8
    return stackView
  }()
  
  let openNowLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold10
    label.text = localize(L.Atm.InfoWindow.openNow)
    return label
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
    
    addSubview(rootView)
    rootView.addSubview(container)
    container.addSubviews(titleLabel,
                          subtitleLabel,
                          openNowLabel,
                          openHoursStackView)
  }
  
  private func setupLayout() {
    rootView.snp.makeConstraints {
      $0.edges.equalToSuperview().inset(30)
    }
    container.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(17)
      $0.top.bottom.equalToSuperview().inset(13)
      $0.width.equalTo(220)
    }
    titleLabel.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    subtitleLabel.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.top.equalTo(titleLabel.snp.bottom).offset(6)
    }
    openNowLabel.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.top.equalTo(subtitleLabel.snp.bottom).offset(9)
    }
    openHoursStackView.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.top.equalTo(openNowLabel.snp.bottom).offset(8)
    }
  }
  
  private func buildOpenHoursLabel(with weeksString: String, and hoursString: String) -> UILabel {
    let weeksString = weeksString.count > 0 ? weeksString + ": " : ""
    let resultString = weeksString + hoursString
    
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.poppinsSemibold9]
    let attributedText = NSMutableAttributedString(string: resultString, attributes: attributes)
    attributedText.addAttribute(.foregroundColor,
                                value: UIColor.warmGreyFour,
                                range: NSRange(location: 0,
                                               length: weeksString.count))
    attributedText.addAttribute(.foregroundColor,
                                value: UIColor.ceruleanBlue,
                                range: NSRange(location: weeksString.count,
                                               length: hoursString.count))
    
    let label = UILabel()
    label.attributedText = attributedText
    return label
  }
  
  func configure(for mapAddress: MapAddress) {
    titleLabel.text = mapAddress.name
    subtitleLabel.text = mapAddress.address
    
    let openHoursLabels = mapAddress.openHours.map {
      return buildOpenHoursLabel(with: $0.days, and: $0.hours)
    }
    
    openHoursStackView.addArrangedSubviews(openHoursLabels)
  }
}
