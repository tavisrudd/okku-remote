(ns creation.core
  (use okku.core
       common-actors.core))

(defn m-op [op a b]
  {:type :operation :op op :1 a :2 b})
(defn m-tell [actor msg]
  {:type :proxy :target actor :msg msg})

(def printer
  (actor
    (onReceive [{t :type act :target m :msg op :op a :1 b :2 r :result}]
      (dispatch-on [t op]
        [:proxy nil] (! act m)
        [:result :*] (println (format "Add result: %s * %s = %s" a b r))
        [:result :d] (println (format "Sub result: %s / %s = %s" a b r))))))

(defn -main [& args]
  (let [as (actor-system "CreationApplication" :port 2554)
        la (spawn printer :in as)
        ra (spawn advanced-calculator :in as
                  :name "created"
                  :deploy-on "akka://CalculatorApplication@127.0.0.1:2552")]
    (while true
      (.tell la (m-tell ra (if (zero? (rem (rand-int 100) 2))
                             (m-op :* (rand-int 100) (rand-int 100))
                             (m-op :d (rand-int 10000) (inc (rand-int 99))))))
      (try (Thread/sleep 2000)
        (catch InterruptedException e)))))
