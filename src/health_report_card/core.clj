(ns health_report_card.core
  (:import  (javancss Javancss Main)
            (net.sourceforge.pmd PMD)
            (net.sourceforge.pmd.cpd CPD CPDCommandLineInterface)
            (java.util Locale)
            (java.io ByteArrayInputStream ByteArrayOutputStream PrintStream))  
  (:require [clojure.pprint :refer :all]
            [clojure.xml :as xml]
            [saxon :as sax]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]])
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
  
  (defn capture-console [msg f debug] "Read contents of System.out into a stream"
    (let [bstream (ByteArrayOutputStream.)
          pstream (PrintStream. bstream)
          out (System/out)]
      (log/debug "Starting:" msg)
      (System/setOut pstream)
      (f)
      (System/setOut out)
      
      (if debug (println (.toString bstream)))
      
      (log/debug "Finished:" msg)
      bstream))

  (defn capture-console-in [msg f debug]
    (to-inputstream (capture-console msg f debug)))

  ;Run CPD 
  (defn cpd-line-count ([srcdir] (cpd-line-count srcdir false))
                       ([srcdir debug] 
    (System/setProperty (CPDCommandLineInterface/NO_EXIT_AFTER_RUN) "true" )
    
    (letfn [(run-cpd [] (CPD/main (into-array ["--files" srcdir "--minimum-tokens" "20" "--format" "xml" "--encoding" "utf-8"])))]  
      (let [xml-stream (capture-console-in "CPD" run-cpd debug)]
         (if (> (.available xml-stream) 0)
          (def cpd-seq (xml-seq (xml/parse xml-stream)))
          (def cpd-seq nil))))
                           
      {:duplicate-lines-total (apply + (for [node cpd-seq :when (= :duplication (:tag node))] (read-string (:lines (:attrs node))))) } ))

  ;Run NCSS
  (defn ncss-line-count ([srcdir] (ncss-line-count srcdir false))
                        ([srcdir debug] 
    (Locale/setDefault (Locale/US))
    
    (letfn [(run-ncss [] (Javancss. (into-array ["-xml" "-all" "-encoding" "utf-8" srcdir]) Main/S_RCS_HEADER))
            (to-int [seq] (read-string (clojure.string/replace (first seq) #"," "")))]
      (try
        (def ncss-xml (sax/compile-xml(.toString (capture-console "NCSS" run-ncss debug))))    
        (catch Exception e (do (log/warn e) ) (def cpd-seq nil) ))  
      
      (let [ccn-total (sax/query "sum(//function/ccn)" ncss-xml)
            ccn-excl-small-average (sax/query "avg(//function[ncss>2]/ccn)" ncss-xml)
            ccn-average (sax/query "distinct-values(//function_averages/ccn)" ncss-xml)
            method-len-average (sax/query "avg(//function/ncss)" ncss-xml)
            method-len-excl-small-average (sax/query "avg(//function[ncss>2]/ncss)" ncss-xml)
            methods-lines-violation-count (sax/query "count(//function[ncss>30])" ncss-xml)
            methods-ccn-violation-count (sax/query "count(//function[ccn>10])" ncss-xml)
            class-len-violation-count (sax/query "count(//object[ncss>300])" ncss-xml)
            class-len-average (sax/query "avg(//object/ncss)" ncss-xml)
            package-class-violation-count (sax/query "count(//package[classes>25])" ncss-xml)
            ncss-total (sax/query "distinct-values(//functions/ncss)" ncss-xml)]
      
        { :cyclomatic-complexity-total ccn-total
          :cyclomatic-complexity-average (read-string ccn-average)
          :cyclomatic-complexity-excl-small-average (format-num (zero-if-nil ccn-excl-small-average))
          :method-len-average (format-num (zero-if-nil method-len-average))
          :method-len-excl-small-average (format-num (zero-if-nil method-len-excl-small-average))
          :methods-lines-violation-count (format-num (zero-if-nil methods-lines-violation-count))
          :methods-ccn-violation-count (format-num (zero-if-nil methods-ccn-violation-count))
          :class-len-violation-count (format-num (zero-if-nil class-len-violation-count))
          :class-len-average (format-num (zero-if-nil class-len-average))
          :package-class-violation-count (format-num (zero-if-nil package-class-violation-count))
          :non-comment-lines-total (read-string (clojure.string/replace ncss-total #"," "")) }))) )

  
  ;Run PMD
  (defn pmd-length [srcdir]
    (letfn [(run-pmd [] (PMD/main (into-array ["-R" "ruleset.xml" "-f" "xml" "-d" srcdir]))) ] 
  
      (let [xml-stream (capture-console-in "PMD" run-pmd false)]
         (if (> (.available xml-stream) 0)
          (def pmd-seq (xml-seq (xml/parse xml-stream)))
          (def pmd-seq nil))))     
  
      (letfn [ (is-included? [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
         (length [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
         (loop-lengths [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node))) ]
    
        (let [all-classes (loop-lengths "ExcessiveClassLength")
              method-param-violations (loop-lengths "ExcessiveParameterList")]
    
          { :method-param-violation-count (format-num (count method-param-violations))
            ;:class-len-violation-count (format-num (count (filter #(> % 500) all-classes)))
            ;:class-length-average (average all-classes) 
            :lines-total (format-num (apply + all-classes)) } )))


  (defn collect-metrics [srcdir debug]    
      (let [results (merge (cpd-line-count srcdir debug) 
                    (ncss-line-count srcdir debug) 
                    (pmd-length srcdir))]       
        (merge results
               { :duplicate-lines-percentage (format-num (/ (* 100 (:duplicate-lines-total results)) (:lines-total results))) }))) ;move to up

  (defn print-results [results]
    (println)    
    (let [;duplicate-lines-percentage (format-num (/ (* 100 (:duplicate-lines-total results)) (:lines-total results)))
          whitespace-percentage (format-num (/ (* 100 (- (:lines-total results) (:non-comment-lines-total results))) (:lines-total results))) 
          ;method-len (str  (:method-len-average results) " [" (:method-len-excl-small-average results) "]") 
          ;ccn (str (:cyclomatic-complexity-average results) " [" (:cyclomatic-complexity-excl-small-average results) "]")
          ]
      (print-table [{"Metric" "Total lines",      "Value" (:lines-total results),             "T-shirt size" ""}
                    {"Metric" "Total statements", "Value" (:non-comment-lines-total results), "T-shirt size" "tbd"}])
      (println)
      (print-table [;{"Metric" "% Duplication", "Value" duplicate-lines-percentage, "RAG Status" "tbd"}
                    ;{"Metric" "Average class length", "Value" (:class-length-average results), "RAG Status" "tbd"} 
                    ;{"Metric" "Average method length [excluding single-line methods]", "Value" method-len, "RAG Status" "tbd"}
                    ;{"Metric" "Average cyclomatic complexity [excluding single-line methods]", "Value" ccn, "RAG Status" "tbd"}
                    {"Metric" "# Duplicated lines",                        "Value" (:duplicate-lines-total results),        "RAG Status" "tbd"}
                    {"Metric" "# Methods with # statements > 30",          "Value" (:methods-lines-violation-count results),  "RAG Status" "tbd"}
                    {"Metric" "# Methods with cyclomatic complexity > 10", "Value" (:methods-ccn-violation-count results),    "RAG Status" "tbd"}
                    {"Metric" "# Methods with parameters > 3",             "Value" (:method-param-violation-count results), "RAG Status" "tbd"}
                    {"Metric" "# Packages with classes > 25",              "Value" (:package-class-violation-count results),          "RAG Status" "tbd"}
                    {"Metric" "# Classes with # statements > 300",         "Value" (:class-len-violation-count  results),   "RAG Status" "tbd"}
                    ]))
  (println)
  (println))

  
  (def cli-options
    [["-d" "--debug"]])
  
  (defn -main [& args] 
    
    (let [cmd (parse-opts args cli-options) 
          debug (:debug (:options cmd))
          srcdir (first (:arguments cmd))]
      (if debug (println "Debug mode: ON"))
      (println "Scanning: " srcdir)
      (if args
        (print-results (collect-metrics srcdir debug))
        (println "Parameters: [-d --debug] <Java source folder>"))))
  


   