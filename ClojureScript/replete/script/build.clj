(ns script.bootstrap.build
  (:require [clojure.java.io :as io]
    [cljs.build.api :as api]
    [cljs.analyzer]
    [cognitect.transit :as transit])
  (:import [java.io ByteArrayOutputStream FileInputStream]))

(defn write-cache [cache out-path]
  (let [out (ByteArrayOutputStream. 1000000)
        writer (transit/writer out :json)]
    (transit/write writer cache)
    (spit (io/file out-path) (.toString out))))

(println "Building")
(api/build (api/inputs "src")
  {:output-dir         "out"
   :output-to          "out/main.js"
   :optimizations      :none
   :static-fns         true
   :optimize-constants false
   :dump-core          false})
(println "Done building")

(defn copy-source
  [path]
  (spit (str "out/" path)
    (slurp (io/resource path))))

(copy-source "cljs/test.cljc")
(copy-source "cljs/spec.cljc")
(copy-source "cljs/spec/test.cljc")
(copy-source "cljs/spec/impl/gen.cljc")
(copy-source "cljs/analyzer/api.cljc")
(copy-source "clojure/template.clj")
(copy-source "cljs/core/async/macros.cljc")
(copy-source "cljs/core/async/impl/ioc_macros.clj")

(let [res (io/resource "cljs/core.cljs.cache.aot.edn")
      cache (read-string (slurp res))]
  (doseq [key (keys cache)]
    (write-cache (key cache) (str "out/cljs/core.cljs.cache.aot." (munge key) ".json"))))

(let [res "out/cljs/core$macros.cljc.cache.json"
      cache (transit/read (transit/reader (FileInputStream. res) :json))]
  (doseq [key (keys cache)]
    (write-cache (key cache) (str "out/cljs/core$macros.cljc.cache." (munge key) ".json"))))

(System/exit 0)
