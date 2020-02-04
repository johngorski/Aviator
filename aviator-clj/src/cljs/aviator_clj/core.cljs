(ns aviator-clj.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn sz [size m]
  (merge {:type "text" :maxLength size :size size} m))

(defn trip-leg []
  [:tr.trip_leg
   [:input {:type "hidden" :name "legmarker" :value ""}]
   [:td [:button.remove_leg "Remove"]]
   [:td ;; winds
    [:table
     [:tr
      [:td [:input.direction (sz 3 {:name "wind_dir"})]]
      [:td [:input (sz 3 {:name "wind_speed"})]]]
     [:tr
      [:td {:colSpan "2"} [:input (sz 3 {:name "temp"})]]]]]
   [:td ;; headings
    [:table
     [:tr
      [:td [:input.direction (sz 3 {:name "tc" :placeholder "TC"})]]
      [:td [:input.direction.calculated (sz 3 {:name "th" :placeholder "TH"})]]
      [:td [:input.direction.calculated (sz 3 {:name "mh" :placeholder "MH"})]]
      [:td {:rowSpan "2"} [:input.direction.calculated (sz 3 {:name "ch" :placeholder "CH"})]]]
     [:tr
      [:td [:input.direction.calculated (sz 3 {:name "wca" :placeholder "WCA"})]]
      [:td [:input.direction.calculated (sz 3 {:name "var" :placeholder "Var"})]]
      [:td [:input.direction.calculated (sz 3 {:name "dev" :placeholder "Dev"})]]]]]
   [:td ;; next waypoint
    [:table
     [:tr [:td {:colSpan "2"} [:input {:type "text" :name "waypoint"}]]]
     [:tr
      [:td "alt" [:input.altitude (sz 5 {:name "altitude"})] "ft"]
      [:td "std tmp" [:input.calculated (sz 3 {:name "std_tmp_c"})] "C"]]]]
   [:td ;; dist
    [:table
     [:tr [:td [:input (sz 4 {:name "leg_dist"})]]]
     [:tr [:td [:input.calculated (sz 4 {:name "remaining_dist"})]]]]]
   [:td ;; gs
    [:table
     [:tr [:td [:input.calculated (sz 3 {:name "gs_est"})]]]
     [:tr [:td [:input.calculated (sz 3 {:name "gs_act"})]]]]]
   [:td ;; enroute time
    [:table
     [:tr
      [:td [:input.calculated (sz 4 {:name "ete"})]]]
     [:tr
      [:td [:input.calculated (sz 4 {:name "ate"})]]]]]
   [:td ;; arrival time
    [:table
     [:tr [:td [:input.calculated (sz 6 {:name "eta"})] "Z"]]
     [:tr [:td [:input (sz 6 {:name "ata" :readOnly "readonly"})] "Z"]]]]
   [:td ;; fuel
    [:table
     [:tr [:td [:input.calculated (sz 4 {:name "leg_fuel"})]]]
     [:tr [:td [:input.calculated (sz 4 {:name "remaining_fuel"})]]]]]])

(defn flight-plan []
  [:form {:name "flight_plan"}
   [:table#aircraft_cruise_profile
    [:tr
     [:td [:input {:type "text" :name "plan_title"}]]
     [:td [:button#save "Save flight plan"]]
     [:td [:button#new_plan "New plan"]]]
    [:tr
     [:td [:select {:name "saved_plans" :multiple "false"}]]
     [:td
      [:button#load "Load flight plan"][:br]
      [:button#remove "Remove flight plan"]]]
    [:tr
     [:th "KTAS"][:td [:input#aircraft_ktas.number {:type "text"}]]]
    [:tr
     [:th "Fuel burn (gph)"][:td [:input#aircraft_fuel_burn_gph.number {:type "text"}]]]
    [:tr
     [:th "Magnetic deviance"][:td [:table]]]]
   [:table#vfr_plan.subgrid {:style {:border "1px solid"} :cellPadding "4"} ;; route
    [:thead
     [:tr [:th][:th "Winds"][:th "Heading"][:th "Next waypoint"][:th "DIST"][:th "GS"][:th {:colSpan 2} "Time"][:th "Fuel"]]
     [:tr [:td]
      [:td ;; winds
       [:table.subgrid {:border "1px solid" :cellPadding "4"}
        [:tr [:th "dir"][:th "vel"]]
        [:tr [:th {:colSpan "2"} "temp"]]]]
      [:td ;; heading
       [:table.subgrid {:border "1" :cellPadding "4"}
        [:tr [:th "TC"][:th "TH"][:th "MH"][:th {:rowSpan "2"} "CH"]]
        [:tr [:th "WCA"][:th "Var"][:th "Dev"]]]]
      [:td] ;; next waypoint
      [:td ;; dist
       [:table.subgrid {:border "1" :cellPadding "4"}
        [:tr [:th "LEG"]]
        [:tr [:th "REM"]]]]
      [:td ;; GS
       [:table.subgrid {:border "1" :cellPadding "4"}
        [:tr [:th "EST"]]
        [:tr [:th "ACT"]]]]
      [:td ;; enroute time
       [:table.subgrid
        [:tr [:th "ETE"]]
        [:tr [:th "ATE"]]]]
      [:td ;; arrival time
       [:table.subgrid
        [:tr [:th "ETA"]]
        [:tr [:th "ATA"]]]]
      [:td ;; fuel
       [:table.subgrid {:border "1" :cellPadding "4"}
        [:tr [:th "LEG"]]
        [:tr [:th "REM"]]]]]]
    [:tfoot]
    [:tbody
     [trip-leg]
     [trip-leg]]]
   [:button#add "Add waypoint"] [:button#calculate "Calculate"]])

(reagent/render
 [flight-plan]
 (.getElementById js/document "content"))
