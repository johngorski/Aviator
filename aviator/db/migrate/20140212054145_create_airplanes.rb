class CreateAirplanes < ActiveRecord::Migration
  def change
    create_table :airplanes do |t|
      t.string :number
      t.timestamp :last_annual
      t.float :tach_hours
      t.float :hobbs_hours
      t.timestamp :last_100_hour

      t.timestamps
    end
  end
end
