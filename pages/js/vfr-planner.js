jogo = {};

jogo.standardTemperatureCforAltitudeFt = function(altitudeFt) {
  var stdTemperatureCDiff = altitudeFt * 2 / 1000;
  return 15 - stdTemperatureCDiff;
};

jogo.updateForm = function(calculatedValues) {
};

jogo.twoDigitString = function(val) {
  // TODO: There has to be a better way in JavaScript
  return val < 10 ? '0' + val : val + '';
};

// Query local storage for all flight plans
// Allow storage namespacing by prefixing with 'jogo.'
jogo.loadFlightPlans = function() {
  var i;
  var key;
  var thisPlan;
  
  var planList = $('select[name="saved_plans"]');
  planList.find('option').remove();
  
  for(i = 0; i < localStorage.length; i++) {
    key = localStorage.key(i);
    if(key.match(/^jogo\..+$/) !== null) {
      thisPlan = JSON.parse(localStorage[key]);
      // add plan information as selection options
      planList.append('<option value="' + thisPlan.title + '">' + thisPlan.title + '</option>');
    }
  }
}

// Load flight plan from HTML5 local storage
// Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
jogo.loadFlightPlan = function(inTitle) {
  var title = $.trim(inTitle);
  var planKey = 'jogo.' + title;
  var flightPlan = JSON.parse(localStorage[planKey]);
  jogo.displayFlightPlan(flightPlan);
}

jogo.displayFlightPlan = function(flightPlan) {
  var i, f, numLegs, candidateNumLegs;
  document.flight_plan.plan_title.value = flightPlan.title;
  document.flight_plan.aircraft_ktas.value = flightPlan.ktas;
  document.flight_plan.aircraft_fuel_burn_gph.value = flightPlan.gph;
  // TODO: don't use tc.length, use whatever the max field length is.
  numLegs = flightPlan[jogo.legFields[0]].length;
  for(f = 1; f < jogo.legFields.length; f++) {
    candidateNumLegs = flightPlan[jogo.legFields[f]].length;
    if(candidateNumLegs > numLegs) {
      numLegs = jogo.legFields[f].length;
    }
  }
  for(i = 0; i < numLegs; i++) {
    if(i > 1) {
      jogo.addFlightPlanRow();
    }
    for(f = 0; f < jogo.legFields.length; f++) {
      document.flight_plan[jogo.legFields[f]][i].value = flightPlan[jogo.legFields[f]][i];
    }
  }
}

jogo.removeFlightPlan = function(title) {
  localStorage.removeItem('jogo.' + title);
  jogo.loadFlightPlans();
}

jogo.legFields = [
  "wind_dir", "wind_speed", "temp",
  "tc", "th", "mh", "ch", "wca", "var", "dev",
  "waypoint", "altitude", "std_tmp_c",
  "leg_dist", "remaining_dist", "gs_est", "gs_act", "ete", "ate", "eta", "ata",
  "leg_fuel", "remaining_fuel"
];

// Load flight plan from HTML5 local storage
// Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
jogo.saveFlightPlan = function(inTitle) {
  var title = $.trim(inTitle);
  var plan = {};
  plan.title = title;
  var planKey = 'jogo.' + title;
  var f, i;

  plan.ktas = document.flight_plan.aircraft_ktas.value;
  plan.gph = document.flight_plan.aircraft_fuel_burn_gph.value;
  
  // plan.flight_plan = document.flight_plan;
  // Substitute for "document.flight_plan" yielding an unserializable circular structure
  // wind_dir wind_speed temp
  // tc th mh ch wca var dev
  // waypoint altitude std_tmp_c
  // leg_dist remaining_dist gs_est gs_act ete ate eta ata
  // leg_fuel remaining_fuel
  for(f = 0; f < jogo.legFields.length; f++) {
    plan[jogo.legFields[f]] = [];
  }

  for(i = 0; i < document.flight_plan.legmarker.length; i++) {
    for(f = 0; f < jogo.legFields.length; f++) {
      plan[jogo.legFields[f]][i] = document.flight_plan[jogo.legFields[f]][i].value;
    }
  }
  localStorage[planKey] = JSON.stringify(plan);
  jogo.loadFlightPlans();
}

jogo.toZulu = function(t) {
  return jogo.twoDigitString(t.getUTCDate()) + jogo.twoDigitString(t.getUTCHours()) + jogo.twoDigitString(t.getUTCMinutes()) + "Z";
};

jogo.validate = function() {
  // TODO
  // alert("Validated!");
};

// flightIntent = airspeed, trueCourse, distance, fuelBurn
// wind = speed, direction
// output:
//   compassHeading
//   groundSpeed
//   eteHours
//   fuelBurn
jogo.calculateLeg = function(flightIntent, wind) {
  var output = {};
  var windAngleRad = (flightIntent.trueCourse - wind.direction) * Math.PI * 2;
  
  output.groundSpeed = flightIntent.airspeed - wind.speed * Math.cos(windAngleRad); // NOPE! That's wrong (TODO)
  output.eteHours = flightIntent.distance / output.groundSpeed;
  return output;
}

jogo.calculate = function() {
  var dev;
  var i;
  var flightPlan = document.flight_plan;
  var flightIntent = {};
  var wind = {};
  var legInfo = {};
  var waypoint = {};
  
  // Aircraft information needed:
  //   Cruise true airspeed
  flightIntent.airspeed = $('#aircraft_ktas').val();
  //   Cruise fuel burn
  flightIntent.fuelBurn = $('#aircraft_fuel_burn_gph').val();
  // TODO: Magnetic deviance
  
  if(undefined === flightPlan.waypoint.length) {
    waypoint.altitude = flightPlan['altitude'].value;
    waypoint.stdtmpc = jogo.standardTemperatureCforAltitudeFt(waypoint.altitude);
  
    wind.direction = flightPlan['wind_dir'].value;
    wind.speed = flightPlan['wind_speed'].value;
    
    flightIntent.trueCourse = flightPlan['tc'].value;
    flightIntent.distance = flightPlan['leg_dist'].value;

    legInfo = jogo.calculateLeg = flightIntent, wind;
    flightPlan['gs_est'].value = legInfo.groundSpeed;
    flightPlan['ete'].value = legInfo.eteHours;
    flightPlan['std_tmp_c'].value = waypoint.stdtmpc;
  } else {
    for(i = 0; i < flightPlan.waypoint.length; i++) {
      waypoint.altitude = flightPlan['altitude'][i].value;
      waypoint.stdtmpc = jogo.standardTemperatureCforAltitudeFt(waypoint.altitude);
    
      wind.direction = flightPlan['wind_dir'][i].value;
      wind.speed = flightPlan['wind_speed'][i].value;
      
      flightIntent.trueCourse = flightPlan['tc'][i].value;
      flightIntent.distance = flightPlan['leg_dist'][i].value;

      legInfo = jogo.calculateLeg = flightIntent, wind;
      flightPlan['gs_est'][i].value = legInfo.groundSpeed;
      flightPlan['ete'][i].value = legInfo.eteHours;
      flightPlan['std_tmp_c'][i].value = waypoint.stdtmpc;
    }
  }
};

jogo.addFlightPlanRow = function() {
  $('#vfr_plan tr[class="trip_leg"]:last').clone(true).insertAfter('#vfr_plan tr[class="trip_leg"]:last');
  $('#vfr_plan tr[class="trip_leg"]:last input').each(function() { this.value = ""; });
}

jogo.clearFlightPlan = function() {
  // $('#vfr_plan tr[class="trip_leg"]:last').remove();
  // alert($('#vfr_plan tr[class="trip_leg"]:eq(1)'));
  var flightPlan = document.flight_plan;
  var i;
  for(i = flightPlan.waypoint.length - 1; i >= 1; i--) {
    $('#vfr_plan tr[class="trip_leg"]:eq(' + i + ')').remove();
  }
  // TODO: clear remaining seed leg
}

$(document).ready(function() {
  jogo.loadFlightPlans();
  $('#add').click(function() {
    jogo.addFlightPlanRow();
    return false;
  });
  
  $('.calculated').each(function() {
    this.readOnly = true;
  });
  
  $('.direction').each(function() {
    this.size = 3;
    this.maxLength = 3;
  });
  
  $('.altitude').each(function() {
    this.size = 5;
    this.maxLength = 5;
  });
  
  $('input[name="ata"]').click(function() {
    var now = new Date();
    this.value = jogo.toZulu(now);
  });
  
  $('input').change(function() {
     jogo.validate();
     jogo.calculate();
  });
  
  $('#calculate').click(function() {
    var calculatedValues = jogo.calculate();
    jogo.updateForm(calculatedValues);
    return false;
  });
  
  $('#save').click(function() {
    jogo.saveFlightPlan(document.flight_plan.plan_title.value);
    return false;
  });
  
  $('#load').click(function() {
    jogo.loadFlightPlan(document.flight_plan.saved_plans.value);
    // return false to avoid a second page reload
    return false;
  });
  
  $('#remove').click(function() {
    jogo.removeFlightPlan(document.flight_plan.saved_plans.value);
    return false;
  });
  
  $('#new_plan').click(function() {
    jogo.clearFlightPlan();
    return false;
  });
});
