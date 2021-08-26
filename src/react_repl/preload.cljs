(ns react-repl.preload
  (:require
   [goog.object :as gobj]
   [react-repl.state :as state]))


(defn on-commit-fiber-root
  [id root _maybe-priority-level _did-error?]
  (swap! state/roots assoc id root))


(defn inject-hook!
  []
  (let [hook (gobj/get js/window "__REACT_DEVTOOLS_GLOBAL_HOOK__")
        onCommitFiberRoot (gobj/get hook "onCommitFiberRoot")]
    (gobj/set hook "onCommitFiberRoot"
              (fn [& args]
                (apply on-commit-fiber-root args)
                (this-as this
                  (.apply onCommitFiberRoot this (to-array args)))))))

(inject-hook!)
