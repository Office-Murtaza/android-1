import UIKit
import SnapKit

class P2PTradeCell: UITableViewCell {
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        paymentMethodsView.arrangedSubviews.forEach{ $0.removeFromSuperview() }
    }
    
    private var viewModel: TradeViewModel?
    
    private let coinView = P2PCoinView()
    private let markerIdView = MarkerIdView()
    private let rateView = P2PCellRateView()
    private let distanceView = P2PDistanceView()
    
    private lazy var priceLabel: UILabel = {
        let price = UILabel()
        price.font = .systemFont(ofSize: 16, weight: .bold)
        return price
    }()
    
    private lazy var separatorView: UIView = {
        let view = UIView()
        view.backgroundColor = .gray
        return view
    }()
    
    private lazy var limitLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    private lazy var paymentMethodsView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    
    func update(viewModel: TradeViewModel) {
        self.viewModel = viewModel
        coinView.update(coin: viewModel.coin)
        priceLabel.text = viewModel.price
        markerIdView.update(markerId: viewModel.markerPublicId, statusImage: viewModel.tradeStatusImage)
        limitLabel.text = viewModel.limit
        setupPaymentMethods(images: viewModel.paymentMethods)
   
        rateView.update(rate: viewModel.tradingRate, tradesCount: viewModel.totalTrades)
        rateView.isHidden = viewModel.isRateHidden
        
        if let distance = viewModel.distanceInMiles {
            distanceView.update(distance: "\(distance) miles", isDistanceNeeded: false)
        }
    }

    func setupPaymentMethods(images: [UIImage]?) {
        guard let images = images else { return }
        let imageViews = images.map { UIImageView(image: $0) }
        paymentMethodsView.addArrangedSubviews(imageViews)
    }
    
    private func setupUI() {
        addSubviews([
            coinView,
            priceLabel,
            markerIdView,
            separatorView,
            limitLabel,
            rateView,
            paymentMethodsView,
            distanceView
        ])
    }
    
    private func setupLayout() {
        coinView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(16)
            $0.left.equalToSuperview().offset(16)
            $0.height.equalTo(24)
        }
        
        priceLabel.snp.makeConstraints {
            $0.top.equalTo(coinView.snp.bottom).offset(5)
            $0.left.equalTo(coinView)
        }
        
        markerIdView.snp.makeConstraints {
            $0.left.equalTo(priceLabel)
            $0.top.equalTo(priceLabel.snp.bottom).offset(10)
        }
        
        let separatorHeight = 1 / UIScreen.main.scale
        separatorView.snp.makeConstraints {
            $0.height.equalTo(separatorHeight)
            $0.bottom.equalToSuperview()
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
        }
        
        limitLabel.snp.makeConstraints {
            $0.top.equalTo(coinView)
            $0.right.equalToSuperview().offset(-16)
        }
        
        rateView.snp.makeConstraints {
            $0.top.equalTo(markerIdView.snp.bottom).offset(5)
            $0.left.equalTo(coinView)
        }
        
        
        paymentMethodsView.snp.makeConstraints {
            $0.right.equalTo(limitLabel)
            $0.top.equalTo(limitLabel.snp.bottom).offset(5)
        }
        
        distanceView.snp.makeConstraints {
            $0.top.equalTo(markerIdView.snp.top)
            $0.right.equalTo(paymentMethodsView)
        }
    }
}

// MARK: - Marker ID view

class MarkerIdView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(markerId: String, statusImage: UIImage?) {
        titleLabel.text = markerId
        if statusImage != nil {
            checkView.image = statusImage
        }
    }
    
    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    private lazy var imageView: UIImageView = {
        let image = UIImageView()
        image.contentMode = .scaleAspectFit
        image.image = UIImage(named: "p2p_user_placeholder")
        return image
    }()
    
    private lazy var checkView: UIImageView = {
        let image = UIImageView()
        image.contentMode = .scaleAspectFit
        return image
    }()
    
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        return label
    }()
    
    private func setupUI() {
        stackView.addArrangedSubviews([
            imageView,
            titleLabel,
            checkView
        ])
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
}

//MARK: - Rate View

class P2PCellRateView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public func update(rate: String, tradesCount: String) {
        rateLabel.text = rate
        tradesLabel.text = "\(tradesCount)+"
        tradesTitleLabel.text = "Trades"
    }
    
    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    private lazy var rateImageView: UIImageView = {
        let image = UIImageView()
        image.contentMode = .scaleAspectFit
        image.image = UIImage(named: "p2p_star")
        return image
    }()
    
    private lazy var rateLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        return label
    }()
    
    private lazy var tradesLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        return label
    }()
    
    private lazy var tradesTitleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.textColor = .darkGray
        return label
    }()
    
    private lazy var verticalSeparator: UIView = {
        let separator = UIView()
        separator.backgroundColor = .gray
        return separator
    }()
    
    func setupUI() {
        stackView.addArrangedSubviews([
            rateImageView,
            rateLabel,
            verticalSeparator,
            tradesLabel,
            tradesTitleLabel
        ])
    }
    
    func setupLayout() {
        stackView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        
        let separatorWith = 1 / UIScreen.main.scale
        verticalSeparator.snp.makeConstraints {
            $0.width.equalTo(separatorWith)
        }
        
    }
}

protocol P2PDistanceViewDelegate: AnyObject {
    func didTapDistance()
}

class P2PDistanceView: UIView {
    
    weak var delegate: P2PDistanceViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    private lazy var bigStackView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 16
        return stack
    }()
    
    private lazy var myLocationImageView: UIImageView = {
        let image = UIImageView()
        image.contentMode = .scaleAspectFit
        return image
    }()
    
    private lazy var distanceLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.adjustsFontSizeToFitWidth = true
        return label
    }()
    
    private lazy var userDistanceButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "p2p_user_distance"), for: .normal)
        button.addTarget(self, action: #selector(didTapDistance), for: .touchUpInside)
        return button
    }()
    
    private var isDistanceNeeded: Bool = false
    
    
    @objc func didTapDistance() {
        delegate?.didTapDistance()
    }
    
    public func update(distance: String, isDistanceNeeded: Bool) {
        self.isDistanceNeeded = isDistanceNeeded
        
        distanceLabel.text = distance
        myLocationImageView.image = UIImage(named: "p2p_my_location")
        userDistanceButton.isHidden = !isDistanceNeeded
        
        setupUI()
        setupLayout()
    }
    
    private func setupUI() {
        addSubview(bigStackView)
        stackView.addArrangedSubviews([
            myLocationImageView,
            distanceLabel
        ])
        
        bigStackView.addArrangedSubview(stackView)
        if isDistanceNeeded {
            bigStackView.addArrangedSubview(userDistanceButton)
        }
    }

    private func setupLayout() {
        let offset = isDistanceNeeded ? -16 : 0
        bigStackView.snp.makeConstraints {
            $0.top.left.bottom.equalToSuperview()
            $0.right.equalToSuperview().offset(offset)
        }
    }
}
