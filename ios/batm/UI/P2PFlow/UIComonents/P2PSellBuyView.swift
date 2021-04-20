import UIKit

protocol P2PSellBuyViewDelegate: class {
   func didTap(view: P2PSellBuyView)
}

enum P2PSellBuyViewType: Int {
    case sell = 1
    case buy
    
    var backgroundColor: UIColor {
        switch self {
        case .sell: return UIColor(hexString: "#EDB900", alpha: 0.15)
        case .buy: return UIColor(hexString: "#48C583", alpha: 0.15)
        }
    }
    
    var contentColor: UIColor {
        switch self {
        case .sell: return UIColor(hexString: "#EDB900")
        case .buy: return UIColor(hexString: "#48C583")
        }
    }
    
    var arrowImage: UIImage {
        switch self {
        case .sell: return UIImage(named:"ptp_arrow_up")!
        case .buy: return UIImage(named: "ptp_arrow_down")!
        }
    }
    
    var title: String {
        switch self {
        case .sell: return "Sell"
        case .buy: return "Buy"
        }
    }
}

class P2PSellBuyView: UIView {
    
    weak var delegate: P2PSellBuyViewDelegate? {
        didSet {
            setupRecognizer()
        }
    }
    
    var currentType: P2PSellBuyViewType? {
        return type
    }
    
    private var selected = false
    private var type: P2PSellBuyViewType?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    init(radius: CGFloat = 12) {
        super.init(frame: .zero)
        setupUI()
        setupLayout()
        contentView.layer.cornerRadius = radius
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupRecognizer() {
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(didTap))
        addGestureRecognizer(tapRecognizer)
    }
    
    @objc private func didTap() {
        didSelected()
        delegate?.didTap(view: self)
    }
    
    func didSelected() {
        selected = !selected
        setSelected(selected)
    }
    
    func setSelected(_ isSelected: Bool) {
        if isSelected {
            selectedState()
        } else {
            regularState()
        }
    }
    
    private func selectedState() {
        contentView.layer.borderColor = type?.contentColor.cgColor
        contentView.layer.borderWidth = 2
    }
    
    private func regularState() {
        contentView.layer.borderColor = nil
        contentView.layer.borderWidth = 0
    }

    private lazy var contentView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 12
        view.layer.masksToBounds = true
        return view
    }()
    
    private lazy var arrowImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        return label
    }()

    func update(type: P2PSellBuyViewType) {
        contentView.backgroundColor = type.backgroundColor
        arrowImageView.image = type.arrowImage
        titleLabel.text = type.title
        titleLabel.textColor = type.contentColor
        self.type = type
    }
    
    func setupUI() {
        addSubview(contentView)
        contentView.addSubviews([
            arrowImageView,
            titleLabel
        ])
    }
    
    func setupLayout() {
        contentView.snp.makeConstraints{
            $0.height.equalTo(24)
            $0.width.equalTo(61)
            $0.edges.equalToSuperview()
        }
       
        titleLabel.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.bottom.equalToSuperview()
            $0.right.equalToSuperview().offset(-12)
        }
        arrowImageView.snp.makeConstraints {
            $0.height.equalTo(10)
            $0.centerY.equalToSuperview()
            $0.right.equalTo(titleLabel.snp.left).offset(-8)
        }
    }

}
