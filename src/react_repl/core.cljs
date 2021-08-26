(ns react-repl.core
  (:require
   [goog.object :as gobj]
   [cljs-bean.core :as b]
   [react-repl.state :as state])
  (:refer-clojure :exclude [find type]))


(defn current-fiber
  "Gets the current fiber rendered at the root.
  If no root-id is passed in, assumes that it is `1`, which is standard if
  only a single React root exists in the JS environment."
  ([] (current-fiber 1))
  ([id] (gobj/get (get @state/roots id) "current")))


(defn fiber?
  "Returns true or false whether `x` is a React fiber."
  [x]
  (= (clojure.core/type x)
     (clojure.core/type (current-fiber))))


(defn child-fiber
  "Returns the direct child of the fiber."
  [fiber]
  (gobj/get fiber "child"))


(defn has-child?
  "Returns true or false whether the fiber has a child"
  [fiber]
  (some? (child-fiber fiber)))


(defn parent
  "Returns the direct parent of the fiber."
  [fiber]
  (gobj/get fiber "return"))


(defn sibling-fiber
  "Returns the sibling to the right of the fiber."
  [fiber]
  (gobj/get fiber "sibling"))


(defn siblings
  "Returns all siblings to hte right of the fiber."
  [fiber]
  (when (some? fiber)
    (lazy-seq
     (cons fiber (siblings (sibling-fiber fiber))))))


(defn children
  "Returns all of the direct children of this fiber."
  [fiber]
  (siblings (child-fiber fiber)))


(defn all-fibers
  "Returns a seq of all the fibers.
  Optionally provide a root ID in the case that you have multiple React roots
  in the environment."
  ([]
   (tree-seq has-child? children (current-fiber)))
  ([id]
   (tree-seq has-child? children id)))


;;
;; Displaying
;;


(defn type
  "Returns the element type (class, function component, etc.) used to construct
  the fiber."
  [fiber]
  (gobj/get fiber "type"))


(defn display-type
  "Returns a human-readable version of the fiber type."
  [fiber]
  (let [t (type fiber)]
    (cond
      (nil? t) :react/text
      (string? t) t
      (fn? t) t
      (= (clojure.core/type t) js/Symbol) t
      ;; memo, other built in HOC
      :else t)))


(defn- has-hooks?
  [fiber]
  (not (nil? (gobj/get fiber "_debugHookTypes"))))


(defn- hook->map
  [hook-type hook]
  (let [queue (gobj/get hook "queue")]
    (with-meta
      (cond-> {:type hook-type
               :current (gobj/get hook "memoizedState")}
        (not (nil? queue))
        (assoc :dispatch (gobj/get queue "dispatch")))
      {:hook hook
       :hook/type hook-type})))


(defn props
  "Returns the current props of the fiber."
  [fiber]
  (let [p (gobj/get fiber "memoizedProps")]
    (if (string? p)
      ;; text nodes have strings as props
      {:text p}
      (b/bean p))))


(defn state
  "Returns the current state (hooks or class component state) of the fiber."
  [fiber]
  (if (has-hooks? fiber)
    ;; :memoizedState linked list of hooks each w/ :next, :baseState and :memoizedState
    ;; type is in _debugHookTypes
    (let [hook-types (gobj/get fiber "_debugHookTypes")]
      (loop [current (gobj/get fiber "memoizedState")
             hooks (-> (hook->map (first hook-types) current)
                       (vector))
             hook-types (rest hook-types)]
        (if-let [next (gobj/get current "next")]
          (recur next
                 (conj hooks (hook->map (first hook-types) next))
                 (rest hook-types))
          hooks)))
    (-> fiber
        (gobj/get "memoizedState")
        (b/bean))))


(defn set-state
  "Tries to set the state of a fiber constructed out of a class component.
  Returns `true` if fiber is of a valid class component and `nil` if not."
  [fiber f]
  (when-not (has-hooks? fiber)
    (when-some [state-node (gobj/get fiber "stateNode")]
      (when-some [updater (gobj/get state-node "updater")]
        (when-some [set-state (gobj/get updater "enqueueSetState")]
          (set-state state-node f)
          true)))))


(defn fiber->map
  "Returns the fiber as a map for display."
  [fiber]
  (when (some? fiber)
    (with-meta
      {:props (props fiber)
       :type (type fiber)
       :state-node (gobj/get fiber "stateNode")
       :parent (parent fiber)
       :state (state fiber)
       :children (map fiber->map (children fiber))}
      {:fiber fiber})))


#_(defn fiber->hiccup
  "Returns the fiber and children as hiccup for display."
  [fiber]
  (if-not (fiber? fiber)
    fiber
    (with-meta
      (let [props (props fiber)]
        (cond-> [(display-type fiber)]
          (seq (dissoc props :children))
          (conj (dissoc props :children))

          (seq (children fiber))
          (into (map fiber->hiccup (children fiber)))))
      {:fiber fiber})))


;;
;; Hook operations
;;


(defn hook-deps
  "Returns the last rendered deps of a hook, if applicable."
  [hook]
  (case (:type hook)
    ("useEffect"
     "useLayoutEffect"
     "useMemo"
     "useCallback") (second (:current hook))))


(defn hook-dispatch
  "Dispatches a change with a useState or useReducer hook."
  [{:keys [dispatch] :as _hook} & args]
  (apply dispatch args))


;;
;; Querying
;;


(defn find-all
  "Find all currently rendered fibers that use a component type."
  ([type]
   (->> (all-fibers)
        (filter #(= (gobj/get % "type") type))))
  ([type root-id]
   (->> (all-fibers root-id)
        (filter #(= (gobj/get % "type") type)))))


(defn find
  "Find the first fiber currently rendered that use a component type."
  ([type]
   (first (find-all type)))
  ([type root-id]
   (first (find-all type root-id))))
