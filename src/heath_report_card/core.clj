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
(defn cpd-line-count [srcdir]
  (System/setProperty (CPDCommandLineInterface/NO_EXIT_AFTER_RUN) "true" )
  
  (letfn [(run-cpd [] (CPD/main (into-array ["--files" srcdir "--minimum-tokens" "10" "--format" "xml" "--encoding" "utf-8"])))]  
    
    (try
      (def cpd-seq (xml-seq (xml/parse (capture-console "CPD" run-cpd))))    
      (catch Exception e (do (log/warn e) (log/info "No duplications found") ) (def cpd-seq nil) )) 
    
    {:duplicate-lines (apply + (for [node cpd-seq :when (= :duplication (:tag node))] (read-string (:lines (:attrs node))))) } ))

;Run NCSS
(defn ncss-line-count [srcdir] 
  (Locale/setDefault (Locale/US))
  
  (letfn [(run-ncss [] (Javancss. (into-array ["-xml" "-all" srcdir]) Main/S_RCS_HEADER))
          (to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))]
    (try
      (def ncss-zip (zip/xml-zip (xml/parse (capture-console "NCSS" run-ncss))))    
      (catch Exception e (do (log/warn e) ) (def cpd-seq nil) ))  
    
    { :cyclomatic-complexity-total (apply + (map read-string (zf/xml-> ncss-zip :functions :function :ccn zf/text))) 
      :cyclomatic-complexity-average (to-int (zf/xml-> ncss-zip :functions :function_averages :ccn zf/text ))
      :non-comment-lines-total (to-int (zf/xml-> ncss-zip :functions :ncss zf/text)) } )) 

;Run PMD
(defn pmd-length [srcdir]
  (letfn [(run-pmd [] (PMD/main (into-array ["-R" "./rulesets/ruleset.xml" "-f" "xml" "-d" srcdir "--encoding" "utf-8"]))) ] 
    (def pmd-seq (xml-seq (xml/parse (capture-console "PMD" run-pmd)))))

  (letfn [ (is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
           (length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
           (loop-lengths [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node))) ]
  
    (let [all-methods (loop-lengths "ExcessiveMethodLength")  
          all-classes (loop-lengths "ExcessiveClassLength")]
  
    { :average-method-length (double (/ (apply + all-methods) (count all-methods) ))
      :average-class-length (double (/ (apply + all-classes) (count all-classes) )) } )))

(defn collect[srcdir]   
  (merge (cpd-line-count srcdir) 
         (ncss-line-count srcdir) 
         (pmd-length srcdir)))

