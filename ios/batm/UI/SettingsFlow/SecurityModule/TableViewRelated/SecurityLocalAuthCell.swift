import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

protocol SecurityLocalAuthCellDelegate: AnyObject {
    func didTapChangeLocalAuthCell()
}

class SecurityLocalAuthCell: UITableViewCell {
    var disposeBag = DisposeBag()
    weak var delegate: SecurityLocalAuthCellDelegate?
    
    lazy var cellSwitch: UISwitch = {
        let toggle = UISwitch()
        toggle.onTintColor = .ceruleanBlue
        return toggle
    }()
    
    lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.spacing = 15
        return stackView
    }()
    
    lazy var iconImageView = UIImageView(image: nil)
    
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .medium)
        label.textColor = .slateGrey
        return label
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        cellSwitch.isOn = UserDefaultsHelper.isLocalAuthEnabled
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
        cellSwitch.isOn = UserDefaultsHelper.isLocalAuthEnabled
    }
    
    private func setupUI() {
        contentView.addSubviews(stackView,
                                cellSwitch)
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
        
        cellSwitch.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-15)
            $0.centerY.equalToSuperview()
        }
    }
    
    func configure(for type: SettingsCellTypeRepresentable) {
        accessoryType = .none
        iconImageView.image = type.image
        iconImageView.isHidden = type.image == nil
        iconImageView.contentMode = .center
        titleLabel.text = type.title
        isUserInteractionEnabled = type.isEnabled
        
        cellSwitch.rx.value.changed.subscribe(onNext: { [weak self] isChanged in
            self?.delegate?.didTapChangeLocalAuthCell()
        })
        .disposed(by: disposeBag)
    }
}
