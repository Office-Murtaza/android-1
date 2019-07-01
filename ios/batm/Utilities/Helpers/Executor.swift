import Foundation

typealias Task = () -> Void
typealias Executor = (@escaping Task) -> Void

func asyncExecutor(queue: DispatchQueue) -> Executor {
  return { task in
    queue.async(execute: task)
  }
}

func syncExecutor(queue: DispatchQueue) -> Executor {
  let specificKey = DispatchSpecificKey<Void>()
  queue.setSpecific(key: specificKey, value: ())
  
  return { task in
    if queue.getSpecific(key: specificKey) != nil {
      task()
    } else {
      queue.sync(execute: task)
    }
  }
}

func immediateExecutor() -> Executor {
  return { $0() }
}
