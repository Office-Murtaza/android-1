import UIKit
import RxSwift
import RxCocoa

class WelcomeSupportView: UIView, HasDisposeBag {
  
  let supportLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Welcome.Support.title)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold20
    return label
  }()
  
  let closeButton: UIButton = {
    let button = UIButton(type: .system)
    let image = UIImage(named: "welcome_close")!.withRenderingMode(.alwaysOriginal)
    button.setImage(image, for: .normal)
    return button
  }()
  
  let bottomContainer = UIView()
  
  let phoneCellView: WelcomeSupportCellView = {
    let view = WelcomeSupportCellView()
    view.configure(for: .phone)
    return view
  }()
  
  let mailCellView: WelcomeSupportCellView = {
    let view = WelcomeSupportCellView()
    view.configure(for: .mail)
    return view
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    backgroundColor = .white
    layer.masksToBounds = true
    layer.cornerRadius = 33
    
    addSubviews(supportLabel,
                closeButton,
                bottomContainer)
    
    bottomContainer.backgroundColor = .whiteFour
    bottomContainer.addSubviews(phoneCellView,
                                mailCellView)
  }
  
  private func setupLayout() {
    supportLabel.snp.makeConstraints {
      $0.top.left.equalToSuperview().inset(30)
    }
    closeButton.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-30)
      $0.centerY.equalTo(supportLabel)
    }
    bottomContainer.snp.makeConstraints {
      $0.top.equalTo(supportLabel.snp.bottom).offset(30)
      $0.left.right.bottom.equalToSuperview()
    }
    phoneCellView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    mailCellView.snp.makeConstraints {
      $0.top.equalTo(phoneCellView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(25)
      $0.bottom.equalToSuperview().offset(-35)
    }
  }
  
  private func setupBindings() {
    phoneCellView.rx.copyTap
      .drive(onNext: { [phoneCellView] in UIPasteboard.general.string = phoneCellView.titleLabel.text })
      .disposed(by: disposeBag)
    mailCellView.rx.copyTap
      .drive(onNext: { [mailCellView] in UIPasteboard.general.string = mailCellView.titleLabel.text })
      .disposed(by: disposeBag)
  }
}
