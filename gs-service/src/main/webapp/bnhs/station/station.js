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
				text: t('accept'),
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

// i18n support
var i18n = { current: 'en', en: {}, it: {} };
function loadI18nSync(lang) {
	var desired = (localStorage.getItem('lang') || lang || 'en').toLowerCase();
	i18n.current = (desired === 'it') ? 'it' : 'en';

	function loadJsonSync(paths) {
		for (var j = 0; j < paths.length; j++) {
			try {
				var xhr = new XMLHttpRequest();
				xhr.open('GET', paths[j], false);
				xhr.send(null);
				if (xhr.status >= 200 && xhr.status < 300 && (xhr.responseText || '').trim().length > 0) {
					return JSON.parse(xhr.responseText);
				}
			} catch (e) { }
		}
		return {};
	}

	// Base translations from gi-portal
	// Try multiple path resolutions
	var baseEnUrls = [
		'/gs-service/gi-portal/lang/en.json',
		'../gi-portal/lang/en.json',
		'../../gi-portal/lang/en.json'
	];
	var baseItUrls = [
		'/gs-service/gi-portal/lang/it.json',
		'../gi-portal/lang/it.json',
		'../../gi-portal/lang/it.json'
	];

	// Station-specific translations (try multiple paths)
	var localEnUrls = [
		'/gs-service/bnhs/station/lang/en.json',
		'./lang/en.json',
		'lang/en.json',
		'../station/lang/en.json'
	];
	var localItUrls = [
		'/gs-service/bnhs/station/lang/it.json',
		'./lang/it.json',
		'lang/it.json',
		'../station/lang/it.json'
	];

	var baseEn = loadJsonSync(baseEnUrls);
	var baseIt = loadJsonSync(baseItUrls);
	var overrideEn = loadJsonSync(localEnUrls);
	var overrideIt = loadJsonSync(localItUrls);

	// Merge base + overrides (overrides win)
	i18n.en = Object.assign({}, baseEn, overrideEn);
	i18n.it = Object.assign({}, baseIt, overrideIt);
	
	// Debug: log if translations loaded
	if (Object.keys(i18n[i18n.current]).length === 0) {
		console.warn('No translations loaded for language:', i18n.current);
	} else {
		console.log('Loaded', Object.keys(i18n[i18n.current]).length, 'translations for', i18n.current);
	}
}

function interpolate(template, vars) {
	if (!template || !vars) return template;
	return template.replace(/\$\{([^}]+)\}/g, function(_, k) { return (vars[k] != null) ? vars[k] : ''; });
}

function t(key, vars) {
	var cur = (i18n[i18n.current] || {});
	var str = cur[key] || i18n.en[key] || key;
	return interpolate(str, vars);
}

function lang() {
	return i18n.current;
}

function openLanguageChooser() {
	try {
		const dialogDiv = $('<div>');
		dialogDiv.append($('<div>').text(t('choose_language')).css({ 'margin-bottom': '10px' }));
		const current = i18n.current || 'en';
		const enOpt = $('<div>');
		enOpt.append($('<input>').attr({ type: 'radio', name: 'langSel', id: 'lang_en', value: 'en', checked: (current === 'en') }));
		enOpt.append($('<label>').attr('for', 'lang_en').text(t('language_en')).css({ 'margin-left': '6px' }));
		const itOpt = $('<div>');
		itOpt.append($('<input>').attr({ type: 'radio', name: 'langSel', id: 'lang_it', value: 'it', checked: (current === 'it') }));
		itOpt.append($('<label>').attr('for', 'lang_it').text(t('language_it')).css({ 'margin-left': '6px' }));
		dialogDiv.append(enOpt).append(itOpt);
		dialogDiv.dialog({
			title: t('change_language_title'),
			modal: true,
			width: 360,
			buttons: [
				{
					text: 'OK', click: function() {
						const sel = dialogDiv.find('input[name="langSel"]:checked').val() || 'en';
						localStorage.setItem('lang', sel);
						$(this).dialog('close');
						window.location.reload();
					}
				},
				{ text: t('cancel'), click: function() { $(this).dialog('close'); } }
			]
		});
	} catch (e) {
		// Fallback: just toggle en/it and reload
		const next = (i18n.current === 'it') ? 'en' : 'it';
		try { localStorage.setItem('lang', next); } catch (ex) { }
		window.location.reload();
	}
}

// Initialize i18n
try { 
	loadI18nSync(); 
} catch (e) { 
	console.error('Error loading translations:', e);
}

// Translate static HTML content
function translateStaticContent() {
	// Translate page info text
	var $pageInfo = $('.page_info_text');
	if ($pageInfo.length) {
		var netcdfLink = '<a href="https://www.ogc.org/standards/netcdf">' + t('netcdf_link') + '</a>';
		var waterml2Link = '<a href="https://www.ogc.org/standards/waterml">' + t('waterml2_link') + '</a>';
		var waterml1Link = '<a href="https://his.cuahsi.org/documents/WaterML_1_0_part1.pdf">' + t('waterml1_link') + '</a>';
		
		var metadataSectionInfo = t('metadata_section_info', {
			netcdf: netcdfLink,
			waterml2: waterml2Link,
			waterml1: waterml1Link
		});
		
		$pageInfo.html(
			t('page_info_text') + ' <br> ' +
			t('plot_features_include') + ' <br> ' +
			'• ' + t('interactive_zoom') + '<br> ' +
			'• ' + t('interactive_legend') + ' <br> ' +
			'<br> ' +
			t('metadata_info') + ' <br> ' +
			'<br> ' +
			metadataSectionInfo
		);
	}
	
	// Translate sub title
	var $subTitle = $('.sub_title');
	if ($subTitle.length) {
		var subTitleText = $subTitle.text().trim();
		if (subTitleText === 'Station information page' || subTitleText.indexOf('Station') !== -1) {
			$subTitle.text(t('station_information_page'));
		}
	}
	
	// Translate footer note
	var $footerNote = $('.page_footer > div:first-child');
	if ($footerNote.length) {
		var footerText = $footerNote.text();
		if (footerText.indexOf('Please note') !== -1 || footerText.indexOf('data plotting') !== -1 || footerText.indexOf('*') === 0) {
			$footerNote.text(t('full_extent_note'));
		}
	}
	
	// Translate station table headers
	var $stationTable = $('.station-meta-table');
	if ($stationTable.length) {
		$stationTable.find('td:contains("Station/platform")').text(t('station_platform'));
		$stationTable.find('td:contains("Name")').not('#station-name-td').text(t('name'));
		$stationTable.find('td:contains("Data publisher")').not('#source-label-td').text(t('data_publisher'));
		$stationTable.find('td:contains("Platform id")').not('#platform_id_local').text(t('platform_id'));
		$stationTable.find('td:contains("Territory of origin of data")').not('#country-td').text(t('territory_origin'));
		$stationTable.find('td:contains("Involved organizations")').not('#originator-td').text(t('involved_organizations'));
		$stationTable.find('td:contains("Geospatial location")').not('#geolocation-td').text(t('geospatial_location'));
	}
}

// Add language button to header and translate static content
$(document).ready(function() {
	const curLang = (i18n.current || 'en').toUpperCase();
	const langBtn = $('<button>')
		.attr('id', 'langBtn')
		.addClass('lang-button')
		.css({
			'float': 'right',
			'margin-right': '20px',
			'margin-top': '10px',
			'padding': '5px 15px',
			'background-color': 'var(--color4)',
			'color': 'var(--color1)',
			'border': '1px solid var(--color1)',
			'cursor': 'pointer',
			'font-size': '14px'
		})
		.text(curLang)
		.attr('title', t('menu_change_language'));
	
	langBtn.on('click', function(e) {
		e.preventDefault();
		openLanguageChooser();
	});
	
	$('.page_header').append(langBtn);
	
	// Translate static HTML content
	translateStaticContent();
	
	// Translate page title if it contains "Pagina della stazione" or "Station"
	var pageTitle = document.title;
	if (pageTitle.indexOf('Pagina della stazione') !== -1 || pageTitle.indexOf('Station') !== -1) {
		document.title = t('station_information_page');
	}
});

// Time series plots (formerly GWIS-based) and shared helpers
var plots = [];

var cutDate = function(date) {

	return date.substring(0, date.lastIndexOf(":")) + "Z";
};

// Extract YYYY-MM-DD from various ISO-ish strings (e.g. "2025-12-18", "2025-12-18T10:00:00Z")
var dateOnly = function(value) {
	if (!value || typeof value !== 'string') {
		return "";
	}
	var m = value.match(/^(\d{4}-\d{2}-\d{2})/);
	return m ? m[1] : "";
};

var formatPickerDate = function(value) {
	if (!value) {
		return "";
	}
	// jQuery UI datepicker option values may be Date objects
	if (value instanceof Date) {
		try {
			return $.datepicker.formatDate('yy-mm-dd', value);
		} catch (e) {
			return "";
		}
	}
	if (typeof value === 'string') {
		return dateOnly(value) || value;
	}
	return "";
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

// Get attribute label with language support (uses attribute_label_it for Italian, attribute_label otherwise)
var getAttributeLabel = function(data, i) {
	var currentLang = lang();
	if (currentLang === 'it') {
		var itLabel = findValue(data, i, 'attribute_label_it');
		if (itLabel !== undefined && itLabel !== null && itLabel !== "") {
			return itLabel;
		}
	}
	return findValue(data, i, 'attribute_label');
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

	items.push("<tr><td class='data_table_header' colspan='2'>" + t('timeseries_metadata') + "</td></tr>");

	items.push("<tr><td class='data_table_label_td'>" + t('observed_variable') + "</td><td>" + getAttributeLabel(data, i) + "</td></tr>");

	var attributeUnits = findValue(data, i, 'attribute_units');
	var unitAbbr = findValue(data, i, 'attribute_units_abbreviation');
	
	var unitDisplay = "";
	if (attributeUnits !== undefined && attributeUnits !== null && attributeUnits !== "") {
		// If attribute_units exists, show it with abbreviation in parentheses if available
		if (unitAbbr !== undefined && unitAbbr !== null && unitAbbr !== "") {
			unitDisplay = attributeUnits + " (" + unitAbbr + ")";
		} else {
			unitDisplay = attributeUnits;
		}
	} else {
		// If attribute_units is undefined, just show the abbreviation
		if (unitAbbr !== undefined && unitAbbr !== null && unitAbbr !== "") {
			unitDisplay = unitAbbr;
		} else {
			unitDisplay = "";
		}
	}
					
	items.push("<tr><td>" + t('measurement_unit') + "</td><td>" + unitDisplay + "</td></tr>");	

	items.push("<tr><td>" + t('temporal_extent') + "</td><td>" + tempExtent + "</td></tr>");

	if (!(typeof aggregationDuration === 'undefined' || aggregationDuration === "")) {
		items.push("<tr><td>" + t('aggregation_duration') + "</td><td>" + aggregationDuration + "</td></tr>");
	}

	if (!(typeof interpolationType === 'undefined' || interpolationType === "")) {
		switch (interpolationType) {
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/maxprec":
				interpolationType = t('maximum_preceding');
				break;
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/minprec":
				interpolationType = t('minimum_preceding');
				break;
			case "http://www.opengis.net/def/waterml/2.0/interpolationtype/totalprec":
				interpolationType = t('total_preceding');
				break;

		}
		items.push("<tr><td>" + t('interpolation_type') + "</td><td>" + interpolationType + "</td></tr>");
	}





	$("<table></table>", {

		"class": "data_table",
		html: items.join("")

	}).appendTo("#dataTable_" + i);
};

var getTemporalExtent = function(data, i) {

	var continuous = findValue(data, i, 'time_interpolation') === "CONTINUOUS" ? t('continuous') : null;

	var timeSupport = findValue(data, i, 'time_support');
	var timeUnits = findValue(data, i, 'time_units');

	var startDate = convertDate(findValue(data, i, 'time_start'));
	var endDate = convertDate(findValue(data, i, 'time_end'));
	var nearRealTime = findValue(data, i, 'near_real_time') === "yes" ? "yes" : null;
	if (nearRealTime) {
		endDate = t('present');
	}

	var aggregates = continuous ? continuous : 'TO DO';

	if (continuous) {

		// as for Igor meeting 2020-12-3 
		//return continuous +", available for the period from "+startDate+" to "+endDate;   
	}

	return t('available_for_period') + " " + startDate + " " + t('to') + " " + endDate;
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
		interpolationType = t('mean');
	}
	if (interpolationType === "CONTINUOUS") {
		interpolationType = t('instantaneous');
	}
	if (interpolationType === "MAXIMUM") {
		interpolationType = t('maximum');
	}
	if (interpolationType === "MINIMUM") {
		interpolationType = t('minimum');
	}

	return interpolationType;

};




var getSpatialExtent = function(data, i) {

	var shape = findValue(data, i, 'DRAINAGE_SHAPEFILE') === "Y" ? "shape file available" : "shape file not available";

	var area = findValue(data, i, 'DRAINAGE_AREA');
	var effArea = findValue(data, i, 'EFFECTIVE_DRAINAGE_AREA');

	area = (area && area !== '#N/A') ? "river drainage area: " + area + " mq" : "river drainage area not available, ";
	effArea = (effArea && effArea !== '#N/A') ? "river effective drainage area: " + effArea + " mq" : "river effective drainage area not available";

	return getAttributeLabel(data, i) + " by gauge.  " + area + " " + effArea + " (" + shape + ")";
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



// Build OM API URL for a given time series and time window.
// format: optional; when set, used for download (no limit). When omitted, limit=1 for plot.
var buildOmApiUrl = function(timeseriesId, startTime, endTime, format) {
	// Use relative path so token/view are inherited from the current URL.
	// Current page is typically .../view/<viewId>/annali/bnhs/station/<stationCode>
	// OM API lives at .../view/<viewId>/annali/om-api/observations
	var baseUrl = "../../om-api/observations";
	var params = [];
	if (timeseriesId) {
		params.push("observationIdentifier=" + encodeURIComponent(timeseriesId));
	}
	if (startTime) {
		params.push("beginPosition=" + encodeURIComponent(startTime));
	}
	if (endTime) {
		params.push("endPosition=" + encodeURIComponent(endTime));
	}
	params.push("includeData=true");
	if (format) {
		params.push("format=" + encodeURIComponent(format));
	} else {
		params.push("limit=1");
	}
	return baseUrl + "?" + params.join("&");
};

// Analyze qualifiers for a set of points and update the qualifiers box UI
var updateQualifiersBox = function(seriesIndex, points) {

	var boxId = "qualifiers-box_" + seriesIndex;
	var $box = $("#" + boxId);
	if ($box.length === 0) {
		$box = $("<div></div>", {
			id: boxId,
			class: "qualifiers-box"
		}).css({
			"margin-top": "10px",
			"padding": "8px",
			"border": "1px solid #ccc",
			"background-color": "#f9f9f9",
			"font-size": "12px",
			"max-width": "420px"
		}).appendTo("#bottom-td_" + seriesIndex);
	}

	// Collect qualifiers per point
	var qualifiersPerPoint = [];
	for (var p = 0; p < points.length; p++) {
		var pt = points[p] || {};
		var meta = pt.metadata || {};
		var quals = meta.qualifiers || [];
		if (!Array.isArray(quals)) {
			quals = [quals];
		}
		qualifiersPerPoint.push(quals);
	}

	var allKeysMap = {};
	for (var i = 0; i < qualifiersPerPoint.length; i++) {
		var qArr = qualifiersPerPoint[i];
		for (var j = 0; j < qArr.length; j++) {
			var q = qArr[j] || {};
			var key = q.key || "";
			if (!key) {
				continue;
			}
			allKeysMap[key] = true;
		}
	}

	var commonQualifiers = {};
	var variableQualifiers = {};

	for (var key in allKeysMap) {
		if (!Object.prototype.hasOwnProperty.call(allKeysMap, key)) {
			continue;
		}
		var valuesForKey = [];
		var allEqual = true;
		var firstValueSet = false;
		var firstValue = null;

		for (var pi = 0; pi < qualifiersPerPoint.length; pi++) {
			var qList = qualifiersPerPoint[pi];
			var valueForPoint = null;
			for (var qi = 0; qi < qList.length; qi++) {
				if (qList[qi] && qList[qi].key === key) {
					valueForPoint = qList[qi].term || "";
					break;
				}
			}
			valuesForKey.push(valueForPoint);
			if (!firstValueSet) {
				firstValue = valueForPoint;
				firstValueSet = true;
			} else if (valueForPoint !== firstValue) {
				allEqual = false;
			}
		}

		if (allEqual) {
			commonQualifiers[key] = firstValue;
		} else {
			variableQualifiers[key] = valuesForKey;
		}
	}

	// Store variable qualifiers so hover handler can update values
	if (!plots[seriesIndex]) {
		plots[seriesIndex] = {};
	}
	plots[seriesIndex].variableQualifiers = variableQualifiers;

	// Build UI content
	$box.empty();



	// Helper: get a human-readable, localized label for a qualifier key
	var qualifierLabel = function(key) {
		if (!key) {
			return '';
		}
		// Try i18n key like "qualifier_qualityFlag"
		var tKey = 'qualifier_' + key;
		var translated = t(tKey);
		if (translated && translated !== tKey) {
			return translated;
		}
		// Fallback: normalize key (camelCase / snake_case) to spaced label
		var normalized = key.replace(/_/g, ' ');
		normalized = normalized.replace(/([a-z0-9])([A-Z])/g, '$1 $2');
		normalized = normalized.charAt(0).toUpperCase() + normalized.slice(1);
		return normalized;
	};

	// Predefined order for qualifiers
	var qualifierOrder = [
		'referenceCitation',
		'editorIndividual',
		'editorRole',
		'editorOrganization',
		'annalBasinName',
		'annalStationName',		
		'annalInstrumentType',
		'annalInstrumentQuote',
		'qualityFlag'
	];

	// Helper: get sort order for a qualifier key (lower = earlier)
	var qualifierSortOrder = function(key) {
		var index = qualifierOrder.indexOf(key);
		return index >= 0 ? index : 999; // Unknown keys go to the end
	};

	// Sort common qualifier keys according to predefined order
	var commonKeys = [];
	for (var cKey in commonQualifiers) {
		if (Object.prototype.hasOwnProperty.call(commonQualifiers, cKey)) {
			commonKeys.push(cKey);
		}
	}
	commonKeys.sort(function(a, b) {
		return qualifierSortOrder(a) - qualifierSortOrder(b);
	});

	// Common qualifiers (identical for all points)
	var hasCommon = false;
	for (var ci = 0; ci < commonKeys.length; ci++) {
		var cKey = commonKeys[ci];
		hasCommon = true;
		var cVal = commonQualifiers[cKey];
		var cLabel = qualifierLabel(cKey);
		var $row = $("<div></div>").css({ "margin-bottom": "2px" });
		$row.append($("<span></span>").css({ "font-weight": "bold" }).text(cLabel + ": "));
		$row.append($("<span></span>").text(cVal));
		$box.append($row);
	}

	// Sort variable qualifier keys according to predefined order
	var variableKeys = [];
	for (var vKey in variableQualifiers) {
		if (Object.prototype.hasOwnProperty.call(variableQualifiers, vKey)) {
			variableKeys.push(vKey);
		}
	}
	variableKeys.sort(function(a, b) {
		return qualifierSortOrder(a) - qualifierSortOrder(b);
	});

	// Variable qualifiers (change per point; show placeholder until hover)
	var hasVariable = false;
	for (var vi = 0; vi < variableKeys.length; vi++) {
		var vKey = variableKeys[vi];
		hasVariable = true;
		var vLabel = qualifierLabel(vKey);
		var placeholder = t('hover_to_display');
		var spanId = "qual-var_" + seriesIndex + "_" + vKey;
		var $vRow = $("<div></div>").css({ "margin-bottom": "2px" });
		$vRow.append($("<span></span>").css({ "font-weight": "bold" }).text(vLabel + ": "));
		$vRow.append($("<span></span>").attr("id", spanId).text(placeholder));
		$box.append($vRow);
	}

	if (!hasCommon && !hasVariable) {
		$box.append($("<div></div>").text(t('no_data_available')));
	}
};

// Create / update a time series plot in the given div using Plotly and OM JSON
var createPlot = function(data, i) {

	var containerId = "plot_" + i;
	var $container = $("#" + containerId);

	// Fallback if container is missing
	if ($container.length === 0) {
		return;
	}

	// Extract current time window from metadata (already kept in sync with datepickers)
	var startTime = cutDate(data[i].time_start.value);
	var endTime = cutDate(data[i].time_end.value);

	// Extract time series identifier for OM API
	var timeseriesId = findValue(data, i, 'timeseries_id');

	if (!timeseriesId) {
		$container.html('<div style="padding: 20px; text-align: center; color: red;">' +
			t('no_data_available') + ' (' + 'missing timeseries_id' + ')' +
			'</div>');
		return;
	}

	// Show loading message
	$container.html('<div style="padding: 20px; text-align: center;">' + t('loading') + '...</div>');

	// Ensure Plotly is available (similar pattern as rating curves)
	var ensurePlotlyAndRender = function(points) {
		if (typeof Plotly === 'undefined') {
			var plotlyScript = document.querySelector('script[src*="plotly"]');
			if (!plotlyScript) {
				$container.html('<div style="padding: 20px; text-align: center; color: red;">' +
					t('plot_library_error') + '<br><small>Plotly.js script tag not found</small></div>');
				return;
			}
			var attempts = 0;
			var maxAttempts = 50;
			var checkPlotly = setInterval(function() {
				attempts++;
				if (typeof Plotly !== 'undefined') {
					clearInterval(checkPlotly);
					renderPlot(points);
				} else if (attempts >= maxAttempts) {
					clearInterval(checkPlotly);
					$container.html('<div style="padding: 20px; text-align: center; color: red;">' +
						t('plot_library_error') + '<br><small>Check browser console for details</small></div>');
				}
			}, 100);
		} else {
			renderPlot(points);
		}
	};

	// Render the Plotly time series chart
	var renderPlot = function(points) {
		if (!points || points.length === 0) {
			$container.html('<div style="padding: 20px; text-align: center;">' + t('no_data_available') + '</div>');
			return;
		}

		var times = [];
		var values = [];
		var qualityFlags = [];
		for (var p = 0; p < points.length; p++) {
			var pt = points[p];
			// Skip missing data values (-9999)
			if (pt.time && pt.time.instant != null && typeof pt.value !== 'undefined' && pt.value !== null && pt.value !== -9999) {
				times.push(pt.time.instant);
				values.push(pt.value);
				
				// Extract qualityFlag from qualifiers
				var qualityFlag = "";
				var meta = pt.metadata || {};
				var quals = meta.qualifiers || [];
				if (!Array.isArray(quals)) {
					quals = [quals];
				}
				for (var q = 0; q < quals.length; q++) {
					if (quals[q] && quals[q].key === "qualityFlag") {
						qualityFlag = quals[q].term || "";
						break;
					}
				}
				qualityFlags.push(qualityFlag);
			}
		}

		if (times.length === 0) {
			$container.html('<div style="padding: 20px; text-align: center;">' + t('no_data_available') + '</div>');
			return;
		}

		var yUnitAbbr = data[i].attribute_units_abbreviation && data[i].attribute_units_abbreviation.value ?
			data[i].attribute_units_abbreviation.value : "";
		var yAxisTitle = getAttributeLabel(data, i) +
			(yUnitAbbr ? " (" + yUnitAbbr + ")" : "");

		// Clear any previous content such as the "Loading..." label
		$container.empty();

		// Build hover text array with qualityFlag and units
		var hoverTexts = [];
		for (var h = 0; h < times.length; h++) {
			var hoverText = times[h];
			var valueWithUnit = values[h];
			if (yUnitAbbr) {
				valueWithUnit = valueWithUnit + " " + yUnitAbbr;
			}
			hoverText += "<br>" + valueWithUnit;
			if (qualityFlags[h]) {
				hoverText += "<br>qualityFlag: " + qualityFlags[h];
			}
			hoverTexts.push(hoverText);
		}

		var trace = {
			x: times,
			y: values,
			type: 'scatter',
			mode: 'lines+markers',
			name: getAttributeLabel(data, i),
			line: {
				color: '#00529c',
				width: 2
			},
			marker: {
				color: '#00529c',
				size: 4
			},
			hovertemplate: '%{text}<extra></extra>',
			text: hoverTexts
		};

		var layout = {
			title: {
				text: getAttributeLabel(data, i) + " " + t('at_station') + " " + data[i].platform_label.value,
				font: {
					size: 16,
					color: '#000000'
				}
			},
			xaxis: {
				title: {
					text: t('time')
				},
				showgrid: true,
				gridcolor: '#e0e0e0'
			},
			yaxis: {
				title: {
					text: yAxisTitle
				},
				showgrid: true,
				gridcolor: '#e0e0e0'
			},
			plot_bgcolor: '#ffffff',
			paper_bgcolor: '#ffffff',
			hovermode: 'closest',
			margin: {
				l: 60,
				r: 30,
				t: 50,
				b: 50
			},
			showlegend: false
		};

		var config = {
			displayModeBar: true,
			displaylogo: false,
			modeBarButtonsToRemove: ['lasso2d', 'select2d'],
			responsive: true
		};

		var plotElement = document.getElementById(containerId);
		if (!plotElement) {
			return;
		}

		Plotly.newPlot(containerId, [trace], layout, config);

		// Attach hover handler once to update variable qualifier values
		if (!plotElement._qualifiersHoverBound) {
			plotElement._qualifiersHoverBound = true;
			plotElement.on('plotly_hover', function(eventData) {
				if (!eventData || !eventData.points || !eventData.points.length) {
					return;
				}
				var pointIndex = eventData.points[0].pointIndex;
				var seriesMeta = plots[i] && plots[i].variableQualifiers;
				if (!seriesMeta) {
					return;
				}
				for (var vKey in seriesMeta) {
					if (!Object.prototype.hasOwnProperty.call(seriesMeta, vKey)) {
						continue;
					}
					var vals = seriesMeta[vKey];
					var term = (vals && typeof vals[pointIndex] !== 'undefined' && vals[pointIndex] !== null) ?
						vals[pointIndex] : "";
					var spanId = "qual-var_" + i + "_" + vKey;
					var $span = $("#" + spanId);
					if ($span.length) {
						if (term === "") {
							var placeholder = t('no_data_available');
							$span.text(placeholder);
						} else {
							$span.text(term);
						}
					}
				}
			});

			plotElement.on('plotly_unhover', function() {
				var seriesMeta = plots[i] && plots[i].variableQualifiers;
				if (!seriesMeta) {
					return;
				}
				for (var vKey in seriesMeta) {
					if (!Object.prototype.hasOwnProperty.call(seriesMeta, vKey)) {
						continue;
					}
					var spanId = "qual-var_" + i + "_" + vKey;
					var $span = $("#" + spanId);
					if ($span.length) {
						var placeholder = t('hover_to_display');
						$span.text(placeholder);
					}
				}
			});
		}
	};

	// Fetch data from OM API using fetch without cookies (no JSESSIONID)
	var url = buildOmApiUrl(timeseriesId, startTime, endTime);

	fetch(url, {
		method: 'GET',
		credentials: 'omit',
		headers: {
			'Accept': 'application/json'
		}
	})
		.then(function(response) {
			if (!response.ok) {
				throw new Error('HTTP ' + response.status);
			}
			return response.json();
		})
		.then(function(response) {
			try {
				var members = response && response.member;
				if (!members || !members.length) {
					ensurePlotlyAndRender([]);
					return;
				}
				var observation = members[0];
				var result = observation.result || {};
				var points = result.points || [];

				// Update qualifiers box (common + variable qualifiers)
				if (points && points.length) {
					updateQualifiersBox(i, points);
				}

				ensurePlotlyAndRender(points);
			} catch (e) {
				console.error('Error parsing OM API response:', e);
				$container.html('<div style="padding: 20px; text-align: center; color: red;">' +
					t('no_data_available') + '</div>');
			}
		})
		.catch(function(error) {
			console.error('Error loading OM API data:', error);
			$container.html('<div style="padding: 20px; text-align: center; color: red;">' +
				t('no_data_available') + '</div>');
		});
};

var initDatePickers = function(data, i) {

	var maxEndDate = dateOnly(data[i].time_end.value);
	var minStartDate = dateOnly(data[i].time_start.value);

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

			// Keep seconds so cutDate() trims only seconds (not minutes)
			data[plotId].time_start.value = dateText + "T00:00:00Z";

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

			// Keep seconds so cutDate() trims only seconds (not minutes)
			data[plotId].time_end.value = dateText + "T23:59:00Z";
		}
	});
};

var createTempExtentTable = function(data, i) {

	var dstartId = "datepicker-start_" + i;
	var dendId = "datepicker-end_" + i;

	var timeTable = "<table class='temp_extent_table'>";

	timeTable += "<tr><td colspan='3' class='temp_extent_info'>" + t('please_select_time_period') + "</td></tr>";

	timeTable += "<tr><td colspan='3'>";
	timeTable += "<button class='time_button' id='time-button-2m_" + i + "'>" + t('last_2_months') + "</button> ";
	timeTable += "<button class='time_button' id='time-button-1y_" + i + "'>" + t('last_year') + "</button> ";
	timeTable += "<button class='time_button' id='time-button-fe_" + i + "'>" + t('full_extent') + "</button>";
	timeTable += "</td></tr>";

	timeTable += "<tr><td class='start_date'>" + t('start_date') + "</td><td><input type='text' id='" + dstartId + "' autocomplete='off' readonly></td><td rowspan=2 id='update-button-td_" + i + "'></td></tr>";

	timeTable += "<tr><td class='end_date'>" + t('end_date') + "</td><td><input type='text' id='" + dendId + "' autocomplete='off' readonly></td><td></td></tr>";

	timeTable += "</table>";

	$(timeTable).appendTo("#bottom-td_" + i);

	$("<button class='update_graph_button' id='button_" + i + "'>" + t('view_plot') + "<i style='margin-left:5px' class='fas fa-sync'></i></button>").appendTo("#bottom-td_" + i);
	$("<button class='download_button' id='download_" + i + "' style='margin-left: 10px;'>" + t('download_data') + "<i style='margin-left:5px' class='fas fa-download'></i></button>").appendTo("#bottom-td_" + i);

	$("#button_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		// Check if this is the first click (button still says "View plot")
		var $button = $(this);
		if ($button.html().indexOf(t('view_plot')) !== -1) {
			// Change button text to "Update plot" after first click
			$button.html(t('update_plot') + '<i style="margin-left:5px" class="fas fa-sync"></i>');
		}

		// Clear existing plot content to avoid stacking multiple renders
		$("#plot_" + plotId).empty();
		plots[plotId] = createPlot(data, plotId);
	});

	$("#time-button-2m_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var recentDate = dateOnly(data[plotId].time_end_recent.value);

		data[plotId].time_start.value = recentDate + "T00:00:00Z";
		$("#" + dstartId).datepicker("setDate", recentDate);

		// End date defaults to the series max end date
		var maxEnd = $("#" + dendId).datepicker("option", "maxDate");
		var maxEndStr = formatPickerDate(maxEnd) || dateOnly(data[plotId].time_end.value);
		data[plotId].time_end.value = maxEndStr + "T23:59:00Z";
		$("#" + dendId).datepicker("setDate", maxEndStr);
	});

	$("#time-button-1y_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var lastYearDate = dateOnly(data[plotId].time_end_last_year.value);

		data[plotId].time_start.value = lastYearDate + "T00:00:00Z";
		$("#" + dstartId).datepicker("setDate", lastYearDate);

		var maxEndDate = $("#" + dendId).datepicker("option", "maxDate");
		var maxEndStr = formatPickerDate(maxEndDate) || dateOnly(data[plotId].time_end.value);
		data[plotId].time_end.value = maxEndStr + "T23:59:00Z";
		$("#" + dendId).datepicker("setDate", maxEndStr);
	});

	$("#time-button-fe_" + i).click(function(event) {

		var plotId = this.id.substring(this.id.indexOf('_') + 1, this.id.length);

		var minStartDate = $("#" + dstartId).datepicker("option", "minDate");
		var minStartStr = formatPickerDate(minStartDate) || dateOnly(data[plotId].time_start.value);
		data[plotId].time_start.value = minStartStr + "T00:00:00Z";
		$("#" + dstartId).datepicker("setDate", minStartStr);

		var maxEndDate = $("#" + dendId).datepicker("option", "maxDate");
		var maxEndStr = formatPickerDate(maxEndDate) || dateOnly(data[plotId].time_end.value);
		data[plotId].time_end.value = maxEndStr + "T23:59:00Z";
		$("#" + dendId).datepicker("setDate", maxEndStr);
	});


};

// Helper function to find elements by local name (handles namespaces)
var findByName = function($parent, localName) {
	var result = $parent.find('*');
	var filtered = $();
	result.each(function() {
		var nodeName = this.nodeName || this.tagName || '';
		// Remove namespace prefix if present
		var local = nodeName.indexOf(':') >= 0 ? nodeName.split(':')[1] : nodeName;
		if (local === localName) {
			filtered = filtered.add(this);
		}
	});
	return filtered;
};

// Parse rating curves XML and extract all periods with their points
var parseRatingCurvesXML = function(xmlData) {
	var ratingCurves = [];
	var $xml = $(xmlData);
	
	// Find all ConversionPeriod elements - try multiple namespace patterns
	var periods = $xml.find('rgs\\:ConversionPeriod');
	if (periods.length === 0) {
		// Try without namespace prefix
		periods = $xml.find('ConversionPeriod');
	}
	if (periods.length === 0) {
		// Try using helper function for local name
		periods = findByName($xml, 'ConversionPeriod');
	}
	
	periods.each(function() {
		var period = $(this);
		
		// Extract period start date - try multiple patterns
		var periodStart = period.find('rgs\\:periodStart rgs\\:TimeInstant gml\\:timePosition').first().text();
		if (!periodStart) {
			periodStart = period.find('rgs\\:periodStart gml\\:TimeInstant gml\\:timePosition').first().text();
		}
		if (!periodStart) {
			periodStart = period.find('periodStart TimeInstant timePosition').first().text();
		}
		if (!periodStart) {
			var periodStartEl = findByName(period, 'periodStart');
			var timePos = findByName(periodStartEl, 'timePosition');
			periodStart = timePos.first().text();
		}
		
		// Extract period end date - try multiple patterns
		var periodEnd = period.find('rgs\\:periodEnd rgs\\:TimeInstant gml\\:timePosition').first().text();
		if (!periodEnd) {
			periodEnd = period.find('rgs\\:periodEnd gml\\:TimeInstant gml\\:timePosition').first().text();
		}
		if (!periodEnd) {
			periodEnd = period.find('periodEnd TimeInstant timePosition').first().text();
		}
		if (!periodEnd) {
			var periodEndEl = findByName(period, 'periodEnd');
			var timePos = findByName(periodEndEl, 'timePosition');
			periodEnd = timePos.first().text();
		}
		
		// Extract points from ConversionTable
		var points = [];
		// Find ConversionTable within applicableConversion
		var conversionTable = period.find('rgs\\:applicableConversion rgs\\:ConversionTable').first();
		if (conversionTable.length === 0) {
			conversionTable = period.find('applicableConversion ConversionTable').first();
		}
		if (conversionTable.length === 0) {
			var applicableConv = findByName(period, 'applicableConversion');
			conversionTable = findByName(applicableConv, 'ConversionTable').first();
		}
		if (conversionTable.length === 0) {
			// Try direct ConversionTable
			conversionTable = period.find('rgs\\:ConversionTable').first();
		}
		if (conversionTable.length === 0) {
			conversionTable = findByName(period, 'ConversionTable').first();
		}
		
		// Find all point elements containing TableTuple
		var tableTuples = conversionTable.find('rgs\\:point rgs\\:TableTuple');
		if (tableTuples.length === 0) {
			tableTuples = conversionTable.find('point TableTuple');
		}
		if (tableTuples.length === 0) {
			var points = findByName(conversionTable, 'point');
			tableTuples = findByName(points, 'TableTuple');
		}
		if (tableTuples.length === 0) {
			// Try direct TableTuple
			tableTuples = conversionTable.find('rgs\\:TableTuple');
		}
		if (tableTuples.length === 0) {
			tableTuples = findByName(conversionTable, 'TableTuple');
		}
		
		tableTuples.each(function() {
			var tuple = $(this);
			
			// Extract input value (water level)
			var inputValueText = tuple.find('rgs\\:inputValue swe\\:Quantity swe\\:value').first().text();
			if (!inputValueText) {
				inputValueText = tuple.find('inputValue Quantity value').first().text();
			}
			if (!inputValueText) {
				var inputValueEl = findByName(tuple, 'inputValue');
				var quantityEl = findByName(inputValueEl, 'Quantity');
				var valueEl = findByName(quantityEl, 'value');
				inputValueText = valueEl.first().text();
			}
			
			// Extract output value (discharge)
			var outputValueText = tuple.find('rgs\\:outputValue swe\\:Quantity swe\\:value').first().text();
			if (!outputValueText) {
				outputValueText = tuple.find('outputValue Quantity value').first().text();
			}
			if (!outputValueText) {
				var outputValueEl = findByName(tuple, 'outputValue');
				var quantityEl = findByName(outputValueEl, 'Quantity');
				var valueEl = findByName(quantityEl, 'value');
				outputValueText = valueEl.first().text();
			}
			
			var inputValue = parseFloat(inputValueText);
			var outputValue = parseFloat(outputValueText);
			
			if (!isNaN(inputValue) && !isNaN(outputValue)) {
				points.push({
					level: inputValue,
					discharge: outputValue
				});
			}
		});
		
		// Sort points by level (input value)
		points.sort(function(a, b) {
			return a.level - b.level;
		});
		
		if (points.length > 0 && periodStart && periodEnd) {
			ratingCurves.push({
				periodStart: periodStart,
				periodEnd: periodEnd,
				points: points
			});
		}
	});
	
	// Sort rating curves by period start date (most recent first)
	ratingCurves.sort(function(a, b) {
		var dateA = new Date(a.periodStart);
		var dateB = new Date(b.periodStart);
		return dateB - dateA;
	});
	
	return ratingCurves;
};

// Display rating curves section with dropdown and plot
var displayRatingCurvesSection = function(ratingCurves) {
	if (!ratingCurves || ratingCurves.length === 0) {
		return;
	}
	
	// Check if Plotly script is in the page
	var plotlyScript = document.querySelector('script[src*="plotly"]');
	if (!plotlyScript && typeof Plotly === 'undefined') {
		console.warn('Plotly.js script not found in page. Rating curves will not be displayed.');
		// Still create the section but show a message
		var sectionHtml = '<table class="layout_table" id="ratingCurvesTable">';
		sectionHtml += '<thead><tr><th colspan="2" class="timeseries-header">';
		sectionHtml += t('rating_curves');
		sectionHtml += '</th></tr></thead>';
		sectionHtml += '<tbody><tr><td colspan="2" style="padding: 20px; text-align: center; color: red;">';
		sectionHtml += t('plot_library_error') + '<br><small>Please ensure Plotly.js is loaded</small>';
		sectionHtml += '</td></tr></tbody></table>';
		// Insert after station-overview, before timeseries sections
		var $stationOverview = $("#timeseries .station-overview");
		if ($stationOverview.length > 0) {
			$(sectionHtml).insertAfter($stationOverview);
		} else {
			$(sectionHtml).prependTo("#timeseries");
		}
		return;
	}
	
	// Create the rating curves section
	var sectionHtml = '<table class="layout_table" id="ratingCurvesTable">';
	sectionHtml += '<thead><tr><th colspan="2" class="timeseries-header" id="ratingCurves-header" style="cursor: pointer;">';
	sectionHtml += '<span class="expand-icon" style="display: inline-block; margin-right: 8px;">▶</span>';
	sectionHtml += t('rating_curves');
	sectionHtml += '</th></tr></thead>';
	sectionHtml += '<tbody id="ratingCurves-content" style="display: none;">';
	sectionHtml += '<tr><td class="data_table_td" id="ratingCurvesDataTable"></td>';
	sectionHtml += '<td rowspan="2" id="ratingCurvesPlotDiv"></td></tr>';
	sectionHtml += '<tr><td id="ratingCurvesBottomTd"></td></tr>';
	sectionHtml += '</tbody>';
	sectionHtml += '</table>';
	
	// Insert after station-overview, before timeseries sections
	var $stationOverview = $("#timeseries .station-overview");
	if ($stationOverview.length > 0) {
		$(sectionHtml).insertAfter($stationOverview);
	} else {
		// Fallback: if station-overview not found, prepend to timeseries
		$(sectionHtml).prependTo("#timeseries");
	}
	
	// Create metadata table
	var metadataItems = [];
	metadataItems.push("<tr><td class='data_table_header' colspan='2'>" + t('rating_curve_metadata') + "</td></tr>");
	metadataItems.push("<tr><td class='data_table_label_td'>" + t('available_periods') + "</td><td id='ratingCurvesPeriodInfo'></td></tr>");
	metadataItems.push("<tr><td class='data_table_label_td'>" + t('selected_period') + "</td><td id='ratingCurvesSelectedPeriod'></td></tr>");
	
	$("<table></table>", {
		"class": "data_table",
		html: metadataItems.join("")
	}).appendTo("#ratingCurvesDataTable");
	
	// Create dropdown for period selection
	var selectHtml = '<select id="ratingCurvesPeriodSelect" style="width: 100%; padding: 5px; margin-top: 10px;">';
	for (var i = 0; i < ratingCurves.length; i++) {
		var curve = ratingCurves[i];
		var periodLabel = curve.periodStart + ' - ' + curve.periodEnd;
		selectHtml += '<option value="' + i + '">' + periodLabel + '</option>';
	}
	selectHtml += '</select>';
	
	$("#ratingCurvesBottomTd").html(selectHtml);
	
	// Create plot container
	$("<div id='ratingCurvesPlot' style='width: 100%; height: 400px; min-height: 400px;'></div>").appendTo("#ratingCurvesPlotDiv");
	
	// Update period info
	var periodsText = ratingCurves.length + ' ' + (ratingCurves.length === 1 ? t('period') : t('periods'));
	$("#ratingCurvesPeriodInfo").text(periodsText);
	
	// Set initial selected period (last one)
	var selectedIndex = 0;
	$("#ratingCurvesPeriodSelect").val(selectedIndex);
	updateRatingCurvePlot(ratingCurves, selectedIndex);
	updateSelectedPeriodInfo(ratingCurves, selectedIndex);
	
	// Add click handler to toggle content visibility
	$("#ratingCurves-header").click(function() {
		var $content = $("#ratingCurves-content");
		var $icon = $(this).find('.expand-icon');
		if ($content.is(':visible')) {
			$content.slideUp();
			$icon.text('▶');
		} else {
			$content.slideDown();
			$icon.text('▼');
			// Redraw plot when section is expanded to ensure proper sizing
			setTimeout(function() {
				if (typeof Plotly !== 'undefined') {
					var currentIndex = parseInt($("#ratingCurvesPeriodSelect").val());
					Plotly.Plots.resize('ratingCurvesPlot');
				}
			}, 300);
		}
	});
	
	// Add change handler for dropdown
	$("#ratingCurvesPeriodSelect").change(function() {
		var selectedIndex = parseInt($(this).val());
		updateRatingCurvePlot(ratingCurves, selectedIndex);
		updateSelectedPeriodInfo(ratingCurves, selectedIndex);
	});
};

// Update selected period info
var updateSelectedPeriodInfo = function(ratingCurves, index) {
	if (index >= 0 && index < ratingCurves.length) {
		var curve = ratingCurves[index];
		var periodText = curve.periodStart + ' - ' + curve.periodEnd;
		$("#ratingCurvesSelectedPeriod").text(periodText);
	}
};

// Plot rating curve using Plotly.js
var updateRatingCurvePlot = function(ratingCurves, index) {
	if (index < 0 || index >= ratingCurves.length) {
		return;
	}
	
	var curve = ratingCurves[index];
	var points = curve.points;
	
	if (points.length === 0) {
		$("#ratingCurvesPlot").html('<div style="padding: 20px; text-align: center;">' + t('no_data_available') + '</div>');
		return;
	}
	
	// Check if Plotly is available, wait a bit if not (script might still be loading)
	if (typeof Plotly === 'undefined') {
		console.warn('Plotly.js not yet loaded, waiting...');
		// Check if script tag exists
		var plotlyScript = document.querySelector('script[src*="plotly"]');
		if (!plotlyScript) {
			console.error('Plotly.js script tag not found in page');
			$("#ratingCurvesPlot").html('<div style="padding: 20px; text-align: center; color: red;">' + t('plot_library_error') + '<br><small>Script tag not found</small></div>');
			return;
		}
		// Wait for Plotly to load (max 5 seconds)
		var attempts = 0;
		var maxAttempts = 50; // 50 attempts * 100ms = 5 seconds
		var checkPlotly = setInterval(function() {
			attempts++;
			if (typeof Plotly !== 'undefined') {
				clearInterval(checkPlotly);
				console.log('Plotly.js loaded successfully after', attempts * 100, 'ms');
				// Retry plotting now that Plotly is loaded
				updateRatingCurvePlot(ratingCurves, index);
			} else if (attempts >= maxAttempts) {
				clearInterval(checkPlotly);
				console.error('Plotly.js failed to load after waiting', maxAttempts * 100, 'ms');
				console.error('Script tag found:', plotlyScript ? plotlyScript.src : 'none');
				$("#ratingCurvesPlot").html('<div style="padding: 20px; text-align: center; color: red;">' + t('plot_library_error') + '<br><small>Check browser console for details</small></div>');
			}
		}, 100);
		return;
	}
	
	// Prepare data arrays (flipped: discharge on x-axis, level on y-axis)
	var levels = points.map(function(p) { return p.level; });
	var discharges = points.map(function(p) { return p.discharge; });
	
	// Create hover text with detailed information
	var hoverText = points.map(function(p, i) {
		return t('water_level') + ': ' + p.level.toFixed(3) + ' m<br>' +
		       t('discharge') + ': ' + p.discharge.toFixed(3) + ' m³/s';
	});
	
	// Create trace for the line (flipped axes: x=discharge, y=level)
	var trace = {
		x: discharges,
		y: levels,
		type: 'scatter',
		mode: 'lines+markers',
		name: t('rating_curve'),
		line: {
			color: '#00529c',
			width: 2
		},
		marker: {
			color: '#00529c',
			size: 6,
			line: {
				color: '#ffffff',
				width: 1
			}
		},
		hovertemplate: '<b>%{text}</b><extra></extra>',
		text: hoverText
	};
	
	// Layout configuration (flipped axes)
	var layout = {
		title: {
			text: t('rating_curve'),
			font: {
				size: 16,
				color: '#000000'
			}
		},
		xaxis: {
			title: {
				text: t('discharge') + ' (m³/s)',
				font: {
					size: 12
				}
			},
			showgrid: true,
			gridcolor: '#e0e0e0',
			zeroline: false
		},
		yaxis: {
			title: {
				text: t('water_level') + ' (m)',
				font: {
					size: 12
				}
			},
			showgrid: true,
			gridcolor: '#e0e0e0',
			zeroline: false
		},
		plot_bgcolor: '#ffffff',
		paper_bgcolor: '#ffffff',
		hovermode: 'closest',
		margin: {
			l: 60,
			r: 30,
			t: 50,
			b: 50
		},
		showlegend: false
	};
	
	// Configuration for interactivity
	var config = {
		displayModeBar: true,
		displaylogo: false,
		modeBarButtonsToRemove: ['lasso2d', 'select2d'],
		toImageButtonOptions: {
			format: 'png',
			filename: 'rating_curve',
			height: 600,
			width: 800,
			scale: 1
		},
		responsive: true
	};
	
	// Create or update the plot
	Plotly.newPlot('ratingCurvesPlot', [trace], layout, config);
};

var createLayoutTable = function(data, i, k) {

	var layoutTable = "<table class='layout_table' id='layoutTable_" + i + "'>";

	// Prefer the time series title from the response (when available), otherwise fallback to observed property label
	var seriesTitle = findValue(data, i, 'title');
	if (typeof seriesTitle === 'undefined' || seriesTitle === null || ('' + seriesTitle).trim() === '') {
		seriesTitle = getAttributeLabel(data, i);
	}
	var label = seriesTitle; // +" from "+convertDate(data[i].time_start.value)+" to "+convertDate(data[i].time_end.value);

	layoutTable += "<thead><tr><th colspan='2' class='timeseries-header' id='timeseries-header_" + i + "' style='cursor: pointer;'>";
	layoutTable += "<span class='expand-icon' style='display: inline-block; margin-right: 8px;'>▶</span>";
	layoutTable += t('time_series') + " " + (k + 1) + ": " + label;
	layoutTable += "</th></tr></thead>";

	layoutTable += "<tbody id='timeseries-content_" + i + "' style='display: none;'>";
	layoutTable += "<tr><td class='data_table_td' id='dataTable_" + i + "'></td>";

	layoutTable += "<td rowspan='2'id='plotDiv_" + i + "'></td></tr>";

	layoutTable += "<tr><td id='bottom-td_" + i + "'></td></tr>";
	layoutTable += "</tbody>";

	layoutTable += "</table>";

	$(layoutTable).appendTo("#timeseries");

	// Add click handler to toggle content visibility
	$("#timeseries-header_" + i).click(function() {
		var $content = $("#timeseries-content_" + i);
		var $icon = $(this).find('.expand-icon');
		if ($content.is(':visible')) {
			$content.slideUp();
			$icon.text('▶');
		} else {
			$content.slideDown();
			$icon.text('▼');
		}
	});
};

var download = function(button, event, data) {

	$("#dialog-download").dialog({
		resizable: false,
		height: "auto",
		modal: true,
		open: function() {
			var $dialog = $(this).dialog("widget");
			var $dialogContent = $("#dialog-download");
			
			// Always create/update the dialog content with format selection
			$dialogContent.empty();
			var $formatLabel = $("<label>").attr("for", "format").text(t('please_select_data_format') + ":").css({
				"display": "block",
				"margin-bottom": "5px"
			});
			var $formatSelect = $("<select>").attr("id", "format").css({
				"width": "100%",
				"padding": "5px",
				"margin-top": "10px",
				"margin-bottom": "10px"
			});

			// Add format options
			$formatSelect.append($("<option>").attr("value", "waterml").text("WaterML 1.0"));
			$formatSelect.append($("<option>").attr("value", "waterml2").text("WaterML 2.0"));
			$formatSelect.append($("<option>").attr("value", "netcdf").text("NetCDF"));
			$formatSelect.append($("<option>").attr("value", "csv").text("CSV"));
			
			$dialogContent.append($formatLabel);
			$dialogContent.append($formatSelect);
			
			// Use CSS transform to center - this is more reliable
			$dialog.css({
				position: "fixed",
				top: "50%",
				left: "50%",
				margin: "0",
				transform: "translate(-50%, -50%)",
				"-webkit-transform": "translate(-50%, -50%)",
				"-ms-transform": "translate(-50%, -50%)"
			});
		},
		buttons: {
			"Download": function() {

				var dataId = button.id.substring(button.id.indexOf('_') + 1, button.id.length);

				var timeseriesId = findValue(data, dataId, 'timeseries_id');
				if (!timeseriesId) {
					alert(t('no_data_available') + ' (missing timeseries_id)');
					return;
				}

				var startTime = cutDate(data[dataId].time_start.value);
				var endTime = cutDate(data[dataId].time_end.value);
				var format = $("#format").val();

				var query = buildOmApiUrl(timeseriesId, startTime, endTime, format);

				$(this).dialog("close");

				window.open(query);

			},
			Cancel: function() {
				$(this).dialog("close");
			}
		},
		title: t('please_select_data_format')
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
		dialogEl.append($('<b>').text(t('disclaimer_to_accept')));
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
		v1 = getAttributeLabel(data, a);
		v2 = getAttributeLabel(data, b);
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

	var alt = findValue(data, 0, 'vertical_extent');

	
	var geoLocation = t('latitude') + ": " + lat + "°<br/> " + t('longitude') + ": " + lon + "°";
	
	if (alt!=null && alt!= undefined){
		geoLocation +="<br/> " + t('elevation') + ": "+alt+" " + t('elevation_unit');
	}

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

	var timeserisId = findValue(data, 0, 'timeseries_id');

	// Fetch rating curves for this platform
	var platformId = findValue(data, 0, 'platform_id');
	if (platformId) {
		// Extract view from URL if available, or use default
		var viewId = 'his-central'; // default
		var pathSegments = window.location.pathname.split('/');
		var viewIndex = pathSegments.indexOf('view');
		if (viewIndex !== -1 && viewIndex + 1 < pathSegments.length) {
			viewId = pathSegments[viewIndex + 1];
		}
		
		// Construct the rating curves endpoint URL
		var ratingCurvesUrl = '/gs-service/services/support/rating-curves?platformId=' + encodeURIComponent(platformId) + '&view=' + encodeURIComponent(viewId);
		
		// Fetch XML data instead of JSON
		$.ajax({
			url: ratingCurvesUrl,
			type: 'GET',
			dataType: 'xml',
			success: function(xmlData) {
				try {
					var ratingCurves = parseRatingCurvesXML(xmlData);
					if (ratingCurves && ratingCurves.length > 0) {
						displayRatingCurvesSection(ratingCurves);
					}
				} catch (e) {
					console.error('Error parsing rating curves XML:', e);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('No rating curves found or error loading rating curves:', textStatus, errorThrown);
				// It's okay if there are no rating curves - not all stations have them
			}
		});
	}

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

		//
		// temporal extent table
		//

		createTempExtentTable(data, i);

		// Do NOT load plots automatically on page load.
		// Initialize date pickers and default dates; user will load plots via "Update plot".
		// IMPORTANT: datepicker inputs are created by createTempExtentTable(), so initDatePickers() must run AFTER it.
		initDatePickers(data, i);

		// Default date range: last 2 months (reuse existing button logic)
		$("#time-button-2m_" + i).trigger('click');

		//
		// download
		//

		$("#download_" + i).click(function(event) {

			download(this, event, data);
		});
	}

	$(".page_footer").css("visibility", "visible");

})



