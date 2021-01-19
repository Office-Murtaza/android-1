
import UIKit

class TransferContactCell: UITableViewCell {
    lazy var contactView = ContactView()

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
        contactView.clearData()
    }
    
    func setupUI(){
        addSubview(contactView)
    }
    
    func setupLayout() {
        contactView.snp.makeConstraints{
            $0.edges.equalToSuperview()
        }
    }
}
