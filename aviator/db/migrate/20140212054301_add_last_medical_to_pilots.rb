class AddLastMedicalToPilots < ActiveRecord::Migration
  def change
    add_column :pilots, :last_medical, :timestamp
  end
end
