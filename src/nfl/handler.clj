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

(s/def :name/query-params (s/keys :req-un [::name]))
(s/def :name-pag/query-params (s/keys :req-un [::name ::page ::per-page]))
(s/def :pag/query-params (s/keys :req-un [::page ::per-page]))

(s/def :search-by/req (s/or :pag (s/keys :req-un [:name-pag/query-params])
                            :full (s/keys :req-un [:name/query-params])))

;; (s/fdef search-by
;;   :args :search-by/req
;;   :ret)


(defn search-by [req]
  (let [name (get-query-params req :name)
        page (get-query-params req :page)
        per-page (get-query-params req :per-page)]
    (cond
      (and name page per-page) (response (db/find-by-player-name-pag name (read-string page) (read-string per-page)))
      name (response (db/find-by-player-name name))
      :else (response {:error "provide a query like ?name=Lucas"}))))

(s/fdef rushes
  :args (s/or :partial map?
              :complete (s/keys :req-un [:pag/query-params])))
(defn rushes [req]
  (let [page (get-query-params req :page)
        per-page (get-query-params req :per-page)]
    (clojure.pprint/pprint [req (:params page) page per-page])
    (if (and page per-page)
      (response (db/rushes-pag (read-string page) (read-string per-page)))
      (response (db/rushes)))))

(db/rushes)

(defn ->sort-col [sorter]
  (cond
    (= sorter "yds") :rush/yds
    (= sorter "lng") :rush/lng
    (= sorter "td") :rush/td
    :else :player/name))

(defn ->sort-ord [ord]
  (if (= ord "asc") 
    :asc 
    :desc))

(defn query [req]
  (let [name (get-query-params req :name)
        sort-ord (get-query-params req :sort-ord)
        sort-col (get-query-params req :sort-col)
        page (get-query-params req :page)
        per-page (get-query-params req :per-page)]
    (response (db/query :name name
                        :curr-page (Integer/parseInt page)
                        :per-page (Integer/parseInt per-page)
                        :sort-ord (->sort-ord sort-ord)
                        :sort-col (->sort-col sort-col)))))



(defn keyword-map [s-map]
  (into {} (map (fn [[k v]] [(keyword k) v])) s-map))

(defn middleware-keyword-query-params [handler & _]
  (fn [req]
    (handler (assoc req :query-params (keyword-map (:query-params req))))))


(defn middleware-keyword-params [handler & _]
  (fn [req]
    (handler (assoc req :params (keyword-map (:params req))))))

(def app
  (-> (rr/router [["/search-by" search-by]
                  ["/rushes"  rushes]
                  ["/query" query]]
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