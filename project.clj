(defproject com.ozjongwon/pgxjdbc "0.1.0-SNAPSHOT"
  :author "Jong-won Choi"
  :description "Clojure JDBC type conversion extension for PostgreSQL"
  :url "https://github.com/ozjongwon/pgxjdbc/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "Same as Clojure"}
  :min-lein-version "2.3.3"
  :global-vars {*warn-on-reflection* true
                *assert* true}
  :dependencies [[org.clojure/clojure         "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [cheshire "5.3.1"]]
  :profiles
  {:dev {:plugins [[lein-ancient "0.5.5"]
                   [lein-pprint    "1.1.1"]
                   [lein-swank     "1.4.5"]]}}
  :jvm-opts ["-Xmx512m"
             "-XX:MaxPermSize=256m"
             "-XX:+UseParNewGC"
             "-XX:+UseConcMarkSweepGC"
             "-Dfile.encoding=UTF-8"
             "-Dsun.jnu.encoding=UTF-8"
             "-Dsun.io.useCanonCaches=false"]
  :encoding "utf-8"
  )