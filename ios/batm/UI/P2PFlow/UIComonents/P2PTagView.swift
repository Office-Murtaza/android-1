import UIKit
import SnapKit

protocol P2PTagViewDelegate: class {
    func didTapTag(view: P2PTagView)
}

class P2PTagView: UIView {
    
    weak var delegte: P2PTagViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        translatesAutoresizingMaskIntoConstraints = false
        setupUI()
        setupRecognizer()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private let selectedView = P2PTagViewSelectedOverlay()
    
    var title: String? {
        return titleLabel.text
    }
    
    var isSelected: Bool {
        return selected
    }
   
    func reset() {
        selected = false
        setSelected(selected)
    }
   
    private lazy var container: UIView = {
        let container = UIView()
        container.backgroundColor = UIColor(hexString: "#8D8D8D", alpha: 0.1)
        container.layer.cornerRadius = 16
        container.layer.masksToBounds = true
        return container
    }()
    
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        return label
    }()
    
    private var selected = false
    
    private func setupRecognizer() {
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(didTap))
        addGestureRecognizer(tapRecognizer)
    }

    @objc private func didTap() {
        didSelected()
        delegte?.didTapTag(view: self)
    }
    
    func didSelected() {
        selected = !selected
        setSelected(selected)
    }
    
    private func setupUI() {
        addSubview(container)
        container.addSubviews([
            imageView,
            titleLabel,
            selectedView
        ])
        setSelected(selected)
    }
    
    private func setupLayout() {
        
        let imageViewWidth = imageView.image != nil ? 20 : 0
        
        container.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        
        imageView.snp.makeConstraints {
            $0.left.equalToSuperview().offset(8)
            $0.top.equalToSuperview().offset(6)
            $0.bottom.equalToSuperview().offset(-6)
            $0.height.width.equalTo(20)
            $0.width.equalTo(imageViewWidth)
        }
        
        titleLabel.snp.makeConstraints {
            $0.left.equalTo(imageView.snp.right).offset(4)
            $0.top.equalTo(imageView)
            $0.bottom.equalTo(imageView)
            $0.right.equalToSuperview().offset(-8)
        }
        
        selectedView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    func update(image: UIImage?, title: String) {
        imageView.image = image
        titleLabel.text = title
        setupLayout()
        selectedView.update(isImageHidden: image == nil)
    }
    
    func setSelected(_ isSelected: Bool) {
        if isSelected {
            selectedState()
        } else {
            regularState()
        }
    }
    
    private func selectedState() {
        container.layer.borderColor = UIColor(hexString: "#0073E4").cgColor
        container.layer.borderWidth = 2
        selectedView.isHidden = false
       
    }
    
    private func regularState() {
        container.layer.borderColor = nil
        container.layer.borderWidth = 0
        selectedView.isHidden = true
    }
    
}

class P2PTagViewSelectedOverlay: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var imageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFit
        view.image = UIImage(named: "p2p_tag_selected")
        return view
    }()
    
    private func setupUI() {
        backgroundColor = UIColor(hexString: "#0073E4", alpha: 0.2)
        addSubview(imageView)
    }
    
    private func setupLayout() {
        imageView.snp.makeConstraints {
            $0.left.equalToSuperview().offset(8)
            $0.top.equalToSuperview().offset(6)
            $0.bottom.equalToSuperview().offset(-6)
            $0.height.width.equalTo(20)
        }
    }
    
    func update(isImageHidden: Bool = false) {
        imageView.isHidden = isImageHidden
    }
}

