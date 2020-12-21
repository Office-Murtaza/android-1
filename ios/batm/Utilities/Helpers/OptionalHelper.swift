import Foundation

extension Optional {
    @discardableResult
    func unwrap<Ret>(_ f: @escaping (Wrapped) -> Ret) -> Ret? {
        if case .some(let wrapped) = self { return f(wrapped) }
        return nil
    }
}

extension Optional where Wrapped == Bool {
    /// The unwrapped value or false
    var value: Bool {
        switch self {
        case .some(let value):
            return value
        default:
            return false
        }
    }
}
