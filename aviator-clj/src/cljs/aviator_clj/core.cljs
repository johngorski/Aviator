(ns aviator-clj.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn sz [size m]
  (merge {:type "text" :maxLength size :size size} m))

(defn trip-leg [idx plan]
  (letfn [(bind [field input]
            (merge input
                   {:value (get-in @plan [:legs idx field])
                    :on-change #(swap! plan assoc-in [:legs idx field] (-> % .-target .-value))}))]
    [:<>
     [:tr {:style {:border-top "1px solid"}}
      [:td
       {:rowSpan 2, :style {:border-right "1px solid"}}
       [:a.remove_leg
        {:on-click (fn [] (swap! plan update :legs #(vec (concat (subvec % 0 idx) (subvec % (inc idx))))))}
        "Remove"]]
      [:td [:input.direction (sz 3 (bind :wind-dir {:name "wind_dir"}))]]
      [:td {:style {:border-right "1px solid"}} [:input (sz 3 (bind :wind-speed {:name "wind_speed"}))]]
      [:td [:input.direction (sz 3 (bind :tc {:name "tc" :placeholder "TC"}))]]
      [:td [:input.direction.calculated (sz 3 {:name "th" :placeholder "TH"})]]
      [:td [:input.direction.calculated (sz 3 {:name "mh" :placeholder "MH"})]]
      [:td {:rowSpan 2, :style {:border-right "1px solid"}} [:input.direction.calculated (sz 3 {:name "ch" :placeholder "CH"})]]
      [:td {:colSpan 2} [:input (bind :waypoint {:type "text" :name "waypoint"})]]
      [:td {:style {:border-left "1px solid"}} [:input (sz 4 (bind :leg-dist {:name "leg_dist"}))]]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 3 {:name "gs_est"})]]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "ete"})]]
      [:td [:input.calculated (sz 6 {:name "eta"})] "Z"]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "leg_fuel"})]]]
     [:tr {:style {:border-bottom "1px solid"}}
      [:td {:colSpan 2, :style {:border-right "1px solid"}} [:input (sz 3 (bind :temp {:name "temp"}))]]
      [:td [:input.direction.calculated (sz 3 {:name "wca" :placeholder "WCA"})]]
      [:td [:input.direction (sz 3 (bind :var {:name "var" :placeholder "Var"}))]]
      [:td [:input.direction.calculated (sz 3 {:name "dev" :placeholder "Dev"})]]
      [:td "alt " [:input.altitude (sz 5 (bind :altitude {:name "altitude"}))] "ft"]
      [:td "std tmp " [:input.calculated (sz 3 {:name "std_tmp_c"})] "℃"]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "remaining_dist"})]]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 3 {:name "gs_act"})]]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "ate"})]]
      [:td [:input (sz 6 {:name "ata" :readOnly "readonly"})] "Z"]
      [:td {:style {:border-left "1px solid"}} [:input.calculated (sz 4 {:name "remaining_fuel"})]]]]))

(defn flight-plan []
  (let [plan (atom {:legs [{:tc 1, :var -17, :waypoint "way"} {:waypoint "point"}]})
        bind (fn [field input]
               (merge input
                      {:value (get @plan field)
                       :on-change #(swap! plan assoc field (-> % .-target .-value))}))]
    (do
      (add-watch plan :log (fn [key a old new] (js/console.log (str old " -> " new))))
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
            [:th {:colSpan 2, :rowSpan 2} [:input (bind :start {:type "text" :name "waypoint"})]]
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
