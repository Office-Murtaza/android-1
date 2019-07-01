import Foundation
import Swinject
import CoreData

enum ManagedContext: String {
  case mainQueue
  case backgroundQueue
}

class PersistentStackAssembly: Assembly {
  func assemble(container: Container) {
    container.register(NSPersistentContainer.self) { _ in
      let model = NSManagedObjectModel.mergedModel(from: [Bundle.main])!
      let persistentContainer = NSPersistentContainer(name: "", managedObjectModel: model)
      persistentContainer.loadPersistentStores()
      return persistentContainer
      }.inObjectScope(.container)
    
    container.register(StorageTransactionExecutor.self) { ioc in
      let context = ioc.resolve(NSManagedObjectContext.self,
                                name: ManagedContext.backgroundQueue.rawValue)!
      return StorageTransactionExecutorImpl(context: context)
      }.inObjectScope(.container)
    
    container.register(NSManagedObjectContext.self, name: ManagedContext.mainQueue.rawValue) { ioc in
      let service = ioc.resolve(NSPersistentContainer.self)!
      let context = service.viewContext
      context.automaticallyMergesChangesFromParent = true
      return context
      }.inObjectScope(.container)
    
    container.register(NSManagedObjectContext.self, name: ManagedContext.backgroundQueue.rawValue) { ioc in
      let service = ioc.resolve(NSPersistentContainer.self)!
      return service.newBackgroundContext()
      }.inObjectScope(.transient)
  }
}
