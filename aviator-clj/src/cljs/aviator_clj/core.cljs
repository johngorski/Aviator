(ns aviator-clj.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn home []
  [:h2 "Hello, Reagent!"])

(defn sz [size m]
  (merge {:maxlength size :size size} m))

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
   [:table#vfr_plan {:border "1" :cellpadding "4"} ;; route
    [:thead
     [:tr [:th][:th "Winds"][:th "Heading"][:th "Next waypoint"][:th "DIST"][:th "GS"][:th "Time"][:th "Fuel"]]
     [:tr [:td]
      [:td ;; winds
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "dir"][:th "vel"]]
        [:tr [:th {:colspan "2"} "temp"]]]]
      [:td ;; heading
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "TC"][:th "TH"][:th "MH"][:th {:rowspan "2"} "CH"]]
        [:tr [:th "WCA"][:th "Var"][:th "Dev"]]]]
      [:td] ;; next waypoint
      [:td ;; dist
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "LEG"]]
        [:tr [:th "REM"]]]]
      [:td ;; GS
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "EST"]]
        [:tr [:th "ACT"]]]]
      [:td ;; time
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "ETE"][:th "ETA"]]
        [:tr [:th "ATE"][:th "ATA"]]]]
      [:td ;; fuel
       [:table {:border "1" :cellpadding "4"}
        [:tr [:th "LEG"]]
        [:tr [:th "REM"]]]]]]
    [:tfoot]
    [:tbody
     [:tr.trip_leg
      [:input {:type "hidden" :name "legmarker" :value ""}]
      [:td [:button.remove_leg "Remove"]]
      [:td ;; winds
       [:table
        [:tr
         [:td [:input.direction (sz 3 {:type "text" :name "wind_dir"})]]
         [:td [:input (sz 3 {:type "text" :name "wind_speed"})]]]
        [:tr
         [:td {:colspan "2"} [:input (sz 3 {:type "text" :name "temp"})]]]]]
      [:td ;; headings
       [:table
        [:tr
         [:td [:input.direction (sz 3 {:type "text" :name "tc" :placeholder "TC"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "th" :placeholder "TH"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "mh" :placeholder "MH"})]]
         [:td {:rowspan "2"} [:input.direction.calculated (sz 3 {:type "text" :name "ch" :placeholder "CH"})]]]
        [:tr
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "wca" :placeholder "WCA"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "var" :placeholder "Var"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "dev" :placeholder "Dev"})]]]]]
      [:td ;; next waypoint
       [:table
        [:tr [:td {:colspan "2"} [:input {:type "text" :name "waypoint"}]]]
        [:tr
         [:td "alt" [:input.altitude (sz 5 {:type "text" :name "altitude"})] "ft"]
         [:td "std tmp" [:input.calculated (sz 3 {:type "text" :name "std_tmp_c"})] "C"]]]]
      [:td ;; dist
       [:table
        [:tr [:td [:input (sz 4 {:type "text" :name "leg_dist"})]]]
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "remaining_dist"})]]]]]
      [:td ;; gs
       [:table
        [:tr [:td [:input.calculated (sz 3 {:type "text" :name "gs_est"})]]]
        [:tr [:td [:input.calculated (sz 3 {:type "text" :name "gs_act"})]]]]]
      [:td ;; time
       [:table
        [:tr
         [:td [:input.calculated (sz 4 {:type "text" :name "ete"})]]
         [:td [:input.calculated (sz 4 {:type "text" :name "eta"})]]]
        [:tr
         [:td [:input.calculated (sz 4 {:type "text" :name "ate"})]]
         [:td [:input (sz 4 {:type "text" :name "ata" :readonly "readonly"})]]]]]
      [:td ;; fuel
       [:table
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "leg_fuel"})]]]
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "remaining_fuel"})]]]]]]
     
     [:tr.trip_leg
      [:input {:type "hidden" :name "legmarker" :value ""}]
      [:td [:button.remove_leg "Remove"]]
      [:td ;; winds
       [:table
        [:tr
         [:td [:input.direction (sz 3 {:type "text" :name "wind_dir"})]]
         [:td [:input (sz 3 {:type "text" :name "wind_speed"})]]]
        [:tr
         [:td {:colspan "2"} [:input (sz 3 {:type "text" :name "temp" :maxlength "3" :size "3"})]]]]]
      [:td ;; headings
       [:table
        [:tr
         [:td [:input.direction (sz 3 {:type "text" :name "tc" :placeholder "TC"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "th" :placeholder "TH"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "mh" :placeholder "MH"})]]
         [:td {:rowspan "2"} [:input.direction.calculated (sz 3 {:type "text" :name "ch" :placeholder "CH"})]]]
        [:tr
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "wca" :placeholder "WCA"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "var" :placeholder "Var"})]]
         [:td [:input.direction.calculated (sz 3 {:type "text" :name "dev" :placeholder "Dev"})]]]]]
      [:td ;; next waypoint
       [:table
        [:tr [:td {:colspan "2"} [:input {:type "text" :name "waypoint"}]]]
        [:tr
         [:td "alt" [:input.altitude (sz 5 {:type "text" :name "altitude"})] "ft"]
         [:td "std tmp" [:input.calculated (sz 3 {:type "text" :name "std_tmp_c"})] "C"]]]]
      [:td ;; dist
       [:table
        [:tr [:td [:input (sz 4 {:type "text" :name "leg_dist"})]]]
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "remaining_dist"})]]]]]
      [:td ;; gs
       [:table
        [:tr [:td [:input.calculated (sz 3 {:type "text" :name "gs_est"})]]]
        [:tr [:td [:input.calculated (sz 3 {:type "text" :name "gs_act"})]]]]]
      [:td ;; time
       [:table
        [:tr
         [:td [:input.calculated (sz 4 {:type "text" :name "ete"})]]
         [:td [:input.calculated (sz 4 {:type "text" :name "eta"})]]]
        [:tr
         [:td [:input.calculated (sz 4 {:type "text" :name "ate"})]]
         [:td [:input (sz 4 {:type "text" :name "ata" :readonly "readonly"})]]]]]
      [:td ;; fuel
       [:table
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "leg_fuel"})]]]
        [:tr [:td [:input.calculated (sz 4 {:type "text" :name "remaining_fuel"})]]]]]]
     ]]
   [:button#add "Add waypoint"] [:button#calculate "Calculate"]])

(reagent/render
 [flight-plan]
 (.getElementById js/document "content"))
