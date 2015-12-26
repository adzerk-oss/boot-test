(ns adzerk.boot-test
  {:boot/export-tasks true}
  (:refer-clojure :exclude [test])
  (:require [boot.pod  :as pod]
            [boot.core :as core]))

(def base-pod-deps
  '[[org.clojure/tools.namespace "0.2.11" :exclusions [org.clojure/clojure]]
    [pjstadig/humane-test-output "0.6.0"  :exclusions [org.clojure/clojure]]])

(defn init [requires fresh-pod]
  (dorun (map (partial pod/require-in fresh-pod) requires))
  (doto fresh-pod
    (pod/with-eval-in
     (require '[clojure.test :as t]
              '[clojure.java.io :as io]
              '[pjstadig.humane-test-output :refer [activate!]]
              '[clojure.tools.namespace.find :refer [find-namespaces-in-dir]])
     (activate!)
     (defn all-ns* [& dirs]
       (distinct (mapcat #(find-namespaces-in-dir (io/file %)) dirs)))
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

(core/deftask run-tests
  "Run clojure.test tests in a pod.

  The --namespaces option specifies the namespaces to test. The default is to
  run tests in all namespaces found in the project.

  The --exclusions option specifies the namespaces to exclude from testing.

  The --filters option specifies Clojure expressions that are evaluated with %
  bound to a Var in a namespace under test. All must evaluate to true for a Var
  to be considered for testing by clojure.test/test-vars."

  [n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   e exclusions NAMESPACE #{sym} "The set of namespace symbols to be excluded from test."
   f filters    EXPR      #{edn} "The set of expressions to use to filter namespaces."
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed."]

  (let [pod-deps (update-in (core/get-env) [:dependencies] into base-pod-deps)
        worker-pods (pod/pod-pool pod-deps :init (partial init requires))]
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (let [worker-pod (worker-pods :refresh)
            namespaces (or (seq namespaces)
                           (pod/with-eval-in worker-pod
                             (all-ns* ~@(->> fileset
                                             core/input-dirs
                                             (map (memfn getPath))))))
            namespaces (remove (or exclusions #{}) namespaces)]
        (if (seq namespaces)
          (let [filterf `(~'fn [~'%] (and ~@filters))
                tmp (core/tmp-dir!)
                summary (pod/with-eval-in worker-pod
                          (doseq [ns '~namespaces] (require ns))
                          (let [ns-results (map (partial test-ns* ~filterf) '~namespaces)]
                            (-> (reduce (partial merge-with +) ns-results)
                                (assoc :type :summary)
                                (doto t/do-report))))
                summary-out-filename "clojure.test.result.edn"]
            (spit (clojure.java.io/file tmp summary-out-filename)
                  (pr-str summary))
            (-> fileset
                (core/add-asset tmp)
                (core/add-meta {summary-out-filename {:clojure.test/result summary}})
                core/commit!))
          (do
            (println "No namespaces were tested.")
            fileset))))))

(core/deftask test
  "Run clojure.test tests in a pod. Throws on test errors or failures.

  The --namespaces option specifies the namespaces to test. The default is to
  run tests in all namespaces found in the project.

  The --exclusions option specifies the namespaces to exclude from testing.

  The --filters option specifies Clojure expressions that are evaluated with %
  bound to a Var in a namespace under test. All must evaluate to true for a Var
  to be considered for testing by clojure.test/test-vars."
  [n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   e exclusions NAMESPACE #{sym} "The set of namespace symbols to be excluded from test."
   f filters    EXPR      #{edn} "The set of expressions to use to filter namespaces."
   r requires   REQUIRES  #{sym} "Extra namespaces to pre-load into the pool of test pods for speed."]
  (comp
    (run-tests :namespaces namespaces
               :exclusions exclusions
               :filters filters
               :requires requires)
    (core/with-pre-wrap
      fileset
      (let [summary (->> fileset
                         core/output-files
                         (core/by-name ["clojure.test.result.edn"])
                         first
                         :clojure.test/result)]
        (if (> (apply + (map summary [:fail :error])) 0)
          (throw (ex-info "Some tests failed or errored" summary))
          fileset)))))
