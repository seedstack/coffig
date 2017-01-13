# SeedStack config library "Coffig"

[![Build status](https://travis-ci.org/seedstack/coffig.svg?branch=master)](https://travis-ci.org/seedstack/coffig) [![Coverage Status](https://coveralls.io/repos/seedstack/coffig/badge.svg?branch=master)](https://coveralls.io/r/seedstack/coffig?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seedstack.coffig/coffig/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.seedstack.coffig/coffig)

Coffig is a simple and powerful configuration library for the Java language. It is built upon the idea of mapping configuration trees to  Java objects. The following configuration tree in YAML:

```yaml
some:
  string: value1
  array: [1, 2, 3]
  object:
    attr1: value1
    attr2: [iris, jasmine, kiwi]
```    
        
Can be retrieved as simply as:        

```java
Coffig coffig = Coffig.builder().withProviders(new JacksonProvider().addSource("url/to/file.yaml")).build();
    
String stringValue = coffig.get(String.class, "some.string");
int[] intArray = coffig.get(int[].class, "some.array");
MyPojo myPojo = coffig.get(MyPojo.class, "some.object");
String defaultValue = coffig.getOptional(MyPojo.class, "unknown.node").orElse("default");
```

## Mapping    
    
Coffig is able to map any Java class by introspecting its members but can handle special cases with dedicated mappers. Mappers for several types are built-in: 

* Array, 
* List, 
* Set,
* Map, 
* Enum, 
* Optional, 
* URI/URL,
* Properties, 
* Class.

You can add you own mappers by implementing the `ConfigurationMapper` interface and registering it through the [ServiceLoader](http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism.

## Providers

Multiple providers can be used at once, each providing a separate configuration tree that will be merged into a global one. Several providers are built-in: 

* Jackson (for YAML and JSON sources), 
* Environment variables, 
* System properties.

You can add you own mappers by implementing the `ConfigurationProvider` interface and registering it through the [ServiceLoader](http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism.

# Copyright and license

This source code is copyrighted by [The SeedStack Authors](https://github.com/seedstack/seedstack/blob/master/AUTHORS) and
released under the terms of the [Mozilla Public License 2.0](https://www.mozilla.org/MPL/2.0/). 
