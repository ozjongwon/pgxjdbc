;;;;   -*- Mode: clojure; encoding: utf-8; -*-
;;
;; Copyright (C) 2014 Jong-won Choi
;;
;; Distributed under the Eclipse Public License, the same as Clojure.
;;
;;;; Commentary:
;;
;;
;;
;;;; Code:

(ns ozjongwon.pgxjdbc
  (:require [clojure.java.jdbc :as jdbc]
            [cheshire.core :as json])
  (:import  [org.postgresql.util PGobject]
            [java.sql PreparedStatement]
            [com.mchange.v2.c3p0 ComboPooledDataSource]))
;;;
;;; Extending JDBC protocols to support JSON
;;;
(extend-protocol jdbc/ISQLValue
  ;; Map Clojure keyword :keyword to string ":keyword"
  clojure.lang.Keyword
  (jdbc/sql-value [value]
    (str value)))

(defn- %maybe-convert-to-json [v ^PreparedStatement s ^long i]
  (.setObject s i (jdbc/sql-value (if (-> (.getParameterMetaData s)
                                     (.getParameterTypeName i)
                                     (= "json"))
                               (json/generate-string v)
                               v))))

;;
;; Map Clojure object -> PostgreSQL JSON objects
;;
(extend-protocol jdbc/ISQLParameter
  Object
  (jdbc/set-parameter [v ^PreparedStatement s ^long i]
    (%maybe-convert-to-json v s i))

  nil
  (jdbc/set-parameter [v ^PreparedStatement s ^long i]
    (%maybe-convert-to-json v s i)))

;;
;; Map PostgreSQL object -> JSON or keyword
;;
(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (jdbc/result-set-read-column [x _2 _3]
    (case (.getType x)
      "json" (json/decode (.getValue x))
      ;; default
      x))

  String ;; keyword conversion
  (result-set-read-column [x _2 _3]
    (if (= (get x 0) \:)
      (or (find-keyword (subs x 1)) x)
      x)))

;;
;; Connection Pooling from korma
;;

(defn- connection-pool
  "Create a connection pool for the given database spec."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size
           test-connection-query
           idle-connection-test-period
           test-connection-on-checkin
           test-connection-on-checkout]
    :or {excess-timeout (* 30 60)
         idle-timeout (* 3 60 60)
         minimum-pool-size 3
         maximum-pool-size 15
         test-connection-query nil
         idle-connection-test-period 0
         test-connection-on-checkin false
         test-connection-on-checkout false}
    :as spec}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                 (.setUser user)
                 (.setPassword password)
                 (.setMaxIdleTimeExcessConnections excess-timeout)
                 (.setMaxIdleTime idle-timeout)
                 (.setMinPoolSize minimum-pool-size)
                 (.setMaxPoolSize maximum-pool-size)
                 (.setIdleConnectionTestPeriod idle-connection-test-period)
                 (.setTestConnectionOnCheckin test-connection-on-checkin)
                 (.setTestConnectionOnCheckout test-connection-on-checkout)
                 (.setPreferredTestQuery test-connection-query))})

(defn- delay-pool
  "Return a delay for creating a connection pool for the given spec."
  [spec]
  (delay (connection-pool spec)))

(defonce _default-db (atom nil))

;; External APIs
(defn get-db-conn [] @@_default-db)

(defn connect-db [db-spec]
  (reset! _default-db (delay-pool db-spec)))

;;;
;;; JDBC Wrapper functions/macros
;;;
(def ^:dynamic *db-conn*)

(defmacro make-dml-fn [fn-name]
  `(defn ~fn-name [& args#]
     (apply ~(symbol (str "clojure.java.jdbc/" fn-name))
            *db-conn*
            args#)))

(defmacro make-dml-fns [fn-names]
  `(do ~@(map (fn [fn-name]
                `(make-dml-fn ~fn-name))
              fn-names)))

(make-dml-fns [insert! query update! delete! execute! db-do-commands])

(defn get-search-path []
  (-> (query ["show search_path"])
      first
      :search_path))

(defn set-search-path [path]
  (db-do-commands false (str "SET search_path to " path)))

(defmacro with-db-connection [& body]
  `(jdbc/with-db-connection [con# (get-db-conn)]
     (binding [*db-conn* con#]
       ~@body)))

(defmacro with-schema [[schema & {:keys [strict] :or {strict false}}] & body]
  `(let [original-schema# (get-search-path)]
     (util/unwind-protect (do (set-search-path (if ~strict ~schema (str ~schema ",public")))
                              ~@body)
                          (set-search-path original-schema#))))

(def ^:dynamic *inside-transaction?* false)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [db-conn# *db-conn*]
     (binding [*inside-transaction?* true
               *db-conn* db-conn#]
       ~@body)))

(defn assert-transaction []
  (assert *inside-transaction?* "Not in a transaction!"))

;;;
;;; Example connection spec for PostgreSQL
;;;

;; (def db-spec {:classname "org.postgresql.Driver"
;;               :subprotocol "postgresql"
;;               :subname "//localhost:5432/mydb"
;;               :user "myuser"
;;               ;; Password must be provided for PostgreSQL JDBC (UNIX socket not work for JDBC)
;;               :password "mypassword"})