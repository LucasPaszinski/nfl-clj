(ns nfl.xtdb
  (:require [xtdb.api :as xt]))

(def node (xt/start-node {}))

(defn- where-find-player-name-q [name]
  (let [re (re-pattern (str "(?i)" name))]
    ['[e :player/name n]
     [(list 're-find re 'n)]]))

(where-find-player-name-q "Luc")

(defn find-by-player-name [name]
  (xt/q (xt/db node)
        {:find '[(pull e [*])]
         :where (where-find-player-name-q name)}))

(find-by-player-name "Lu")


(defn- rush-paginated-q [curr-page per-page]
  (let [limit per-page
        offset (* per-page (dec curr-page))]
    {:find '[n (pull e [*])]
     :where '[[e :player/name n]
              [e :xt/id id]]
     :order-by '[[n :asc]]
     :limit limit
     :offset offset}))

(defn do-q-pag [q]
  (->> (xt/q (xt/db node) q)
       (map (fn [[_name e]] e))))

(defn rush-pag [curr-page per-page]
  (->>  (rush-paginated-q curr-page per-page)
        (do-q-pag)))

(defn xt-where-q [q where-q]
  (update-in q [:where] #(into [] (concat % where-q))))

(defn find-by-player-name-pag [name curr-page per-page]
  (-> (rush-paginated-q curr-page per-page)
      (xt-where-q (where-find-player-name-q name))
      (do-q-pag)))