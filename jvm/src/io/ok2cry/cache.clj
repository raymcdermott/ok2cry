(ns io.ok2cry.cache)

(set! *warn-on-reflection* true)

(def cache (atom {}))

(defn ttl-delete
  [k & {:keys [ttl] :or {ttl 3000}}]
  (try
    (future (Thread/sleep ^long ttl)
            (swap! cache dissoc k))
    (catch Exception _always-delete
      (swap! cache dissoc k))))

(defn >put [k v]
  (swap! cache assoc k v)
  (ttl-delete k v)
  v)

(defn <get [k]
  (@cache k))


