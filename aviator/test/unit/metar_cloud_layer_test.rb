require 'test_helper'

class MetarCloudLayerTest < ActiveSupport::TestCase
  test 'description reads from type to description map' do
    layer = MetarCloudLayer.new
    layer.type = 1
    assert_equal :FEW, layer.description
  end

  test 'type set based on description to type map' do
    layer = MetarCloudLayer.new
    layer.description = 'OVC'
    assert_equal 8, layer.type
  end
end
