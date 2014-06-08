class CreateMetarCloudLayers < ActiveRecord::Migration
  def change
    create_table :metar_cloud_layers do |t|
      t.integer :type
      t.string :altitude_feet_agl

      t.timestamps
    end
  end
end
