require 'test_helper'

class MetarTest < ActiveSupport::TestCase
  def setup
    @m = Metar.parse 'KPAE 071705Z 0717/0812 00000KT P6SM BKN013 OVC070'
  end

  test 'parse station ID' do
    assert_equal 'KPAE', @m.station_id
  end

  test 'parse timestamp' do
    assert_equal '7', @m.observation_time.strftime('%-d'), @m.observation_time
    assert_equal '17', @m.observation_time.strftime('%H'), @m.observation_time
    assert_equal '05', @m.observation_time.strftime('%M'), @m.observation_time
  end
end
