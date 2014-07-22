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


(defn capture-console [msg f] 
  (def bstream (ByteArrayOutputStream.))
  (def pstream (PrintStream. bstream))
  (def out (System/out))
  (log/info "Starting:" msg)
  (System/setOut pstream)
  (eval f)
  (System/setOut out)
  (log/info "Finished:" msg)
  (ByteArrayInputStream. (.toByteArray bstream)))

;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
(def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")


;Run CPD
(do
  (System/setProperty (CPDCommandLineInterface/NO_EXIT_AFTER_RUN) "true" )
  (def cpd-seq (xml-seq (xml/parse (capture-console "CPD" '(CPD/main (into-array ["--files" src "--minimum-tokens" "80" "--format" "xml"]))))))
  (def duplicated-lines (apply + (for [node cpd-seq :when (= :duplication (:tag node))] (read-string (:lines (:attrs node) ))))))


;Run NCSS
(do
  (Locale/setDefault (Locale/US))
  (defn to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))
  (def ncss-zip (zip/xml-zip (xml/parse (capture-console "NCSS" '(Javancss. (into-array ["-xml" "-all" src]) Main/S_RCS_HEADER)))))
  (def ccn (to-int (zf/xml-> ncss-zip :functions :function_averages :ccn  zf/text )))
  (def ncss (to-int (zf/xml-> ncss-zip :functions :ncss zf/text))))

;Run PMD
(do    
  (def pmd-seq (xml-seq (xml/parse (capture-console "PMD" '(PMD/main (into-array ["-R" "./rulesets/ruleset.xml" "-f" "xml" "-d" src])))  )))    
  
  (defn is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
  (defn length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
  (defn loop-lengths [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node)))
  
  (def all-methods (loop-lengths "ExcessiveMethodLength"))
  (def all-classes (loop-lengths "ExcessiveClassLength"))
  
  (def average-method-length (double (/ (apply + all-methods) (count all-methods) )))
  (def average-class-length (double (/ (apply + all-classes) (count all-classes) ))))


  (pprint {:non-comment-lines ncss
           :cyclomatic-complexity ccn            
           :average-method-length average-method-length
           :average-class-length average-class-length
           :duplicated-lines duplicated-lines})
  
  
;TODO 
; CPD - count instances, if 3, then should add this total again
; save the xml for reference/debugging
; ccn / ncss to double
; test calculations on some real code
; capture console to XML, not to byte array
; multithread
; package
; scm

(deftest locale-US
  (testing "Locale should be US"
    (is (= (= (Locale/getDefault) (Locale/US))))))


(run-tests)

