(ns nfl.xtdb
  (:require [xtdb.api :as xt]
            [clojure.pprint :as p]
            [clojure.spec.alpha :as s]))

(def node (xt/start-node {}))

(defn- xt-q-append [q key statement]
  (update-in q [key] #(into [] (concat % statement))))

(defn- xt-q-replace [q key value]
  (update-in q [key] (fn [_] value)))


(defn- add-pag [q curr-page per-page]
  (let [limit per-page
        offset (* per-page (dec curr-page))]
    (merge q {:limit limit :offset offset})))

(defn- query-first [q]
  (->> (xt/q (xt/db node) q) (map first)))

(defn- filter-by-name [q name]
  (let [re (re-pattern (str "(?i)" name))]
    (xt-q-append q :where ['[e :player/name n]
                           [(list 're-find re 'n)]])))

(defn- sort-by [q sort-col sort-ord]
  (-> (xt-q-append q :find ['s])
      (xt-q-append :where [['e (or sort-col :player/name) 's]])
      (xt-q-replace :order-by [['s (or sort-ord :asc)]])))

(s/def ::page integer?)
(s/def ::per-page integer?)
(s/def ::name string?)
(s/def ::sort-col (s/or :rush/yds :rush/lng :rush/td :player/name))
(s/def ::sort-ord (s/or :asc :desc))
(s/def ::query (s/keys :opt-un [::name ::page ::per-page ::sort-col ::sort-ord]))

(s/fdef query
  :args ::query
  :ret :nfl.rush.spec/rushes)

(defn query [& {:keys [name curr-page per-page sort-col sort-ord]}]
  (let [q '{:find [(pull e [*])] :where [[e :player/name n]]}
        q (sort-by q sort-col sort-ord)
        q (if name (filter-by-name q name) q)
        q (if (and curr-page per-page) (add-pag q curr-page per-page) q)]
    (p/pprint q)
    (query-first q)))