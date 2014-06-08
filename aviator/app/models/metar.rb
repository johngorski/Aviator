class Metar < ActiveRecord::Base
  attr_accessible :altimeter_inHg, :dew_point_celsius, :observation_time, :station_id, :temperature_celsius, :wind_dir_deg, :wind_speed_kts

  def self.parse metar
    tokens = metar.split
    
    m = Metar.new
    m.station_id = tokens[0]

    obs_time = Metar.parse_time tokens[1]
    m.observation_time = obs_time

    m
  end

  def self.parse_time t
    DateTime.strptime t, '%d%H%M%Z'
  end
end
