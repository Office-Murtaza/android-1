import Foundation
import PathKit
import StencilSwiftKit

func fromSnakeToCammelCase(str: String) -> String {
  return str.split(separator: "_").map({
    return $0.prefix(1).uppercased() + $0.dropFirst()
  }).joined(separator: "")
}

func prepareData() throws -> [String: Any] {
  let jsonInput = readLine()!
  let jsonData = jsonInput.data(using: .utf8)!
  let json = try JSONSerialization.jsonObject(with: jsonData) as! Dictionary<String, String>
  
  let regex = try NSRegularExpression(pattern: "^[a-z][a-z0-9._]*\\.[a-z0-9._]*[a-z0-9]$") // domain.sub7_domain.key
  
  //  "structs": [
  //      "StructName": ["keyName": "value"],
  //    ]
  var structs = [String: [String: String]]()
  
  //    "extensions": [
  //      "Ext.Name": [
  //        "StructName" : [
  //          "keyName": "value"
  //        ]
  //      ]
  //    ]
  var extensions = [String: [String: [String: String]]]()
  
  json.keys.forEach { key in
    let range = NSRange(key.startIndex..<key.endIndex, in: key)
    guard regex.firstMatch(in: key, range: range) != nil else {
      print("warning: invalid localization key: \(key)\nkeys should be like domain.sub7_domain.key")
      return;
    }
    
    let keyComponents = key.split(separator: ".")
    let localizationKeyCammel = fromSnakeToCammelCase(str: String(keyComponents.last!))
    let localizationKey = localizationKeyCammel.prefix(1).lowercased() + localizationKeyCammel.dropFirst()
    
    // root structs
    let rootStructKey = fromSnakeToCammelCase(str: String(keyComponents.first!))
    
    if var structVal = structs[rootStructKey] {
      if keyComponents.count == 2 {
        structVal[localizationKey] = key
        structs[rootStructKey] = structVal
      }
    } else {
      structs[rootStructKey] = keyComponents.count == 2 ? [localizationKey: key] : [:]
    }
    
    // extensions
    if keyComponents.count > 2 {
      let extComponents = keyComponents.dropLast()
      for (index, _) in extComponents.dropFirst().enumerated() {
        let extKey = extComponents[0...index]
          .map(String.init)
          .map(fromSnakeToCammelCase)
          .joined(separator: ".")
        var ext: [String: [String: String]] = extensions[extKey] ?? [:]
        
        let structKey = fromSnakeToCammelCase(str: String(extComponents[index+1]))
        var vals = ext[structKey] ?? [:]
        
        if index == extComponents.count - 2 {
          vals[localizationKey] = key
        }
        
        ext[structKey] = vals
        extensions[extKey] = ext
      }
    }
  }
  
  return [
    "structs": structs,
    "extensions": extensions,
  ]
}

do {
  let map = try prepareData()
  
  let scriptFilePath = Path(CommandLine.arguments.first!).parent()
  let scriptDirectory = Path.current + scriptFilePath
  let projectRoot = scriptDirectory.parent().parent().parent().parent()
  let templatePath = projectRoot + Path("Templates/L10n.stencil")
  
  let template = StencilSwiftTemplate(templateString: try templatePath.read())
  
  let localizationResultPath = projectRoot + Path("batm/Codegen/L10n.generated.swift")
  let result = try template.render(["root": map])
  try localizationResultPath.write(result)
  
} catch let err {
  print("error: \(err)")
}
