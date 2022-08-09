(ns nfl.handler
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :refer [response]]
            [reitit.ring :as rr]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.parameters :as parameters]
            [nfl.xtdb :as db])
  (:gen-class))

(defn get-query-params
  ([req name] (get-query-params req name nil))
  ([req name default] (-> req (get :query-params) (get name default))))

(defn search-by [req]
  (if-let [name (get-query-params req "name")]
    (response (db/find-by-player-name name))
    (response {:error "provide a query like ?name=Lucas"})))

(defn rushes [req]
  (let [page (read-string (get-query-params req "page" "1"))
        per-page (read-string  (get-query-params req "per-page" "25"))]
    (response (db/rush-pag page per-page))))

(def routes [["/search-by" search-by]
             ["/rushes"  rushes]])

(def app
  (-> (rr/router routes {:data {:muuntaja m/instance
                                :middleware [parameters/parameters-middleware
                                             muuntaja/format-response-middleware
                                             exception/exception-middleware
                                             muuntaja/format-request-middleware]}})
      (rr/ring-handler)))

(defn start [] (jetty/run-jetty #'app {:port 3000 :join? false}))

(start)

(app {:request-method :get
      :uri "/search-by"
      :query-params {:name "Lu"}})

(app {:request-method :get
      :uri "/rushes"
      :query-params {:page 1 :per-page 25}})