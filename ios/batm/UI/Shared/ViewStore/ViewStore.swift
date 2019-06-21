import Foundation
import RxSwift
import RxCocoa

extension ObservableType {
  /// Apply a transformation function to the Observable.
  func apply<T>(_ transform: (Observable<Self.E>) -> Observable<T>) -> Observable<T> {
    return transform(self.asObservable())
  }
}

class ViewStore<Action, State> {
  
  // MARK: - Inputs
  
  /// The action from the view or business logic. Bind user inputs to this subject.
  final let action: PublishRelay<Action> = PublishRelay()
  
  // MARK: - Outputs
  
  /// The state stream. Use this observable to observe the state changes.
  var state: Driver<State> {
    return _state.asDriver()
  }
  
  /// The current state. This value is changed just after the state stream emits a new state.
  var currentState: State {
    return _state.value
  }
  
  // MARK: - Abstract
  
  /// The initial state.
  var initialState: State {
    fatalError("Abstract method should be overriden")
  }
  
  /// Generates a new state with the previous state and the action. It should be purely functional
  /// so it should not perform any side-effects here. This method is called every time when the
  /// action is committed.
  func reduce(state: State, action: Action) -> State {
    fatalError("Abstract method should be overriden")
  }
  
  /// Transforms the action. Use this function to combine with other observables. This method is
  /// called once before the state stream is created.
  func transform(action: Observable<Action>) -> Observable<Action> {
    return action
  }
  
  /// Transforms the state stream. Use this function to perform side-effects such as logging. This
  /// method is called once after the state stream is created.
  func transform(state: Observable<State>) -> Observable<State> {
    return state
  }
  
  // MARK: - Bindings
  
  /// Asynchronous operation that could emit actions
  typealias ActionCreator = (State) -> Observable<Action>
  
  /// Binds input signal to an ActionCreator factory
  func bind<Input>(trigger: Driver<Input>, actionCreator: @escaping (Input) -> ActionCreator) -> Disposable {
    return trigger
      .asObservable()
      .flatMapLatest { [weak self] input -> Observable<Action> in
        guard let strongSelf = self else { return .empty() }
        let factory = actionCreator(input)
        return factory(strongSelf.currentState).catchError { _ in .empty ()}
      }.bind(to: action)
  }
  
  // MARK: - Private
  
  private let disposeBag: DisposeBag = DisposeBag()
  private lazy var _state: BehaviorRelay<State> = BehaviorRelay(value: initialState)
  
  init() {
    action.asObservable()
      .apply(transform(action:))
      .scan(initialState, accumulator: { [unowned self] state, action in
        return self.reduce(state: state, action: action)
      })
      .apply(transform(state:))
      .bind(to: _state)
      .disposed(by: disposeBag)
    
    state.drive().disposed(by: disposeBag)
  }
}
