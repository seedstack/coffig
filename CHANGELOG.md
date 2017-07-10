# Version 2.1.2 (2017-07-??)

* [fix] Properly parse and handle null values.
* [chg] When converting an array node to a map node, items are used as keys instead of values.

# Version 2.1.1 (2017-06-07)

* [fix] Fix instantiation of arrays as default values.

# Version 2.1.0 (2017-04-28)

* [new] Mapper for durations.
* [chg] Map properties files as in-depth trees (not flat keys at the top-level).

# Version 2.0.0 (2017-02-20)

* [fix] Fix formatting of configuration YAML dump.

# Version 2.0.0 (2017-02-16)

* [brk] Replace immutable/mutable dual data-structure with a single one and an unmodifiable wrapper.
* [brk] Replace collections by streams when accessing nodes.
* [brk] API refactoring
* [chg] Add debug and trace logging for main configuration operations (create, fork, refresh, get).
* [chg] No provider is registered by default anymore.
* [chg] No processor is registered by default anymore.
* [chg] No evaluator is registered by default anymore.

# Version 1.1.0 (2017-01-12)

* [new] Allow to ignore macro and function resolution by escaping them with `\`.  
* [new] Visit superclasses when gathering field info for object mapping.
* [new] Add a mapper for configuration builders (i.e. objects that provide functions for configuration). 

# Version 1.0.0 (2016-12-12)

* [new] Initial version.
