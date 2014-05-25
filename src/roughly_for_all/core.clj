(ns roughly-for-all.core
  (use [midje.checking.checkers.defining :only [checker defchecker]]))

(def TOLERANCE  1.0E-6)
(def roughly-equal?)

(defn number-roughly-equal?
  [expected actual tolerance]
  (and (>= expected (-' actual tolerance))
       (<= expected (+' actual tolerance))))

(defn- not-same-length?
  [c1 c2]
  (not= (count c1) (count c2)))

(defn coll-roughly-equal?
  [compare-val compared-val tolerance]
  (if (not-same-length? compare-val compared-val)
    false
    (loop [remain-compare-val compare-val
           remain-compared-val compared-val
           eq true]
      (cond (not eq) false
            (empty? remain-compare-val) true
            :else (recur
                   (rest remain-compare-val)
                   (rest remain-compared-val)
                   (roughly-equal? (first remain-compare-val) (first remain-compared-val) tolerance))))))

(defn- double-array?
  [arr]
  (= (type arr) (Class/forName "[D")))

(defn- int-array?
  [arr]
  (= (type arr) (Class/forName "[I" )))

(defn- array?
  [arr]
  (or (double-array? arr) (int-array? arr)))

(def arr-roughly-equal? coll-roughly-equal?) ;; coll-roughly-equal? can deal with array

(defn- hash-map?
  [h]
  (= clojure.lang.PersistentArrayMap (type h)))

(defn hash-map-roughly-equal?
  [compare-hm compared-hm tolerance]
  (let [compare-hm-keys (keys compare-hm)
        compared-hm-keys (keys compared-hm)]
    (if (not= compare-hm-keys compared-hm-keys)
      false
      (loop [remain-compare-hm-keys compare-hm-keys
             eq-flag true]
        (cond (not eq-flag) false
              (empty? remain-compare-hm-keys) true
              :else (recur
                     (rest remain-compare-hm-keys)
                     (roughly-equal?
                      ((first remain-compare-hm-keys) compare-hm)
                      ((first remain-compare-hm-keys) compared-hm)
                      tolerance)))))))

(defn- to-hash-map
  [record]
  (merge {} record))

(defn record-roughly-equal?
  [compare compared tolerance]
  (hash-map-roughly-equal? (to-hash-map compare) (to-hash-map compared) tolerance))

(defn comparable?
  "same type or both are number"
  [a1 a2]
  (or (=  (type a1) (type a2))
      (and (number? a1) (number? a2))))

(defn roughly-equal?
  [expected actual tolerance]
  (cond (not (comparable? expected actual)) false
        (number? expected) (number-roughly-equal? expected actual tolerance)
        (array? expected) (arr-roughly-equal? expected actual tolerance)
        (hash-map? expected) (hash-map-roughly-equal? expected actual tolerance) ; hahs-map is coll too... here is bad...
        (coll? expected) (coll-roughly-equal? expected actual tolerance)
        :else (record-roughly-equal? expected actual tolerance)))

(defchecker roughly-all
  ([expected tolerance]
     (checker [actual]
              (roughly-equal? expected actual tolerance)))
  ([expected]
     (roughly-all expected TOLERANCE)))
