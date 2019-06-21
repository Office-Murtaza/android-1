import Foundation

extension SectionInfo: Equatable where E: Equatable {
  static func == (lhs: SectionInfo<E>, rhs: SectionInfo<E>) -> Bool {
    return lhs.name == rhs.name && lhs._elements == rhs._elements
  }
}

struct SectionInfo<E>: SectionType {
  
  var rawElements: [E?] {
    return _elements
  }
  
  func index(after index: Int) -> Int {
    return _elements.index(after: index)
  }
  
  var startIndex: Int {
    return _elements.startIndex
  }
  
  var endIndex: Int {
    return _elements.endIndex
  }
  
  var compactElements: [E] {
    return _elements.compactMap { $0 }
  }
  
  private var _elements: [E?]
  
  let name: String
  
  init() {
    self.init(elements: [])
  }
  
  init(name: String = "", elements: [E] = []) {
    self.name = name
    _elements = elements
  }
  
  mutating func compact() {
    _elements = compactElements
  }
  
  mutating func insert(_ newElement: E, at index: Int) {
    compact()
    stub(to: index)
    if index < _elements.endIndex && _elements[index] == nil {
      _elements[index] = newElement
    } else {
      _elements.insert(newElement, at: index)
    }
  }
  
  subscript(index: Int) -> E {
    get {
      return _elements[index]!
    }
    set(newElement) {
      _elements[index] = newElement
    }
  }

    mutating func replaceSubrange<C>(_ subrange: Range<SectionInfo.Index>, with newElements: C)
    where C: Collection, C.Element == E {
      if newElements.isEmpty {
        _elements.replaceSubrange(subrange, with: Array(repeating: nil, count: subrange.count))
      } else {
        _elements.replaceSubrange(subrange, with: Array(newElements))
      }
    }
  
  private mutating func stub(to index: Int) {
    let stubs = Array(repeating: E?.none, count: Swift.max(index - _elements.endIndex, 0))
    _elements.append(contentsOf: stubs)
  }
}
