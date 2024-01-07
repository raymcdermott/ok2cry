(ns io.ok2cry.data
  (:require [clojure.edn :as edn])
  (:import (java.util Base64)))

(set! *warn-on-reflection* true)

(defn edn->bytes
  [edn-data]
  (-> (pr-str edn-data)
      (.getBytes "UTF-8")))

(defn bytes->edn
  [^bytes ba]
  (edn/read-string (String. ba)))

(defn bytes->base64
  [ba]
  (-> (Base64/getEncoder)
      (.encodeToString ba)))

(defn base64->bytes
  [^String base64]
  (-> (Base64/getDecoder)
      (.decode base64)))

(defn bytes->hex-string
  [^bytes ba]
  (format "%x" (new BigInteger ba)))

(defn hex-string->bytes
  [hex-string]
  {:pre [(even? (count hex-string))]}
  (->> hex-string
       (partition 2)
       (map (fn [[c1 c2]]
              (-> (str c1 c2) (Short/parseShort 16) unchecked-byte)))
       (into-array Byte/TYPE)))

(defn edn->hex
  [edn-data]
  (-> (pr-str edn-data)
      (.getBytes "UTF-8")
      (bytes->hex-string)))

(defn string->hex-string
  [^String s]
  (-> (.getBytes s "UTF-8")
      (bytes->hex-string)))

(defn hex-string->base64-string
  [s]
  (let [ba (->> (partition 2 s)
                (map (fn [[x y]]
                       (-> (Integer/parseInt (str x y) 16)
                           (unchecked-byte))))
                (into-array Byte/TYPE))]
    (String. ^bytes ba "UTF-8")))

(defn hex->edn
  [hex-string]
  (-> hex-string
      ^bytes (hex-string->bytes)
      (String. "UTF-8")
      (edn/read-string)))

