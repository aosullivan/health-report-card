(defproject heath-report-card "0.2.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :local-repo "C://Users/Public/Documents/maven/repository"   
  :mirrors {#".+"    "http://gmrepo.gslb.db.com:8481/nexus-webapp/content/groups/artefact-db-aggregator/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [javancss/javancss "30.51"]
                 [net.sourceforge.pmd/pmd "5.1.2"]
                 [org.clojure/tools.logging "0.3.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.zip "0.1.1"]]
  :main health_report_card.core)


