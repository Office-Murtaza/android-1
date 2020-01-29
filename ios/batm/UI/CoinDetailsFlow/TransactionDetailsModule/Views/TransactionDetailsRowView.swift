import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK

enum TransactionDetailsRowType {
  case text(String)
  case link(String)
  case image(String)
  case status(TransactionStatus)
  case cashStatus(TransactionCashStatus)
}

class TransactionDetailsRowView: UIView, HasDisposeBag {
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsMedium12
    return label
  }()
  
  let tapRelay = PublishRelay<Void>()
  
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
    
    addSubview(titleLabel)
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.top.left.equalToSuperview()
    }
    titleLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    titleLabel.setContentHuggingPriority(.required, for: .horizontal)
  }
  
  private func addTitle(with text: String) {
    let label = UILabel()
    label.text = text
    label.textColor = .slateGrey
    label.font = .poppinsSemibold12
    label.numberOfLines = 0
    
    addSubview(label)
    
    label.snp.makeConstraints {
      $0.left.equalToSuperview().offset(130)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  private func addLink(with text: String) {
    let label = UnderlinedLabelView()
    label.configure(for: .custom(text))
    
    addSubview(label)
    
    label.snp.makeConstraints {
      $0.left.equalToSuperview().offset(130)
      $0.top.right.bottom.equalToSuperview()
    }
    
    label.rx.tap
      .asObservable()
      .bind(to: tapRelay)
      .disposed(by: disposeBag)
  }
  
  private func addImage(with imageId: String) {
    let view = GPHMediaView()
    
    GiphyCore.shared.gifByID(imageId) { (response, error) in
      guard let media = response?.data else { return }
      
      DispatchQueue.main.async { [weak self] in
        guard let self = self else { return }
        
        self.addSubview(view)
        
        view.snp.makeConstraints {
          $0.left.equalToSuperview().offset(130)
          $0.top.bottom.equalToSuperview()
          $0.width.equalTo(view.snp.height).multipliedBy(media.aspectRatio)
        }
        
        view.setMedia(media, rendition: .fixedHeightSmall)
      }
    }
  }
  
  private func addStatus(with status: TransactionStatus) {
    let view = TransactionStatusView()
    view.configure(for: status)
    
    addSubview(view)
    
    view.snp.makeConstraints {
      $0.left.equalToSuperview().offset(130)
      $0.top.bottom.equalToSuperview()
    }
  }
  
  private func addCashStatus(with status: TransactionCashStatus) {
    let label = UILabel()
    label.text = status.verboseValue
    label.textColor = status.associatedColor
    label.font = .poppinsMedium12
    label.numberOfLines = 0
    
    addSubview(label)
    
    label.snp.makeConstraints {
      $0.left.equalToSuperview().offset(130)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  func configure(for type: TransactionDetailsRowType, with title: String) {
    titleLabel.text = title
    
    switch type {
    case let .text(text): addTitle(with: text)
    case let .link(text): addLink(with: text)
    case let .image(imageId): addImage(with: imageId)
    case let .status(status): addStatus(with: status)
    case let .cashStatus(cashStatus): addCashStatus(with: cashStatus)
    }
  }
}

extension Reactive where Base == TransactionDetailsRowView {
  var tap: Driver<Void> {
    return base.tapRelay.asDriver(onErrorJustReturn: ())
  }
}
