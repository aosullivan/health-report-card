(defproject health-report-card "0.2.2-SNAPSHOT"
  :description "Collect and print simple code metrics through existing static code analysis tools"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :local-repo "C://Users/Public/Documents/maven/repository"   
  :mirrors {#".+"    "http://gmrepo.gslb.db.com:8481/nexus-webapp/content/groups/artefact-db-aggregator/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.codehaus.javancss/javancss "33.54"]
                 [net.sourceforge.pmd/pmd "5.1.2"]
                 [org.clojure/tools.logging "0.3.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [clojure-saxon "0.9.4"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main health_report_card.core)


