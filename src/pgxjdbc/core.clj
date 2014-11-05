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
            [java.sql PreparedStatement]))
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
