var jogo = {};

jogo.standardTemperatureCforAltitudeFt = function (altitudeFt) { "use strict";
  var stdTemperatureCDiff = altitudeFt * 2 / 1000;
  return 15 - stdTemperatureCDiff;
};

jogo.updateForm = function (calculatedValues) { "use strict";
};

jogo.twoDigitString = function (val) { "use strict";
  // TODO: There has to be a better way in JavaScript
  return val < 10 ? '0' + val : val + "";
};

// Query local storage for all flight plans
// Allow storage namespacing by prefixing with 'jogo.'
jogo.loadFlightPlans = function () { "use strict";
  var i, key,thisPlan, planList;
  planList = $('select[name="saved_plans"]');
  planList.find('option').remove();
  
  for(i = 0; i < localStorage.length; i += 1) {
    key = localStorage.key(i);
    if(key.match(/^jogo\..+$/) !== null) {
      thisPlan = JSON.parse(localStorage[key]);
      // add plan information as selection options
      planList.append('<option value="' + thisPlan.title + '">' + thisPlan.title + '</option>');
    }
  }
};

// Load flight plan from HTML5 local storage
// Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
jogo.loadFlightPlan = function (inTitle) { "use strict";
  var title, planKey, flightPlan;
  title = $.trim(inTitle);
  planKey = 'jogo.' + title;
  flightPlan = JSON.parse(localStorage[planKey]);
  jogo.displayFlightPlan(flightPlan);
};

jogo.displayFlightPlan = function (flightPlan) { "use strict";
  var i, f;
  document.flight_plan.plan_title.value = flightPlan.title;
  document.flight_plan.aircraft_ktas.value = flightPlan.ktas;
  document.flight_plan.aircraft_fuel_burn_gph.value = flightPlan.gph;
 
  for(i = 0; i < flightPlan.legs.length; i += 1) {
    if(i > 1) {
      jogo.addFlightPlanRow();
    }
    for(f = 0; f < jogo.legFields.length; f += 1) {
      document.flight_plan[jogo.legFields[f]][i].value = flightPlan.legs[i][jogo.legFields[f]];
    }
  }
};

jogo.removeFlightPlan = function (title) { "use strict";
  localStorage.removeItem('jogo.' + title);
  jogo.loadFlightPlans();
};

jogo.legFields = [
  "wind_dir", "wind_speed", "temp",
  "tc", "th", "mh", "ch", "wca", "var", "dev",
  "waypoint", "altitude", "std_tmp_c",
  "leg_dist", "remaining_dist", "gs_est", "gs_act", "ete", "ate", "eta", "ata",
  "leg_fuel", "remaining_fuel"
];

jogo.flightPlanFormToObject = function () { "use strict";
  var f, i, plan = {};

  plan.ktas = document.flight_plan.aircraft_ktas.value;
  plan.gph = document.flight_plan.aircraft_fuel_burn_gph.value;
  plan.legs = [];
  
  // Substitute for "document.flight_plan" yielding an unserializable circular structure
  // wind_dir wind_speed temp
  // tc th mh ch wca var dev
  // waypoint altitude std_tmp_c
  // leg_dist remaining_dist gs_est gs_act ete ate eta ata
  // leg_fuel remaining_fuel
  for(i = 0; i < document.flight_plan.legmarker.length; i += 1) {
    plan.legs[i] = {};
    for(f = 0; f < jogo.legFields.length; f += 1) {
      plan.legs[i][jogo.legFields[f]] = document.flight_plan[jogo.legFields[f]][i].value;
    }
  }
  return plan;
};

// Load flight plan from HTML5 local storage
// Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
jogo.saveFlightPlan = function (inTitle) { "use strict";
  var title, plan, planKey;
  title = $.trim(inTitle);
  plan = jogo.flightPlanFormToObject();
  plan.title = title;
  planKey = 'jogo.' + title;
  
  localStorage[planKey] = JSON.stringify(plan);
  jogo.loadFlightPlans();
};

jogo.toZulu = function (t) { "use strict";
  return jogo.twoDigitString(t.getUTCDate()) + jogo.twoDigitString(t.getUTCHours()) + jogo.twoDigitString(t.getUTCMinutes()) + "Z";
};

jogo.validate = function () { "use strict";
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
// This is really two functions:
//   1. applyWinds(trueCourseDeg, airspeedKT, windDirDeg, windSpeedKT) = {wcaDeg, groundSpeedKT}
//   2. tfd(distance, groundSpeed, burnRate) = {timeMin, fuelGal}
jogo.calculateLeg = function (flightIntent, wind) { "use strict";
  var output, windAngleRad;
  output = {};
  windAngleRad = (flightIntent.trueCourse - wind.direction) * Math.PI * 2;
  
  output.groundSpeed = flightIntent.airspeed - wind.speed * Math.cos(windAngleRad); // NOPE! That's wrong (TODO)
  output.eteHours = flightIntent.distance / output.groundSpeed;
  return output;
};

jogo.radToDeg = function (rad) { "use strict";
  return 180 * rad / Math.PI;
};

jogo.degToRad = function (deg) { "use strict";
  return Math.PI * deg / 180;
};

jogo.applyWinds = function (trueCourseDeg, airspeedKT, windDirDeg, windspeedKT) { "use strict";
  var wcaRad, gsKT;
  wcaRad = Math.asin(windspeedKT * Math.sin(jogo.degToRad(windDirDeg - trueCourseDeg)) / airspeedKT);
  gsKT = airspeedKT * Math.cos(wcaRad) - windspeedKT * Math.cos(jogo.degToRad(windDirDeg - trueCourseDeg));
  return { "wcaDeg" : jogo.radToDeg(wcaRad), "gsKT" : gsKT };
};

jogo.calculate = function () { "use strict";
  var dev, i, flightPlan, flightIntent, wind, legInfo, waypoint;
  flightPlan = document.flight_plan;
  flightIntent = {};
  wind = {};
  legInfo = {};
  waypoint = {};
  
  // Aircraft information needed:
  //   Cruise true airspeed
  flightIntent.airspeed = $('#aircraft_ktas').val();
  //   Cruise fuel burn
  flightIntent.fuelBurn = $('#aircraft_fuel_burn_gph').val();
  // TODO: Magnetic deviance
  
  if(undefined === flightPlan.waypoint.length) {
    waypoint.altitude = flightPlan.altitude.value;
    waypoint.stdtmpc = jogo.standardTemperatureCforAltitudeFt(waypoint.altitude);
  
    wind.direction = flightPlan.wind_dir.value;
    wind.speed = flightPlan.wind_speed.value;
    
    flightIntent.trueCourse = flightPlan.tc.value;
    flightIntent.distance = flightPlan.leg_dist.value;

    legInfo = jogo.calculateLeg(flightIntent, wind);
    flightPlan.gs_est.value = legInfo.groundSpeed;
    flightPlan.ete.value = legInfo.eteHours;
    flightPlan.std_tmp_c.value = waypoint.stdtmpc;
  } else {
    for(i = 0; i < flightPlan.waypoint.length; i += 1) {
      waypoint.altitude = flightPlan.altitude[i].value;
      waypoint.stdtmpc = jogo.standardTemperatureCforAltitudeFt(waypoint.altitude);
    
      wind.direction = flightPlan.wind_dir[i].value;
      wind.speed = flightPlan.wind_speed[i].value;
      
      flightIntent.trueCourse = flightPlan.tc[i].value;
      flightIntent.distance = flightPlan.leg_dist[i].value;

      legInfo = jogo.calculateLeg(flightIntent, wind);
      flightPlan.gs_est[i].value = legInfo.groundSpeed;
      flightPlan.ete[i].value = legInfo.eteHours;
      flightPlan.std_tmp_c[i].value = waypoint.stdtmpc;
    }
  }
};

jogo.addFlightPlanRow = function () { "use strict";
  $('#vfr_plan tr[class="trip_leg"]:last').clone(true).insertAfter('#vfr_plan tr[class="trip_leg"]:last');
  $('#vfr_plan tr[class="trip_leg"]:last input').each(function () { this.value = ""; });
};

jogo.clearFlightPlan = function () { "use strict";
  // $('#vfr_plan tr[class="trip_leg"]:last').remove();
  // alert($('#vfr_plan tr[class="trip_leg"]:eq(1)'));
  var i, flightPlan;
  flightPlan = document.flight_plan;

  for(i = flightPlan.waypoint.length - 1; i >= 1; i -= 1) {
    $('#vfr_plan tr[class="trip_leg"]:eq(' + i + ')').remove();
  }
  // TODO: clear remaining seed leg
};

$(document).ready(function () { "use strict";
  jogo.loadFlightPlans();
  $('#add').click(function () {
    jogo.addFlightPlanRow();
    return false;
  });
  
  $('.calculated').each(function () {
    this.readOnly = true;
  });
  
  $('.direction').each(function () {
    this.size = 3;
    this.maxLength = 3;
  });
  
  $('.altitude').each(function () {
    this.size = 5;
    this.maxLength = 5;
  });
  
  $('input[name="ata"]').click(function () {
    var now = new Date();
    this.value = jogo.toZulu(now);
  });
  
  $('input').change(function () {
     jogo.validate();
     jogo.calculate();
  });
  
  $('#calculate').click(function () {
    var calculatedValues = jogo.calculate();
    jogo.updateForm(calculatedValues);
    return false;
  });
  
  $('#save').click(function () {
    jogo.saveFlightPlan(document.flight_plan.plan_title.value);
    return false;
  });
  
  $('#load').click(function () {
    jogo.loadFlightPlan(document.flight_plan.saved_plans.value);
    // return false to avoid a second page reload
    return false;
  });
  
  $('#remove').click(function () {
    jogo.removeFlightPlan(document.flight_plan.saved_plans.value);
    return false;
  });
  
  $('#new_plan').click(function () {
    jogo.clearFlightPlan();
    return false;
  });
});
