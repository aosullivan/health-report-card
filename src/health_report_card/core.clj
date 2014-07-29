(ns health_report_card.core
  (:import  (javancss Javancss Main)
            (net.sourceforge.pmd PMD)
            (net.sourceforge.pmd.cpd CPD CPDCommandLineInterface)
            (java.util Locale)
            (java.io ByteArrayInputStream ByteArrayOutputStream PrintStream))  
  (:require [clojure.pprint :refer :all]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [saxon :as sax]
            [clojure.data.zip.xml :as zf]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:gen-class :main true))


(defn format-num [x] "Format to two decimal places"
    (read-string (format "%.2f" (double x))))

(defn average [seq] "Treat average of zero as 0"
  (if (zero? (count seq))
    0
   (format-num (/ (apply + seq) (count seq)))))

(defn to-inputstream [outputstream] "InputStream to OutputStream"
    (ByteArrayInputStream. (.toByteArray outputstream)) )

(defn zero-if-nil [x] 
  (if (nil? x) 0 x))

(defn capture-console [msg f] "Read contents of System.out into a stream"
  (let [bstream (ByteArrayOutputStream.)
        pstream (PrintStream. bstream)
        out (System/out)]
    (log/debug "Starting:" msg)
    (System/setOut pstream)
    (f)
    (System/setOut out)
    
    (println (.toString bstream)) ;debug
    
    (log/debug "Finished:" msg)
    bstream))

(defn capture-console-in [msg f]
  (to-inputstream (capture-console msg f)))

;Run CPD 
(defn cpd-line-count [srcdir]
  (System/setProperty (CPDCommandLineInterface/NO_EXIT_AFTER_RUN) "true" )
  
  (letfn [(run-cpd [] (CPD/main (into-array ["--files" srcdir "--minimum-tokens" "20" "--format" "xml" "--encoding" "utf-8"])))]  
    
      (let [xml-stream (capture-console-in "CPD" run-cpd)]
         (if (> (.available xml-stream) 0)
          (def cpd-seq (xml-seq (xml/parse xml-stream)))
          (def cpd-seq nil))))
                           
    ; Duplicates are 10 tokens and 5 or more lines, including whitespace
    {:duplicate-lines-total (apply + (for [node cpd-seq :when (= :duplication (:tag node))] (read-string (:lines (:attrs node))))) } )


;Run NCSS
(defn ncss-line-count [srcdir] 
  (Locale/setDefault (Locale/US))
  
  (letfn [(run-ncss [] (Javancss. (into-array ["-xml" "-all" "-encoding" "utf-8" srcdir]) Main/S_RCS_HEADER))
          (to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))]
    (try
      (def ncss-xml (sax/compile-xml(.toString (capture-console "NCSS" run-ncss))))    
      (catch Exception e (do (log/warn e) ) (def cpd-seq nil) ))  
    
    (let [ccn-total (sax/query "sum(//function/ccn)" ncss-xml)
          ccn-excl-small-average (sax/query "avg(//function[ncss>2]/ccn)" ncss-xml)
          ccn-average (sax/query "distinct-values(//function_averages/ccn)" ncss-xml)
          method-len-average (sax/query "avg(//function/ncss)" ncss-xml)
          method-len-excl-small-average (sax/query "avg(//function[ncss>2]/ncss)" ncss-xml)
          ncss-total (sax/query "distinct-values(//functions/ncss)" ncss-xml)]
    
      { :cyclomatic-complexity-total ccn-total
        :cyclomatic-complexity-average (read-string ccn-average)
        :cyclomatic-complexity-excl-small-average (format-num (zero-if-nil ccn-excl-small-average))
        :method-len-average (format-num (zero-if-nil method-len-average))
        :method-len-excl-small-average (format-num (zero-if-nil method-len-excl-small-average))
        :non-comment-lines-total (read-string (clojure.string/replace ncss-total #"," "")) }))) 

 ;Run PMD
 (defn pmd-length [srcdir]
   (letfn [(run-pmd [] (PMD/main (into-array ["-R" "./rulesets/ruleset.xml" "-f" "xml" "-d" srcdir]))) ] 

     (try
        (def pmd-seq (xml-seq (xml/parse (capture-console-in "PMD" run-pmd))))    
        (catch Exception e (do (log/warn e) ) (def pmd-seq nil) )))  

   (letfn [ (is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
     (length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
     (loop-lengths [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node))) ]

     (let [all-classes (loop-lengths "ExcessiveClassLength")]

       { :class-length-average (average all-classes) 
         :lines-total (format-num (apply + all-classes)) } )))


 (defn collect-metrics [srcdir]    
     (let [results (merge (cpd-line-count srcdir) 
                   (ncss-line-count srcdir) 
                   (pmd-length srcdir))]       
       (merge results
              { :duplicate-lines-percentage (format-num (/ (* 100 (:duplicate-lines-total results)) (:lines-total results))) }))) ;move to up

 (defn print-results [results]
     (println)    
     (let [duplicate-lines-percentage (format-num (/ (* 100 (:duplicate-lines-total results)) (:lines-total results)))
           whitespace-percentage (format-num (/ (* 100 (- (:lines-total results) (:non-comment-lines-total results))) (:lines-total results))) 
           method-len (str  (:method-len-average results) " [" (:method-len-excl-small-average results) "]") 
           ccn (str (:cyclomatic-complexity-average results) " [" (:cyclomatic-complexity-excl-small-average results) "]")]
       (print-table [{"Metric" "Total lines", "Value" (:lines-total results), "T-shirt size" ""}
                     {"Metric" "% Whitespace, braces only, comments", "Value" whitespace-percentage, "T-shirt size" ""}
                     {"Metric" "Total lines excl. whitespace etc.", "Value" (:non-comment-lines-total results), "T-shirt size" "MED"}])
       (println)
       (print-table [{"Metric" "% Duplication", "Value" duplicate-lines-percentage, "RAG Status" "Red"}
                     {"Metric" "Average class length", "Value" (:class-length-average results), "RAG Status" "Green"} 
                     {"Metric" "Average method length [excluding single-line methods]", "Value" method-len, "RAG Status" "Green"}
                     {"Metric" "Average cyclomatic complexity [excluding single-line methods]", "Value" ccn, "RAG Status" "Green"}]))
   (println)
   (println))

 (defn -main [& args] 
   (let [srcdir (first args)]
   (println "Scanning: " srcdir)
  (if args
    (print-results (collect-metrics srcdir)  )
    (println "Usage: healthreportcard <source folder>"))))
  


   