# surefire-scalatest

A [Maven Surefire Provider](https://maven.apache.org/surefire/maven-surefire-plugin/api.html) for [ScalaTest](https://www.scalatest.org).

## Usage

Just add a dependency to your Surefire plugin configuration and set the `<includes>` according to your test class names:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <includes>
            <include>**/*Test.*</include>
            <include>**/*Spec.*</include>
        </includes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.github.janhicken</groupId>
            <artifactId>surefire-scalatest</artifactId>
            <version>0.1</version>
        </dependency>
    </dependencies>
</plugin>
```

In the `test` phase, your ScalaTest specs will be executed using Surefire now.

## Configuration

As already displayed above, all discovery parameters, such as `<includes>` and `<excludes>` are working.

Furthermore, you may configure a parallel execution of test suites by setting `parallel=suites` and configuring a
[thread count](https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#threadCount).

Apart from this, no further configuration has been tested. Your desired configuration parameter may just work
out-of-the-box.

