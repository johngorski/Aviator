require 'test_helper'

class AirplanesControllerTest < ActionController::TestCase
  setup do
    @airplane = airplanes(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:airplanes)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create airplane" do
    assert_difference('Airplane.count') do
      post :create, airplane: { hobbs_hours: @airplane.hobbs_hours, last_100_hour: @airplane.last_100_hour, last_annual: @airplane.last_annual, number: @airplane.number, tach_hours: @airplane.tach_hours }
    end

    assert_redirected_to airplane_path(assigns(:airplane))
  end

  test "should show airplane" do
    get :show, id: @airplane
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @airplane
    assert_response :success
  end

  test "should update airplane" do
    put :update, id: @airplane, airplane: { hobbs_hours: @airplane.hobbs_hours, last_100_hour: @airplane.last_100_hour, last_annual: @airplane.last_annual, number: @airplane.number, tach_hours: @airplane.tach_hours }
    assert_redirected_to airplane_path(assigns(:airplane))
  end

  test "should destroy airplane" do
    assert_difference('Airplane.count', -1) do
      delete :destroy, id: @airplane
    end

    assert_redirected_to airplanes_path
  end
end
