class AirplanesController < ApplicationController
  # GET /airplanes
  # GET /airplanes.json
  def index
    @airplanes = Airplane.all

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @airplanes }
    end
  end

  # GET /airplanes/1
  # GET /airplanes/1.json
  def show
    @airplane = Airplane.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @airplane }
    end
  end

  # GET /airplanes/new
  # GET /airplanes/new.json
  def new
    @airplane = Airplane.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @airplane }
    end
  end

  # GET /airplanes/1/edit
  def edit
    @airplane = Airplane.find(params[:id])
  end

  # POST /airplanes
  # POST /airplanes.json
  def create
    @airplane = Airplane.new(params[:airplane])

    respond_to do |format|
      if @airplane.save
        format.html { redirect_to @airplane, notice: 'Airplane was successfully created.' }
        format.json { render json: @airplane, status: :created, location: @airplane }
      else
        format.html { render action: "new" }
        format.json { render json: @airplane.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /airplanes/1
  # PUT /airplanes/1.json
  def update
    @airplane = Airplane.find(params[:id])

    respond_to do |format|
      if @airplane.update_attributes(params[:airplane])
        format.html { redirect_to @airplane, notice: 'Airplane was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @airplane.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /airplanes/1
  # DELETE /airplanes/1.json
  def destroy
    @airplane = Airplane.find(params[:id])
    @airplane.destroy

    respond_to do |format|
      format.html { redirect_to airplanes_url }
      format.json { head :no_content }
    end
  end
end
