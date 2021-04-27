import UIKit
import SnapKit
import MultiSlider
import MaterialComponents

class P2PDistanceRangeView: UIView {
    
    typealias P2PDistanceRangeAction = (Int) -> Void
    
    lazy var distanceSlider: MultiSlider = {
        let slider = MultiSlider()
        slider.orientation = .horizontal
        slider.tintColor = UIColor(hexString: "0073E4")
        slider.showsThumbImageShadow = false
        
        slider.minimumValue = 0
        slider.maximumValue = 1

        slider.value = [0, 1]
        slider.outerTrackColor = .lightGray
        slider.thumbImage = UIImage(named: "p2p_slider_thumb")
        slider.addTarget(self, action: #selector(sliderChanged(_:)), for: .valueChanged)
        
        
        return slider
    }()
    
    private var minRange: P2PDistanceRangeAction?
    private var maxRange: P2PDistanceRangeAction?
    
    let fromField = P2POutlinedTextField()
    let toField = P2POutlinedTextField()
    private var measureString = ""
    private var isMeasurePosistionLast: Bool = true
    
    private lazy var hyphenView: UIView = {
        let view = UIView()
        view.backgroundColor = .black
        return view
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func sliderChanged(_ slider: MultiSlider) {
        let (fromAttrString, toAttrString) = transformToAttributedRange(from: slider.value.first ?? 0, to: slider.value.last ?? 0)
        minRange?(Int(slider.value.first ?? 0))
        maxRange?(Int(slider.value.last ?? 0))
        fromField.update(attributedText: fromAttrString)
        toField.update(attributedText: toAttrString)
    }
  
  func setInitFieldsValues(from: CGFloat, to: CGFloat) {
    let (fromAttrString, toAttrString) = transformToAttributedRange(from: from, to: to)
    fromField.update(attributedText: fromAttrString)
    toField.update(attributedText: toAttrString)
  }
    
    func selectedMinRange(_ minRange: @escaping P2PDistanceRangeAction, maxRange: @escaping P2PDistanceRangeAction) {
        self.minRange = minRange
        self.maxRange = maxRange
    }
    
    fileprivate func rangeWithAppendedMeasureString(_ fromRangeString: String, _ valueAttributes: [NSAttributedString.Key : UIFont], _ milesAttributes: [NSAttributedString.Key : UIFont], _ toRangeString: String) -> (from: NSAttributedString, to: NSAttributedString) {
        let fromMutableAttr = NSMutableAttributedString(string: fromRangeString, attributes: valueAttributes)
        let milesAttr = NSAttributedString(string: measureString, attributes: milesAttributes)
        fromMutableAttr.append(milesAttr)
        
        let toMutableAttr = NSMutableAttributedString(string: toRangeString, attributes: valueAttributes)
        toMutableAttr.append(milesAttr)
        
        return (from: fromMutableAttr, to: toMutableAttr)
    }
    
    fileprivate func rangeWithFrontMeasureString(_ fromRangeString: String, _ valueAttributes: [NSAttributedString.Key : UIFont], _ milesAttributes: [NSAttributedString.Key : UIFont], _ toRangeString: String) -> (from: NSAttributedString, to: NSAttributedString) {
        let fromRangeAttr = NSMutableAttributedString(string: fromRangeString, attributes: valueAttributes)
       
        let fromAttrString = NSMutableAttributedString(string: measureString, attributes: milesAttributes)
        
        fromAttrString.append(fromRangeAttr)
        
        let toAttrString = NSMutableAttributedString(string: measureString, attributes: milesAttributes)
        let toRangeAttr = NSMutableAttributedString(string: toRangeString, attributes: valueAttributes)
        
        toAttrString.append(toRangeAttr)
        
        return (from: fromAttrString, to: toAttrString)
    }
    
    private func transformToAttributedRange(from: CGFloat, to: CGFloat) -> (from: NSAttributedString, to: NSAttributedString) {
        let fromRangeString = Int(from).description
        let toRangeString = Int(to).description
        
        let valueFont = UIFont.systemFont(ofSize: 16, weight: .semibold)
        let milesFont = UIFont.systemFont(ofSize: 16, weight: .regular)
        
        let valueAttributes = [NSAttributedString.Key.font: valueFont]
        let milesAttributes = [NSAttributedString.Key.font: milesFont]
        
        if isMeasurePosistionLast {
            return rangeWithAppendedMeasureString(fromRangeString, valueAttributes, milesAttributes, toRangeString)
        } else {
            return rangeWithFrontMeasureString(fromRangeString, valueAttributes, milesAttributes, toRangeString)
        }
    }
    
    
    func setupUI() {
        
        fromField.setup(placeholder: "From", attributedText: NSAttributedString(), userInteractionEnabled: false)
        toField.setup(placeholder: "To", attributedText: NSAttributedString(), userInteractionEnabled: false)
        
        addSubviews([
            distanceSlider,
            fromField,
            hyphenView,
            toField
        ])
    }
    
    func setupLayout() {
        distanceSlider.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.left.equalToSuperview()
            $0.right.equalToSuperview()
        }
        
        fromField.snp.makeConstraints {
            $0.top.equalTo(distanceSlider.snp.bottom)
            $0.left.equalToSuperview()
            $0.height.equalTo(40)
            $0.right.equalTo(hyphenView.snp.left).offset(-16)
        }
        
        hyphenView.snp.remakeConstraints {
            $0.centerX.equalTo(self.snp.centerX)
            $0.centerY.equalTo(fromField).offset(5)
            $0.width.equalTo(13)
            $0.height.equalTo(2/UIScreen.main.scale)
        }
        
        toField.snp.makeConstraints {
            $0.top.equalTo(fromField)
            $0.right.equalToSuperview()
            $0.left.equalTo(hyphenView.snp.right).offset(16)
            $0.bottom.equalTo(fromField)
        }
    }
    
    func setup(range: [CGFloat], measureString: String, isMeasurePosistionLast: Bool = true , stepSize: CGFloat = 100) {
        distanceSlider.minimumValue = range.first ?? 0
        distanceSlider.maximumValue = range.last ?? 1
        distanceSlider.snapStepSize = stepSize
        distanceSlider.value = range
        self.measureString = measureString
        self.isMeasurePosistionLast = isMeasurePosistionLast
        let (from, to) = transformToAttributedRange(from: distanceSlider.minimumValue, to: distanceSlider.maximumValue)
        fromField.update(attributedText: from)
        toField.update(attributedText: to)
    }
}
