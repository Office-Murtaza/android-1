import UIKit

class P2PTagContainerView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        translatesAutoresizingMaskIntoConstraints = false
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    init(width: CGFloat) {
        super.init(frame: .zero)
        self.estimatedWidth = width
    }

    private var estimatedWidth: CGFloat = 0
    
    func update(tags: [P2PTagView]) {
        subviews.forEach { (tagView) in
            tagView.removeFromSuperview()
        }
        addSubviews(tags)
        reloadLayout()
    }
    
    func selectedTitles() -> [String] {
        return subviews
            .compactMap { $0 as? P2PTagView }
            .filter { $0.isSelected == true }
            .compactMap { $0.title }
    }
    
    func resetAll() {
        subviews
            .compactMap { $0 as? P2PTagView }
            .forEach { $0.reset() }
    }
    
    func selectTag(title: String) {
        let tag =  subviews.compactMap { $0 as? P2PTagView }
                   .first(where: { $0.title == title } )
        tag?.didSelected()
    }
    
    func selectAll() {
        subviews
            .compactMap { $0 as? P2PTagView }
            .forEach { $0.didSelected() }
    }
    
    func reloadLayout() {
        let containerWidth = estimatedWidth
        let tagYSpacing: CGFloat = 8
        let tagXSpacing: CGFloat = 8
        var currentOriginX: CGFloat = 0
        var currentOriginY: CGFloat = 0
        var prevView: UIView? = nil
        let firstTag = subviews.first
        subviews.forEach { (tag) in

            if currentOriginX + tag.frame.width > containerWidth {
                currentOriginX = 0
                currentOriginY += tag.frame.size.height + tagYSpacing
                prevView = nil
            }

            tag.snp.makeConstraints {
                if let pr = prevView {
                    $0.left.equalTo(pr.snp.right).offset(8)
                } else {
                    $0.left.equalToSuperview().offset(8)
                }
                $0.top.equalToSuperview().offset(currentOriginY)
            }

            prevView = tag
            currentOriginX += tag.frame.width + tagXSpacing
        }
        self.snp.makeConstraints {
            $0.height.equalTo(currentOriginY + (firstTag?.frame.size.height ?? 0) * 2)
        }
    }
}
