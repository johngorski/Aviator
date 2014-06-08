# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20140607155212) do

  create_table "airplanes", :force => true do |t|
    t.string   "number"
    t.datetime "last_annual"
    t.float    "tach_hours"
    t.float    "hobbs_hours"
    t.datetime "last_100_hour"
    t.datetime "created_at",    :null => false
    t.datetime "updated_at",    :null => false
  end

  create_table "metar_cloud_layers", :force => true do |t|
    t.integer  "type"
    t.string   "altitude_feet_agl"
    t.datetime "created_at",        :null => false
    t.datetime "updated_at",        :null => false
  end

  create_table "metars", :force => true do |t|
    t.string   "station_id"
    t.datetime "observation_time"
    t.decimal  "wind_dir_deg"
    t.decimal  "wind_speed_kts"
    t.decimal  "temperature_celsius"
    t.decimal  "dew_point_celsius"
    t.decimal  "altimeter_inHg"
    t.datetime "created_at",          :null => false
    t.datetime "updated_at",          :null => false
  end

  create_table "pilots", :force => true do |t|
    t.string   "name"
    t.string   "certificate"
    t.datetime "created_at",   :null => false
    t.datetime "updated_at",   :null => false
    t.integer  "weight"
    t.datetime "last_medical"
  end

end
