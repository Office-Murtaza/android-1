import Foundation
import Swinject
import RxFlow
import RxSwift
import RxCocoa

enum BaseSteps: Step, Equatable {
  case empty
  case completed
}

class BaseFlow<T: Presentable, C: FlowController>: DIProvider, Flow {
  
  /// Overrided visible property to skip pausing flow when its presentable is hidden
  var rxVisible: Observable<Bool> {
    return BehaviorSubject(value: true)
  }
  
  internal lazy var controller: C = {
    let controller = resolver.resolve(C.self)!
    (controller as? FlowActivator)?.activate()
    return controller
  }()
  
  var view: T
  
  weak var parent: DIProvider?
  
  lazy var assembler: Assembler = {
    return Assembler(assemblies(), parent: parent?.assembler)
  }()
  
  var stepper: Stepper {
    return controller
  }
  
  required init(view: T, parent: DIProvider? = nil) {
    self.view = view
    self.parent = parent
  }
  
  static func createModally<ParentT, ParentU>(over flow: BaseFlow<ParentT, ParentU>,
                                              transitionDelegate: UIViewControllerTransitioningDelegate? = nil,
                                              animated: Bool = true) -> Self
    where ParentT: UIViewController, ParentU: FlowController, T: UIViewController {
      
      let view = T()
      
      transitionDelegate.flatMap {
        view.modalPresentationStyle = .custom
        view.transitioningDelegate = $0
      }
      
      defer {
        flow.view.present(view, animated: animated, completion: nil)
      }
      
      return self.init(view: view, parent: flow)
  }
  
  var root: Presentable { return view }
  
  final func navigate(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleBaseFlow(step:))
      .extract() ?? route(to: step)
  }
  
  func route(to step: Step) -> NextFlowItems {
    return .none
  }
  
  func onComplete() {}
  
  private func handleBaseFlow(step: BaseSteps) -> NextFlowItems {
    switch step {
    case .completed:
      onComplete()
      return .end(withStepForParentFlow: BaseSteps.empty)
    case .empty:
      return .none
    }
  }
  
  // MARK: Activations
  
  func run(_ action: (C) -> Void) {
    action(controller)
  }
  
  func runAsync(_ action: @escaping (C) -> Void) {
    DispatchQueue.main.async { action(self.controller) }
  }
  
  func runOnFirstAppear(_ action: @escaping (C) -> Void) {
    rxFirstTimeVisible
      .subscribe { [controller] _ in action(controller) }
      .disposed(by: disposeBag)
  }
  
  // MARK: DI
  
  func assemblies() -> [Assembly] {
    return []
  }
  
  // MARK: NextFlowItems
  
  func next<T, C>(flow: BaseFlow<T, C>, step: Step) -> NextFlowItems {
    _ = flow.navigate(to: step)
    
    return NextFlowItems.one(flowItem: NextFlowItem(nextPresentable: flow, nextStepper: flow.controller))
  }
  
  func next<T, C>(flow: BaseFlow<T, C>) -> NextFlowItems {
    return NextFlowItems.one(flowItem: NextFlowItem(nextPresentable: flow, nextStepper: flow.controller))
  }
  
  func next<T1, C1, T2, C2>(flow1: BaseFlow<T1, C1>, flow2: BaseFlow<T2, C2>) -> NextFlowItems {
    return NextFlowItems.multiple(flowItems: [NextFlowItem(nextPresentable: flow1, nextStepper: flow1.controller),
                                              NextFlowItem(nextPresentable: flow2, nextStepper: flow2.controller)])
  }
  
  func next(flows: [Flow], steppers: [Stepper]) -> NextFlowItems {
    assert(flows.count == steppers.count, "Count of `flows` should match to the count of `steppers`")
    
    let nextItems: [NextFlowItem] = flows
      .enumerated()
      .reduce([]) { accum, next in
        var result = accum
        result.append(NextFlowItem(nextPresentable: next.element,
                                   nextStepper: steppers[next.offset]))
        return result
    }
    return NextFlowItems.multiple(flowItems: nextItems)
  }
}

extension BaseFlow where T == UIWindow {
  func replaceRoot<I>(with module: Module<I>, animated: Bool = false) -> NextFlowItems {
    view.setRootViewController(module.controller, animated: animated)
    return .none
  }
  
  func replaceRoot<P: UIViewController, C>(with flow: BaseFlow<P, C>, animated: Bool = false) -> NextFlowItems {
    Flows.whenReady(flow1: flow) { [weak self] (root: P) in
      self?.view.setRootViewController(root, animated: animated)
    }
    return next(flow: flow)
  }
}

extension BaseFlow where T: UINavigationController {
  func push(_ screen: UIViewController, animated: Bool = true) -> NextFlowItems {
    view.pushViewController(screen, animated: animated)
    return .none
  }
  
  func pop(animated: Bool = true) -> NextFlowItems {
    view.popViewController(animated: animated)
    return .none
  }
  
  func popToRoot(animated: Bool = true) -> NextFlowItems {
    view.popToRootViewController(animated: animated)
    return .none
  }
  
  func present(_ screen: UIViewController, animated: Bool = true) -> NextFlowItems {
    view.present(screen, animated: animated, completion: nil)
    return .none
  }
  
  func dismiss(animated: Bool = true, completion: (() -> Void)? = nil) -> NextFlowItems {
    view.dismiss(animated: animated, completion: completion)
    return .none
  }
  
  func replaceRoot(_ screen: UIViewController, animated: Bool = true) -> NextFlowItems {
    view.setViewControllers([screen], animated: animated)
    return .none
  }
}

extension BaseFlow where T: UITabBarController {
  func setTabs<P1, C1, P2, C2>(with flow1: BaseFlow<P1, C1>, flow2: BaseFlow<P2, C2>) -> NextFlowItems {
    Flows.whenReady(flow1: flow1,
                    flow2: flow2) { [weak self] (view1: UIViewController, view2: UIViewController) in
                      self?.view.viewControllers = [view1, view2]
    }
    return next(flow1: flow1, flow2: flow2)
  }
  
  func setTabs(with flows: [Flow], steppers: [Stepper]) -> NextFlowItems {
    assert(flows.count == steppers.count, "Count of `flows` should match to the count of `steppers`")
    Flows.whenReady(flows: flows) { [weak self] (controllers: [UIViewController]) in
      self?.view.viewControllers = controllers
    }
    return next(flows: flows, steppers: steppers)
  }
}

extension UIWindow {
  func setRootViewController(_ viewController: UIViewController, animated: Bool) {
    if animated {
      var options = UIWindow.TransitionOptions(direction: .toRight, style: .easeInOut)
      options.background = .snapshot
      options.duration = 0.3
      setRootViewController(viewController, options: options)
    } else {
      rootViewController = viewController
    }
  }
}
