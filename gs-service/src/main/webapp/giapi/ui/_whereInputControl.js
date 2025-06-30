import { GIAPI } from '../core/GIAPI.js';

GIAPI._whereInputControl = function(resultsMapWidget, options) {

	var inControl = {};

	var locInfoLabelId = GIAPI.random();
	var southFieldId = GIAPI.random();
	var westFieldId = GIAPI.random();
	var northFieldId = GIAPI.random();
	var eastFieldId = GIAPI.random();

	var topSearchButtonId = GIAPI.random();
	var topSearchFieldId = GIAPI.random();
	var inputControlId = GIAPI.random();
	var spRelId = GIAPI.random();
	var spRelDivId = GIAPI.random();
	var applyId = GIAPI.random();
	
	var shapeLayer = null;

	var predefinedLayers = [];

	if (options.olMap) {
		options.olMap.inputControl(inControl);
	}

	/**
	 * Adds the control to the given map
	 */
	inControl.add = function(map) {

		var controlDiv = document.createElement('div');

		if (!options.dialogMode) {
			// adds a class for the ResultsMapWidget
			jQuery(controlDiv).attr('class', 'cnst-widget-where-input-control-div resultset-layout');
		} else {
			jQuery(controlDiv).attr('class', 'cnst-widget-where-input-control-div');
			jQuery(controlDiv).css('margin-top', '10px');
		}

		var control = create();
		jQuery(controlDiv).append(control);

		var openCloseDiv = document.createElement('div');
		jQuery(openCloseDiv).addClass('fa fa-caret-left');
		jQuery(openCloseDiv).addClass('cnst-widget-where-input-control-button');
		jQuery(openCloseDiv).addClass('cnst-widget-where-input-control-button-open');

		if (!options.dialogMode) {
			jQuery(openCloseDiv).addClass('cnst-widget-where-input-control-button-open-no-dialog');
		}

		var openCloseDivId = GIAPI.random();
		jQuery(openCloseDiv).attr('id', 'openCloseDivId' + openCloseDivId);
		jQuery(openCloseDiv).attr('state', 'open');
		jQuery(openCloseDiv).attr('title', 'Hide control panel');

		jQuery(controlDiv).append(openCloseDiv);

		jQuery(document).on('click', '#' + openCloseDivId, function() {

			var effect = 'slide';
			var duration = 200;
			var options = {
				direction: 'left'
			};
			var margin;

			switch (jQuery('#' + openCloseDivId).attr('state')) {
				case 'open':
					margin = '-=145px';
					break;
				case 'closed':
					margin = '+=145px';

					jQuery(openCloseDiv).addClass('cnst-widget-where-input-control-button-open');
					jQuery(openCloseDiv).removeClass('cnst-widget-where-input-control-button-closed');

					break;
			}

			jQuery('#' + openCloseDivId).animate({
				'margin-left': margin,
			}, duration);

			jQuery('#' + inputControlId).toggle(effect, options, duration, function() {

				switch (jQuery('#' + openCloseDivId).attr('state')) {
					case 'open':

						jQuery(openCloseDiv).removeClass('cnst-widget-where-input-control-button-open');
						jQuery(openCloseDiv).addClass('cnst-widget-where-input-control-button-closed');

						jQuery(openCloseDiv).removeClass('fa fa-caret-left');
						jQuery(openCloseDiv).addClass('fa fa-caret-right');

						jQuery(openCloseDiv).attr('state', 'closed');
						jQuery(openCloseDiv).attr('title', 'Show control panel');

						break;
					case 'closed':

						jQuery(openCloseDiv).removeClass('fa fa-caret-right');
						jQuery(openCloseDiv).addClass('fa fa-caret-left');

						jQuery(openCloseDiv).attr('state', 'open');
						jQuery(openCloseDiv).attr('title', 'Hide control panel');

						break;
				}
			});
		});

		controlDiv.className = 'map-control-div ol-control';

		jQuery(controlDiv).css({
			'margin-top': '5px',
			// 'margin-left' : '5px',
			'background-color': 'transparent'
		});

		var mirrorDiv = document.createElement('div');
		mirrorDiv.setAttribute("id", "mapControlDiv");

		if (options.mapTypeControlOptions && // 

			options.mapTypeControlOptions.position !== undefined && //
			options.mapTypeControlOptions.position === 'ol') {

			jQuery('#controlToggleButtonDiv').append(mirrorDiv);
			jQuery(mirrorDiv).css({
				'right': 160,
				'top': 20,
				'margin-top': 440,
				//'margin-left': 440,
				'position': 'relative',
				'float': 'right',
				'z-index': 100,
			});

		} else {

			jQuery('.ol-viewport').append(mirrorDiv);

			jQuery(mirrorDiv).append('<label id="whereTableCaption" class="cnst-widget-where-input-table-ol-caption">Spatial extent</label>');

			if (options.dialogMode) {

				jQuery(mirrorDiv).position({

					of: '#' + options.dialogId,
					my: 'left top',
					at: 'left+12 top+55',
					//collision : 'none'
				});

			} else {

				//jQuery(mirrorDiv).position({
				//    of : '.ol-viewport',
				//    my : 'left top',
				//    at : 'left+1065 top-50',
				//   //collision : 'none'
				// });
			}

			// jQuery(mirrorDiv).css({       
			//     'z-index' : 1000,
			// });
		}

		var myControl = new ol.control.Control({
			element: controlDiv,
			target: mirrorDiv
		});

		map.addControl(myControl);


		if (options.wmsEndpoint) {

			//
			// creates the layers selector
			//

			var layerSelectorDiv = '<div id="layerSelectorDiv" style="display:none" class="cnst-widget-where-wms-selector-div">';

			layerSelectorDiv += '<table style="width:100%" id="wrapLayerSelectorTable">';

			layerSelectorDiv += '<tr><th style="background: lightgray;">Predefined selection</th></tr>';

			layerSelectorDiv += '<tr><td><input id="layerNameSearchInput" style="width: 376px;" placeholder="Enter search text"></input></td></tr>';

			layerSelectorDiv += '<tr><td><div style="overflow-y: scroll;height: 210px"><table style="width:100%" id="layerSelectorTable"></table></div></td></tr>';

			layerSelectorDiv += '</table>';

			layerSelectorDiv += '</div>';

			jQuery(controlDiv).append(layerSelectorDiv);

			jQuery("#layerNameSearchInput").on("keyup", function() {

				updateWmsLayersTable(jQuery("#layerNameSearchInput").val());
			});

			downloadWmsLayers();
		}
	};

	var updateWhereFields = function(west, south, east, north, fromLatLon) {

		if (fromLatLon) {

			jQuery('#' + westFieldId).val(west.toFixed(3));
			jQuery('#' + southFieldId).val(south.toFixed(3));
			jQuery('#' + eastFieldId).val(east.toFixed(3));
			jQuery('#' + northFieldId).val(north.toFixed(3));

		} else {

			var minlatLon = ol.proj.transform([west, south], 'EPSG:3857', 'EPSG:4326');
			var maxlatLon = ol.proj.transform([east, north], 'EPSG:3857', 'EPSG:4326');

			jQuery('#' + westFieldId).val(minlatLon[0].toFixed(3));
			jQuery('#' + southFieldId).val(minlatLon[1].toFixed(3));
			jQuery('#' + eastFieldId).val(maxlatLon[0].toFixed(3));
			jQuery('#' + northFieldId).val(maxlatLon[1].toFixed(3));
		}
	};

	/**
	 * Updates the where fields from the current selection
	 */
	inControl.updateWhereFields = function() {

		updateWhereFields(
			options.olMap.where().west,
			options.olMap.where().south,
			options.olMap.where().east,
			options.olMap.where().north
		);
	};

	/**
	 * 
	 */
	inControl.spatialRelation = function() {

		// spatial relation
		var contains = jQuery('#containsButton').attr('check') === 'true';
		return contains || contains === undefined ? "CONTAINS" : "OVERLAPS";
	};

	/**
	 * 
	 */
	inControl.where = function(toLatLon) {

		var south = parseFloat(jQuery('#' + southFieldId).val());
		var west = parseFloat(jQuery('#' + westFieldId).val());
		var north = parseFloat(jQuery('#' + northFieldId).val());
		var east = parseFloat(jQuery('#' + eastFieldId).val());

		var minlatLon = ol.proj.transform([west, south], 'EPSG:4326', 'EPSG:3857');
		var maxlatLon = ol.proj.transform([east, north], 'EPSG:4326', 'EPSG:3857');

		if (toLatLon) {

			return { 'south': south, 'west': west, 'north': north, 'east': east };
		}

		return { 'south': minlatLon[1], 'west': minlatLon[0], 'north': maxlatLon[1], 'east': maxlatLon[0] };
	};

	/**
	 * 
	 */
	inControl.selection = function() {

		return options.selection;
	};

	/**
	 * Clears the text of the location label
	 */
	inControl.clearLocationLabel = function() {

		jQuery('#' + locInfoLabelId).text('');
	};

	/**
	 * 
	 */
	inControl.fitMapToBounds = function() {

		fitMapToBounds();
	};

	var updateSelection = function() {

		options.olMap.select(inControl.where());
	};

	var fitMapToBounds = function() {

		options.olMap.fitBounds(inControl.where());
	};

	/**
	  * 
	  */
	var downloadWmsLayers = function() {

		var endpoint = options.dabNode.endpoint();
		endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

		var servicePath = options.dabNode.servicePath();

		var query = endpoint + servicePath + '/opensearch/wmslayershandler?request=capabilities&endpoint=' + options.wmsEndpoint + '&version=' + options.wmsVersion;

		jQuery.ajax({

			type: 'GET',
			url: query,
			crossDomain: true,
			dataType: 'jsonp',

			success: function(data, status, jqXHR) {

				if (data && data.layers) {

					predefinedLayers = data.layers;

					updateWmsLayersTable();
				}
			}
		});
	};

	/**
	  * 
	  */
	var updateWmsLayersTable = function(searchText) {

		// clears the table rows by removing the table body
		jQuery('#layerSelectorTable > tbody').remove();

		predefinedLayers.forEach((layer) => {

			//
			// creates the table rows using the layer names witch match the search text
			//		

			if (layer.title.toLowerCase().indexOf(searchText) >= 0 || !searchText) {

				var rowId = "layerSelectorTableRow_" + GIAPI.random();

				jQuery('#layerSelectorTable').append('<tr id="' + rowId + '"><td>' + layer.title + '</td></tr>');
			}

			//
			// set the click listener which shows add the layer and its bbox
			//

			jQuery(document).on('click', '#' + rowId, function() {

				if ($('#' + rowId + ' > td').hasClass('highlighted')) {

					$('#layerSelectorTable td').removeClass('highlighted');
					if (shapeLayer!=null){
							resultsMapWidget.removeLayers(shapeLayer);
						}
						options.value.predefinedLayer = null;
						shapeLayer = null;
						
					return;
				}
				options.value.predefinedLayer = layer.name;
				$('#layerSelectorTable td').removeClass('highlighted');
        
				$('#' + rowId + ' > td').addClass('highlighted');
        
				var title = jQuery('#' + rowId + ' > td').text();

				predefinedLayers.forEach((layer) => {

					if (layer.title === title) {

						//
						// add the layers
						//

						var onlineArray = [];

						var protocol = 'urn:ogc:serviceType:WebMapService:' + options.wmsVersion + ':HTTP';

						var online = {

							'function': 'download',
							'name': layer.name,
							'title': layer.title,
							'protocol': protocol,
							'url': options.wmsEndpoint
						};

						onlineArray.push(online);

						var mapLayers = GIAPI.LayersFactory.layers(onlineArray, 'urn:ogc:serviceType:WebMapService:');

						if (shapeLayer!=null){
							resultsMapWidget.removeLayers(shapeLayer);
						}

						resultsMapWidget.addLayers(mapLayers);
						shapeLayer = mapLayers;

						//
						// add the layer bbox
						//

						var bbox = layer.bbox.split(',');

						updateWhereFields(
							parseFloat(bbox[0]),
							parseFloat(bbox[1]),
							parseFloat(bbox[2]),
							parseFloat(bbox[3]),
							true
						);

						updateSelection();
						fitMapToBounds();
						jQuery('#' + westFieldId).val('');
						jQuery('#' + southFieldId).val('');
						jQuery('#' + eastFieldId).val('');
						jQuery('#' + northFieldId).val('');


						updateSelection();
					}
				});
			});
		});
	};


	var create = function() {

		var style = 'style="';
		if (!options.dialogMode) {
			//			style += 'height: 265px;';
		}

		style += 'font-size: 80%"';

		var inputTable = '<table ' + style + ' id="' + inputControlId + '" class="cnst-widget-where-input-control">';
		//	inputTable += '<caption id="whereTableCaption" class="cnst-widget-where-input-table-ol-caption">Bounding box</caption>';

		if (!options.value) {
			options.value = {};
			options.value.south = '';
			options.value.west = '';
			options.value.north = '';
			options.value.east = '';
		}

		var southVal = options.value.south;
		var westVal = options.value.west;
		var northVal = options.value.north;
		var eastVal = options.value.east;

		var south = '<td style="padding-left: 5px;padding-top:15px"><label>South</label></td>';
		south += '<td style="padding-top:10px;padding-right: 5px;"><input value="' + southVal + '" style="font-size: 11px;width: 140px" id="' + southFieldId + '" min="-90" max="90" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + south + '</tr>';

		var west = '<td style="padding-left: 5px;padding-top:5px"><label>West</label></td>';
		west += '<td style="padding-right: 5px;"><input value="' + westVal + '" style="font-size: 11px;width: 140px" id="' + westFieldId + '" min="-180" max="180" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + west + '</tr>';

		var north = '<td style="padding-left: 5px;padding-top:5px"><label>North</label></td>';
		north += '<td style="padding-right: 5px;"><input value="' + northVal + '" style="font-size: 11px;width: 140px"  min="-90" max="90" id="' + northFieldId + '" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + north + '</tr>';

		var east = '<td style="padding-left: 5px;padding-top:5px"><label>East</label></td>';
		east += '<td style="padding-right: 5px; padding-bottom:10px"><input value="' + eastVal + '" style="font-size: 11px;width: 140px"  min="-180" max="180" id="' + eastFieldId + '" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + east + '</tr>';

		//
		// Location ---
		//

		var title = 'Location';
		help = 'Name of a location (town, city, state, continent) such as "florence", "italy", "u.s.a", "africa", etc.<br>';
		help += 'This is a free-form query, so you may search for "rome, italy" or you can also search for a complete address such as "via nomentana, rome, italy". Commas are optional, but improve performance by reducing the complexity of the search<br><br>';
		help += 'Click the "Search location" button or the enter key; if the location is found, the selection is updated with the correspondent bounding box';

		var locLabel = '<td colspan="2" style="padding-left: 5px;padding-top:5px"><label>Location</label>' + GIAPI.UI_Utils.helpImage(title, help, 'vertical-align: text-bottom;margin-left: 5px;') + '</td>';
		var locField = '<td colspan="2" style="padding-left:5px"><input id="' + topSearchFieldId + '" style="vertical-align: top;" class="cnst-widget-location-field"/>';

		var searchLocation = function() {

			jQuery('#' + locInfoLabelId).css('color', 'blue');
			jQuery('#' + locInfoLabelId).text('Searching location...');

			var endpoint = options.dabNode.endpoint();
			endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

			var servicePath = options.dabNode.servicePath();

			var query = endpoint + servicePath + '/opensearch/nominatim?reqID=' + GIAPI.random() + '&query=' + jQuery('#' + topSearchFieldId).val();

			jQuery.ajax({

				type: 'GET',
				url: query,
				crossDomain: true,
				dataType: 'jsonp',

				success: function(data, status, jqXHR) {

					if (data && data.response && data.response.bbox) {

						jQuery('#' + locInfoLabelId).css('color', 'black');
						jQuery('#' + locInfoLabelId).text('Location found');

						jQuery('#' + southFieldId).val(data.response.bbox.south.toFixed(3));
						jQuery('#' + westFieldId).val(data.response.bbox.west.toFixed(3));
						jQuery('#' + northFieldId).val(data.response.bbox.north.toFixed(3));
						jQuery('#' + eastFieldId).val(data.response.bbox.east.toFixed(3));

						updateSelection();
						fitMapToBounds();

						if (jQuery('#' + applyId).attr('check') === 'false') {
							jQuery('#' + applyId).click();
						}

					} else {
						jQuery('#' + locInfoLabelId).css('color', 'red');
						jQuery('#' + locInfoLabelId).text('Location not found');
					}
				}
			});

			return false;
		};

		var locButton = GIAPI.FontAwesomeButton({
			'id': topSearchButtonId,
			'width': 12,

			'label': '',
			'icon': 'fa-search',
			'attr': [{ name: 'title', value: 'Search location' }],

			'handler': searchLocation
		});

		locButton.css('div', 'height', '11px');
		locButton.css('div', 'margin-left', '3px');
		locButton.css('div', 'padding', '3px');

		locButton.css('icon', 'margin-left', '1px');
		locButton.css('icon', 'font-size', '10px');
		locButton.css('icon', 'vertical-align', 'super');

		locField += locButton.div() + '</td>';

		if (options.showLocationControl) {
			inputTable += '<tr>' + locLabel + '</tr>';
			inputTable += '<tr>' + locField + '</tr>';
		}

		jQuery(document).on('keydown', '#' + topSearchFieldId, (function() {
			return GIAPI.UI_Utils.enterKeyDown(searchLocation);
		}));

		//
		// WMS Layers selector
		//

		if (options.wmsEndpoint!==undefined) {

			var layersSelectorButton = GIAPI.ButtonsFactory.onOffSwitchButton('Select', 'Hide', {
				'id': 'layersSelectorButton',
				'checked': true,
				'size': 'medium'
			});

			jQuery(document).on('click', '#layersSelectorButton', function() {

				if (jQuery('#layersSelectorButton').is(":checked")) {

					jQuery('#layerSelectorDiv').css('display', 'none');

				} else {

					jQuery('#layerSelectorDiv').css('display', 'block');
				}
			});

			var style = "display: inline-block;";
			style += "padding-top: 6px;";
			style += "padding-left: 5px;";
			style += "vertical-align: top;";
			style += "padding-right: 20px;";

			var help = 'help';
			var layerSelectorLabel = '<div style="' + style + '"><label>Predefined</label></div>';

			inputTable += '<tr><td  colspan="2"><div style="margin-top:10px">' + layerSelectorLabel +

				GIAPI.UI_Utils.helpImage('Predefined selection', help, 'vertical-align: top;margin-left: -15px;padding-right: 5px;') +

				'<div style="display: inline-block;margin-left: 15px;">' + layersSelectorButton + '</div></div></td></tr>';
		}

		//
		// Contains button
		//

		var containsButton = GIAPI.FontAwesomeButton({
			'id': 'containsButton',
			'width': 90,
			'label': 'CONTAINS',
			'icon': 'fa-dot-circle-o',
			'attr': [{ 'name': 'check', 'value': true }],

			'handler': function() {

				if (jQuery('#containsButton').attr('check') === 'true') {
					return false;
				}

				jQuery('#containsButton').attr('check', 'true');
				jQuery('#overlapsButton').attr('check', 'false');

				jQuery('#containsButtonIcon').attr('class', 'font-awesome-button-icon fa fa-dot-circle-o');
				jQuery('#overlapsButtonIcon').attr('class', 'font-awesome-button-icon fa fa-circle-o');

				return false;
			}
		});

		containsButton.css('icon', 'font-size', ' 16px');
		containsButton.css('icon', 'color', '#25418f');

		containsButton.css('div', 'margin-left', '3px');
		containsButton.css('div', 'background', 'transparent');
		containsButton.css('div', 'padding', '0px');

		containsButton.css('label', 'color', '#25418f');
		containsButton.css('label', 'vertical-align', 'text-bottom');

		//
		// Overlaps button
		//

		var overlapsButton = GIAPI.FontAwesomeButton({
			'id': 'overlapsButton',
			'width': 90,
			'label': 'OVERLAPS',
			'icon': 'fa-circle-o',
			'attr': [{ 'name': 'check', 'value': false }],

			'handler': function() {

				if (jQuery('#overlapsButton').attr('check') === 'true') {
					return false;
				}

				jQuery('#containsButton').attr('check', 'false');
				jQuery('#overlapsButton').attr('check', 'true');

				jQuery('#overlapsButtonIcon').attr('class', 'font-awesome-button-icon fa fa-dot-circle-o');
				jQuery('#containsButtonIcon').attr('class', 'font-awesome-button-icon fa fa-circle-o');

				return false;
			}
		});

		overlapsButton.css('icon', 'font-size', ' 16px');
		overlapsButton.css('icon', 'color', '#25418f');

		overlapsButton.css('div', 'background', 'transparent');
		overlapsButton.css('div', 'padding', '0px');

		overlapsButton.css('label', 'color', '#25418f');
		overlapsButton.css('label', 'vertical-align', 'text-bottom');

		var contains = '<td colspan="2"><div style="margin-top:10px">' + containsButton.div() + overlapsButton.div() + '</div></td>';

		if (options.showSpatialRelationControl) {
			inputTable += '<tr>' + contains + '</tr>';
		}

		var applyButton;
		var closeButton;

		if (options.dialogMode) {

			applyButton = GIAPI.FontAwesomeButton({
				'id': 'applybboxbutton',
				'width': 85,
				'label': 'Apply',
				'icon': 'fa-check',
				'handler': function() {
					jQuery('#' + options.dialogId).attr('apply', 'true');
					jQuery("#" + options.dialogId).dialog('close');

					return false;
				}
			});
			applyButton.css('div', 'margin-left', '3px');
			applyButton.css('div', 'background', '#25418F');
			applyButton.css('div', 'margin-left', '3px');

			closeButton = GIAPI.FontAwesomeButton({
				'id': 'closebboxbutton',
				'width': 85,
				'label': 'Close',
				'icon': 'fa-times',
				'handler': function() {
					jQuery('#' + options.dialogId).attr('apply', 'false');
					jQuery("#" + options.dialogId).dialog('close');

					return false;
				}
			});
			closeButton.css('div', 'margin-left', '3px');
			closeButton.css('div', 'margin-bottom', '5px');

		} else {

			closeButton = GIAPI.FontAwesomeButton({
				'id': GIAPI.random(),
				'width': 85,
				'label': 'CLEAR',
				'icon': 'fa-times',

				'handler': function() {

					options.olMap.selectionVisible(false);
					jQuery('#' + southFieldId).val('');
					jQuery('#' + westFieldId).val('');
					jQuery('#' + northFieldId).val('');
					jQuery('#' + eastFieldId).val('');
					return false;
				}
			});

			closeButton.css('div', 'margin-top', '10px');
			closeButton.css('div', 'margin-left', '3px');
			closeButton.css('div', 'margin-bottom', '3px');
		}

		title = 'Select Spatial Extent';

		if (options.dialogMode) {
			help = 'Click CTRL-key (meta-key on Mac) and left mouse to select the desired spatial extent and click the "APPLY" button. Click the "CLEAR" button to clear the selection';
		} else {
			help = 'Click CTRL-key (meta-key on Mac) and left mouse to select and apply the desired spatial extent. Click the "CLEAR" button to clear the selection';
		}

		var css = 'display: inherit;float: right;margin-right: 14px;'

		if (options.dialogMode) {

			inputTable += '<tr><td style="" colspan="2">' + applyButton.div() + GIAPI.UI_Utils.helpImage(title, help, css) + '</td></tr>';
			inputTable += '<tr><td  colspan="2"><div class="line" style="width: 120px;"></div></td></tr>';
			inputTable += '<tr><td  colspan="2">' + closeButton.div() + '</td></tr>';

		} else {

			inputTable += '<tr><td colspan="2"></td></tr>';

			inputTable += '<tr><td colspan="2">' + closeButton.div() + GIAPI.UI_Utils.helpImage(title, help, css) + '</td></tr>';
		}

		inputTable += '</table>';

		// set fields listeners
		jQuery(document).on('change', '#' + northFieldId, function(e) {
			updateSelection();
		});
		jQuery(document).on('change', '#' + eastFieldId, function(e) {
			updateSelection();
		});
		jQuery(document).on('change', '#' + southFieldId, function(e) {
			updateSelection();
		});
		jQuery(document).on('change', '#' + westFieldId, function(e) {
			updateSelection();
		});

		return inputTable;
	};

	return inControl;
};
