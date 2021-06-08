import UIKit
import MaterialComponents

protocol P2POrderRateViewControllerDelegate: AnyObject {
  func selectedRate(rate: Int)
}

class P2POrderRateViewController: UIViewController {
  
  weak var delegate: P2POrderRateViewControllerDelegate?
  
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
    
      doneButton.addTarget(self, action: #selector(doneAction), for: .touchUpInside)
  }
  
  func setup(title: String) {
    titleLabel.text = title
  }
  
  func setupRecognizers() {
      let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(hideTap(sender:)))
      let swipeRecogizer = UISwipeGestureRecognizer(target: self, action: #selector(hideSwap(sender:)))
      swipeRecogizer.direction = .down
      
      view.addGestureRecognizer(tapRecognizer)
      view.addGestureRecognizer(swipeRecogizer)
  }
  
  @objc func doneAction() {
    let selectedRate = rateView.selectedRate
    delegate?.selectedRate(rate: selectedRate)
    dismiss(animated: true, completion: nil)
  }
  
  @objc func hideTap(sender: UITapGestureRecognizer) {
    let point = sender.location(in: view)
    if containerView.frame.contains(point) { return }
    dismiss(animated: true, completion: nil)
  }
  
  @objc func hideSwap(sender: UITapGestureRecognizer) {
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
      $0.top.equalTo(rateView.snp.bottom).offset(50)
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
    stack.spacing = 10
    stack.alignment = .center
    stack.distribution = .fillEqually
    return stack
  }()
  
  var selectedRate: Int {
    let selected = stackView.arrangedSubviews.compactMap{ $0 as? UIButton }.filter { $0.isSelected == true }
    return selected.count
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    
    addSubview(stackView)
    
    tags.forEach { (tag) in
      let button = UIButton(type: .custom)
      button.tag = tag
      if tag == 1 {
        button.isSelected = true
      }
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
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  @objc func didTap(_ button: UIButton) {
    
    let tag = button.tag
    let selectedRage = 0...tag
    
    stackView.arrangedSubviews.forEach { view in
      if let button = view as? UIButton {
        button.isSelected = false
      }
    }
    
    stackView.arrangedSubviews.forEach { view in
      if let button = view as? UIButton, selectedRage ~= button.tag {
        button.isSelected = true
      }
    }
  }
}

