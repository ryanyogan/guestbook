(ns guestbook.test.db.core
  (:require [guestbook.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [guestbook.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'guestbook.config/env
      #'guestbook.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-messages
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [timestamp (java.util.Date.)]
      (is (= 1 (db/save-message!
                t-conn
                {:name "Ryan"
                 :message "Do I work?"
                 :timestamp timestamp}
                {:connection t-conn})))
      (is (=
           {:name "Ryan"
            :message "Do I work?"
            :timestamp timestamp}
           (-> (db/get-messages t-conn {})
               (first)
               (select-keys [:name :message :timestamp])))))))
