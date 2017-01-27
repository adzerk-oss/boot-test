# boot-test [![Build Status][badge]][build]

[Boot] task for running clojure.test tests

[](dependency)
```clojure
[adzerk/boot-test "1.2.0"] ;; latest release
```
[](/dependency)

## Usage

Add `boot-test` to your `build.boot` dependencies and `require` the
namespace:

```clj
(set-env! :dependencies '[[adzerk/boot-test "X.Y.Z" :scope "test"]])
(require '[adzerk.boot-test :refer :all])
```

If your tests are in a directory that is not included in the classpath, you will need to add it

```
(set-env! :source-paths #{"test"})
```

You can see the options available on the command line:

```bash
$ boot test -h
```

or in the REPL:

```clj
boot.user=> (doc test)
```

## Continuous Testing

Whisper some magic incantations to boot, and it will run tests every time you save a file
```
boot watch test
```
with sound!
```
boot watch speak test
```

## License

Copyright © 2014 Adzerk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: http://clojars.org/adzerk/boot-test/latest-version.svg?cache=5
[2]: http://clojars.org/adzerk/boot-test
[Boot]: https://github.com/boot-clj/boot
[badge]: https://travis-ci.org/adzerk-oss/boot-test.png?branch=master
[build]: https://travis-ci.org/adzerk-oss/boot-test
