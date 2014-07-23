(ns heath-report-card.core-test  
  (:require [clojure.test :refer :all]
            [heath-report-card.core :refer :all]))


  ;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
  (def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")
  
  (def src-map { :src-dup "E:\\workspace-sdlc\\test\\src\\main\\java\\duplication"
                 :src-dupmore "E:\\workspace-sdlc\\test\\src\\main\\java\\duplicationx3"
                 :src-nodup "E:\\workspace-sdlc\\test\\src\\main\\java\\noduplication"
                 :src-linecount "E:\\workspace-sdlc\\test\\src\\main\\java\\linecount"
                 :src-complexity "E:\\workspace-sdlc\\test\\src\\main\\java\\complexity" } )

  (deftest cpd-duplicated-lines-test
    (testing "10 line duplicate, includes whitespace and braces"
     (is (= 10 (:duplicate-lines (cpd-line-count (:src-dup src-map)))))))
  
   (deftest cpd-duplicated-morelines-test
    (testing "10 line duplicate in 3 files, includes whitespace and braces"
     (is (= 10 (:duplicate-lines (cpd-line-count (:src-dupmore src-map)))))))

  (deftest cpd-no-duplicated-lines-test
    (testing "Zero lines duplication"
     (is (= 0 (:duplicate-lines (cpd-line-count (:src-nodup src-map)))))))
  
  (deftest ncss-line-count-test
    (testing "Line count: 16 -3 braces, -1 whitespace, -1 comment = 11"
     (is (= 11.0 (:non-comment-lines-total (ncss-line-count (:src-linecount src-map)))))))
  
  (deftest ncss-line-count-test2
    (testing "Line count: 18 minus 3 comments = 15"
     (is (= 15.0 (:non-comment-lines-total (ncss-line-count (:src-complexity src-map)))))))

  (deftest ncss-ccn-test
    (testing "Cyclomatic complexity: simple 1, if,else 2, if,elseif,else 3"
     (is (= 6 (:cyclomatic-complexity-total (ncss-line-count (:src-complexity src-map)))))
     (is (= 2.0 (:cyclomatic-complexity-average (ncss-line-count (:src-complexity src-map)))))))
  
  (deftest pmd-method-length-test
    (testing "Average method length: 16/3 , Class length: 5 + 6 / 2 = 5.5"
     (is (= (double (/ 16 3)) (:average-method-length (pmd-length (:src-nodup src-map)))))
     (is (= 14.0 (:average-class-length (pmd-length (:src-nodup src-map)))))))
  
; src not found
 
(run-tests)


;TODO 
; use let and letfn to descrease mem
; save the xml for reference/debugging
; ccn / ncss to double, tidy up decimals
; when does it actually exec the commandline, cos everything is a def
; capture console to XML, not to byte array
; multithread
; update file sources to be local
; package
; scm

