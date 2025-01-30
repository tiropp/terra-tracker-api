(ns terra-tracker-api.letters
  (:gen-class)
  (:require [schema.core :as s]
            ;[ring.swagger.swagger2 :as rs]
            ))

;; (s/defschema LetterSender {:name s/Str
;;                            :street s/Str
;;                            :place s/Str
;;                            :zip s/Str
;;                            :telephone s/Str
;;                            :mobile s/Str
;;                            :email s/Str})
;; (s/defschema Letter {:sender LetterSender})

;; (s/with-fn-validation
;;   rs/swagger-json
;;   {:info {:version "1.0.0"
;;           :title "Epistula API"
;;           }
;;    :tags [{:name "LetterSender"
;;            :description "Defines the sending entity of a letter"}
;;           {:name "Letter"
;;            :descriptin "Defines a whole letter"}]
;;    :paths {"/letters" {:post {:summary "Create a letter"
;;                               :description "Create a PDF letter from given request information"
;;                               :tags ["Letter", "LetterSender"]
;;                               :parameters {:path {}
;;                                            :body Letter}
;;                               :response {200 {:description "Letter created"}
;;                                          400 {:description "Letter wasn't created"}}}}}
;;    }
;;   )

(defn post
  "POST /letters api handler"
  [{:keys [parameters]}]
  {:status 200
   :body {;:a (-> parameters :body :sender :name)
          :id (parse-uuid "a9744661-f744-4c21-881f-0da19ddd75fc")}}
  )
