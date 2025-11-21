(function() {
	if (location.pathname.match(/\/$/)) {
		// Preserve query string & hash
		const clean = location.pathname.replace(/\/$/, '') +
			location.search + location.hash;
		//  Replace = don’t keep the bad URL in history
		location.replace(clean);
	}
}());

var agreed = false;
$(document).ready(function() {
	$("#dialog").dialog({
		resizable: false,
		height: "auto",
		width: 800,
		modal: true,
		buttons: [
			{
				text: "Accept",
				click: function() {
					agreed = true;
					$(this).dialog("close");
				}

				// Uncommenting the following line would hide the text,
				// resulting in the label being used as a tooltip
				//showText: false
			}
		],
		/*buttons: {
		  "Accept": function() {
		  agreed = true;  
			$( this ).dialog( "close" );
		  }             
		},*/
		beforeClose: function() {
			return agreed;
		}
	});
});

const url = new URL(window.location.href);
const segments = url.pathname.split('/').filter(Boolean);
const stationCode = segments[segments.length - 1];



GWIS._nwisRootUrl = "../../gwis"; // this is the URL of the profiler. E.g. https://gs-service-production.geodab.eu/gs-service/services/bnhs

//
//
//

var plots = [];

var cutDate = function(date) {

	return date.substring(0, date.lastIndexOf(":")) + "Z";
};

var findValueOrEmpty = function(data, i, targetKey) {
	var fv = findValue(data, i, targetKey);
	if (typeof fv === 'undefined') {
		return "";
	} else {
		return fv;
	}
}

var findValue = function(data, i, targetKey) {

	var value;

	$.each(data[i], function(key, object) {

		if (key === targetKey) {
			value = object.value;
		}
	});

	return value;
}

var convertDate = function(isoDate) {

	var year = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(new Date(isoDate));
	var month = new Intl.DateTimeFormat('en', { month: 'long' }).format(new Date(isoDate));
	var day = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(new Date(isoDate));

	return day + " " + month + " " + year;
}

var createDataTable = function(data, i) {

	var items = [];

	var tempExtent = getTemporalExtent(data, i);
	// var spatialExtent = getSpatialExtent(data, i);

	var aggregationDuration = getAggregationDuration(data, i);

	var interpolationType = getInterpolationType(data, i);

	//
	// Observed variable
	//

	items.push("<tr><td class='data_table_header' colspan='2'>Observed variable</td></tr>");

	items.push("<tr><td class='data_table_label_td'>Observed variable</td><td>" + findValue(data, i, 'attribute_label') + "</td></tr>");

	items.push("<tr><td>Measurement unit</td><td>" + findValue(data, i, 'attribute_units') + " (" + findValue(data, i, 'attribute_units_abbreviation') + ") </td></tr>");

	items.push("<tr><td>Temporal extent</td><td>" + tempExtent + "</td></tr>");

	if (!(typeof aggregationDuration === 'undefined' || aggregationDuration === "")) {
		items.push("<tr><td>Aggregation duration</td><td>" + aggregationDuration + "</td></tr>");
	}

	if (!(typeof interpolationType === 'undefined' || interpolationType === "")) {
		switch (interpolationType) {
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/maxprec":
				interpolationType = "Maximum in the preceding interval";
				break;
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/minprec":
				interpolationType = "Minimum in the preceding interval";
				break;
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/totalprec":
				interpolationType = "Total in the preceding interval";
				break;

		}
		items.push("<tr><td>Interpolation type</td><td>" + interpolationType + "</td></tr>");
	}





	$("<table></table>", {

		"class": "data_table",
		html: items.join("")

	}).appendTo("#dataTable_" + i);
};

var getTemporalExtent = function(data, i) {

	var continuous = findValue(data, i, 'time_interpolation') === "CONTINUOUS" ? "Continuous" : null;

	var timeSupport = findValue(data, i, 'time_support');
	var timeUnits = findValue(data, i, 'time_units');

	var startDate = convertDate(findValue(data, i, 'time_start'));
	var endDate = convertDate(findValue(data, i, 'time_end'));
	var nearRealTime = findValue(data, i, 'near_real_time') === "yes" ? "yes" : null;
	if (nearRealTime) {
		endDate = "present";
	}

	var aggregates = continuous ? continuous : 'TO DO';

	if (continuous) {

		// as for Igor meeting 2020-12-3 
		//return continuous +", available for the period from "+startDate+" to "+endDate;   
	}

	return "Available for the period from " + startDate + " to " + endDate;
	// as for Igor meeting 2020-12-3
	//        " <i>(temporal interpolation not available)</i>";  
};

var getAggregationDuration = function(data, i) {

	var timeSupport = findValue(data, i, 'time_support');
	var timeUnits = findValue(data, i, 'time_units');

	if (!(typeof timeSupport === "undefined" || timeSupport === "")) {

		if (!(typeof timeUnits === "undefined" || timeUnits === "")) {

			timeUnits = timeUnits.toLowerCase();

			if (timeUnits === "minutes") {
				if (timeSupport === "1440") {
					timeSupport = "1";
					timeUnits = "day";
				}
				if (timeSupport === "60") {
					timeSupport = "1";
					timeUnits = "hour";
				}
			}
			if (timeSupport === "1" && timeUnits === "day") {
				timeSupport = "daily";
				timeUnits = "";
			}
			if (timeSupport === "7" && timeUnits === "day") {
				timeSupport = "weekly";
				timeUnits = "";
			}
			if (timeSupport === "1" && timeUnits === "hour") {
				timeSupport = "hourly";
				timeUnits = "";
			}
			if (timeSupport === "1" && timeUnits === "month") {
				timeSupport = "monthly";
				timeUnits = "";
			}
			return timeSupport + " " + timeUnits;
		}

	}

	return undefined;
	// as for Igor meeting 2020-12-3
	//        " <i>(temporal interpolation not available)</i>";  
};

var getInterpolationType = function(data, i) {

	var interpolationType = findValue(data, i, 'time_interpolation');


	if (typeof interpolationType === "undefined") {
		interpolationType = "";
	}

	if (interpolationType === "AVERAGE") {
		interpolationType = "mean";
	}
	if (interpolationType === "CONTINUOUS") {
		interpolationType = "instantaneous";
	}
	if (interpolationType === "MAXIMUM") {
		interpolationType = "maximum";
	}
	if (interpolationType === "MINIMUM") {
		interpolationType = "minimum";
	}

	return interpolationType.toLowerCase();

};




var getSpatialExtent = function(data, i) {

	var shape = findValue(data, i, 'DRAINAGE_SHAPEFILE') === "Y" ? "shape file available" : "shape file not available";

	var area = findValue(data, i, 'DRAINAGE_AREA');
	var effArea = findValue(data, i, 'EFFECTIVE_DRAINAGE_AREA');

	area = (area && area !== '#N/A') ? "river drainage area: " + area + " mq" : "river drainage area not available, ";
	effArea = (effArea && effArea !== '#N/A') ? "river effective drainage area: " + effArea + " mq" : "river effective drainage area not available";

	return findValue(data, i, 'attribute_label') + " by gauge.  " + area + " " + effArea + " (" + shape + ")";
};

var normalizeRoleLabel = function(role) {
	if (!role) {
		return '';
	}
	var spaced = role.replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/_/g, ' ');
	var parts = spaced.split(' ');
	for (var i = 0; i < parts.length; i++) {
		if (!parts[i]) {
			continue;
		}
		parts[i] = parts[i].charAt(0).toUpperCase() + parts[i].slice(1).toLowerCase();
	}
	return parts.join(' ').trim();
};

var collectOrganizations = function(data) {
	var groupsMap = {};
	for (var i = 0; i < data.length; i++) {
		var organizations = data[i].organizations;
		if (!organizations || !organizations.length) {
			continue;
		}
		for (var j = 0; j < organizations.length; j++) {
			var org = organizations[j];
			if (!org || typeof org !== 'object') {
				continue;
			}
			var rawName = (org.name || org.uri || '').trim();
			var displayName = rawName || 'Unknown organization';
			var uri = (org.uri || '').trim();
			var groupKey = displayName.toLowerCase() + '|' + uri.toLowerCase();
			if (!groupsMap[groupKey]) {
				groupsMap[groupKey] = {
					name: displayName,
					uri: uri,
					contributorsMap: {}
				};
			}
			var group = groupsMap[groupKey];

			// Normalize role to array
			var roles = org.role || [];
			if (!Array.isArray(roles)) {
				roles = roles ? [roles] : [];
			}

			// Normalize individual_name and individual_uri to arrays
			var individualNames = org.individual_name || [];
			if (!Array.isArray(individualNames)) {
				individualNames = individualNames ? [individualNames] : [];
			}
			var individualUris = org.individual_uri || [];
			if (!Array.isArray(individualUris)) {
				individualUris = individualUris ? [individualUris] : [];
			}

			// Get email (should be a single value, not an array)
			var contributorEmail = (org.email || '').trim();

			// If we have individual names/uris, create a contributor for each
			// Otherwise, create one contributor without a name
			var individuals = [];
			if (individualNames.length > 0 || individualUris.length > 0) {
				var maxLength = Math.max(individualNames.length, individualUris.length);
				for (var k = 0; k < maxLength; k++) {
					var name = (individualNames[k] || individualUris[k] || '').trim();
					if (name) {
						individuals.push(name);
					}
				}
			}

			// If no individuals, create one entry with empty name
			if (individuals.length === 0) {
				individuals.push('');
			}

			// Create or update contributors for each individual
			for (var k = 0; k < individuals.length; k++) {
				var contributorName = individuals[k];
				var contributorKey = contributorName.toLowerCase() + '|' + contributorEmail.toLowerCase();
				if (!contributorName && !contributorEmail) {
					contributorKey = 'organization-default';
				}
				if (!group.contributorsMap[contributorKey]) {
					group.contributorsMap[contributorKey] = {
						name: contributorName,
						email: contributorEmail,
						roles: []
					};
				}
				var contributor = group.contributorsMap[contributorKey];
				// Add all roles to this contributor
				for (var r = 0; r < roles.length; r++) {
					var role = (roles[r] || '').trim();
					if (role && contributor.roles.indexOf(role) === -1) {
						contributor.roles.push(role);
					}
				}
			}
		}
	}

	var groups = [];
	for (var key in groupsMap) {
		if (!Object.prototype.hasOwnProperty.call(groupsMap, key)) {
			continue;
		}
		var group = groupsMap[key];
		var contributors = [];
		for (var contributorKey in group.contributorsMap) {
			if (!Object.prototype.hasOwnProperty.call(group.contributorsMap, contributorKey)) {
				continue;
			}
			var contributor = group.contributorsMap[contributorKey];
			contributor.roles.sort(function(a, b) {
				var labelA = normalizeRoleLabel(a).toLowerCase();
				var labelB = normalizeRoleLabel(b).toLowerCase();
				if (labelA < labelB) {
					return -1;
				}
				if (labelA > labelB) {
					return 1;
				}
				return 0;
			});
			contributors.push(contributor);
		}
		contributors.sort(function(a, b) {
			var nameA = (a.name || '').toLowerCase();
			var nameB = (b.name || '').toLowerCase();
			if (nameA && !nameB) {
				return -1;
			}
			if (!nameA && nameB) {
				return 1;
			}
			if (nameA < nameB) {
				return -1;
			}
			if (nameA > nameB) {
				return 1;
			}
			var emailA = (a.email || '').toLowerCase();
			var emailB = (b.email || '').toLowerCase();
			if (emailA < emailB) {
				return -1;
			}
			if (emailA > emailB) {
				return 1;
			}
			return 0;
		});
		groups.push({
			name: group.name,
			uri: group.uri,
			contributors: contributors
		});
	}

	groups.sort(function(a, b) {
		var nameA = (a.name || '').toLowerCase();
		var nameB = (b.name || '').toLowerCase();
		if (nameA < nameB) {
			return -1;
		}
		if (nameA > nameB) {
			return 1;
		}
		return 0;
	});

	return groups;
};

var renderOrganizations = function($container, organizations) {
	$container.empty();
	if (!organizations.length) {
		return false;
	}
	var $list = $('<ul></ul>').addClass('organization-list');
	for (var i = 0; i < organizations.length; i++) {
		var group = organizations[i];
		var $item = $('<li></li>').addClass('organization-list-item');
		var $header = $('<div></div>').addClass('organization-header');

		if (group.uri) {
			$('<a></a>')
				.addClass('organization-name')
				.attr('href', group.uri)
				.attr('target', '_blank')
				.attr('rel', 'noopener noreferrer')
				.text(group.name)
				.appendTo($header);
		} else {
			$('<span></span>')
				.addClass('organization-name')
				.text(group.name)
				.appendTo($header);
		}

		$item.append($header);

		if (group.contributors && group.contributors.length) {
			var $contributorsList = $('<ul></ul>').addClass('organization-contributor-list');
			for (var j = 0; j < group.contributors.length; j++) {
				var contributor = group.contributors[j];
				var $contributorItem = $('<li></li>').addClass('organization-contributor-item');
				var contributorName = contributor.name;
				if (contributorName) {
					$('<span></span>')
						.addClass('organization-contributor-name')
						.text(contributorName)
						.appendTo($contributorItem);
				}

				if (contributor.roles && contributor.roles.length) {
					var roleLabels = [];
					for (var r = 0; r < contributor.roles.length; r++) {
						var normalized = normalizeRoleLabel(contributor.roles[r]);
						if (normalized) {
							roleLabels.push(normalized);
						}
					}
					if (roleLabels.length) {
						var rolesText = roleLabels.join(', ');
						var prefix = contributorName ? ' – ' : '';
						$('<span></span>')
							.addClass('organization-contributor-roles')
							.text(prefix + rolesText)
							.appendTo($contributorItem);
					}
				}

				if (contributor.email) {
					$('<a></a>')
						.addClass('organization-contributor-email')
						.attr('href', 'mailto:' + contributor.email)
						.text(contributor.email)
						.appendTo($contributorItem);
				}

				$contributorsList.append($contributorItem);
			}
			$item.append($contributorsList);
		}

		$list.append($item);
	}
	$container.append($list);
	return true;
};



var createPlot = function(data, i, recent) {

	var startTime = recent ? data[i].time_end_recent.value : cutDate(data[i].time_start.value);

	var endTime = cutDate(data[i].time_end.value);
	


	return GWIS.plot({

		title: data[i].attribute_label.value + " at the station: " + data[i].platform_label.value,

		xlabel: 'Time',

		ylabel: data[i].attribute_label.value + " (" + data[i].attribute_units_abbreviation.value + ")",

		start_dt: startTime,

		end_dt: endTime,

		div_id: "plot_" + i,

		series: [
			{
				site: data[i].platform_id.value,
				pcode: data[i].attribute_id.value,
				stroke_pattern: [7, 3],
				draw_points: true,
				label: data[i].attribute_label.value + " in " + data[i].attribute_units_abbreviation.value

			}
		],

		iv_local_or_utc: "utc",

		controls: "all",

		on_success: function(plot) {

			initDatePickers(data, i);
		},

		on_error: function(plot) {

			initDatePickers(data, i);
		}
	});
};

var initDatePickers = function(data, i) {

	var maxEndDate = cutDate(data[i].time_end.value);
	maxEndDate = maxEndDate.substring(0, maxEndDate.indexOf('T'));

	var minStartDate = cutDate(data[i].time_start.value);
	minStartDate = minStartDate.substring(0, minStartDate.indexOf('T'));

	var dstartId = "datepicker-start_" + i;
	var dendId = "datepicker-end_" + i;

	$("#" + dstartId).datepicker({

		dateFormat: 'yy-mm-dd',
		minDate: minStartDate,
		maxDate: maxEndDate,
		changeMonth: true,
		changeYear: true,

		onSelect: function(dateText) {

			var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

			data[plotId].time_start.value = dateText + "T00:00Z";

			$("#" + dendId).datepicker("option", "minDate", dateText);
		}
	});

	$("#" + dendId).datepicker({

		dateFormat: 'yy-mm-dd',
		minDate: minStartDate,
		maxDate: maxEndDate,
		changeMonth: true,
		changeYear: true,

		onSelect: function(dateText) {

			var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

			data[plotId].time_end.value = dateText + "T23:59Z";
		}
	});
};

var createTempExtentTable = function(data, i) {

	var dstartId = "datepicker-start_" + i;
	var dendId = "datepicker-end_" + i;

	var timeTable = "<table class='temp_extent_table'>";

	timeTable += "<tr><td colspan='3' class='temp_extent_info'>Please select time period of interest:</td></tr>";

	timeTable += "<tr><td colspan='3'>";
	timeTable += "<button class='time_button' id='time-button-2m_" + i + "'>Last 2 months</button> ";
	timeTable += "<button class='time_button' id='time-button-1y_" + i + "'>Last year</button> ";
	timeTable += "<button class='time_button' id='time-button-fe_" + i + "'>Full extent *</button>";
	timeTable += "</td></tr>";

	timeTable += "<tr><td class='start_date'>Start date</td><td><input type='text' id='" + dstartId + "' autocomplete='off'></td><td rowspan=2 id='update-button-td_" + i + "'></td></tr>";

	timeTable += "<tr><td class='end_date'>End date</td><td><input type='text' id='" + dendId + "' autocomplete='off'></td><td></td></tr>";

	timeTable += "</table>";

	$(timeTable).appendTo("#bottom-td_" + i);

	$("<button class='update_graph_button' id='button_" + i + "'>Update plot<i style='margin-left:5px' class='fas fa-sync'></i></button>").appendTo("#update-button-td_" + i);

	$("#button_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		plots[plotId] = createPlot(data, plotId);
	});

	$("#time-button-2m_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var recentDate = cutDate(data[i].time_end_recent.value);
		recentDate = recentDate.substring(0, recentDate.indexOf('T'));

		data[plotId].time_start.value = recentDate + "T00:00Z";
		$("#" + dstartId).datepicker("setDate", recentDate);

		var maxEndDate = $("#" + dendId).datepicker("option", "maxDate");

		data[plotId].time_end.value = maxEndDate + "T23:59Z";
		$("#" + dendId).datepicker("setDate", maxEndDate);
	});

	$("#time-button-1y_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var lastYearDate = cutDate(data[i].time_end_last_year.value);
		lastYearDate = lastYearDate.substring(0, lastYearDate.indexOf('T'));

		data[plotId].time_start.value = lastYearDate + "T00:00Z";
		$("#" + dstartId).datepicker("setDate", lastYearDate);

		var maxEndDate = $("#" + dendId).datepicker("option", "maxDate");

		data[plotId].time_end.value = maxEndDate + "T23:59Z";
		$("#" + dendId).datepicker("setDate", maxEndDate);
	});

	$("#time-button-fe_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var minStartDate = $("#" + dstartId).datepicker("option", "minDate");

		data[plotId].time_start.value = minStartDate + "T00:00Z";
		$("#" + dstartId).datepicker("setDate", minStartDate);

		var maxEndDate = $("#" + dendId).datepicker("option", "maxDate");

		data[plotId].time_end.value = maxEndDate + "T23:59Z";
		$("#" + dendId).datepicker("setDate", maxEndDate);
	});


};

var createLayoutTable = function(data, i, k) {

	var layoutTable = "<table class='layout_table' id='layoutTable_" + i + "'>";

	var label = data[i].attribute_label.value; // +" from "+convertDate(data[i].time_start.value)+" to "+convertDate(data[i].time_end.value);

	layoutTable += "<th colspan='2'>Time series " + (k + 1) + ": " + label + "</th>";

	layoutTable += "<tr><td class='data_table_td' id='dataTable_" + i + "'></td>";

	layoutTable += "<td rowspan='2'id='plotDiv_" + i + "'></td></tr>";

	layoutTable += "<tr><td id='bottom-td_" + i + "'></td></tr>";

	layoutTable += "</table>";

	$(layoutTable).appendTo("#timeseries");
};

var download = function(button, event, data) {

	$("#dialog-download").dialog({
		resizable: false,
		height: "auto",

		modal: true,
		buttons: {
			"Download": function() {

				var dataId = button.id.substring(button.id.indexOf('_') + 1, button.id.length);

				var query = "../../cuahsi_1_1.asmx?";

				query += "request=GetValuesObject&site=" + data[dataId].platform_id.value + "&variable=" + data[dataId].attribute_id.value;

				var format = $("#format").val();

				query += "&beginDate=" + data[dataId].time_start.value + "&endDate=" + data[dataId].time_end.value + "&format=" + format;

				$(this).dialog("close");

				window.open(query);

			},
			Cancel: function() {
				$(this).dialog("close");
			}
		}
	});


}
const queryString = window.location.search; // includes the leading "?" if present

$.getJSON(stationCode + "/timeseries" + queryString, function(data) {
	const disclaimersSet = new Set();
	for (let i = 0; i < data.length; i++) {
		const disclaimerValue = findValue(data, i, 'data_disclaimer');
		if (disclaimerValue && typeof disclaimerValue === 'string' && disclaimerValue.trim().length > 0) {
			disclaimersSet.add(disclaimerValue.trim());
		}
	}
	if (disclaimersSet.size > 0) {
		const dialogEl = $('#dialog');
		dialogEl.empty();
		dialogEl.append($('<b>').text('Disclaimer to accept'));
		dialogEl.append('<br><br>');
		const list = $('<ul>');
		Array.from(disclaimersSet).forEach(function(text) {
			list.append($('<li>').text(text));
		});
		dialogEl.append(list);
	}

	$("#timeseries").removeAttr("class");



	var indexes = [];
	for (var i = 0, len = data.length; i < len; i++) {
		indexes.push(i);
	}
	indexes.sort(function(a, b) {
		v1 = findValue(data, a, 'attribute_label');
		v2 = findValue(data, b, 'attribute_label');
		if (v1 < v2) {
			return -1;
		}
		if (v1 > v2) {
			return 1;
		}
		return 0;
	})

	//
	// Station/platform
	//




	var lat = findValue(data, 0, 'latitude');

	var lon = findValue(data, 0, 'longitude');

	var geoLocation = "lat: " + lat + "° lon: " + lon + "°";

	const pointerCoordinates = ol.proj.fromLonLat([lon, lat]);

	// Create a vector layer for the pointer
	const pointerLayer = new ol.layer.Vector({
		source: new ol.source.Vector({
			features: [
				new ol.Feature({
					geometry: new ol.geom.Point(pointerCoordinates),
				}),
			],
		}),
		style: new ol.style.Style({
			image: new ol.style.Circle({
				radius: 6,
				fill: new ol.style.Fill({ color: 'red' }),
				stroke: new ol.style.Stroke({ color: 'white', width: 2 }),
			}),
		}),
	});

	// Create the map
	const map = new ol.Map({
		target: 'map',
		layers: [
			// Base layer using OpenStreetMap
			new ol.layer.Tile({
				source: new ol.source.OSM(),
			}),
			// Pointer layer
			pointerLayer,
		],
		view: new ol.View({
			center: pointerCoordinates,
			zoom: 3,
		}),
	});


	document.getElementById('station-name-td').innerHTML = findValue(data, 0, 'platform_label');

	document.getElementById('source-label-td').innerHTML = findValue(data, 0, 'source_label');
	
	pil = findValue(data, 0, 'platform_id_local');
	if (pil!=null){
		document.getElementById('platform_id_local').innerHTML = pil;	
	}
	
	

	document.getElementById('country-td').innerHTML = findValue(data, 0, 'COUNTRY');

	var $originatorCell = $('#originator-td');
	var organizations = collectOrganizations(data);
	var rendered = renderOrganizations($originatorCell, organizations);
	if (!rendered) {
		$originatorCell.text(findValueOrEmpty(data, 0, 'ORIGINATOR'));
	}

	document.getElementById('geolocation-td').innerHTML = geoLocation;



	for (var x = 0, len = data.length; x < len; x++) {

		i = indexes[x];
		//
		// layout table
		//

		createLayoutTable(data, i, x);

		//
		// data table
		//

		createDataTable(data, i);

		//
		// plots
		//

		var divWidth = window.innerWidth - 595;

		$("<div id='plot_" + i + "' class='plot_div'></div>").appendTo("#plotDiv_" + i);

		plots[i] = createPlot(data, i, true);

		//
		// temporal extent table
		//

		createTempExtentTable(data, i);

		//
		// download
		//

		$("<button class='download_button' id='download_" + i + "'>Download data<i style='margin-left:5px' class='fas fa-download'></i></button>").appendTo("#bottom-td_" + i);

		$("#download_" + i).click(function(event) {

			download(this, event, data);
		});
	}

	$(".page_footer").css("visibility", "visible");

})



