# react-repl

[![Clojars Project](https://img.shields.io/clojars/v/town.lilac/react-repl.svg)](https://clojars.org/town.lilac/react-repl)


A library for interacting with a live React application at a REPL.

## Intro

A \"fiber\" in React-lingo is a data representation of the current state of an
element in your application. Each element has a corresponding `type` - a
function, a class, or a built in type - that dictates how to respond to new
props or state dispatches. A fibers data also contains the props and state
last used to render it, any children or sibling fibers, as well as references
to platform objects like DOM nodes.

A \"root\" in React-lingo is the root fiber of an application in your JS
environment. These are created anytime you call e.g. `react-dom/render` or, in
React 18+, `react-dom/createRoot`. You can have multiple roots at a time, for
instance if you call `react-dom/render` multiple times to render different
parts of the page as different applications. Each root gets an associated ID,
an integer that starts at 1.

## Using

To use this library, include `react-repl.preloads` in your developer preloads.
Each time your application is re-rendered, a new tree of fibers is constructed
to represent the new state of the application. When that new fiber tree is
_committed_ - i.e changes are made to what's shown on the screen - the new
tree will be captured and placed in the `react-repl.state/roots` atom.

The functions in this namespace operate on this atom to get the root fiber and
search, display and interact with the fibers that were captured as of the last
render.

`react-repl.core/find-all` will give you the fibers of the last time a
specific component type was rendered. `react-repl.core/find` will return the
first fiber it finds for a component type. You can then use functions like
`react-repl.core/props`, `react-repl.core/children`, and
`react-repl.core/state` to inspect its properties as Clojure data.

`react-repl.core/fiber->map` will return a map corresponding to commonly
looked up information about the fiber.

Note that `state` can sometimes be a deeply nested datastructure. I find that
using `js/console.log` in a browser environment works better for me than
relying on printing at a REPL, but YMMV.
