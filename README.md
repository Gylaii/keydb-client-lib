# KeyDBClient library

A library to simplify interaction with KeyDB with proper implementation using `Lettuce reactive API`

> [!IMPORTANT]
> GO INTO RELEASES AND READ


## TL;DR

> [!NOTE]
> You don't need to clone or anything.   
> Simplest setup possible is just adding this to `build.gradle.kts`:

```gradle
dependencies{
    // add this dependency:
    implementation("com.github.Gylaii:keydb-client-lib:TAG")
}

repositories {
    mavenCentral()
    // add this repo:
    maven { url = uri("https://jitpack.io") }
}
```
