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

(def double-arr-roughly-equal? coll-roughly-equal?) ;; coll-roughly-equal? can deal with double-array

(def int-arr-roughly-equal? coll-roughly-equal?)

(defn comparable?
  [a1 a2]
  (or (=  (type a1) (type a2))
      (and (number? a1) (number? a2))))

(defn roughly-equal?
  [expected actual tolerance]
  (cond (not (comparable? expected actual)) false
        (number? expected) (number-roughly-equal? expected actual tolerance)
        (double-array? expected) (double-arr-roughly-equal? expected actual tolerance)
        (int-array? expected) (int-arr-roughly-equal? expected actual tolerance)
        (coll? expected) (coll-roughly-equal? expected actual tolerance)))

(defchecker roughly-all
  ([expected tolerance]
     (checker [actual]
              (roughly-equal? expected actual tolerance)))
  ([expected]
     (roughly-all expected TOLERANCE)))
