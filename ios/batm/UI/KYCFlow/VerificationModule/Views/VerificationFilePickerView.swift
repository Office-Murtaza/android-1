import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class VerificationFilePickerView: UIView, HasDisposeBag {
    private lazy var container: UIView = {
        let view = UIView()
        let imageView = UIImageView()
        view.addSubview(imageView)
        imageView.image = UIImage(named: "kyc_rectangle")
        imageView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
        return view
    }()

    private lazy var selectedImageView = UIImageView(image: nil)

    fileprivate lazy var removeButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "close_id_button"), for: .normal)
        return button
    }()

    fileprivate lazy var addScanButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "add_id_button"), for: .normal)
        button.setTitleColor(.systemBlue, for: .normal)
        return button
    }()

    fileprivate lazy var errorLabel: UILabel = {
        let label = UILabel()
        label.textColor = .tomato
        label.textAlignment = .center
        label.font = .systemFont(ofSize: 16)
        label.numberOfLines = 0
        return label
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)

        setupUI()
        setupLayout()
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setImageContainer(with title: String, color: UIColor) {
        container.tintColor = color
        addScanButton.setTitle(title, for: .normal)
    }

    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false

        addSubviews(container,
                    errorLabel)
        container.addSubviews(removeButton,
                              addScanButton)
    }

    private func setupLayout() {
        addScanButton.snp.makeConstraints {
            $0.centerY.equalTo(container.snp.centerY)
            $0.centerX.equalTo(container.snp.centerX)
            $0.height.equalTo(24)
        }

        removeButton.snp.makeConstraints {
            $0.centerX.equalTo(container.snp.trailing)
            $0.centerY.equalTo(container.snp.top)
            $0.size.equalTo(40)
        }
        
        errorLabel.snp.makeConstraints {
            $0.top.equalTo(container.snp.bottom).offset(15)
            $0.left.right.bottom.equalToSuperview()
        }

        removeImage()
    }

    func showImage(_ image: UIImage) {
        selectedImageView.image = image

        if selectedImageView.superview == nil {
            container.insertSubview(selectedImageView, at: 0)
        }

        selectedImageView.snp.remakeConstraints {
            $0.edges.equalToSuperview()
            $0.keepRatio(for: selectedImageView)
        }

        container.snp.remakeConstraints {
            $0.top.centerX.equalToSuperview()
            $0.left.greaterThanOrEqualToSuperview()
            $0.right.lessThanOrEqualToSuperview()
            $0.height.equalTo(190)
        }

        removeButton.isHidden = false
        addScanButton.isHidden = true
    }

    func removeImage() {
        selectedImageView.image = nil
        selectedImageView.removeFromSuperview()

        container.snp.remakeConstraints {
            $0.leading.trailing.top.bottom.equalToSuperview()
        }

        removeButton.isHidden = true
        addScanButton.isHidden = false
    }
}

extension Reactive where Base == VerificationFilePickerView {
    var select: Driver<Void> {
        return base.addScanButton.rx.tap.asDriver()
    }
    var remove: Driver<Void> {
        return base.removeButton.rx.tap.asDriver()
    }
    var image: Binder<UIImage?> {
        return Binder(base) { target, value in
            if let image = value {
                target.showImage(image)
            } else {
                target.removeImage()
            }
        }
    }
    var imageErrorText: Binder<String?> {
        return Binder(base) { target, value in
            target.errorLabel.text = value
        }
    }
}
