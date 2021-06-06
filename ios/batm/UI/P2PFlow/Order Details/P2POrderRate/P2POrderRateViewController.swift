import UIKit
import MaterialComponents

class P2POrderRateViewController: UIViewController {
  private let containerView = UIView()
  
  private lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 24)
    label.numberOfLines = 0
    return label
  }()
  
  var doneButton = MDCButton.done
  
  let rateView = P2POrderRateView()
  
  override func viewDidLoad() {
      super.viewDidLoad()
      view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
      setupUI()
      setupLayout()
      setupRecognizers()
  }
  
  func setupRecognizers() {
      let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(hideController))
      let swipeRecogizer = UISwipeGestureRecognizer(target: self, action: #selector(hideController))
      swipeRecogizer.direction = .down
      
      view.addGestureRecognizer(tapRecognizer)
      view.addGestureRecognizer(swipeRecogizer)
  }
  
  @objc func hideController() {
      dismiss(animated: true, completion: nil)
  }
  
  func setupUI() {
      containerView.backgroundColor = .white
      view.addSubviews([
          containerView
      ])
      
      containerView.addSubviews([
        titleLabel,
        doneButton,
        rateView
      ])
  }
  
  func setupLayout() {
      containerView.snp.makeConstraints {
          $0.left.right.equalToSuperview()
          $0.bottom.equalToSuperview()
          $0.height.equalTo(424)
      }
  
    titleLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(48)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    rateView.snp.makeConstraints {
      $0.top.equalTo(titleLabel.snp.bottom).offset(10)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.height.equalTo(50)
    }
    
    doneButton.snp.makeConstraints {
      $0.top.equalTo(rateView).offset(50)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.height.equalTo(50)
    }
    
  }
}

class P2POrderRateView: UIView {
  
  var tags = [1,2,3,4,5]
  
  lazy var stackView: UIStackView = {
    let stack = UIStackView()
    stack.axis = .horizontal
    return stack
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    tags.forEach { (tag) in
      let button = UIButton()
      button.tag = tag
      button.addTarget(self, action: #selector(didTap(_:)), for: .touchUpInside)
      button.setImage(UIImage(named: "rate_star_selected"), for: .selected)
      button.setImage(UIImage(named: "rate_star_not_selected"), for: .normal)
      button.snp.removeConstraints()
      button.snp.makeConstraints {
        $0.width.height.equalTo(50)
      }
      stackView.addArrangedSubview(button)
    }
  }
  
  @objc func didTap(_ button: UIButton) {
    print("BBBB selected button with tag", button.tag)
  }
}

