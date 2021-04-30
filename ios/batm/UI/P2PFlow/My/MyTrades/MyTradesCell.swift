import UIKit
import SnapKit

class MyTradesCell: UITableViewCell {
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
        resetPaymentViewContent()
    }
    
    private func resetPaymentViewContent() {
        for view in paymentMethodsView.arrangedSubviews {
            paymentMethodsView.removeArrangedSubview(view)
            view.removeFromSuperview()
        }
    }
    
    private let coinView = P2PCoinView()
    
    private let sellBuyView = P2PSellBuyView()
    
    private lazy var priceLabel: UILabel = {
        let price = UILabel()
        price.font = .systemFont(ofSize: 16, weight: .regular)
        return price
    }()
    
    private lazy var paymentMethodsView: UIStackView = {
        let stack = UIStackView()
        addSubview(stack)
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        return stack
    }()
    
    private lazy var limitLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .bold)
        return label
    }()
    
    private lazy var separatorView: UIView = {
        let view = UIView()
        view.backgroundColor = .gray
        return view
    }()
    
    private func setupUI() {
        addSubviews([
            coinView,
            priceLabel,
            paymentMethodsView,
            limitLabel,
            separatorView,
            sellBuyView
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
        
        limitLabel.snp.makeConstraints {
            $0.top.equalTo(coinView)
            $0.right.equalToSuperview().offset(-16)
        }
        
        paymentMethodsView.snp.makeConstraints {
            $0.right.equalTo(limitLabel)
            $0.top.equalTo(limitLabel.snp.bottom).offset(5)
        }
        
        let separatorHeight = 1 / UIScreen.main.scale
        separatorView.snp.makeConstraints {
            $0.height.equalTo(separatorHeight)
            $0.bottom.equalToSuperview()
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
        }
        
        sellBuyView.snp.makeConstraints {
            $0.top.equalTo(coinView.snp.top)
            $0.left.equalTo(coinView.snp.right).offset(10)
        }
    }
    
    private func setupPaymentMethods(images: [UIImage]?) {
        guard let images = images else { return }
        let imageViews = images.map { UIImageView(image: $0) }
        
        resetPaymentViewContent()
        
        paymentMethodsView.addArrangedSubviews(imageViews)
    }
    
    func update(viewModel: MyTradesCellViewModel) {
        priceLabel.text = viewModel.price
        coinView.update(coin: viewModel.coin)
        setupPaymentMethods(images: viewModel.paymentMethods)
        limitLabel.text = viewModel.limit
        sellBuyView.update(type: viewModel.sellbuyType)
    }
}
