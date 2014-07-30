(ns health_report_card.core_test  
  (:require [clojure.test :refer :all]
            [health_report_card.core :refer :all]))

  ;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
  ;(def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")
  
  (def src-map { :src-dup "test\\java\\duplication"
                 :src-dupmore "test\\java\\duplicationx3"
                 :src-nodup "test\\java\\noduplication"
                 :src-linecount "test\\java\\linecount"
                 :src-complexity "test\\java\\complexity"
                 :src-bean "test\\java\\bean"
                 :src-bean-crlf "test\\java\\beanwithcrlf"
                 :src-methods "test\\java\\methodlength"
                 :src-params "test\\java\\params"
                 :src-class-len "test\\java\\classlen"} )

  (deftest cpd-duplicated-lines-test (testing "12 line with 20 token duplicate, from open brace to close class brace"
    (is (= 13 (:duplicate-lines-total (cpd-line-count (:src-dup src-map)))))))
  
  (deftest cpd-duplicated-morelines-test (testing "10 line duplicate in 2 files, 9 line duplicate in 2 files"
    (is (= 19 (:duplicate-lines-total (cpd-line-count (:src-dupmore src-map)))))))

  (deftest cpd-no-duplicated-lines-test (testing "Zero lines duplication"
    (is (= 0 (:duplicate-lines-total (cpd-line-count (:src-nodup src-map)))))))
  
  (deftest ncss-linecount-test (testing "Line count: 16 -3 braces, -1 whitespace, -1 comment = 11"
    (is (= 11.0 (:non-comment-lines-total (ncss-line-count (:src-linecount src-map)))))))
  
  (deftest ncss-linecount2-test (testing "Line count: 18 minus 3 comments = 15"
    (is (= 15.0 (:non-comment-lines-total (ncss-line-count (:src-complexity src-map)))))))

  (deftest ncss-ccn-total-test (testing "Cyclomatic complexity: simple 1, if,else 2, if,elseif,else 3"
    (is (= 6.0 (:cyclomatic-complexity-total (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-ccn-average-test (testing "Cyclomatic complexity: simple 1, if,else 2, if,elseif,else 3"
    (is (= 2.0 (:cyclomatic-complexity-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-ccn1-test (testing "ccn exclusive avg if all methods are oneliners = 0"
    (is (= 0.0 (:cyclomatic-complexity-excl-small-average (ncss-line-count (:src-bean-crlf src-map)))))))
  
  (deftest ncss-ccn2-test (testing  "ccn exclusive average if non-oneliners have ccn 2 + 3 / 2 = 2.5"
    (is (= 2.5 (:cyclomatic-complexity-excl-small-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-mlen-test (testing "method len avg for javabean = 1"
    (is (= 2.0 (:method-len-average (ncss-line-count (:src-bean src-map)))))))
  
  (deftest ncss-mlen2-test (testing "method len avg for class 2 + 4 + 7 / 3 = 1"
    (is (= 4.33 (:method-len-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-mlen-excl-test (testing "method len excl one liners avg for javabean = 0"
    (is (= 0.0 (:method-len-excl-small-average (ncss-line-count (:src-bean src-map))))))) 
  
  (deftest ncss-mlen-excl2-test (testing "method len avg for class 4 + 7 / 2 = 5.5"
    (is (= 5.5 (:method-len-excl-small-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-class-test (testing "Class length: 10 + 4 / 2 = 7"
     (is (= 7.0 (:class-len-average (ncss-line-count (:src-nodup src-map)))))))
  
  (deftest pmd-method-param-violation-test (testing "# Methods > 3 params = 2"
     (is (= 2.0 (:method-param-violation-count (pmd-length (:src-params src-map)))))))
  
  (deftest ncss-class-len-test (testing "# Classes len > 300"
     (is (= 2.0 (:class-len-violation-count (ncss-line-count (:src-class-len src-map)))))))
  
  
  
  ;test lines total
 ;loc count

;(run-tests)
  
;TODO 
; count LOC without class len violations 
; additionally, count method len and class len violations without ncss 
; remove duplication
; map to status
; show that tests are excluded
; print xml to console for reference/debugging
; multithread
; split up all tests into 1 line
; src not found
; remove defs
; package
; scm
; check those duplications again - is there a better way