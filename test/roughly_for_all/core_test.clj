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
              (arr-roughly-equal? (int-array [1 2]) (int-array [1.5 2.5]) 1) => true))

(facts "double and int"
       (fact "different type of array is not equal"
             (roughly-equal? (int-array [1 2]) (double-array [2 3]) 4) => false))

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
              (arr-roughly-equal? anything anything anything) => true :times 1)))

(facts "roughly-all"
       (fact "it is a checker"
             #'roughly-all => checker?
             roughly-all => checker?
             (roughly-all 3) => checker?
             (roughly-all 3 1) => checker?)
       (fact "for number"
             (+ 1 HALF-TOLERANCE) => (roughly-all 1)
             1.5 => (roughly-all 1 1)))
