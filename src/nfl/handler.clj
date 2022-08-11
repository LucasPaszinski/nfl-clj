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
  (let [name (get-query-params req "name")
        page (get-query-params req "page")
        per-page (get-query-params req "per-page")]
    (cond
      (and name page per-page) (response (db/find-by-player-name-pag name (read-string page) (read-string per-page)))
      name (response (db/find-by-player-name name))
      :else (response {:error "provide a query like ?name=Lucas"}))))

(defn rushes [req]
  (let [page (get-query-params req "page")
        per-page (get-query-params req "per-page")]
    (if (and page per-page) 
      (response (db/rushes-pag (read-string page) (read-string per-page)))
      (response (db/rushes)))))

(db/rushes)

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