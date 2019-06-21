import Foundation

typealias RecursiveLock = NSRecursiveLock

extension RecursiveLock {
  
  final func calculateLocked<T>(_ action: () throws -> T) rethrows -> T {
    lock(); defer { unlock() }
    let result = try action()
    return result
  }
}
