(ns nfl.data-migration.seed
  (:require [muuntaja.core :as m]
            [nfl.xtdb :as db]
            [xtdb.api :as xt]
            [clojure.string :as str]))

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

(defn seed-db [] 
  (->> (slurp "./resources/rushing.json")
       (m/decode "application/json")
       (map (fn [rush] [::xt/put (json->xtdb rush)]))
       (xt/submit-tx db/node)))

(seed-db)

