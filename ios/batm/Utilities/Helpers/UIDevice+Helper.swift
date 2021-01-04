import UIKit

extension UIDevice {
    var deviceOS: String {
        return "\(UIDevice.current.systemName) \(UIDevice.current.systemVersion)"
    }
    
    var deviceModel: String {
        var sysinfo = utsname()
        uname(&sysinfo)
        guard let encodedDeviceName = String(bytes: Data(bytes: &sysinfo.machine,
                                                         count: Int(_SYS_NAMELEN)),
                                             encoding: .ascii)
        else { return "" }
        return encodedDeviceName.trimmingCharacters(in: .controlCharacters)
    }
}
