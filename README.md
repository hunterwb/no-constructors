# no-constructors

[![Build Status](https://img.shields.io/circleci/project/github/hunterwb/no-constructors.svg)](https://circleci.com/gh/hunterwb/no-constructors)

```java
public class Example {}
```


```console
$ javap Example.class

public class Example {
  public Example();
}
```

```java
@NoConstructors
public class Example {}
```

```console
$ javap Example.class

public final class Example {}
```