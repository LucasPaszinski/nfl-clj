(ns nfl.xtdb
  (:require [xtdb.api :as xt]))

(def node (xt/start-node {}))

(def rush-ord-by-player-name
  {:find '[(pull e [*]) n]
   :where '[[e :player/name n]]
   :order-by '[[n :asc]]})

(defn- where-find-player-name-q [name]
  (let [re (re-pattern (str "(?i)" name))]
    ['[e :player/name n]
     [(list 're-find re 'n)]]))

(defn find-by-player-name [name]
  (xt/q (xt/db node)
        {:find '[(pull e [*])]
         :where (where-find-player-name-q name)}))

(defn- add-pag [q curr-page per-page]
  (let [limit per-page
        offset (* per-page (dec curr-page))]
    (merge q {:limit limit :offset offset})))

(defn query-first [q]
  (->> (xt/q (xt/db node) q) (map first)))

(defn rushes [] 
  (query-first rush-ord-by-player-name))

(defn rushes-pag [curr-page per-page]
  (->>  (add-pag rush-ord-by-player-name curr-page per-page)
        (query-first)))

(defn xt-where-q [q where-q]
  (update-in q [:where] #(into [] (concat % where-q))))

(defn find-by-player-name-pag [name curr-page per-page]
  (-> (add-pag rush-ord-by-player-name curr-page per-page)
      (xt-where-q (where-find-player-name-q name))
      (query-first)))