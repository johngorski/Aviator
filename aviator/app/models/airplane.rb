class Airplane < ActiveRecord::Base
  attr_accessible :hobbs_hours, :last_100_hour, :last_annual, :number, :tach_hours
end
