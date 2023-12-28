## Installation

You can add `mocha` to your project using [Gradle](https://gradle.org/)
*(recommended)*, [Maven](https://maven.apache.org/) or manually downloading the
JAR files from [GitHub Releases](https://github.com/unnamed/mocha/releases).

Note that `mocha` is available in the Maven Central Repository.

### Gradle

```kotlin
dependencies {
    implementation("team.unnamed:mocha:%%REPLACE_latestRelease{team.unnamed:mocha}%%")
}
```

### Maven

<!--@formatter:off-->
```xml
<dependency>
    <groupId>team.unnamed</groupId>
    <artifactId>mocha</artifactId>
    <version>%%REPLACE_latestRelease{team.unnamed:mocha}%%</version>
</dependency>
```
<!--@formatter:on-->