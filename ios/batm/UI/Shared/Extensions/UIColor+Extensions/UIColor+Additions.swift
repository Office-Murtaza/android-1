import UIKit

protocol StatusColorPalette {
    static var background: UIColor { get }
    static var border: UIColor { get }
    static var font: UIColor { get }
}

extension UIColor {
    enum StatusColor {
        enum Gray: StatusColorPalette {
            static let background = UIColor(hexString: "#f3f3f3")
            static let border = UIColor(hexString: "#d9d9d9")
            static let font = UIColor(hexString: "#d9d9d9")
        }
        enum Yellow: StatusColorPalette {
            static let background = UIColor(hexString: "#fffcf3")
            static let border = UIColor(hexString: "#ffcd4c")
            static let font = UIColor(hexString: "#ffcd4c")
        }
        enum Green: StatusColorPalette {
            static let background = UIColor(hexString: "#effff0")
            static let border = UIColor(hexString: "#48be53")
            static let font = UIColor(hexString: "#48be53")
        }
        enum Orange: StatusColorPalette {
            static let background = UIColor(hexString: "#fff8f3")
            static let border = UIColor(hexString: "#ff984c")
            static let font = UIColor(hexString: "#ff984c")
        }
        enum Pink: StatusColorPalette {
            static let background = UIColor(hexString: "#8033FF")
            static let border = UIColor(hexString: "#6B33FF")
            static let font = UIColor(hexString: "#6B33FF")
        }
        enum Red: StatusColorPalette {
            static let background = UIColor(hexString: "#FF6433")
            static let border = UIColor(hexString: "#FF3333")
            static let font = UIColor(hexString: "#FF3333")
        }
        enum Blue: StatusColorPalette {
            static let background = UIColor(hexString: "#0073e4")
            static let border = UIColor(hexString: "#0000E4")
            static let font = UIColor(hexString: "#0000E4")
        }
    }
    
    @nonobjc class var warmGrey: UIColor {
        return UIColor(white: 141.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var warmGreyTwo: UIColor {
        return UIColor(white: 124.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var warmGreyThree: UIColor {
        return UIColor(white: 152.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var warmGreyFour: UIColor {
        return UIColor(white: 155.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var white: UIColor {
        return UIColor(white: 1.0, alpha: 1.0)
    }
    
    @nonobjc class var whiteTwo: UIColor {
        return UIColor(white: 243.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var whiteThree: UIColor {
        return UIColor(white: 234.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var whiteFour: UIColor {
        return UIColor(white: 247.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var whiteFive: UIColor {
        return UIColor(white: 224.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var whiteSix: UIColor {
        return UIColor(white: 213.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var slateGrey: UIColor {
        return UIColor(red: 88.0 / 255.0, green: 88.0 / 255.0, blue: 90.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var ceruleanBlue: UIColor {
        return UIColor(red: 0.0, green: 115.0 / 255.0, blue: 228.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var greyish: UIColor {
        return UIColor(red: 175.0 / 255.0, green: 176.0 / 255.0, blue: 171.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var greyishTwo: UIColor {
        return UIColor(white: 169.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var lightGold: UIColor {
        return UIColor(red: 1.0, green: 205.0 / 255.0, blue: 76.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var pinkishGrey: UIColor {
        return UIColor(white: 202.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var pinkishGreyTwo: UIColor {
        return UIColor(white: 193.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var orangeyRed: UIColor {
        return UIColor(red: 1.0, green: 50.0 / 255.0, blue: 50.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var black: UIColor {
        return UIColor(white: 0.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var black10: UIColor {
        return UIColor(white: 0.0 / 255.0, alpha: 0.1)
    }
    
    @nonobjc class var blackTwo: UIColor {
        return UIColor(white: 10.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var blackThree: UIColor {
        return UIColor(white: 35.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var cadetBlue: UIColor {
        return UIColor(red: 77.0 / 255.0, green: 108.0 / 255.0, blue: 145.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var darkMint: UIColor {
        return UIColor(red: 72.0 / 255.0, green: 190.0 / 255.0, blue: 83.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var tomato: UIColor {
        return UIColor(red: 224.0 / 255.0, green: 45.0 / 255.0, blue: 45.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var mango: UIColor {
        return UIColor(red: 255.0 / 255.0, green: 163.0 / 255.0, blue: 43.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var puce: UIColor {
        return UIColor(red: 161.0 / 255.0, green: 126.0 / 255.0, blue: 85.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var skyBlue: UIColor {
        return UIColor(red: 98.0 / 255.0, green: 159.0 / 255.0, blue: 252.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var carolinaBlue: UIColor {
        return UIColor(red: 132.0 / 255.0, green: 180.0 / 255.0, blue: 253.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var brownishGrey: UIColor {
        return UIColor(white: 93.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var duckEggBlue: UIColor {
        return UIColor(red: 235.0 / 255.0, green: 244.0 / 255.0, blue: 253.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var paleOliveGreen: UIColor {
        return UIColor(red: 159.0 / 255.0, green: 210.0 / 255.0, blue: 86.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var pastelOrange: UIColor {
        return UIColor(red: 255.0 / 255.0, green: 152.0 / 255.0, blue: 76.0 / 255.0, alpha: 1.0)
    }
    
    @nonobjc class var textfieldLightGray: UIColor {
        return UIColor(red: 141.0 / 255.0, green: 141.0 / 255.0, blue: 141.0 / 255.0, alpha: 0.1)
    }
    
    @nonobjc class var errorRed: UIColor {
        return UIColor(hexString: "B00020")
    }
}

extension UIColor {
    convenience init(hexString: String, alpha: CGFloat = 1.0) {
        let hexString: String = hexString.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        let scanner = Scanner(string: hexString)
        if (hexString.hasPrefix("#")) {
            scanner.scanLocation = 1
        }
        var color: UInt32 = 0
        scanner.scanHexInt32(&color)
        let mask = 0x000000FF
        let r = Int(color >> 16) & mask
        let g = Int(color >> 8) & mask
        let b = Int(color) & mask
        let red   = CGFloat(r) / 255.0
        let green = CGFloat(g) / 255.0
        let blue  = CGFloat(b) / 255.0
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
