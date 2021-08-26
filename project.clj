(defproject org.clojars.lilactown/react-repl "0.0.1"
  :description "Tools for interacting with a React app from a ClojureScript REPL"
  :url "https://github.com/lilactown/react-repl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v20.html"}
  :source-paths ["src"]
  :dependencies [[cljs-bean "1.5.0"]]
  :deploy-repositories [["snapshots" {:sign-releases false :url "https://clojars.org"}]])
