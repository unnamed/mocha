# MoLang Engine
A MoLang scripting engine for Java 8+<br>
See the MoLang specification [here](https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang)

## Install
Add the repository and to your pom.xml or build.gradle(.kts):
**Maven:**
```xml
<repository>
    <id>unnamed-public</id>
    <url>https://repo.unnamed.team/repository/unnamed-public/</url>
</repository>
```
```xml
<dependency>
    <groupId>team.unnamed</groupId>
    <artifactId>molang</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle:** (Groovy DSL)
```groovy
repository { url 'https://repo.unnamed.team/repository/unnamed-public/' }
```
```groovy
implementation 'team.unnamed:molang:0.1.0'
```
**Gradle:** (Kotlin DSL)
```kotlin
maven("https://repo.unnamed.team/repository/unnamed-public")
```
```kotlin
implementation("team.unnamed:molang:0.1.0")
```

## Usage
MoLang is pretty easy to use, you can eval strings or `java.io.Reader`

### Basic Usage:
```java
import team.unnamed.molang.MoLangEngine;

class MyProgram {

    public void run() throws ScriptException {
        MoLangEngine engine = MoLangEngine.createDefault();
        
        System.out.println(engine.eval("math.cos(90) * 16"));
    }

}
```