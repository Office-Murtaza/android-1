import UIKit
import MaterialComponents

class LinkView: UIView, HasDisposeBag {
  
  let label: UILabel = {
    let label = UILabel()
    label.numberOfLines = 0
    return label
  }()
  
  let button = DummyButton()
  
  var link: URL?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func configure(text: String, link: URL) {
    self.link = link
    
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: 16),
                                                     .foregroundColor: UIColor.ceruleanBlue,
                                                     .underlineStyle: NSUnderlineStyle.single.rawValue]
    let attributedText = NSAttributedString(string: text, attributes: attributes)
    
    label.attributedText = attributedText
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(label,
                button)
  }
  
  private func setupLayout() {
    label.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    button.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    button.rx.tap.asDriver()
      .drive(onNext: { [unowned self] in
        if let link = self.link, UIApplication.shared.canOpenURL(link) {
          UIApplication.shared.open(link)
        }
      })
      .disposed(by: disposeBag)
  }
  
}
