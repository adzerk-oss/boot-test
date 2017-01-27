# Changelog

### 1.2.0

* Added `-c` / `--clojure` option that accepts a string for the Clojure version to use for the tests pod. [#30][30]
* Added `-S` / `--startup` option that accepts a set of function symbols to run (zero arguments) prior to running the tests. [#30][30]
* Added `-s` / `--shutdown` option that accepts a set of function symbols to run (zero arguments) after running the tests. [#30][30]
* Added `-I` / `--include` option to specify a regex for including namespaces, and `-X` / `--exclude` option to specify a regex for excluding namespaces. [#30][30]

### 1.1.1

* add stats to fileset metadata even if no tests have been on classpath [#23][23]

[23]: https://github.com/adzerk-oss/boot-test/pull/23
[30]: https://github.com/adzerk-oss/boot-test/pull/30
