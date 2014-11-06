# pgxjdbc, a Clojure JDBC extension for PostgreSQL (JSON)

After playing with DynamoDB for a while, I learnt limitations of NoSQL and returned back to trusty PostgreSQL.
I couldn't be satisfied with any of existing Clojure SQL libraries and decided to use the plain JDBC library.

Being a JDBC library, org.clojure/java.jdbc does not support PostgreSQL's extensions like JSON, but its design is easy to make changes by redefining three protocols.

## Getting started

### Dependencies

Add the necessary dependency to your [Leiningen][] `project.clj` and `require` the library in your ns:

```clojure
[com.ozjongwon/pgxjdbc "0.1.0-SNAPSHOT"]              ; project.clj

(ns my-app (:require [ozjongwon.pgxjdbc :as jdbc]))
```

## Usage
For INSERT and UPDATE, "::json" is required:

```clojure
(jdbc/with-db-connection
   (jdbc/with-schema ["test1"]
      (jdbc/with-transaction
	 (jdbc/execute! ["insert into site (timezone, locale, name, data) values (?,?,?,?::json)" :aus/nsw :en_AU "new name12" false]))))

(jdbc/with-db-connection
   (jdbc/with-schema ["test1"]
      (jdbc/query ["select * from site where timezone = ?" :aus/nsw])))
```

## License

Copyright &copy; 2014 Jong-won Choi. Distributed under the [Eclipse Public License][], the same as Clojure.



[Eclipse Public License]: <https://raw2.github.com/ozjongwon/dynohub/master/LICENSE>