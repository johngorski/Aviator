class CreateMetars < ActiveRecord::Migration
  def change
    create_table :metars do |t|
      t.string :station_id
      t.timestamp :observation_time
      t.decimal :wind_dir_deg
      t.decimal :wind_speed_kts
      t.decimal :temperature_celsius
      t.decimal :dew_point_celsius
      t.decimal :altimeter_inHg

      t.timestamps
    end
  end
end
