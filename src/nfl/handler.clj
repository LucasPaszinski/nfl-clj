(ns nfl.handler
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :refer [response]]
            [reitit.ring :as rr]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.parameters :as parameters]
            [nfl.xtdb :as db]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn get-query-params
  ([req name] (get-query-params req name nil))
  ([req name default] (-> req (get :params) (get name default))))

(s/def ::page string?)
(s/def ::per-page string?)
(s/def ::name string?)
(s/def ::sort-col string?)
(s/def ::sort-ord string?)
(s/def ::query (s/keys :opt-un [::name ::page ::per-page ::sort-col ::sort-ord]))

(defn ->sort-col [sorter]
  (cond (= sorter "yds") :rush/yds
        (= sorter "yds") :rush/yds
        (= sorter "lng") :rush/lng
        (= sorter "td") :rush/td
        :else :player/name))

(defn ->sort-ord [ord]
  (if (= ord "asc") :asc :desc))

(s/fdef query
  :args (s/keys :req-un [::query]))

(defn query [req]
  (response (db/query :name (get-query-params req :name)
                      :curr-page (Integer/parseInt (get-query-params req :page))
                      :per-page (Integer/parseInt (get-query-params req :per-page))
                      :sort-ord (->sort-ord (get-query-params req :sort-ord))
                      :sort-col (->sort-col (get-query-params req :sort-col)))))

(defn keyword-map [s-map]
  (into {} (map (fn [[k v]] [(keyword k) v])) s-map))

(defn middleware-keyword-query-params [handler & _]
  (fn [req]
    (handler (assoc req :query-params (keyword-map (:query-params req))))))

(defn middleware-keyword-params [handler & _]
  (fn [req]
    (handler (assoc req :params (keyword-map (:params req))))))

(def app
  (-> (rr/router [["/query" query]]
                 {:data {:muuntaja m/instance
                         :middleware [parameters/parameters-middleware
                                      muuntaja/format-response-middleware
                                      exception/exception-middleware
                                      muuntaja/format-request-middleware
                                      middleware-keyword-query-params
                                      middleware-keyword-params]}})
      (rr/ring-handler)))

(defn start [] (jetty/run-jetty #'app {:port 3000 :join? false}))

(start)

(app {:request-method :get
      :uri "/search-by"
      :params {"name" "Lu" "page" "1" "per-page" "25"}})

(app {:request-method :get
      :uri "/rushes"
      :params {:page 1 :per-page 25}})

(get-query-params {:params {:name "Hey" :page 1 :per-page 25}} :name)