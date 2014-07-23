(ns heath-report-card.core-test
  (:import  (javancss Javancss Main)
            (net.sourceforge.pmd PMD)
            (net.sourceforge.pmd.cpd CPD CPDCommandLineInterface)
            (java.util Locale)
            (java.io ByteArrayInputStream ByteArrayOutputStream PrintStream))  
  (:require [clojure.test :refer :all]
            [heath-report-card.core :refer :all]
            [clojure.pprint :refer :all]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zf]
            [clojure.tools.logging :as log]))


  ;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
  (def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")
  
  (def src-dup "E:\\workspace-sdlc\\test\\src\\main\\java\\duplication")
  (def src-dupmore "E:\\workspace-sdlc\\test\\src\\main\\java\\duplicationx3")
  (def src-nodup "E:\\workspace-sdlc\\test\\src\\main\\java\\noduplication")

  (deftest cpd-duplicated-lines-test
    (testing "10 line duplicate, includes whitespace and braces"
     (is (= 10 (dup-lines-count src-dup)))))

   (deftest cpd-duplicated-morelines-test
    (testing "10 line duplicate in 3 files, includes whitespace and braces"
     (is (= 10 (dup-lines-count src-dupmore)))))

  (deftest cpd-duplicated-lines-test
    (testing "Zero lines duplication"
     (is (= 0 (dup-lines-count src-nodup)))))
    
; src not found

  
;  (deftest locale-US
;   (testing "Locale should be US"
;     (is (= (= (Locale/getDefault) (Locale/US))))))

(dup-lines-count src-nodup)

(run-tests)


;TODO 
; CPD - count instances, if 3, then should add this total again
; use let and letfn to descrease mem
; save the xml for reference/debugging
; ccn / ncss to double
; when does it actually exec the commandline, cos everything is a def
; unit test calculations on some real code
; capture console to XML, not to byte array
; multithread
; package
; scm

