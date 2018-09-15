[![Release](https://jitpack.io/v/masterwok/tpb-search-android.svg)](https://jitpack.io/#masterwok/tpb-search-android)

# tpb-search-android
An Android library for querying magnets from [thepiratebay.org](https://thepiratebay.org).

When a query is started, the library attempts to query against all defined [hosts](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/constants/Hosts.kt) simultaneously until a successful [QueryResult](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/models/QueryResult.kt) containing [TorrentResult](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/models/TorrentResult.kt) instances is returned from an endpoint. When this happens, all pending queries are cancelled. A request to an endpoint will timeout after the defined, ```requestTimeout```. The query as a whole will timeout after the defined ```queryTimeout```.

Please see the companion demo application of this library for a detailed example of how to use this library alongside the [Android JetPack Paging](https://developer.android.com/topic/libraries/architecture/paging/) library.


## Usage

Simply invoke ```QueryService.query(..)``` to query for magnets. For example, to query for *The Hobbit from 1977* the first page of results with a query timeout of 10,000 milliseconds, a timeout per site of 5,000 milliseconds, and a maximum successful response count of 5, one would do the following:

```kotlin
val queryService: QuerySerivce = QueryService(
    queryFactories = QueryFactories
)

...

launch() {
    val queryResult = queryService.query(
            query = "The Hobbit 1977"
            , pageIndex = 0
            , queryTimeout = 10000L
            , requestTimeout = 5000
    ): QueryResult<TorrentResult>
}
```

## Configuration

Add this in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and add the following in the dependent module:

```gradle
dependencies {
    implementation 'com.github.masterwok:tpb-search-android:0.0.2'
}
```

## Demo Screenshot

<img src="/app/screenshots/search_screenshot.jpg?raw=true" height="600" title="Demo Search">
