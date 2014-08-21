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
    
    (let [run-cpd  (fn [] (CPD/main (into-array ["--files" srcdir "--minimum-tokens" "20" "--format" "xml" "--encoding" "utf-8"])))  
          xml-stream (capture-console-in "CPD" run-cpd debug)]
         (if (> (.available xml-stream) 0)
          (def cpd-seq (xml-seq (xml/parse xml-stream)))
          (def cpd-seq nil)))
                           
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
      
      (let [methods-len-violation-count (sax/query "count(//function[ncss>30])" ncss-xml)
            methods-ccn-violation-count (sax/query "count(//function[ccn>10])" ncss-xml)
            class-len-violation-count (sax/query "count(//object[ncss>300])" ncss-xml)
            package-class-violation-count (sax/query "count(//package[classes>25])" ncss-xml)
            methods-total (sax/query "count(//function)" ncss-xml)
            class-total (sax/query "count(//object)" ncss-xml)
            packages-total (sax/query "count(//package)" ncss-xml)
            ncss-total (sax/query "distinct-values(//functions/ncss)" ncss-xml)]
      
        { :methods-len-violation-count (format-num (zero-if-nil methods-len-violation-count))
          :methods-ccn-violation-count (format-num (zero-if-nil methods-ccn-violation-count))
          :class-len-violation-count (format-num (zero-if-nil class-len-violation-count))
          :package-class-violation-count (format-num (zero-if-nil package-class-violation-count))
          :methods-total (format-num (zero-if-nil methods-total))
          :class-total (format-num (zero-if-nil class-total)) 
          :packages-total (format-num (zero-if-nil packages-total)) 
          :non-comment-lines-total (read-string (clojure.string/replace ncss-total #"," "")) }))) )

  
  ;Run PMD
  (defn pmd-length [srcdir]
    (letfn [(run-pmd [] (PMD/main (into-array ["-R" "ruleset.xml" "-f" "xml" "-d" srcdir]))) ] 
  
      (let [xml-stream (capture-console-in "PMD" run-pmd false)]
         (if (> (.available xml-stream) 0)
          (def pmd-seq (xml-seq (xml/parse xml-stream)))
          (def pmd-seq nil))))     
  
      (let [ is-included? (fn [node rule] (and (= :violation (:tag node)) (= rule (:rule (:attrs node)))))
             length (fn [node] (- (read-string (:endline (:attrs node))) (read-string (:beginline (:attrs node)))))
             loop-lengths (fn [rule] (for [node pmd-seq :when (is-included? node rule) ]  (length node))) 
             all-classes (loop-lengths "ExcessiveClassLength")
             method-param-violations (loop-lengths "ExcessiveParameterList")]
    
          { :method-param-violation-count (format-num (count method-param-violations))
            :lines-total (format-num (apply + all-classes)) } ))

  (defn collect-metrics [srcdir debug]    
      (merge (cpd-line-count srcdir debug) 
                    (ncss-line-count srcdir debug) 
                    (pmd-length srcdir)))       

  (defn print-results [results]
    (println)   
    ;move this to collect-metrics
    (let [duplicate-lines-percentage (format-num (/ (* 100 (:duplicate-lines-total results)) (:lines-total results)))
          methods-violating-len-percentage (format-num (/ (* 100 (:methods-len-violation-count results)) (:methods-total results)))
          methods-violating-ccn-percentage (format-num (/ (* 100 (:methods-ccn-violation-count results)) (:methods-total results)))
          methods-violating-params-percentage (format-num (/ (* 100 (:method-param-violation-count results)) (:methods-total results)))
          classes-violating-len-percentage (format-num (/ (* 100 (:class-len-violation-count results)) (:class-total results)))
          packages-violating-size-percentage (format-num (/ (* 100 (:package-class-violation-count results)) (:packages-total results)))
          whitespace-percentage (format-num (/ (* 100 (- (:lines-total results) (:non-comment-lines-total results))) (:lines-total results)))]
      
      (print-table [{"Metric" "Total lines",      "Value" (:lines-total results),             "T-shirt size" ""}
                    {"Metric" "Total statements", "Value" (:non-comment-lines-total results), "T-shirt size" "tbd"}
                    {"Metric" "Total methods",    "Value" (:methods-total results), "T-shirt size" "tbd"}
                    {"Metric" "Total classes",    "Value" (:class-total results), "T-shirt size" "tbd"}
                    {"Metric" "Total packages",   "Value" (:packages-total results), "T-shirt size" "tbd"}
                    
                    ])
      (println)
      (print-table [{"Metric" "# Duplicated lines",                        "Total" (:duplicate-lines-total results),         ,"RAG Status" "tbd"}
                    {"Metric" "# Methods with # statements > 30",          "Total" (:methods-len-violation-count results),   ,"RAG Status" "tbd"}
                    {"Metric" "# Methods with cyclomatic complexity > 10", "Total" (:methods-ccn-violation-count results),   ,"RAG Status" "tbd"}
                    {"Metric" "# Methods with parameters > 3",             "Total" (:method-param-violation-count results),  ,"RAG Status" "tbd"}
                    {"Metric" "# Classes with # statements > 300",         "Total" (:class-len-violation-count  results),    ,"RAG Status" "tbd"}
                    {"Metric" "# Packages with classes > 25",              "Total" (:package-class-violation-count results), ,"RAG Status" "tbd"}
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
  


   