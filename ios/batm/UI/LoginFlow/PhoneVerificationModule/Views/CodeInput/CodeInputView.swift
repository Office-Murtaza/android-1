import UIKit
import SnapKit

protocol CodeInputViewDelegate: AnyObject {
  func didComplete(with code: String, by inputView: CodeInputView)
}

final class CodeInputView: UIControl {
  
  weak var delegate: CodeInputViewDelegate?
  
  var symbolCount: Int = 4 {
    didSet { updateSymbolCount() }
  }
  
  private let stackView: UIStackView = {
    let view = UIStackView()
    view.isUserInteractionEnabled = false
    view.axis = .horizontal
    view.spacing = 20
    view.translatesAutoresizingMaskIntoConstraints = false
    return view
  }()
  
  private var _code: String = "" {
    didSet {
      renderState()
    }
  }
  
  private(set) var isEditing: Bool = false {
    didSet {
      sendActions(for: isEditing ? .editingDidBegin : .editingDidEnd)
      renderState()
    }
  }
  
  var activeColor: UIColor = .ceruleanBlue {
    didSet { renderState() }
  }
  
  var font: UIFont = .systemFont(ofSize: 24) {
    didSet { renderState() }
  }
  
  var textColor: UIColor = .slateGrey {
    didSet { renderState() }
  }
  
  private func updateSymbolCount() {
    removeSymbolViews()
    setupSymbolViews()
  }
  
  private func renderState() {
    assert(symbolViews.count == symbolCount)
    
    symbolViews.enumerated().forEach { offset, view in
      applyStyle(to: view)
      
      switch offset {
      case let num where num < _code.count:
        let index = _code.index(_code.startIndex, offsetBy: offset)
        let character = _code[index]
        view.currentState = .filled(character)
      case let num where num == _code.count && self.isEditing:
        view.currentState = .active
      default:
        view.currentState = .empty
        return
      }
    }
  }
  
  private func applyStyle(to view: CodeSymbolView) {
    view.activeColor = activeColor
    view.font = font
    view.textColor = textColor
  }

  private var symbolViews: [CodeSymbolView] {
    return stackView.arrangedSubviews.compactMap { $0 as? CodeSymbolView }
  }
  
  private func removeSymbolViews() {
    let views = stackView.arrangedSubviews
    views.forEach { stackView.removeArrangedSubview($0) }
  }
  
  private func setupSymbolViews() {
    Array(0 ..< symbolCount).forEach { _ in
      stackView.addArrangedSubview(CodeSymbolView())
    }
  }

  var code: String {
    set { _code = newValue.trimmingCharacters(in: allowedCharacters.inverted).truncate(length: symbolCount) }
    get { return _code }
  }
  
  private let allowedCharacters: CharacterSet = CharacterSet.decimalDigits
  
  // MARK: - Lifecycle
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupDefaults()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setupDefaults()
  }
  
  private var didUpdateConstraints: Bool = false
  override func updateConstraints() {
    if !didUpdateConstraints {
      stackView.snp.makeConstraints { $0.edges.equalToSuperview() }
      didUpdateConstraints = true
    }
    super.updateConstraints()
  }
  
  private func setupDefaults() {
    addTarget(self, action: #selector(didTouchUpInside(_:)), for: .touchUpInside)
    addTarget(self, action: #selector(onEditingChanged(_:)), for: .editingChanged)
    isExclusiveTouch = true
    
    addSubview(stackView)
    
    setupSymbolViews()
    renderState()
  }
  
  // MARK: - Actions
  
  @objc private func onEditingChanged(_ view: CodeInputView) {
    let code = view.code
    
    guard code.count == view.symbolCount else { return }
    
    delegate?.didComplete(with: code, by: view)
  }
  
  @objc private func didTouchUpInside(_ view: CodeInputView) {
    _ = becomeFirstResponder()
  }
  
  // MARK: - UIResponder
  
  override var canBecomeFirstResponder: Bool {
    return true
  }
  
  @discardableResult
  override func becomeFirstResponder() -> Bool {
    defer { isEditing = true }
    return super.becomeFirstResponder()
  }
  
  @discardableResult
  override func resignFirstResponder() -> Bool {
    defer { isEditing = false }
    return super.resignFirstResponder()
  }
  
  // MARK: - UITextInputTraits
  
  var keyboardType: UIKeyboardType = .numberPad
  var autorcorrectionType: UITextAutocorrectionType = .no
  var autocapitalizationType: UITextAutocapitalizationType = .none
  var spellCheckingType: UITextSpellCheckingType = .no
  var keyboardAppearance: UIKeyboardAppearance = .default
  var isSecureTextEntry: Bool = false
}

extension CodeInputView: UIKeyInput {
  var hasText: Bool {
    return code.isNotEmpty
  }
  
  func insertText(_ text: String) {
    guard isEnabled, text.isNotEmpty else { return }
    code += text
    sendActions(for: .editingChanged)
  }
  
  func deleteBackward() {
    guard isEnabled, code.isNotEmpty else { return }
    code = String(code.dropLast())
    sendActions(for: .editingChanged)
  }
}
