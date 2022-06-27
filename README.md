# Belco project

## Technology stack and architecture

The project is written fully on Kotlin. Kotlin coroutines library is used as a multithreading library.
All the code is located in a single module [app](app/src/main). All the code consists of three main packages:
* [data](app/src/main/java/com/belcobtm/data). The package represents data layer from the
[Robert Marting's Clean architecture approach](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) 
Overall structure of the `data` package is following:
    * [cloud](app/src/main/java/com/belcobtm/data/cloud). Contains logic related to Firebase authorization and Cloud storage.
  Firebase Authorization is required for Cloud Storage and Cloud Storage is used as a kind of CDN
  (upload images and send file names in the request to the REST server)
    * [core](app/src/main/java/com/belcobtm/data/cloud). The most important classes here is
  [TransactionHelper](app/src/main/java/com/belcobtm/data/core/TransactionHelper.kt).
  It contains logic to sign all the transactions (withdraw, swap, transfer etc.).
  The class encapsulates the sign logic for all the coins in the app.
  Each coin has its own helper located in [helpers package](app/src/main/java/com/belcobtm/data/core/helper).
  Each coin helper uses factory to build the transaction input to sign. All the factories located in
  [factory module](app/src/main/java/com/belcobtm/data/core/factory).
  [trx](app/src/main/java/com/belcobtm/data/core/trx) package contains additional helper classes for TRX coin
    * [disk](app/src/main/java/com/belcobtm/data/disk). Contains logic to work with database and Shared Preferences.
  All the work with database is done by using Room library.
  All secured data is stored in the Shared Preferences that is accessible by
  [helper](app/src/main/java/com/belcobtm/data/disk/shared/preferences/SharedPreferencesHelper.kt).
  The helper wraps encrypted shared preferences from Google's library.
  There are three main parts that is stored in DB:
      * [Account information](app/src/main/java/com/belcobtm/data/disk/database/account). 
      Contains info about user's private/public key and information about what coins are actually enabled in the app
      * [Service information](app/src/main/java/com/belcobtm/data/disk/database/service).
      Contains info about available services for the user (swap, sell, trade etc.) and their fees.
      * [Wallet balance](app/src/main/java/com/belcobtm/data/disk/database/wallet).
      Contains info about current balance. The balance is stored in the database to show some data during the loading
      to the user while the actual response is still not received
    * [inmemory](app/src/main/java/com/belcobtm/data/inmemory). Another type of the cache in the app.
  It is mostly used for the data that changes quite fast and can be used on multiple screens or
  can be updated by multiple channels (rest, websockets). There is no special abstractions
  for that in general they are just wrappers around MutableStateFlow.
    * [rest](app/src/main/java/com/belcobtm/data/rest). Contains all the REST services logic.
  Retrofit is used to describe REST services. There are a bunch of custom
  [interceptors](app/src/main/java/com/belcobtm/data/rest/interceptor) to handle basic errors and 
  [Authenticator](app/src/main/java/com/belcobtm/data/rest/interceptor/TokenAuthenticator.kt) to handle authorization.
  All the services are split by features in the package. The feature package contains Retrofit interface definition,
  request/response models and ApiService that wraps Retrofit interface and provides mapping from exception to
  [Either](app/src/main/java/com/belcobtm/domain/Either.kt) that holds error or value.
    * [websockets](app/src/main/java/com/belcobtm/data/websockets). Contains the logic to handle websocket connection.
  There is only one websocket connection that handles multiple subscriptions.
  [Base](app/src/main/java/com/belcobtm/data/websockets/base) package contains logic to serialize/deserialize websocket request/response.
  Also, it contains [main implementation](app/src/main/java/com/belcobtm/data/websockets/base/OkHttpSocketClient.kt) for the 
  [SocketClient](app/src/main/java/com/belcobtm/data/websockets/base/SocketClient.kt) that uses OkHttp library to handle websocket connections.
  [manager](app/src/main/java/com/belcobtm/data/websockets/manager) package contains logic to manage multiple subscriptions for the websockets.
  Subscription is a feature based endpoint for the data (wallet balance, transactions, trades, available services).
  Each subscription has unique path that is used as an identifier in the [manager](app/src/main/java/com/belcobtm/data/websockets/manager/SocketManager.kt).
  When the subscription is not needed any more unsubscribe method is used to clean up resources.
  [The implementation of subscribe logic](app/src/main/java/com/belcobtm/data/websockets/trade/WebSocketTradesObserver.kt)
  contains following steps:
      1. Listen for open connection status of the websocket
      2. Map open connection state to the subscription request
      3. Subscribe to the endpoint
      4. Collect and process the responses
* [domain](app/src/main/java/com/belcobtm/domain). Domain package is pretty straight forward.
It is split by features and contains [UseCases](app/src/main/java/com/belcobtm/domain/UseCase.kt) 
to get the data from the repository. All the thread switch logic is done inside the base UseCase.
Each UseCase returns [Either](app/src/main/java/com/belcobtm/domain/Either.kt).
* [presentation](app/src/main/java/com/belcobtm/presentation). MVVM is used on the presentation layer.
ViewModel and LiveData from the Jetpack libraries are main components of the presentation layer.
ViewBinding is used to bind xml layout in the Fragments.
ViewModel gets all the data by calling UseCases. The package consists of:
  * [core](app/src/main/java/com/belcobtm/presentation/core). The most important class is
  [CoinInputLayout](app/src/main/java/com/belcobtm/presentation/core/views/CoinInputLayout.kt).
  It represents the view that is used to enter coin amount. It contains a lot of helper labels, coin dropdown,
  error messages and max amount button. Another important class is 
  [LoadingData](app/src/main/java/com/belcobtm/presentation/core/mvvm/LoadingData.kt).
  It can be [listened](app/src/main/java/com/belcobtm/presentation/core/ui/fragment/BaseFragment.kt) (listen method).
  Also, BaseFragment contains logic to show hide progress/errors/snackbars and setup actionbar.
  The [BaseBottomSheetFragment](app/src/main/java/com/belcobtm/presentation/core/ui/fragment/BaseBottomSheetFragment.kt)
  behaves quite similar to the BaseFragment.
  * [features](app/src/main/java/com/belcobtm/presentation/features). Contains actual feature screens. [HostActivity](app/src/main/java/com/belcobtm/presentation/features/HostActivity.kt)
  is main activity of the app. Jetpack Navigation is used to perform navigation. There are two main navigation graphs.
  [nav_app.xml](app/src/main/res/navigation/nav_app.xml) represents main navigation graphs of the app (starting from the authorization).
  [nav_main.xml](app/src/main/res/navigation/nav_main.xml) represents main authorized navigation graph with four sub 
  graphs that represent tabs of the BottomNavigationView. [MainFragment](app/src/main/java/com/belcobtm/presentation/features/MainFragment.kt)
  is fragment with BottomNavigationView.

## Gradle and Dependencies
The most important library is [Trust Wallet Core](https://github.com/trustwallet/wallet-core).
The library is distributed by Github packages, so there is a special repository definition in the root
[build.gradle](build.gradle) file for that. All the dependencies are stored [here](gradle/dependencies.gradle)

The project has two flavors:
* `dev`. Regular environment for development and testing. 
* `prod`. Production environments. All the requests will be executed on the real data. It shouldn't be used during development. 
Only in case prod specific issue that is no reproducible on the dev environment.

All possible build variants:
* `devDebug` - build variant for quick development.
* `devRelease` - minified and obfuscated version of the `devDebug` build. It is used for Firebase distribution for testing.
* `prodDebug` - debug production build. It should be used only for critical debugging issues on production
* `prodRelease` - build variant that is used to distribute in the Play Market

There is a Github Action to trigger upload build to Firebase Distribution on each merge to the dev branch.
The release build is assembled by `bundleProdRelease` task. All the signing information contains in the [repo](signing) 