//
//  SocketRequestBuilder.swift
//  batm
//
//  Created by Dmytro Frolov on 08.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation

enum SocketMessageType: String {
  case CONNECT
  case SUBSCRIBE
  case UNSUBSCRIBE
  case CONNECTED
  case MESSAGE
  case UNDEFINED
  case ERROR
}

protocol SocketMessageBuilder {
  var messageType: SocketMessageType { get }
  func build(with payload: [String: String]) -> String
}

extension SocketMessageBuilder {
  func build(with payload: [String: String]) -> String {
    let nextLineString = "\n"
    let endOfString = "\0"
    var message = ""
    message.append(messageType.rawValue)
    message.append(nextLineString)
    payload.forEach{message.append("\($0):\($1)\(nextLineString)")}
    message.append(nextLineString)
    message.append(endOfString)
    return message
  }
}

struct ConnectMessageBuilder: SocketMessageBuilder {
  var messageType: SocketMessageType {
    return .CONNECT
  }
}

struct SubscribeMessageBuilder: SocketMessageBuilder {
  var messageType: SocketMessageType {
    return .SUBSCRIBE
  }
}

struct UnsubscribeMessageBuilder: SocketMessageBuilder {
  var messageType: SocketMessageType {
    return .UNSUBSCRIBE
  }
}
