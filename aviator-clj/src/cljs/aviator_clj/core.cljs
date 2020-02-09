(ns aviator-clj.core
  (:require
   [aviator-clj.vfr-planner :as vfr-planner]
   [clojure.edn :as edn]
   [reagent.core :as reagent :refer [atom]]))

(defn sz [size m]
  (merge {:type "text" :maxLength size :size size} m))

(defn trip-leg [idx plan]
  (fn []
    (letfn [(bind [field input]
              (merge input
                     {:value (get-in @plan [:legs idx field])
                      :on-change #(swap! plan assoc-in [:legs idx field] (-> % .-target .-value))}))
            (get-field [f]
              (js/parseFloat (get-in @plan [:legs idx f])))]
      (let [tc (get-field :tc)
            {:keys [gs-kt wca-deg]} (vfr-planner/apply-winds tc (js/parseFloat (:ktas @plan)) (get-field :wind-dir) (get-field :wind-speed))
            th (+ tc wca-deg)
            var (get-field :var)
            mh (+ th var)
            leg-dist (get-field :leg-dist)
            ete-min (vfr-planner/ete-min leg-dist gs-kt)
            fuel-burn-gph (js/parseFloat (:fuel-burn-gph @plan))
            leg-fuel-gal (vfr-planner/leg-fuel-gal ete-min fuel-burn-gph)
            ignore (js/console.log (pr [tc gs-kt wca-deg var mh leg-dist ete-min fuel-burn-gph leg-fuel-gal]))
            ]
        [:<>
         [:tr {:style {:border-top "1px solid"}}
          [:td
           {:rowSpan 2, :style {:border-right "1px solid"}}
           [:a.remove_leg
            {:on-click (fn [] (swap! plan update :legs #(vec (concat (subvec % 0 idx) (subvec % (inc idx))))))}
            "Remove"]]
          [:td [:input.direction (sz 3 (bind :wind-dir {:name "wind_dir"}))]]
          [:td {:style {:border-right "1px solid"}} [:input (sz 3 (bind :wind-speed {:name "wind_speed"}))]]
          [:td [:input.direction (sz 3 (bind :tc {:name "tc" :placeholder "TC"}))] "°"]
          [:td (Math/round th) "°"]
          [:td (Math/round mh) "°"]
          [:td {:rowSpan 2, :style {:border-right "1px solid"}} [:input.direction.calculated (sz 3 {:name "ch" :placeholder "CH"})]]
          [:td {:colSpan 2} [:input (bind :waypoint {:type "text" :name "waypoint"})]]
          [:td {:style {:border-left "1px solid"}} [:input (sz 4 (bind :leg-dist {:name "leg_dist"}))]]
          [:td {:style {:border-left "1px solid"}} (Math/round gs-kt)]
          [:td {:style {:border-left "1px solid"}} (Math/round ete-min)]
          [:td [:input.calculated (sz 6 {:name "eta"})] "Z"]
          [:td {:style {:border-left "1px solid"}} (.toFixed leg-fuel-gal 1)]]
         [:tr {:style {:border-bottom "1px solid"}}
          [:td {:colSpan 2, :style {:border-right "1px solid"}} [:input (sz 3 (bind :temp {:name "temp"}))]]
          [:td (Math/round wca-deg)]
          [:td [:input.direction (sz 3 (bind :var {:name "var" :placeholder "Var"}))]]
          [:td [:input.direction.calculated (sz 3 {:name "dev" :placeholder "Dev"})]]
          [:td "alt " [:input.altitude (sz 5 (bind :altitude {:name "altitude"}))] "ft"]
          [:td
           "(std tmp "
           (if-let [alt (edn/read-string (get-in @plan [:legs idx :altitude]))]
             (Math/round (vfr-planner/standard-temperature-c-for-altitude-ft alt))
             "____")  "℃)"]
          [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "remaining_dist"})]]
          [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 3 {:name "gs_act"})]]
          [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "ate"})]]
          [:td [:input (sz 6 {:name "ata" :readOnly "readonly"})] "Z"]
          [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "remaining_fuel"})]]]]))))

(defn flight-plan []
  (let [plan (atom {:ktas "100", :legs [{:wind-dir "090", :wind-speed "25", :tc "180", :var "-17", :waypoint "way", :altitude "5500", :leg-dist "18"} {:waypoint "point", :wind-dir "190", :wind-speed "20", :tc "100", :var "-15", :altitude "4500", :leg-dist "22"}], :fuel-burn-gph "9.1", :start "KPAE"})
        bind (fn [field input]
               (merge input
                      {:value (get @plan field)
                       :on-change #(swap! plan assoc field (-> % .-target .-value))}))]
    (do
      (add-watch plan :log (fn [key a old new] (js/console.log (str new))))
      (fn []
        [:form {:name "flight_plan"}
         [:table#aircraft_cruise_profile
          [:tbody
           [:tr
            [:td [:input (bind :plan-title {:type "text" :name "plan_title"})]]
            [:td [:button#save "Save flight plan"]]
            [:td [:button#new_plan "New plan"]]]
           [:tr
            [:td [:select {:name "saved_plans" :multiple false}]]
            [:td
             [:button#load "Load flight plan"][:br]
             [:button#remove "Remove flight plan"]]]
           [:tr [:th "KTAS"][:td [:input#aircraft_ktas.number (sz 4 (bind :ktas {:type "text"}))]]]
           [:tr [:th "Fuel burn (gph)"][:td [:input#aircraft_fuel_burn_gph.number (sz 4 (bind :fuel-burn-gph {:type "text"}))]]]
           [:tr [:th "Magnetic deviance"][:td [:table]]]]]
         [:table#vfr_plan.subgrid {:style {:border "1px solid"} :cellPadding "4"}
          [:thead
           [:tr
            [:th {:rowSpan 3}]
            [:th {:colSpan 2} "Winds"]
            [:th {:colSpan 4} "Heading"]
            [:th {:colSpan 2} "Next waypoint"]
            [:th "DIST"]
            [:th "GS"]
            [:th {:colSpan 2} "Time"]
            [:th "Fuel"]]
           [:tr
            [:th "dir"]
            [:th "vel"]
            [:th "TC"]
            [:th "TH"]
            [:th "MH"]
            [:th {:rowSpan 2} "CH"]
            [:td {:colSpan 2, :rowSpan 2} [:input (bind :start {:type "text" :name "waypoint"})]]
            [:th "LEG"]
            [:th "EST"]
            [:th "ETE"]
            [:th "ETA"]
            [:th "LEG"]]
           [:tr
            [:th {:colSpan "2"} "temp"]
            [:th "WCA"]
            [:th "Var"]
            [:th "Dev"]
            [:th "REM"]
            [:th "ACT"]
            [:th "ATE"]
            [:th "ATA"]
            [:th "REM"]]]
          [:tfoot]
          (vec (cons :tbody (map (fn [idx] [trip-leg idx plan]) (range (count (:legs @plan))))))]         
         [:a#add {:on-click (fn [] (swap! plan update :legs #(conj (or % []) {})))} "Add waypoint"]]))))

(reagent/render
 [flight-plan]
 (.getElementById js/document "content"))
