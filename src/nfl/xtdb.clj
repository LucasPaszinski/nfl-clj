(ns nfl.xtdb
  (:require [xtdb.api :as xt]
            [clojure.string :as str]
            [muuntaja.core :as m]))

(def db (->> (slurp "./resources/rushing.json")
             (m/decode "application/json")))

;; Real DB
(def node (xt/start-node {}))


(.close node) ;; kill the node if you want to


(defn json->xtdb [rush]
  {:xt/id (java.util.UUID/randomUUID)
   :player/name (:Player rush)
   :player/pos (:Pos rush)
   :player/team (:Team rush)
   :rush/:1st (:1st rush)
   :rush/:1st% (:1st rush)
   :rush/:20+ (:20+ rush)
   :rush/:40+ (:40+ rush)
   :rush/:att (:Att rush)
   :rush/:att-g (:Att/G rush)
   :rush/:td (:TD rush)
   :rush/avg (:Avg rush)
   :rush/fum (:FUM rush)
   :rush/lng (:Lng rush)
   :rush/touchdown? (str/includes? (:Lng rush) "T")
   :rush/yds (:Yds rush)
   :rush/yds-g (:Yds/G rush)})

(def players (map (fn [json] [::xt/put (json->xtdb json)]) db))


(xt/submit-tx node players) ;; seed the database

(defn find-by-player-name [name]
  (xt/q (xt/db node)
        '{:find [(pull e [*])]
          :in [re-name]
          :where [[e :player/name n]
                  [(re-find re-name n)]]}
        (re-pattern (str "(?i)" name))))

(defn rush-pag [curr-page per-page]
  (let [limit per-page
        offset (* per-page (dec curr-page))]
    (->> (xt/q (xt/db node)
               {:find '[name (pull e [*])]
                :where '[[e :player/name name]
                         [e :xt/id id]]
                :order-by '[[name :asc]]
                :limit limit
                :offset offset})
         (map (fn [[_name e]] e)))))

(rush-pag 1 10)

