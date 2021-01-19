import Foundation

struct MessageModel {
  let type: SocketMessageType
  let headers: [String: String]
  let jsonData : [String : Any]?
  
  static var errorMessage = MessageModel(type: .ERROR, headers: ["":""], jsonData: nil)
}

class SocketResultMessageMapper {
  private enum Const {
    static let newLineSymbol = "\n"
    static let payloadSeparator = ":"
    static let endOfStringSymbol = "\0"
    static let emptyString = ""
  }
  
  func mapMessage(_ message: String) -> MessageModel {
    var content = message.components(separatedBy: Const.newLineSymbol)
    let type = getType(content: &content)
    let headers = getHeaders(content: content)
    let json = getJson(content: content)
    return MessageModel(type: type, headers: headers, jsonData: json)
  }
  
  private func getType(content: inout [String]) -> SocketMessageType {
    guard let type = SocketMessageType(rawValue: content.removeFirst()) else {
      return .UNDEFINED
    }
    return type
  }
  
  private func getHeaders(content: [String] ) -> [String : String] {
    var payload = [String: String]()
    content.forEach { (header) in
      let data = header.components(separatedBy: Const.payloadSeparator)
      if let header = data.first, let value = data.last {
        payload[header] = value
      }
    }
    return payload
  }
  
  private func getJson(content: [String]) -> [String: Any]? {
    let lastString = content.last
    var json:[String: Any]?

    guard let lastResult =
            lastString?.replacingOccurrences(of: Const.endOfStringSymbol, with: Const.emptyString) else { return json }
    if let data = lastResult.data(using: .utf8) {
      do {
        json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
      } catch {
        print(error.localizedDescription)
      }
    }
    return json
  }
}






