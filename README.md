[![Release](https://jitpack.io/v/masterwok/tpb-search-android.svg)](https://jitpack.io/#masterwok/tpb-search-android)

# tpb-search-android
An Android library for querying magnets from [thepiratebay.org](https://thepiratebay.org). The goal of this project is to provide a simple interface for querying thepiratebay.org via the site itself or through [various other proxies](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/constants/Hosts.kt). 

When a query is started, the library attempts to query against all defined hosts simultanteously until the ```maxSuccessfulHosts``` count is achieved. When this happens, all pending queries are cancelled. Should the ```maxSuccessfulHosts``` count never be hit after the defined ```queryTimeout``` expires, then all pending queries are cancelled. An attempt to query a single host is aborted after the defined ```requestTimeout```.

Once all of the responses are received they are flattened down into a single [QueryResult](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/models/QueryResult.kt) containing [TorrentResult](https://github.com/masterwok/tpb-search-android/blob/master/tpbsearchandroid/src/main/java/com/masterwok/tpbsearchandroid/models/TorrentResult.kt) items and paging state.

For a detailed example of how to use the library with a RecyclerView and the [Android JetPack Paging](https://developer.android.com/topic/libraries/architecture/paging/) library that handles errors, paging, queries, and retries please see the demo application alongside this library.

## Usage

Simply invoke ```QueryService.query(..)``` to query for magnets. For example, to query for *The Hobbit from 1977* the first page of results with a query timeout of 10,000 milliseconds, a timeout per site of 5,000 milliseconds, and a maximum successful response count of 5, one would do the following:

```kotlin
val queryService: QuerySerivce = QueryService(
    queryFactories = QueryFactories
    , verboseLogging = true
)

...

launch() {
    val queryResult = queryService.query(
            query = "The Hobbit 1977"
            , pageIndex = 0
            , queryTimeout = 10000L
            , requestTimeout = 5000
            , maxSuccessfulHosts = 5
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
    implementation 'com.github.masterwok:tpb-search-android:0.0.1'
}
```

## Demo Screenshot

<img src="/app/screenshots/search_screenshot.jpg?raw=true" height="600" title="Demo Search">
