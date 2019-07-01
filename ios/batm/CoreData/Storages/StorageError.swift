import Foundation

enum StorageError: Error, Equatable {
  case internalError(String)
  case notFound
  case notValid
}
