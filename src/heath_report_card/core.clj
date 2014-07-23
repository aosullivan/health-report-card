(ns heath-report-card.core
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
  ;(log/info "Starting:" msg)
  (System/setOut pstream)
  (f)
  (System/setOut out)
  ;(log/info "Finished:" msg)
  (ByteArrayInputStream. (.toByteArray bstream)))

;Run CPD
(defn dup-lines-count [srcdir]
  (System/setProperty (CPDCommandLineInterface/NO_EXIT_AFTER_RUN) "true" )
  
  (letfn [(run-cpd [] (CPD/main (into-array ["--files" srcdir "--minimum-tokens" "10" "--format" "xml"])))]  
    (try
      (def cpd-seq (xml-seq (xml/parse (capture-console "CPD" run-cpd))))    
      (catch Exception e (do (log/warn e) (log/info "No duplications found") ) (def cpd-seq nil) )) 
    (apply + (for [node cpd-seq :when (= :duplication (:tag node))] (read-string (:lines (:attrs node)))))))


(defn ncss-line-count [srcdir] 
  (Locale/setDefault (Locale/US))
  (defn to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))
  
  (letfn [(run-ncss [] (Javancss. (into-array ["-xml" "-all" srcdir]) Main/S_RCS_HEADER))]
  (try
    (def ncss-zip (zip/xml-zip (xml/parse (capture-console "NCSS" run-ncss))))    
    (catch Exception e (do (log/warn e) ) (def cpd-seq nil) )))  
  
  { :cyclomatic-complexity-total (apply + (map read-string (zf/xml-> ncss-zip :functions :function :ccn zf/text))) 
    :cyclomatic-complexity-average (to-int (zf/xml-> ncss-zip :functions :function_averages :ccn zf/text ))
    :non-comment-lines (to-int (zf/xml-> ncss-zip :functions :ncss zf/text)) } ) 

                                         
;Run NCSS
;(do
;  (Locale/setDefault (Locale/US))
;  (defn to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))
;  (def ncss-zip (zip/xml-zip (xml/parse (capture-console "NCSS" '(Javancss. (into-array ["-xml" "-all" src]) Main/S_RCS_HEADER)))))
;  (def cyclomatic-complexity (to-int (zf/xml-> ncss-zip :functions :function_averages :ccn  zf/text )))
;  (def non-comment-lines (to-int (zf/xml-> ncss-zip :functions :ncss zf/text))))
;
;Run PMD
;  
;  (def pmd-seq (xml-seq (xml/parse (capture-console "PMD" '(PMD/main (into-array ["-R" "./rulesets/ruleset.xml" "-f" "xml" "-d" src])))  )))    
;  
;  (defn is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
;  (defn length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
;  (defn loop-lengths [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node)))
;  
;  (def all-methods (loop-lengths "ExcessiveMethodLength"))  
;  (def all-classes (loop-lengths "ExcessiveClassLength"))
;  
;  (def average-method-length (double (/ (apply + all-methods) (count all-methods) )))
;  (def average-class-length (double (/ (apply + all-classes) (count all-classes) )))
;
;
;  (pprint {:non-comment-lines non-comment-lines
;           :cyclomatic-complexity cyclomatic-complexity            
;           :average-method-length average-method-length
;           :average-class-length average-class-length
;           :duplicated-lines duplicated-lines})
  

