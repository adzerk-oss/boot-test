(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.6.0"     :scope "provided"]
                 [boot/core           "2.0.0-rc1" :scope "provided"]
                 [adzerk/bootlaces    "0.1.4"     :scope "test"]])

(require '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-test :refer [test]])

(def +version+ "1.0.2")

(bootlaces! +version+)

(task-options!
 pom  {:project     'adzerk/boot-test
       :version     +version+
       :description "Run some tests in boot!"
       :url         "https://github.com/adzerk/boot-test"
       :scm         {:url "https://github.com/adzerk/boot-test"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}}
 test {:namespaces '#{adzerk.boot-test.test}})
