(ns repl.logging
  (:refer-clojure :exclude [name])
  (:require [clojure.tools.logging :as clj-logging]
            [clojure.tools.logging.impl :as clj-log-impl]))

(defprotocol LogHelper
  (name [log-helper])
  (set-level [log-helper level] [log-helper level name-space]))

(defn jul-first-console-handler [logger]
  (when logger
    (if-let [console-handler (first (filter #(instance? java.util.logging.ConsoleHandler %) (.getHandlers logger)))]
      console-handler
      (recur (.getParent logger)))))

(defn jul-log-helper []
   (when (clj-log-impl/class-found? "java.util.logging.Logger")
     (eval
       `(let [levels# {:trace java.util.logging.Level/FINEST
                       :debug java.util.logging.Level/FINE
                       :info  java.util.logging.Level/INFO
                       :warn  java.util.logging.Level/WARNING
                       :error java.util.logging.Level/SEVERE
                       :fatal java.util.logging.Level/SEVERE}]
          (reify LogHelper
            (name [_#]
              "java.util.logging")
            (set-level [log-helper# level#]
              (set-level log-helper# level# (str *ns*)))
            (set-level [_# level# name-space#]
              (when-let [lvl# (get levels# level#)]
                (when-let [logger# (java.util.logging.Logger/getLogger name-space#)]
                  (when-let [console-handler# (jul-first-console-handler logger#)]
                    (do
                      (.setLevel console-handler# lvl#)
                      (.setLevel logger# lvl#)
                      (clj-logging/enabled? level# name-space#)))))))))))

(defn get-log-helper []
  (let [log-factory (clj-log-impl/find-factory)]
    (case (clj-log-impl/name log-factory)
      "java.util.logging" (jul-log-helper)
      nil)))

(defn set-logger-level
  ([level]
   (set-logger-level level (str *ns*)))
  ([level name-space]
   (set-level (get-log-helper) level name-space)))
