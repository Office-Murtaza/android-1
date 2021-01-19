import UIKit

class ContactView: UIView {
    
    var isSeparatorVisible: Bool = true
    static let image = UIImage(named: "person")
    
    let stackView: UIStackView = {
      let stackView = UIStackView()
      stackView.axis = .vertical
      stackView.spacing = 5
      return stackView
    }()
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    init(isSeparatorVisible: Bool = true) {
        super.init(frame: .zero)
        self.isSeparatorVisible = isSeparatorVisible
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        addSubviews([
            contactImage,
            stackView,
            separatorView
        ])
        stackView.addArrangedSubviews([
            nameLabel,
            phoneLabel
        ])
        separatorView.isHidden = !isSeparatorVisible
        
    }
    
    func clearData() {
        nameLabel.text = nil
        phoneLabel.text = nil
        nameLabel.isHidden = false
        phoneLabel.isHidden = false
        contactImage.image = Self.image
    }
    
    func update(contact: BContact) {
        setRelatedStackLabel(label: nameLabel, title: contact.name)
        setRelatedStackLabel(label: phoneLabel, title: contact.phones.first)
        contactImage.image = contact.image != nil ? contact.image : Self.image
    }
    
    private func setRelatedStackLabel(label: UILabel, title: String?) {
        guard let title = title else {
            label.isHidden = true
            return
        }
        label.text = title
    }
    
    func setupLayout() {
        contactImage.snp.makeConstraints{
            $0.left.equalToSuperview().offset(15)
            $0.top.equalToSuperview().offset(15)
            $0.width.height.equalTo(40)
        }
        
        stackView.snp.makeConstraints{
            $0.left.equalTo(contactImage.snp.right).offset(15)
            $0.right.equalToSuperview()
            $0.centerY.equalTo(contactImage.snp.centerY)
        }
        
        separatorView.snp.makeConstraints{
            $0.left.equalTo(nameLabel.snp.left)
            $0.right.equalToSuperview().offset(-15)
            $0.bottom.equalToSuperview()
            $0.height.equalTo((1/UIScreen.main.scale))
        }
    }
    
    lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    lazy var phoneLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black.withAlphaComponent(0.6)
        label.font = .systemFont(ofSize: 14, weight: .regular)
        return label
    }()
    
    lazy var contactImage: UIImageView = {
        let imageView = UIImageView()
        imageView.layer.masksToBounds = true
        imageView.layer.cornerRadius = 20
        imageView.backgroundColor = UIColor.gray.withAlphaComponent(0.4)
        imageView.image = Self.image
        imageView.contentMode = .center
        return imageView
    }()
    
    lazy var separatorView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "#14212121")
        return view
    }()
    
}
