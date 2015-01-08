# clecs

Entity-component-system for Clojure.

[![Build Status](https://travis-ci.org/muhuk/clecs.svg?branch=master)](https://travis-ci.org/muhuk/clecs)


## Latest Release

### Leiningen

Add this to your `:dependencies` in `project.clj`:

[![Clojars Project](http://clojars.org/clecs/latest-version.svg)](http://clojars.org/clecs)


## Usage

To do...


## Examples

-   Official demos are here: [clecs-examples](https://github.com/muhuk/clecs-examples)


## Documentation

Documentation is a work in progress. API documentation is [here](http://clecs.muhuk.com/).


## Changelog

### Changes Since Version 1.0.x

-   Components are no longer records. The same API still works but since
    a java class is not generated anymore you are likely to get an error
    like:

            java.lang.ClassNotFoundException: java_path.to.YourComponent

    To solve this problem declare components in a `:require` instead
    of `:import`.


### Changes Since Version 0.2.x

- Replaced function based queries with data driven queries. See `clecs.query`.


## See Also

-   [Artemis](http://gamadu.com/artemis/)
-   [brute](https://github.com/markmandel/brute)
-   [entreri](https://bitbucket.org/mludwig/entreri/overview)


## License

Copyright (C) 2014  Atamert Ölçgen

This program is distributed under GNU GPL v3 license. See `LICENSE` file.
