(ns roughly-for-all.core-test
  (:require [clojure.test :refer :all]
            [roughly-for-all.core :refer :all])
  (:use midje.sweet
        [midje.checking.checkers.defining :only [checker?]]))

(def HALF-TOLERANCE  (/ 1.0E-6 2))
(def DOUBLE-TOLERANCE  (* 1.0E-6 2))

(facts "number-roughly-equal?"
       (number-roughly-equal? 1 (+ 1 0.5) 1) => true
       (number-roughly-equal? 1 (+ 1 2.0) 1) => false

       (number-roughly-equal? 1 (- 1 0.5) 1) => true
       (number-roughly-equal? 1 (- 1 2.0) 1) => false)

(facts "coll-roughly-equal?"
       (fact "coll"
             (coll-roughly-equal? '(1 2) '(1.5 2.5) 1) => true
             (coll-roughly-equal? '(1 2) '(1.5 2.5) 0.4) => false
             (coll-roughly-equal? [1 2] [1.5 2.5] 0.4) => false)
       (fact "recursive coll"
             (coll-roughly-equal? (list 1 (list 2)) (list 1.5 (list 2.5)) 1) => true
             (coll-roughly-equal? (list 1 (list 2)) (list 1.5 (list 2.5)) 0.4) => false)
       (fact "for different length"
             (coll-roughly-equal? '(1 2) '(1.5 2.5 3) 1) => false))

(facts "arr-roughly-equal?"
       (facts "for double array"
              (fact "simple case"
                    (arr-roughly-equal? (double-array [1 2]) (double-array [1.5 2.5]) 1) => true
                    (arr-roughly-equal? (double-array [1 2]) (double-array [1.5 2.5]) 0.4) => false)
              (fact "for different length"
                    (arr-roughly-equal? (double-array [1 2]) (double-array [1.5 2.5 3]) 1) => false
                    (arr-roughly-equal? (double-array [1 2 10]) (double-array [1.5 2.5]) 1) => false))
       (facts "for int"
              (arr-roughly-equal? (int-array [1 2]) (int-array [1.5 2.5]) 1) => true)
       (fact "different type of array is not equal"
             (roughly-equal? (int-array [1 2]) (double-array [2 3]) 4) => false))

(facts "hash-map-roughly-equal?" ;; may be {:a => 1} called hash or map or dict or obj orsomething elsse... i don't know
       (facts "all keys should be same"
              (fact "simple"
                    (hash-map-roughly-equal? {:a 1} {:b 1} 1) => false)
              (fact "different order"
                    (hash-map-roughly-equal? {:a 1 :b 2} {:b 2 :a 1} 1) =not=> false))
       (fact "one level"
             (hash-map-roughly-equal? {:a 1 :b 2} {:b 2 :a 1} 2) => true
             (hash-map-roughly-equal? {:a 1 :b 2} {:b 3 :a 2} 0.5) => false)
       (fact "more than one level"
             (hash-map-roughly-equal? {:a 1 :b {:b 2}} {:b {:b 2} :a 1} 2) => true
             (hash-map-roughly-equal? {:a 1 :b {:b 2}} {:b {:b 3} :a 2} 0.5) => false))


(defrecord Foo [a b])
(facts "record-roughly-equal?"
       (fact ""
             (record-roughly-equal? (Foo. 1 2) (Foo. 1.1 2.1)  1 ) => true)
       (fact "record should not equal hash-map"
             (roughly-equal? (Foo. 1 2) {:a 1 :b 2} 1) => false))

(facts "roughly-equal? a dispatch by input type"
       (fact "different type, should return false"
             (roughly-equal? 1 [1] 3) => false)
       (fact "compare number by number-roughly-equal?"
             (roughly-equal? 1 2 3) => anything
             (provided
              (number-roughly-equal? 1 2 3) => anything :times 1))
       (fact "it should call coll-roughly-equal?"
             (roughly-equal? (list 1) (list 1.5) 1) => true
             (provided
              (coll-roughly-equal? anything anything anything) => true :times 1))
       (fact "for double-array"
             (roughly-equal? (double-array [1]) (double-array [1.5]) 1) => true
             (provided
              (arr-roughly-equal? anything anything anything) => true :times 1))
       (fact "for int-array"
             (roughly-equal? (int-array [1]) (int-array [1.5]) 1) => true
             (provided
              (arr-roughly-equal? anything anything anything) => true :times 1))
       (fact "for hash-map"
             (roughly-equal? {:a 1} {:a 1.5} 1) => true
             (provided
              (hash-map-roughly-equal? anything anything anything) => true :times 1)))

(facts "roughly-all"
       (fact "it is a checker"
             #'roughly-all => checker?
             roughly-all => checker?
             (roughly-all 3) => checker?
             (roughly-all 3 1) => checker?)
       (fact "for number"
             (+ 1 HALF-TOLERANCE) => (roughly-all 1)
             1.5 => (roughly-all 1 1))
       (fact "roughly equal"
             {:a 1 :b {:bb 2 :cc 3}} => (roughly-all {:a 2 :b {:bb 3 :cc 4}} 2)
             {:a 1 :b {:bb 2 :cc (list 1 2)}} => (roughly-all {:a 2 :b {:bb 3 :cc (list 3 4)}} 2)
             {:a 1 :b {:bb 2 :cc (list 1 {:a 2})}} => (roughly-all {:a 2 :b {:bb 3 :cc (list 3 {:a 3})}} 2)
             {:a 1 :b {:bb 2 :cc (double-array [1 2])}} => (roughly-all {:a 2 :b {:bb 3 :cc (double-array  [3 4])}} 2)
             {:a 1 :b {:bb 2 :cc {:ddd 3}}} => (roughly-all {:a 2 :b {:bb 3 :cc {:ddd 4}}} 2)
             {:a (+ 1 HALF-TOLERANCE) :b {:bb (+ 2 HALF-TOLERANCE) :cc {:ddd (+ 3 HALF-TOLERANCE)}}} => (roughly-all {:a 1 :b {:bb 2 :cc {:ddd 3}}}))
       (fact "roughly not equal"
             {:a 1 :b {:bb 2 :cc 3}} =not=> (roughly-all {:a 2 :b {:bb 3 :cc 4}} 0.5)
             {:a 1 :b {:bb 2 :cc (list 1 {:a 2})}} =not=> (roughly-all {:a 2 :b {:bb 3 :cc (list 3 {:a 3})}} 0.5)
             {:a (+ 1 DOUBLE-TOLERANCE) :b {:bb (+ 2 DOUBLE-TOLERANCE) :cc {:ddd (+ 3 DOUBLE-TOLERANCE)}}} =not=> (roughly-all {:a 1 :b {:bb 2 :cc {:ddd 3}}})))
