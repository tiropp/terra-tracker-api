(ns terra-tracker-api.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            ;[compojure.core :as comp]
            ;[compojure.route :as route]
            ;[ring.swagger.swagger-ui :as swag-ui]

            ;; See reitit doc at https://cljdoc.org/d/metosin/reitit/0.7.2
            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.openapi :as openapi]
            [reitit.ring.spec :as spec]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.coercion.schema :as cschema]
            [reitit.dev.pretty :as pretty]
            [muuntaja.core :as m]
            [schema.core :as s]
            [terra-tracker-api.letters :as letters]
            ))

;; Routes
#_(comp/defroutes routes
  (comp/POST "/letters" req (letters/post req))
  (route/not-found {:status 404
                    :body "Not found"
                    :headers {"Content-Type" "text/plain"}}))

(defn handler [_]
  {:status 200
   :body "ok"})

(s/defschema LetterSender {:name s/Str
                           :street s/Str
                           :place s/Str
                           :zip s/Str
                           :telephone s/Str
                           :mobile s/Str
                           :email s/Str})
(s/defschema Letter {:sender LetterSender})

(def routes
  ["/api" {}
   ["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "Epistula API"
                            :description "Swagger doc for Epistula API"
                            :version "0.0.1"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/openapi.json"
    {:get {:no-doc true
           :openapi {:info {:title "Epistual API"
                            :description "Swagger doc for Epistula API"
                            :version "0.0.1"}
                     :tags [{:name "api"
                             :description "Epsitula API"}]}
           :handler (openapi/create-openapi-handler)}}]

   ["/test"
    {:get {:handler handler
           :summary "Test api"}}]

   ["/letters"
    {:post {:handler letters/post
            :summary "Create a PDF letter from tiven request information"
            :coercion cschema/coercion
            :parameters {:path {}
                         :body Letter}
            :responses {200 {:description "Letter created"
                             :body {:id s/Uuid}}
                        400 {:description "Letter wasn't created"}}}}
    ]
   ])


(def options
  {;; enable spec validation for route data
   :validate spec/validate
   :exception pretty/exception
   :data {:muuntaja m/instance
          :middleware [
                       ;; swagger and openapi
                       swagger/swagger-feature
                       openapi/openapi-feature
                       ;; query-parameters and form-parameters
                       parameters/parameters-middleware
                       ;; content-negotiation
                       muuntaja/format-negotiate-middleware
                       ;; encoding response body
                       muuntaja/format-response-middleware
                       ;; exception handling
                       exception/exception-middleware
                       ;; decoding request body
                       muuntaja/format-request-middleware
                       ;; coercing response bodys
                       coercion/coerce-response-middleware
                       ;; coercing request parameters
                       coercion/coerce-request-middleware
                       ;; multipart
                       multipart/multipart-middleware
                       ]}})

;; Application
#_ (def app
     (-> (fn [req] (routes req))
         ring/ring-handler
                                        ;wrap-keyword-params
                                        ;wrap-params
         ))

(def app
  (ring/ring-handler
   (ring/router routes options)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/api"
      :config {:validatorUrl nil
               :urls [{:name "swagger" :url "swagger.json"}
                      {:name "openapi" :url "openapi.json"}]
               :urls.primaryName "openapi"
               :operationsSorter "alpha"}})
    (ring/create-default-handler))))


;; Run/stop jetty server
(defonce server (atom nil))

(defn- start-server []
  "Start web service"
  (let [port 3001]
    (reset! server
            (jetty/run-jetty #'app
                             {:port port
                              :join? false ; don't block main thread
                              }))
    (println "Server running on port" port)))

(defn- stop-server []
  "Stop web service"
  (when-some [srv @server]
    (.stop srv)
    (reset! server nil)))

(defn -main
  [& args]
  (start-server))
