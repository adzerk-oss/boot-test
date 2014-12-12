(set-env!
 :src-paths    #{"src"}
 :dependencies '[[org.clojure/clojure       "1.6.0"       :scope "provided"]
                 [boot/core                 "2.0.0-pre28" :scope "provided"]
                 [tailrecursion/boot-useful "0.1.3"       :scope "test"]])

(require '[tailrecursion.boot-useful :refer :all]
         '[adzerk.boot-test          :refer [test]])

(def +version+ "1.0.1")

(useful! +version+)

(task-options!
 pom  [:project     'adzerk/boot-test
       :version     +version+
       :description "Run some tests in boot!"
       :url         "https://github.com/adzerk/boot-test"
       :scm         {:url "https://github.com/adzerk/boot-test"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}]
 test [:namespaces '#{adzerk.boot-test.test}])
