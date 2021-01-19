import UIKit
import RxSwift
import RxCocoa

protocol DealsCellTypeRepresentable {
    var title: String { get }
    var image: UIImage? { get }
}

enum DealsCellType: CaseIterable, DealsCellTypeRepresentable {
    case transfer
    case swap
    case staking
    
    var title: String {
        switch self {
        case .transfer: return localize(L.Deals.Cell.transfer)
        case .swap: return localize(L.Deals.Cell.swap)
        case .staking: return localize(L.Deals.Cell.staking)
        }
    }
    
    var image: UIImage? {
        switch self {
        case .transfer: return UIImage(named: "paper-plane")
        case .swap: return UIImage(named: "deals_swap")
        case .staking: return UIImage(named: "deals_staking")
        }
    }
}

final class DealsCell: UITableViewCell {
    let iconImageView = UIImageView(image: nil)
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .medium)
        label.textColor = .slateGrey
        return label
    }()
    
    let stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.spacing = 15
        return stackView
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        iconImageView.image = nil
        titleLabel.text = nil
    }
    
    func configure(for type: DealsCellTypeRepresentable) {
        iconImageView.image = type.image
        iconImageView.isHidden = type.image == nil
        iconImageView.contentMode = .center
        titleLabel.text = type.title
        accessoryView = UIImageView(image: UIImage(named: "deals-disclosure"))
        separatorInset = UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 15)
    }
    
    private func setupUI() {
        contentView.addSubviews(stackView)
        stackView.addArrangedSubviews(iconImageView,
                                      titleLabel)
    }
    
    private func setupLayout() {
        iconImageView.snp.makeConstraints {
            $0.size.equalTo(24)
        }
        stackView.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.centerY.equalToSuperview()
        }
    }
}
