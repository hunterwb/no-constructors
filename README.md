# no-constructors

[![Build Status](https://img.shields.io/circleci/project/github/hunterwb/no-constructors.svg)](https://circleci.com/gh/hunterwb/no-constructors)

If a class contains no constructors, a [default constructor](https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.8.9) is implicitly added

```java
public class Example {}
```

```console
$ javap Example.class

public class Example {
  public Example();
}
```

The `@NoConstructors` annotation and corresponding build plugin prevent the default constructor from being added

```java
@NoConstructors
public class Example {}
```

```console
$ javap Example.class

public final class Example {}
```