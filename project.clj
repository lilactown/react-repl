(defproject org.clojars.lilactown/react-repl "0.0.1"
  :description "Tools for interacting with a React app from a ClojureScript REPL"
  :url "https://github.com/lilactown/react-repl"
  :license {:name "Apache2 License"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :source-paths ["src"]
  :dependencies [[cljs-bean "1.5.0"]]
  :deploy-repositories [["snapshots" {:sign-releases false :url "https://clojars.org"}]])
