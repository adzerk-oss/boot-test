(ns adzerk.boot-test
  {:boot/export-tasks true}
  (:refer-clojure :exclude [test])
  (:require [boot.pod  :as pod]
            [boot.core :as core]))

(def pod-deps
  '[[pjstadig/humane-test-output "0.6.0" :exclusions [org.clojure/clojure]]])

(defn init [fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
     (require '[clojure.test :as t]
              '[pjstadig.humane-test-output :refer [activate!]])
     (activate!)
     (defn test-ns* [pred ns]
       (binding [t/*report-counters* (ref t/*initial-report-counters*)]
         (let [ns-obj (the-ns ns)]
           (t/do-report {:type :begin-test-ns :ns ns-obj})
           (t/test-vars (filter pred (vals (ns-publics ns))))
           (t/do-report {:type :end-test-ns :ns ns-obj}))
         @t/*report-counters*)))))

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(if ((loaded-libs) 'boot.user)
  (ns-unmap 'boot.user 'test))

(core/deftask test
  "Run clojure.test tests in a pod."
  [n namespaces NAMESPACE #{sym} "Symbols of the namespaces to run tests in."
   f filters EXPR #{any} "Clojure expressions that are evaluated with % bound to a Var in a namespace under test. All must evaluate to true for a Var to be considered for testing by clojure.test/test-vars."]
  (let [worker-pods (pod/pod-pool (assoc-in (core/get-env) [:dependencies] pod-deps) :init init)]
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (if (seq namespaces)
        (let [filterf `(~'fn [~'%] (and ~@filters))
              summary (pod/with-eval-in (worker-pods :refresh)
                        (doseq [ns '~namespaces] (require ns))
                        (let [ns-results (map (partial test-ns* ~filterf) '~namespaces)]
                          (-> (reduce (partial merge-with +) ns-results)
                              (assoc :type :summary)
                              (doto t/do-report))))]
          (when (> (apply + (map summary [:fail :error])) 0)
            (throw (ex-info "Some tests failed or errored" summary))))
        (println "No namespaces were tested."))
      fileset)))
