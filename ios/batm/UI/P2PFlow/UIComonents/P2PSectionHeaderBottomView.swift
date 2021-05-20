import UIKit

class P2PSectionHeaderBottomView: P2PSectionHeaderView {
    override func setupLayout() {
         titleLabel.snp.makeConstraints {
             $0.bottom.right.equalToSuperview()
             $0.left.equalToSuperview().offset(15)
         }
     }
}
