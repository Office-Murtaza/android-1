import ObjectMapper

extension CheckAccountResponse: ImmutableMappable {
  init(map: Map) throws {
    phoneExist = try map.value("phoneExist")
    passwordMatch = try map.value("passwordMatch")
  }
}
