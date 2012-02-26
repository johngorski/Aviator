var jogo = {};

jogo.constructLegField = function (name, toString) { "use strict";
  return {
    "name" : name,
    "toString" : toString
  };
};

jogo.passThrough = function (val) { "use strict";
  return val;
};

jogo.roundToOneDecimalPoint = function (val) { "use strict";
  return Math.round(10 * val) / 10;
};

jogo.legFields = [
  jogo.constructLegField("wind_dir", jogo.passThrough),
  jogo.constructLegField("wind_speed", jogo.passThrough),
  jogo.constructLegField("temp", jogo.passThrough),
  jogo.constructLegField("tc", jogo.passThrough),
  jogo.constructLegField("th", Math.round),
  jogo.constructLegField("mh", Math.round),
  jogo.constructLegField("ch", Math.round),
  jogo.constructLegField("wca", Math.round),
  jogo.constructLegField("var", jogo.passThrough),
  jogo.constructLegField("dev", Math.round),
  jogo.constructLegField("waypoint", jogo.passThrough),
  jogo.constructLegField("altitude", jogo.passThrough),
  jogo.constructLegField("std_tmp_c", jogo.passThrough),
  jogo.constructLegField("leg_dist", Math.round),
  jogo.constructLegField("remaining_dist", Math.round),
  jogo.constructLegField("gs_est", Math.round),
  jogo.constructLegField("gs_act", Math.round),
  jogo.constructLegField("ete", Math.round),
  jogo.constructLegField("ate", jogo.passThrough),
  jogo.constructLegField("eta", jogo.passThrough),
  jogo.constructLegField("ata", jogo.passThrough),
  jogo.constructLegField("leg_fuel", jogo.roundToOneDecimalPoint),
  jogo.constructLegField("remaining_fuel", jogo.roundToOneDecimalPoint)
];

jogo.standardTemperatureCforAltitudeFt = function (altitudeFt) { "use strict";
  var stdTemperatureCDiff = altitudeFt * 2 / 1000;
  return 15 - stdTemperatureCDiff;
};

jogo.twoDigitString = function (val) { "use strict";
  val %= 100;
  return val < 10 ? '0' + val : val;
};

// Query local storage for all flight plans
// Allow storage namespacing by prefixing with 'jogo.'
jogo.loadFlightPlans = function () { "use strict";
  var i, key, thisPlan, planList;
  planList = $('select[name="saved_plans"]');
  planList.find('option').remove();

  for (i = 0; i < localStorage.length; i += 1) {
    key = localStorage.key(i);
    if (key.match(/^jogo\.[\w\d\s\-]+$/) !== null) {
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
  jogo.clearFlightPlan();
  jogo.displayFlightPlan(flightPlan);
};

jogo.displayFlightPlan = function (flightPlan) { "use strict";
  var i, f, currentFormField, currentFieldName, currentValue;
  document.flight_plan.plan_title.value = flightPlan.title || "";
  document.flight_plan.aircraft_ktas.value = flightPlan.ktas || "";
  document.flight_plan.aircraft_fuel_burn_gph.value = flightPlan.gph || "";

  for (i = 0; i < flightPlan.legs.length; i += 1) {
    if (i >= document.flight_plan.legmarker.length) { // 1) {
      jogo.addFlightPlanRow();
    }
    for (f = 0; f < jogo.legFields.length; f += 1) {
      currentFieldName = jogo.legFields[f].name;
      currentFormField = document.flight_plan[currentFieldName][i];
      currentValue = jogo.legFields[f].toString(flightPlan.legs[i][currentFieldName]);
      currentFormField.value = currentValue || "";
    }
  }
};

jogo.removeFlightPlan = function (title) { "use strict";
  localStorage.removeItem('jogo.' + title);
  jogo.loadFlightPlans();
};


jogo.flightPlanFormToObject = function () { "use strict";
  var f, i, plan = {};

  plan.title = $.trim(document.flight_plan.plan_title.value);
  plan.ktas = document.flight_plan.aircraft_ktas.value;
  plan.gph = document.flight_plan.aircraft_fuel_burn_gph.value;
  plan.legs = [];
  
  // Substitute for "document.flight_plan" yielding an unserializable circular structure
  // wind_dir wind_speed temp
  // tc th mh ch wca var dev
  // waypoint altitude std_tmp_c
  // leg_dist remaining_dist gs_est gs_act ete ate eta ata
  // leg_fuel remaining_fuel
  for (i = 0; i < document.flight_plan.legmarker.length; i += 1) {
    plan.legs[i] = {};
    for (f = 0; f < jogo.legFields.length; f += 1) {
      plan.legs[i][jogo.legFields[f].name] = document.flight_plan[jogo.legFields[f].name][i].value;
    }
  }
  return plan;
};

// Load flight plan from HTML5 local storage
// Flight plan keys are prefixed with 'jogo.' as a namespacing convention.
jogo.saveFlightPlan = function () { "use strict";
  var plan, planKey;
  plan = jogo.flightPlanFormToObject();
  planKey = 'jogo.' + plan.title;
  
  localStorage[planKey] = JSON.stringify(plan);
  jogo.loadFlightPlans();
};

jogo.toZulu = function (t) { "use strict";
  return jogo.twoDigitString(t.getUTCDate()) + jogo.twoDigitString(t.getUTCHours()) + jogo.twoDigitString(t.getUTCMinutes()) + "Z";
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
  var flightPlan, i, wcaGSPair, leg, remainingDist, remainingFuelBurn;
  flightPlan = jogo.flightPlanFormToObject(); 
  for (i = 0; i < flightPlan.legs.length; i += 1) {
    leg = flightPlan.legs[i];
    // apply winds to get wind correction angle and ground speed for each leg
    wcaGSPair = jogo.applyWinds(leg.tc, flightPlan.ktas, leg.wind_dir, leg.wind_speed);
    leg.wca = wcaGSPair.wcaDeg;
    leg.gs_est = wcaGSPair.gsKT;
    // calculate ETE based on distance and ground speed
    leg.ete = 60 * leg.leg_dist / leg.gs_est;
    // calculate fuel burn based on burn rate and ETE
    leg.leg_fuel = leg.ete * flightPlan.gph / 60;
    // calculate true and magnetic headings
    leg.th = parseFloat(leg.tc) + leg.wca;
    leg.mh = parseFloat(leg.th) + parseFloat(leg['var']);
  }

  remainingDist = 0;
  remainingFuelBurn = 0;
  // calculate distance and fuel burn remaining
  for (i = flightPlan.legs.length - 1; i >=0; i -= 1) {
    leg = flightPlan.legs[i];
    leg.remaining_dist = remainingDist;
    remainingDist += parseFloat(leg.leg_dist);
    leg.remaining_fuel = remainingFuelBurn;
    remainingFuelBurn += parseFloat(leg.leg_fuel);
  }

  // display the results on the form
  jogo.displayFlightPlan(flightPlan);
};

jogo.addFlightPlanRow = function () { "use strict";
  $('#vfr_plan tr[class="trip_leg"]:last').clone(true).insertAfter('#vfr_plan tr[class="trip_leg"]:last');
  $('#vfr_plan tr[class="trip_leg"]:last input').each(function () { this.value = ""; });
};

jogo.clearFlightPlan = function () { "use strict";
  var i, flightPlan;
  flightPlan = document.flight_plan;

  for (i = flightPlan.waypoint.length - 2; i >= 1; i -= 1) {
    $('#vfr_plan tr[class="trip_leg"]:eq(' + i + ')').remove();
  }

  jogo.displayFlightPlan({legs : [{}, {}]});
};

// return false to avoid a second page reload
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

  $('button[class="remove_leg"]').click(function () {
    $(this).parent().parent().remove();
    return false;
  });
 
  $('input[name="ata"]').click(function () {
    var now = new Date();
    this.value = jogo.toZulu(now);
  });
 
  $('#calculate').click(function () {
    jogo.calculate();
    return false;
  });
  
  $('#save').click(function () {
    jogo.saveFlightPlan();
    return false;
  });
  
  $('#load').click(function () {
    jogo.loadFlightPlan(document.flight_plan.saved_plans.value);
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
