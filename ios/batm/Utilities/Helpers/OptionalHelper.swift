import Foundation

extension Optional {
    @discardableResult
    func unwrap<Ret>(_ f: @escaping (Wrapped) -> Ret) -> Ret? {
        if case .some(let wrapped) = self { return f(wrapped) }
        return nil
    }
}
