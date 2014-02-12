class AddWeightToPilots < ActiveRecord::Migration
  def change
    add_column :pilots, :weight, :integer
  end
end
