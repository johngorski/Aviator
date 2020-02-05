(ns aviator-clj.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn sz [size m]
  (merge {:type "text" :maxLength size :size size} m))

(defn trip-leg-top []
  [:tr
   [:td {:rowSpan 2} [:button.remove_leg "Remove"]]
   [:td [:input.direction (sz 3 {:name "wind_dir"})]][:td [:input (sz 3 {:name "wind_speed"})]]
   [:td [:input.direction (sz 3 {:name "tc" :placeholder "TC"})]]
   [:td [:input.direction.calculated (sz 3 {:name "th" :placeholder "TH"})]]
   [:td [:input.direction.calculated (sz 3 {:name "mh" :placeholder "MH"})]]
   [:td {:rowSpan "2"} [:input.direction.calculated (sz 3 {:name "ch" :placeholder "CH"})]]
   [:td {:colSpan "2"} [:input {:type "text" :name "waypoint"}]]
   [:td [:input (sz 4 {:name "leg_dist"})]]
   [:td [:input.calculated (sz 3 {:name "gs_est"})]]
   [:td [:input.calculated (sz 4 {:name "ete"})]]
   [:td [:input.calculated (sz 6 {:name "eta"})] "Z"]
   [:td [:input.calculated (sz 4 {:name "leg_fuel"})]]])

(defn trip-leg-bottom []
  [:tr
   [:td {:colSpan "2"} [:input (sz 3 {:name "temp"})]]
   [:td [:input.direction.calculated (sz 3 {:name "wca" :placeholder "WCA"})]]
   [:td [:input.direction.calculated (sz 3 {:name "var" :placeholder "Var"})]]
   [:td [:input.direction.calculated (sz 3 {:name "dev" :placeholder "Dev"})]]
   [:td "alt" [:input.altitude (sz 5 {:name "altitude"})] "ft"]
   [:td "std tmp" [:input.calculated (sz 3 {:name "std_tmp_c"})] "C"]
   [:td [:input.calculated (sz 4 {:name "remaining_dist"})]]
   [:td [:input.calculated (sz 3 {:name "gs_act"})]]
   [:td [:input.calculated (sz 4 {:name "ate"})]]
   [:td [:input (sz 6 {:name "ata" :readOnly "readonly"})] "Z"]
   [:td [:input.calculated (sz 4 {:name "remaining_fuel"})]]])

(defn trip-leg []
  [:<>
   [trip-leg-top]
   [trip-leg-bottom]])

(defn flight-plan []
  [:form {:name "flight_plan"}
   [:table#aircraft_cruise_profile
    [:tbody
     [:tr
      [:td [:input {:type "text" :name "plan_title"}]]
      [:td [:button#save "Save flight plan"]]
      [:td [:button#new_plan "New plan"]]]
     [:tr
      [:td [:select {:name "saved_plans" :multiple false}]]
      [:td
       [:button#load "Load flight plan"][:br]
       [:button#remove "Remove flight plan"]]]
     [:tr
      [:th "KTAS"][:td [:input#aircraft_ktas.number (sz 4 {:type "text"})]]]
     [:tr
      [:th "Fuel burn (gph)"][:td [:input#aircraft_fuel_burn_gph.number (sz 4 {:type "text"})]]]
     [:tr
      [:th "Magnetic deviance"][:td [:table]]]]]
   [:table#vfr_plan.subgrid {:style {:border "1px solid"} :cellPadding "4"}
    [:thead
     [:tr [:th {:rowSpan 3}][:th {:colSpan 2} "Winds"][:th {:colSpan 4} "Heading"][:th {:colSpan 2} "Next waypoint"][:th "DIST"][:th "GS"][:th {:colSpan 2} "Time"][:th "Fuel"]]
     [:tr [:th "dir"][:th "vel"] [:th "TC"][:th "TH"][:th "MH"][:th {:rowSpan 2} "CH"] [:th {:colSpan 2, :rowSpan 2}] [:th "LEG"] [:th "EST"] [:th "ETE"][:th "ETA"] [:th "LEG"]]
     [:tr [:th {:colSpan "2"} "temp"] [:th "WCA"][:th "Var"][:th "Dev"] [:th "REM"] [:th "ACT"] [:th "ATE"][:th "ATA"] [:th "REM"]]]
    [:tfoot]
    [:tbody
     [trip-leg-top][trip-leg-bottom]
     ;; [trip-leg]
     [trip-leg-top][trip-leg-bottom]]]
   [:button#add "Add waypoint"] [:button#calculate "Calculate"]])

(reagent/render
 [flight-plan]
 (.getElementById js/document "content"))
