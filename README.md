# pgxjdbc, a Clojure JDBC extension for PostgreSQL (JSON)

After playing with DynamoDB for a while, I learnt limitations of NoSQL and return back to trusty PostgreSQL.
I couldn't satisfy with any of existing Clojure SQL libraries and decided use plain JDBC library.

Being a JDBC library, org.clojure/java.jdbc does not support PostgreSQL's extensions like JSON, but its design is easy to change through redefining three protocols.

## Getting started

### Dependencies

Add the necessary dependency to your [Leiningen][] `project.clj` and `require` the library in your ns:

```clojure
[com.ozjongwon/pgxjdbc "0.1.0-SNAPSHOT"]              ; project.clj

(ns my-app (:require [ozjongwon.pgxjdbc]))
```

## Usage
For INSERT and UPDATE, "::json" is required:

```clojure
(jdbc/execute! db-spec ["insert into site (timezone, locale, name, data) values (?,?,?,?::json)" :aus/nsw :en_AU "new name" false])
```

## License

Copyright &copy; 2014 Jong-won Choi. Distributed under the [Eclipse Public License][], the same as Clojure.



[Eclipse Public License]: <https://raw2.github.com/ozjongwon/dynohub/master/LICENSE>