(ns heath-report-card.core-test
  (:import  (javancss Javancss Main)
            (net.sourceforge.pmd PMD)
            (net.sourceforge.pmd.cpd CPD CPDCommandLineInterface)
            (java.util Locale)
            (java.io ByteArrayInputStream ByteArrayOutputStream PrintStream))  
  (:require [clojure.test :refer :all]
            [heath-report-card.core :refer :all]
            [clojure.pprint :refer :all]
            [clojure.tools.logging :as log]))


  ;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
  (def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")
  
  (def src-dup "E:\\workspace-sdlc\\test\\src\\main\\java\\duplication")
  (def src-dupmore "E:\\workspace-sdlc\\test\\src\\main\\java\\duplicationx3")
  (def src-nodup "E:\\workspace-sdlc\\test\\src\\main\\java\\noduplication")
  (def src-linecount "E:\\workspace-sdlc\\test\\src\\main\\java\\linecount")
  (def src-complexity "E:\\workspace-sdlc\\test\\src\\main\\java\\complexity")

;  (deftest cpd-duplicated-lines-test
;    (testing "10 line duplicate, includes whitespace and braces"
;     (is (= 10 (dup-lines-count src-dup)))))
;
;   (deftest cpd-duplicated-morelines-test
;    (testing "10 line duplicate in 3 files, includes whitespace and braces"
;     (is (= 10 (dup-lines-count src-dupmore)))))
;
;  (deftest cpd-duplicated-lines-test
;    (testing "Zero lines duplication"
;     (is (= 0 (dup-lines-count src-nodup)))))
  
  (deftest ncss-line-count-test
    (testing "Line count: 16 -3 braces, -1 whitespace, -1 comment = 11"
     (is (= 11.0 (:non-comment-lines (ncss-line-count src-linecount))))))

  (deftest ncss-ccn-test
    (testing "Cyclomatic complexity: "
     (is (= 6 (:cyclomatic-complexity-total (ncss-line-count src-complexity))))
     (is (= 2.0 (:cyclomatic-complexity-average (ncss-line-count src-complexity))))))
  
  (deftest ncss-test
    (testing "Line count = 18 minus 3 comments = 15"
     (is (= 15.0 (:non-comment-lines (ncss-line-count src-complexity))))))
  
; src not found

 
(:cyclomatic-complexity-total (ncss-line-count src-complexity))

(run-tests)


;TODO 
; use let and letfn to descrease mem
; save the xml for reference/debugging
; ccn / ncss to double, tidy up decimals
; when does it actually exec the commandline, cos everything is a def
; unit test calculations on some real code
; capture console to XML, not to byte array
; multithread
; package
; scm

