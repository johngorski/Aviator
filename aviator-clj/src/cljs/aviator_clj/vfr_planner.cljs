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
  (let [wca-rad (Math/asin (/ (deg-to-rad (- wind-dir-deg true-course-deg)) airspeed-kt))]
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

;; TODO: probably better alternatives now with Reagent
;;jogo.calculate = function () { "use strict";
;;                              var flightPlan, i, wcaGSPair, leg, remainingDist, remainingFuelBurn, legAltitude, cruiseBurn;
;;                              flightPlan = jogo.flightPlanFormToObject(); 
;;                              for (i = 0; i < flightPlan.legs.length; i += 1) {
;;                                     leg = flightPlan.legs[i];
;;                                     // Estimate standard temperature for leg altitude
;;                                     if (legAltitude = parseFloat(leg.altitude)) {
;;                                                                                  leg.std_tmp_c = 15 - (legAltitude * 2 / 1000);
;;                                                                                  }
;;                                     // apply winds to get wind correction angle and ground speed for each leg
;;                                     wcaGSPair = jogo.applyWinds(leg.tc, flightPlan.ktas, leg.wind_dir, leg.wind_speed);
;;                                     leg.wca = wcaGSPair.wcaDeg;
;;                                     leg.gs_est = wcaGSPair.gsKT;
;;                                     // calculate ETE based on distance and ground speed
;;                                     leg.ete = 60 * leg.leg_dist / leg.gs_est;
;;                                     // calculate fuel burn based on burn rate and ETE
;;                                     leg.leg_fuel = leg.ete * flightPlan.gph / 60;
;;                                     // calculate true and magnetic headings
;;                                     leg.th = jogo.normalizedCompassHeading(parseFloat(leg.tc) + leg.wca);
;;                                     leg.mh = jogo.normalizedCompassHeading(parseFloat(leg.th) + parseFloat(leg['var']));
;;                                     }
;;
;;                              remainingDist = 0;
;;                              remainingFuelBurn = 0;
;;                              // If the cruise burn is known, include an extra 45 minutes
;;                              if (cruiseBurn = parseFloat(flightPlan.gph)) {
;;                                                                            remainingFuelBurn = cruiseBurn * 0.75;
;;                                                                            }
;;                              // calculate distance and fuel burn remaining
;;                              for (i = flightPlan.legs.length - 1; i >= 0; i -= 1) {
;;                                     leg = flightPlan.legs[i];
;;                                     leg.remaining_dist = remainingDist;
;;                                     remainingDist += parseFloat(leg.leg_dist);
;;                                     leg.remaining_fuel = remainingFuelBurn;
;;                                     remainingFuelBurn += parseFloat(leg.leg_fuel);
;;                                     }
;;
;;                              // display the results on the form
;;                              jogo.displayFlightPlan(flightPlan);
;;                              };

;; TODO: Also probably a pretty cool Reagent alternative
;;jogo.addFlightPlanRow = function () { "use strict";
;;                                     $('#vfr_plan tr[class="trip_leg"]:last').clone(true).insertAfter('#vfr_plan tr[class="trip_leg"]:last');
;;                                     $('#vfr_plan tr[class="trip_leg"]:last input').each(function () { this.value = ""; });
;;                                                                                                      };
;;
;;                                                                                                  jogo.clearFlightPlan = function () { "use strict";
;;                                                                                                                                      var i, flightPlan;
;;                                                                                                                                      flightPlan = document.flight_plan;
;;
;;                                                                                                                                      for (i = flightPlan.waypoint.length - 2; i >= 1; i -= 1) {
;;                                                                                                                                             $('#vfr_plan tr[class="trip_leg"]:eq(' + i + ')').remove();
;;                                                                                                                                             }
;;
;;                                                                                                                                      jogo.displayFlightPlan({legs : [{}, {}]});
;;                                                                                                                                      };
;;
;;                                                                                                  // return false to avoid a second page reload
;;                                                                                                  $(document).ready(function () { "use strict";
;;                                                                                                                                 jogo.loadFlightPlans();
;;                                                                                                                                 $('#add').click(function () {
;;                                                                                                                                                              jogo.addFlightPlanRow();
;;                                                                                                                                                              return false;
;;                                                                                                                                                              });
;;                                                                                                                                 
;;                                                                                                                                 $('.calculated').each(function () {
;;                                                                                                                                                                    this.readOnly = true;
;;                                                                                                                                                                    });
;;                                                                                                                                 
;;                                                                                                                                 $('.direction').each(function () {
;;                                                                                                                                                                   this.size = 3;
;;                                                                                                                                                                   this.maxLength = 3;
;;                                                                                                                                                                   });
;;                                                                                                                                 
;;                                                                                                                                 $('.altitude').each(function () {
;;                                                                                                                                                                  this.size = 5;
;;                                                                                                                                                                  this.maxLength = 5;
;;                                                                                                                                                                  });
;;
;;                                                                                                                                 $('button[class="remove_leg"]').click(function () {
;;                                                                                                                                                                                    $(this).parent().parent().remove();
;;                                                                                                                                                                                    return false;
;;                                                                                                                                                                                    });
;;                                                                                                                                 
;;                                                                                                                                 $('input[name="ata"]').click(function () {
;;                                                                                                                                                                           var now = new Date();
;;                                                                                                                                                                           this.value = jogo.toZulu(now);
;;                                                                                                                                                                           });
;;                                                                                                                                 
;;                                                                                                                                 $('#calculate').click(function () {
;;                                                                                                                                                                    jogo.calculate();
;;                                                                                                                                                                    return false;
;;                                                                                                                                                                    });
;;                                                                                                                                 
;;                                                                                                                                 $('#save').click(function () {
;;                                                                                                                                                               jogo.saveFlightPlan();
;;                                                                                                                                                               return false;
;;                                                                                                                                                               });
;;                                                                                                                                 
;;                                                                                                                                 $('#load').click(function () {
;;                                                                                                                                                               jogo.loadFlightPlan(document.flight_plan.saved_plans.value);
;;                                                                                                                                                               return false;
;;                                                                                                                                                               });
;;                                                                                                                                 
;;                                                                                                                                 $('#remove').click(function () {
;;                                                                                                                                                                 jogo.removeFlightPlan(document.flight_plan.saved_plans.value);
;;                                                                                                                                                                 return false;
;;                                                                                                                                                                 });
;;                                                                                                                                 
;;                                                                                                                                 $('#new_plan').click(function () {
;;                                                                                                                                                                   jogo.clearFlightPlan();
;;                                                                                                                                                                   return false;
;;                                                                                                                                                                   });
;;                                                                                                                                 });
