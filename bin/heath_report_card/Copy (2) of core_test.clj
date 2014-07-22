(ns heath-report-card.core-test
  (:import  (javancss Javancss Main)
            (net.sourceforge.pmd PMD)
            (java.util Locale)
            (java.io ByteArrayInputStream ByteArrayOutputStream PrintStream))  
  (:require [clojure.test :refer :all]
            [heath-report-card.core :refer :all]
            [clojure.pprint :refer :all]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zf]
            [clojure.tools.logging :as log]))


(defn capture-console [bstream msg f] 
  (def pstream (PrintStream. bstream))
  (def out (System/out))
  (log/info "Starting:" msg)
  (System/setOut pstream)
  (eval f)
  (System/setOut out)
  (log/info "Finished:" msg))

;(def src "E:\\workspace-healthcheck\\CDR\\CDR\\JavaDev")
(def src "E:\\workspace-sdlc\\javancss\\src\\main\\java")

(Locale/setDefault (Locale/US))

;Run NCSS
(do
  (def header Main/S_RCS_HEADER)
  (def ncssargs (into-array ["-xml" "-all" src]))
  (def ncss-bstream (ByteArrayOutputStream.))
  (capture-console ncss-bstream "NCSS" '(Javancss. ncssargs header))
  (def ncss-zip (zip/xml-zip (xml/parse (ByteArrayInputStream. (.toByteArray ncss-bstream)))))
  (def ccn (zf/xml-> ncss-zip :functions :function_averages :ccn  zf/text ))
  (def ncss (zf/xml-> ncss-zip :functions :ncss zf/text)))

;Run PMD
(do  
  (def args (into-array ["-R" "./rulesets/ruleset.xml" "-f" "xml" "-d" src]))
  (def pmd-bstream (ByteArrayOutputStream.))
  (capture-console pmd-bstream "PMD" '(PMD/main args))
  (def pmd-seq (xml-seq (xml/parse (ByteArrayInputStream. (.toByteArray pmd-bstream)))))  
  
  (defn is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
  (defn length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
  
  (def all-methods (for [node pmd-seq :when (is-included? node "ExcessiveMethodLength") ]  (length node)))
  (def all-classes (for [node pmd-seq :when (is-included? node "ExcessiveClassLength") ]  (length node)))  
  (def average-method-length (double (/ (apply + all-methods) (count all-methods) )))
  (def average-class-length (double (/ (apply + all-classes) (count all-classes) ))))



  (pprint {:cyclomatic-complexity ccn 
           :non-comment-lines ncss 
           :average-method-length average-method-length
           :average-class-length average-class-length})
  
  
;TODO 
; save the xml for reference/debugging
; simplify rule
; make all-methods as lazy seq - ensure not done twice
; ccn to double
; test calculations on some real code

(deftest locale-US
  (testing "Locale should be US"
    (is (= (= (Locale/getDefault) (Locale/US))))))


(run-tests)

