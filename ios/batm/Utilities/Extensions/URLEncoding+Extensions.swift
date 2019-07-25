import Moya

extension URLEncoding {
  static let customDefault = URLEncoding(destination: .queryString, arrayEncoding: .noBrackets, boolEncoding: .literal)
}
