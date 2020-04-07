(ns tellar.core)

(defprotocol Trace
  (trace* [node trajectories]))

(extend-protocol Trace
  java.util.List
  (trace*
    [node trajectories]
    (apply concat
           (map-indexed
            #(trace* %2 (conj trajectories %1))
            node)))

  java.util.Map
  (trace*
    [node trajectories]
    (apply concat
           (map
            (fn [[k v]]
              (trace* v (conj trajectories k)))
            node)))

  java.util.Set
  (trace*
    [node trajectories]
    (apply concat
           (map
            #(trace* % (conj trajectories %))
            node)))

  java.lang.Object
  (trace* [node trajectories]
    [trajectories])

  nil
  (trace* [node trajectories]
    [trajectories]))

(defn- break [coll node]
  (->> (range (count coll))
       (map #(vec (drop-last % coll)))
       (filter #(= (last %) node))))

(defn- in?
  [coll node]
  (some #(= node %) coll))

(defn trace
  [coll node]
  (->> (trace* coll [])
       (filter #(in? % node))
       (mapcat #(break % node))
       distinct
       vec))

(defn assoc-nth
  "Associates the value at a specified occurrence of a given key.

  The occurrence is calculated by a depth-first traversal. `coll` is the nested
  structure, `k` is the key, and `n`defines number of times a key should be found
  before the value is associated."
  [coll k v n]
  {:pre [(> n 0)]}
  (let [trajectory (trace coll k)]
    (if (seq trajectory)
      (assoc-in coll (get trajectory (dec n)) v)
      coll)))

(defn assoc-all
  "Associates the value of all the occurrences of a given key.

  `coll` is the nested structure, and `k` is the key."
  [coll k v]
  (let [trajectories (trace coll k)]
    (if (seq trajectories)
      (reduce (fn [acc trajectory]
                (assoc-in acc trajectory v))
              coll trajectories)
      coll)))

(defn dissoc-nth
  "Dissociates the specified occurrence of the mapping of a given key.

  The occurrence is calculated by a depth-first traversal. `coll` is the nested
  structure, `k` is the key, and `n`defines number of times a key should be found
  before the value is associated."
  [coll k n]
  {:pre [(> n 0)]}
  (let [trajectory (get (trace coll k) (dec n))]
    (if (> (count trajectory) 1)
      (update-in coll (drop-last trajectory) dissoc k)
      (dissoc coll k))))

(defn dissoc-all
  "Dissociates all occurrences of a given key.

  `coll` is the nested structure, `k` is the key."
  [coll k]
  (let [trajectories (trace coll k)]
    (reduce (fn [acc trajectory]
              (if (> (count trajectory) 1)
                (update-in acc (drop-last trajectory) dissoc k)
                (dissoc acc k)))
            coll
            trajectories)))

(defn update-all
  "Updates all values in a nested associative structure for all occurrences of the
  given key. The occurrence is calculated by a depth-first traversal.

  `coll` is the nested structure, `k` is the key `f` is a function that will take
  the old value and any supplied `args` and return the new value"
  [coll k f & args]
  (let [trajectories (trace coll k)]
    (reduce (fn [acc trajectory]
              (update-in acc trajectory #(apply f (cons % args))))
            coll trajectories)))
