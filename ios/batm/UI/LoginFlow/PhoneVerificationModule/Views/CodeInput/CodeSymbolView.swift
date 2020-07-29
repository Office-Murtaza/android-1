import UIKit

class CodeSymbolView: UIView {
  private let label: UILabel = UILabel()
  private let cursor: CALayer = CALayer()
  private let separator: UIView = UIView()
  
  var activeColor: UIColor? {
    didSet { render(state: currentState) }
  }
  
  var font: UIFont? {
    didSet { render(state: currentState) }
  }
  
  var textColor: UIColor? {
    didSet { render(state: currentState) }
  }

  // MARK: - State rendering
  
  enum State {
    case empty
    case active
    case filled(Character)
  }
  
  var currentState: State = .empty {
    didSet {
      render(state: currentState)
    }
  }
  
  func render(state: State) {
    cursor.backgroundColor = activeColor?.cgColor
    label.font = font
    label.textColor = textColor
    separator.backgroundColor = .whiteSix
    
    switch state {
    case .empty:
      label.text = ""
      cursor.removeFromSuperlayer()
      cursor.stopBlinking()
    case .active:
      label.text = ""
      layer.addSublayer(cursor)
      cursor.startBlinking()
    case let .filled(symbol):
      label.text = String(symbol)
      cursor.stopBlinking()
      cursor.removeFromSuperlayer()
    }
    
    setNeedsLayout()
  }
  
  // MARK: - Lifecycle
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupDefaults()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setupDefaults()
  }
  
  private func setupDefaults() {
    addSubviews(label, separator)
  }
  
  // MARK: - Layout
  override var intrinsicContentSize: CGSize {
    return CGSize(width: 44.0, height: 56.0)
  }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    
    label.sizeToFit()
    label.center = CGPoint(x: bounds.midX, y: bounds.midY)

    cursor.position = CGPoint(x: bounds.midX, y: bounds.midY)
    cursor.bounds = CGRect(x: 0, y: 0, width: 1, height: cursorHeight)
    
    separator.frame = CGRect(x: 0, y: bounds.height - 1, width: bounds.width, height: 1)
  }
  
  private var cursorHeight: CGFloat {
    return label.font.lineHeight + label.font.descender
  }
}
