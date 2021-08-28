(ns react-repl.dom
  "A namespace for functions that pertain specifically to react-dom
  applciations."
  (:require
   [goog.dom :as gdom]
   [goog.object :as gobj]
   [react-repl.core :as rr]))


(defn parent-node
  "Returns the nearest DOM node in a parent of the fiber."
  [fiber]
  (loop [fiber (rr/parent fiber)]
    (cond
      (gdom/isElement (gobj/get fiber "stateNode"))
      (gobj/get fiber "stateNode")

      (some? (rr/parent fiber))
      (recur (rr/parent fiber)))))


(defn -children-nodes
  [fiber]
  (cond
    (gdom/isElement (gobj/get fiber "stateNode"))
    (gobj/get fiber "stateNode")

    (rr/has-child? fiber)
    (flatten (map -children-nodes (rr/children fiber)))))


(defn children-nodes
  "Returns all of the nearest DOM nodes in the children of the fiber."
  [fiber]
  (when (rr/has-child? fiber)
    (flatten (map -children-nodes (rr/children fiber)))))


(defn child-node
  "Returns the nearest DOM node in the children of the fiber."
  [fiber]
  (first (children-nodes fiber)))


(defn node
  "Returns either the DOM node currently associated with the fiber, or the first
  child DOM node, or the first parent DOM node."
  [fiber]
  (or (gobj/get fiber "stateNode") (child-node fiber) (parent-node fiber)))
