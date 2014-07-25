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
                 :src-methods "test\\java\\methodlength"} )

  (deftest cpd-duplicated-lines-test
    (testing "10 line duplicate, includes whitespace and braces"
     (is (= 10 (:duplicate-lines-total (cpd-line-count (:src-dup src-map)))))))
  
   (deftest cpd-duplicated-morelines-test
    (testing "10 line duplicate in 3 files, includes whitespace and braces"
     (is (= 10 (:duplicate-lines-total (cpd-line-count (:src-dupmore src-map)))))))

  (deftest cpd-no-duplicated-lines-test
    (testing "Zero lines duplication"
     (is (= 0 (:duplicate-lines-total (cpd-line-count (:src-nodup src-map)))))))
  
  (deftest ncss-linecount-test
    (testing "Line count: 16 -3 braces, -1 whitespace, -1 comment = 11"
     (is (= 11.0 (:non-comment-lines-total (ncss-line-count (:src-linecount src-map)))))))
  
  (deftest ncss-linecount2-test
    (testing "Line count: 18 minus 3 comments = 15"
     (is (= 15.0 (:non-comment-lines-total (ncss-line-count (:src-complexity src-map)))))))

  (deftest ncss-ccn-test
    (testing "Cyclomatic complexity: simple 1, if,else 2, if,elseif,else 3"
     (is (= 6.0 (:cyclomatic-complexity-total (ncss-line-count (:src-complexity src-map)))))
     (is (= 2.0 (:cyclomatic-complexity-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest ncss-ccn1-test "ccn exclusive avg if all methods are oneliners = 0"
    (is (= 0.0 (:cyclomatic-complexity-excl-small-average (ncss-line-count (:src-bean-crlf src-map))))))
  
  (deftest ncss-ccn2-test "ccn exclusive average if non-oneliners have ccn 2 + 3 / 2 = 2.5"
    (is (= 2.5 (:cyclomatic-complexity-excl-small-average (ncss-line-count (:src-complexity src-map))))))
  
  (deftest ncss-mlen-test "method len avg for javabean = 1"
    (is (= 2.0 (:method-len-average (ncss-line-count (:src-bean src-map))))))
  
  (deftest ncss-mlen2-test "method len avg for class 2 + 4 + 7 / 3 = 1"
    (is (= 4.33 (:method-len-average (ncss-line-count (:src-complexity src-map))))))
  
  (deftest ncss-mlen-excl-test "method len excl one liners avg for javabean = 0"
    (is (= 0.0 (:method-len-excl-small-average (ncss-line-count (:src-bean src-map)))))) 
  
  (deftest ncss-mlen-excl2-test "method len avg for class 4 + 7 / 2 = 5.5"
    (is (= 5.5 (:method-len-excl-small-average (ncss-line-count (:src-complexity src-map))))))
  
  (deftest pmd-class-test
    (testing "Class length: 5 + 6 / 2 = 5.5"
     (is (= 14.0 (:class-length-average (pmd-length (:src-nodup src-map)))))))
  
  ;test lines total


;(run-tests)
  
;TODO 
; remove 0.0
; remove duplication
; map to status
; show that tests are excluded
; print xml to console for reference/debugging
; multithread
; split up all tests into 1 line
; better null response handling, 
; src not found
; remove defs
; package
; scm
; check those duplications again - is there a better way