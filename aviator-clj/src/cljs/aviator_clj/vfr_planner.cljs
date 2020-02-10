(ns aviator-clj.vfr-planner
  (:require [clojure.string :as string]))

(defn construct-leg-field [name to-string]
  {:name name,
   :toString to-string})

(defn round-to-one-decimal-point [val]
  (Math/round (quot (* 10 val) 10)))

(defn round [n] (Math/round n))

(def leg-fields
  (map (partial apply construct-leg-field)
       [["wind_dir" identity]
        ["wind_speed" identity]
        ["temp" identity]
        ["tc" identity]
        ["th" round]
        ["mh" round]
        ["ch" round]
        ["wca" round]
        ["var" identity]
        ["dev" round]
        ["waypoint" identity]
        ["altitude" identity]
        ["std_tmp_c" identity]
        ["leg_dist" round]
        ["remaining_dist" round]
        ["gs_est" round]
        ["gs_act" round]
        ["ete" round]
        ["ate" identity]
        ["eta" identity]
        ["ata" identity]
        ["leg_fuel" roundToOneDecimalPoint]
        ["remaining_fuel" roundToOneDecimalPoint]]))

(defn two-digit-string [val]
  (let [pct (rem val 100)]
    (str (if (< val 10) "0" "") pct)))

;; Query local storage for all flight plans
;; Allow storage namespacing by prefixing with 'jogo.'
;;(defn load-flight-plans []
;;  ) ;; TODO: Translate

;;jogo.loadFlightPlans = function () { "use strict";
;;                                    var i, key, thisPlan, planList;
;;                                    planList = $('select[name="saved_plans"]');
;;                                    planList.find('option').remove();
;;
;;                                    for (i = 0; i < localStorage.length; i += 1) {
;;                                           key = localStorage.key(i);
;;                                           if (key.match(/^jogo\.[\w\d\s\-]+$/) !== null) {
;;                                                                                           thisPlan = JSON.parse(localStorage[key]);
;;                                                                                           // add plan information as selection options
;;                                                                                           planList.append('<option value="' + thisPlan.title + '">' + thisPlan.title + '</option>');
;;                                                                                           }
;;                                           }
;;                                    };

;; Load flight plan from HTML5 local storage
;; Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
(defn load-flight-plan [in-title]
  (let [plan-key (str "jogo." (string/trim in-title))
        flight-plan (JSON/parse (get js/localStorage plan-key))]
    ;; TODO: probably something better here with react
    (clear-flight-plan)
    (display-flight-plan)))

(defn display-flight-plan [flight-plan]
  ) ;; TODO: This probably looks neat with React, too. As in, I think this is just a (reset!) call
;;jogo.displayFlightPlan = function (flightPlan) { "use strict";
;;                                                var i, f, currentFormField, currentFieldName, currentValue;
;;                                                document.flight_plan.plan_title.value = flightPlan.title || "";
;;                                                document.flight_plan.aircraft_ktas.value = flightPlan.ktas || "";
;;                                                document.flight_plan.aircraft_fuel_burn_gph.value = flightPlan.gph || "";
;;
;;                                                for (i = 0; i < flightPlan.legs.length; i += 1) {
;;                                                       if (i >= document.flight_plan.legmarker.length) { // 1) {
;;                                                                                                                jogo.addFlightPlanRow();
;;                                                                                                                }
;;                                                       for (f = 0; f < jogo.legFields.length; f += 1) {
;;                                                              currentFieldName = jogo.legFields[f].name;
;;                                                              currentFormField = document.flight_plan[currentFieldName][i];
;;                                                              currentValue = jogo.legFields[f].toString(flightPlan.legs[i][currentFieldName]);
;;                                                              currentFormField.value = currentValue || "";
;;                                                              }
;;                                                       }
;;                                                };

(defn remove-flight-plan [title]
  (removeItem js/localStorage (str "jogo." title))
  (load-flight-plans))

;;(defn flight-plan-form-to-object []
;;  ) ;; TODO: Also probably obviated by Reagent
;;jogo.flightPlanFormToObject = function () { "use strict";
;;                                           var f, i, plan = {};
;;
;;                                           plan.title = $.trim(document.flight_plan.plan_title.value);
;;                                           plan.ktas = document.flight_plan.aircraft_ktas.value;
;;                                           plan.gph = document.flight_plan.aircraft_fuel_burn_gph.value;
;;                                           plan.legs = [];
;;                                           
;;                                           // Substitute for "document.flight_plan" yielding an unserializable circular structure
;;                                           // wind_dir wind_speed temp
;;                                           // tc th mh ch wca var dev
;;                                           // waypoint altitude std_tmp_c
;;                                           // leg_dist remaining_dist gs_est gs_act ete ate eta ata
;;                                           // leg_fuel remaining_fuel
;;                                           for (i = 0; i < document.flight_plan.legmarker.length; i += 1) {
;;                                                  plan.legs[i] = {};
;;                                                  for (f = 0; f < jogo.legFields.length; f += 1) {
;;                                                         plan.legs[i][jogo.legFields[f].name] = document.flight_plan[jogo.legFields[f].name][i].value;
;;                                                         }
;;                                                  }
;;                                           return plan;
;;                                           };

;; Load flight plan from HTML5 local storage
;; Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
(comment
(defn save-flight-plan []
  (let [plan (flight-plan-form-to-object)
        plan-key (str "jogo." (.-title plan))]
    (do
      (set! js/localStorage plan-key (JSON/stringify plan))
      (load-flight-plans))))
)

(defn to-zulu [t]
  (str (apply str (map two-digit-string [(.getUTCDate t) (.getUTCDate t) (.getUTCMinutes t)])) "Z"))

(defn rad-to-deg [rad]
  (/ (* 180 rad) Math/PI))

(defn deg-to-rad [deg]
  (/ (* Math.PI deg) 180))

(defn apply-winds [true-course-deg airspeed-kt wind-dir-deg windspeed-kt]
  (let [wca-rad (Math/asin
                 (/ (* windspeed-kt (Math/sin (deg-to-rad (- wind-dir-deg true-course-deg))))
                    airspeed-kt))]
    {:wca-deg (rad-to-deg wca-rad)
     :gs-kt (- (* airspeed-kt (Math/cos wca-rad)) (* windspeed-kt (Math/cos (deg-to-rad (- wind-dir-deg true-course-deg)))))}))

(defn normalized-compass-heading [h]
  (let [n (rem h 360)]
    (if (< n 0) (+ n 360) n)))

(defn standard-temperature-c-for-altitude-ft [altitude-ft]
  (- 15 (/ (* altitude-ft 2) 1000)))

(defn ete-min [leg-dist gs-kt]
  (/ (* 60 leg-dist) gs-kt))

(defn leg-fuel-gal [ete-min burn-gph]
  (/ (* ete-min burn-gph) 60))

(defn th [tc wca-deg]
  (+ tc wca-deg))

(defn mh [th var]
  (+ th var))
