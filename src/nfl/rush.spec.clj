(ns nfl.rush.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

(s/def :player/name string?)
(s/def :player/pos (s/and string? #(>= 2 (count %))))
(s/def :player/team string?)
(s/def :rush/avg number?)
(s/def :rush/att pos-int?)
(s/def :rush/att-g (s/or :fp (s/and float? pos?) :ip pos-int?))
(s/def :rush/td (s/or :pos pos-int? :zero zero?))
(s/def :rush/first (s/or :pos pos-int? :zero zero?))
(s/def :rush/first-percent (s/or :pos (s/or :fp (s/and float? pos?) :ip pos-int?) :zero zero?))
(s/def :rush/fum (s/or :pos pos-int? :zero zero?))
(s/def :rush/lng int?)
(s/def :rush/n20+ (s/or :pos pos-int? :zero zero?))
(s/def :rush/n40+ (s/or :pos pos-int? :zero zero?))
(s/def :rush/touchdown? boolean?)
(s/def :rush/yds int?)
(s/def :rush/yds-g number?)

(s/def ::rush (s/keys :req [:player/name
                            :player/pos
                            :player/team
                            :rush/avg
                            :rush/att
                            :rush/att-g
                            :rush/td
                            :rush/first
                            :rush/first-percent
                            :rush/fum
                            :rush/lng
                            :rush/n20+
                            :rush/n40+
                            :rush/touchdown?
                            :rush/yds
                            :rush/yds-g]))

(s/def ::rushes (s/coll-of ::rush :distinct true))

(def rush {:rush/first-percent 0,
           :rush/avg 3.6,
           :rush/td 0,
           :rush/n20+ 0,
           :rush/lng 8,
           :rush/att-g 0.3,
           :player/pos "WR",
           :rush/yds 18,
           :player/name "Adam Humphries",
           :rush/first 0,
           :rush/n40+ 0,
           :player/team "TB",
           :rush/att 5,
           :xt/id #uuid "7052ec34-3b7d-453e-9dae-be0db995b431",
           :rush/yds-g 1.2,
           :rush/touchdown? false,
           :rush/fum 0})

(s/conform ::rush rush)
(s/explain ::rush rush)

(s/conform ::rushes [rush])


(g/sample (s/gen ::rush))