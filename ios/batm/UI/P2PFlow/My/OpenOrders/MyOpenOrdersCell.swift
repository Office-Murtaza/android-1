import UIKit

class MyOpenOrdersCell: UITableViewCell {
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private let coinView = P2PCoinView()
    private let sellBuyView = P2PSellBuyView()
    private let statusView = P2POrderStatusView()
    private let cryptoAmountView = P2PAmountView()
    private let fiatAmountView = P2PAmountView()

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
            sellBuyView,
            separatorView,
            statusView,
            cryptoAmountView,
            fiatAmountView
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
        
        paymentMethodsView.snp.makeConstraints {
            $0.right.equalTo(statusView)
            $0.top.equalTo(statusView.snp.bottom).offset(5)
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
        
        statusView.snp.makeConstraints {
            $0.top.equalTo(coinView)
            $0.right.equalToSuperview().offset(-16)
            $0.height.equalTo(20)
        }
        
        cryptoAmountView.snp.makeConstraints {
            $0.left.equalTo(coinView)
            $0.top.equalTo(priceLabel.snp.bottom).offset(10)
        }
        
        fiatAmountView.snp.makeConstraints {
            $0.top.equalTo(cryptoAmountView)
            $0.right.equalTo(paymentMethodsView)
        }
        
    }
    
    func update(viewModel: MyOpenOrdersCellViewModel) {
        coinView.update(coin: viewModel.coin)
        priceLabel.text = viewModel.price
        
//        sellBuyView.update(type: .sell)
        
        statusView.update(status: viewModel.orderStatus)
        setupPaymentMethods(images: viewModel.paymentMethods)
        cryptoAmountView.update(title: viewModel.cryptoAmountTitle,
                                value: viewModel.cryptoAmount,
                                textAlignMent: .left)
        fiatAmountView.update(title: viewModel.fiatAmountTitle,
                              value: viewModel.fiatAmount,
                              textAlignMent: .right)
    }

    private func setupPaymentMethods(images: [UIImage]?) {
        guard let images = images else { return }
        let imageViews = images.map { UIImageView(image: $0) }
        paymentMethodsView.addArrangedSubviews(imageViews)
    }
}
