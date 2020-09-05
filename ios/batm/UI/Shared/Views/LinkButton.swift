import UIKit
import MaterialComponents

class LinkButton: MDCButton, HasDisposeBag {
  
  var link: URL?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    contentEdgeInsets = .zero
    applyTextTheme(withScheme: MDCContainerScheme.default)
    
    setupBindings()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func configure(text: String, link: URL) {
    self.link = link
    
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: 16, weight: .medium),
                                                     .foregroundColor: UIColor.ceruleanBlue,
                                                     .underlineStyle: NSUnderlineStyle.single.rawValue]
    let attributedText = NSAttributedString(string: text, attributes: attributes)
    
    setAttributedTitle(attributedText, for: .normal)
    titleLabel?.numberOfLines = 0
  }
  
  private func setupBindings() {
    rx.tap.asDriver()
      .drive(onNext: { [unowned self] in
        if let link = self.link, UIApplication.shared.canOpenURL(link) {
          UIApplication.shared.open(link)
        }
      })
      .disposed(by: disposeBag)
  }
  
}
