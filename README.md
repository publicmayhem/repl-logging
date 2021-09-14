# repl-logging

Add version to dev/repl profile in deps

_egiraffe/repl-logging {mvn/version "1.0.0"}_

Then to set log level for given namespace: foo.bar
```clojure
(set-logger-level :debug "foo.bar")
```
or for current namespace
```clojure
(set-logger-level :debug)
```