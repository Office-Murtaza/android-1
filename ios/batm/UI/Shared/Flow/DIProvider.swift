import Foundation
import Swinject

protocol DIProvider: AnyObject {
  var parent: DIProvider? { get }
  var assembler: Assembler { get }
  var resolver: Resolver { get }
}

extension DIProvider {
  var resolver: Resolver {
    return assembler.resolver
  }
}

extension Assembler: DIProvider {
  var parent: DIProvider? { return nil }
  var assembler: Assembler { return self }
}
