import Foundation

protocol SettingsModule: AnyObject {}
protocol SettingsModuleDelegate: AnyObject {
    func didSelectWallet()
    func didSelectSecurity()
    func didSelectKYC()
    func didSelectAbout()
    func didSelectSupport()
    func didSelectNotifications()
}
