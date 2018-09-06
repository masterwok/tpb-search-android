[![Release](https://jitpack.io/v/masterwok/tpb-search-android.svg)](https://jitpack.io/#masterwok/tpb-search-android)

# open-subtitles-android
An Android library for querying torrents from [thepiratebay.org](https://thepiratebay.org). 

## Usage

Simply invoke ```QueryService.query(..)``` to query for torrents. For example, to query for *The Hobbit from 1977* with a query timeout of 10,000 milliseconds, a timeout per site of 5,000 milliseconds, and a maximum successful response count of 5, one would do the following:

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
            , requestTimeout = 5000L
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
