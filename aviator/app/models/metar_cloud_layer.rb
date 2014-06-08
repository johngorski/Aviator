class MetarCloudLayer < ActiveRecord::Base
  attr_accessible :altitude_feet_agl, :type

  @@Type_to_description = {
    0 => :SKC,
    1 => :FEW,
    2 => :FEW,
    3 => :SCT,
    4 => :SCT,
    5 => :BKN,
    6 => :BKN,
    7 => :BKN,
    8 => :OVC
  }

  @@Description_to_type = {
    :SKC => 0,
    :FEW => 2,
    :SCT => 4,
    :BKN => 7,
    :OVC => 8
  }

  def description=(description)
    self.type = @@Description_to_type[description.upcase.to_sym]
  end

  def description
    @@Type_to_description[type]
  end
end
