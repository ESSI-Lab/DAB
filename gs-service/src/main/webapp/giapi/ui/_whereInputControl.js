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

	var PREDEFINED_NO_GROUP_KEY = '__NO_GROUP__';

	var layerGroupKey = function(layer) {
		var group = (layer.group || '').trim();
		return group ? group : PREDEFINED_NO_GROUP_KEY;
	};

	var groupDisplayLabel = function(groupKey) {
		var __t = window.__t || function(s) { return s; };
		if (groupKey === PREDEFINED_NO_GROUP_KEY) {
			var noGroup = __t('predefined_no_group');
			return noGroup === 'predefined_no_group' ? '(no group)' : noGroup;
		}
		return groupKey;
	};

	var buildGroupIndex = function(layers) {
		var groups = {};
		layers.forEach(function(layer) {
			var key = layerGroupKey(layer);
			if (!groups[key]) {
				groups[key] = [];
			}
			groups[key].push(layer);
		});
		Object.keys(groups).forEach(function(key) {
			groups[key].sort(function(a, b) {
				return (a.title || a.name || '').localeCompare(b.title || b.name || '', undefined, { sensitivity: 'base' });
			});
		});
		return groups;
	};

	var groupSortOrder = function(groupsByKey, groupKey) {
		var layers = groupsByKey[groupKey] || [];
		var order = Number.MAX_SAFE_INTEGER;
		layers.forEach(function(layer) {
			if (typeof layer.groupOrder === 'number' && Number.isInteger(layer.groupOrder) && layer.groupOrder < order) {
				order = layer.groupOrder;
			}
		});
		return order;
	};

	var sortedGroupKeys = function(groupsByKey) {
		return Object.keys(groupsByKey).sort(function(a, b) {
			var aOrder = groupSortOrder(groupsByKey, a);
			var bOrder = groupSortOrder(groupsByKey, b);
			if (aOrder !== bOrder) {
				return aOrder - bOrder;
			}
			return groupDisplayLabel(a).localeCompare(groupDisplayLabel(b), undefined, { sensitivity: 'base' });
		});
	};

	var resolveShapeWmsToken = function() {
		if (typeof window.getShapeWmsToken === 'function') {
			return window.getShapeWmsToken();
		}
		return localStorage.getItem('authToken') || 'public';
	};

	var resolveShapeWmsEndpoint = function() {
		if (options.shapeView && typeof window.buildShapeWmsEndpoint === 'function') {
			return window.buildShapeWmsEndpoint();
		}
		return options.wmsEndpoint;
	};

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
		var __t = window.__t || function(s){ return s; };
		jQuery(openCloseDiv).attr('title', __t('hide_control_panel'));

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
						var __t = window.__t || function(s){ return s; };
						jQuery(openCloseDiv).attr('title', __t('show_control_panel'));

						break;
					case 'closed':
						jQuery(openCloseDiv).removeClass('fa fa-caret-right');
						jQuery(openCloseDiv).addClass('fa fa-caret-left');

						jQuery(openCloseDiv).attr('state', 'open');
						jQuery(openCloseDiv).attr('title', __t('hide_control_panel'));

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

			var __t = window.__t || function(s){ return s; };
			jQuery(mirrorDiv).append('<label id="whereTableCaption" class="cnst-widget-where-input-table-ol-caption">'+__t('spatial_extent')+'</label>');

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


		if (options.shapeView || options.wmsEndpoint) {

			//
			// creates the layers selector
			//

			var groupLabelText = __t('predefined_group_label');
			if (groupLabelText === 'predefined_group_label') {
				groupLabelText = 'Group';
			}
			var areaLabelText = __t('predefined_area_label');
			if (areaLabelText === 'predefined_area_label') {
				areaLabelText = 'Area';
			}

			var layerSelectorDiv = '<div id="layerSelectorDiv" style="display:none" class="cnst-widget-where-wms-selector-div predefined-shape-dropdowns">';
			layerSelectorDiv += '<div class="predefined-shape-dropdown-row">';
			layerSelectorDiv += '<label for="predefinedGroupSelect">' + groupLabelText + '</label>';
			layerSelectorDiv += '<select id="predefinedGroupSelect" class="cnst-widget-input predefined-shape-select"></select>';
			layerSelectorDiv += '</div>';
			layerSelectorDiv += '<div class="predefined-shape-dropdown-row">';
			layerSelectorDiv += '<label for="predefinedAreaSelect">' + areaLabelText + '</label>';
			layerSelectorDiv += '<select id="predefinedAreaSelect" class="cnst-widget-input predefined-shape-select"></select>';
			layerSelectorDiv += '</div>';
			layerSelectorDiv += '<div id="predefinedLayersMessage" class="predefined-layers-message" style="display:none;"></div>';
			layerSelectorDiv += '</div>';

			jQuery(controlDiv).append(layerSelectorDiv);

			jQuery(document).on('change', '#predefinedGroupSelect', function() {
				clearPredefinedLayerSelection();
				populateAreaSelect(jQuery('#predefinedGroupSelect').val(), '');
			});

			jQuery(document).on('change', '#predefinedAreaSelect', function() {
				var selectedName = jQuery('#predefinedAreaSelect').val();
				if (!selectedName) {
					clearPredefinedLayerSelection();
					return;
				}
				var layer = predefinedLayers.find(function(item) {
					return item.name === selectedName;
				});
				if (layer) {
					applyPredefinedLayerSelection(layer);
				}
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

		var containsButton = jQuery('#containsButton');
		if (!containsButton.length) {
			return 'CONTAINS';
		}

		return containsButton.attr('check') === 'true' ? 'CONTAINS' : 'OVERLAPS';
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

	inControl.clearSpatialConstraints = function() {

		if (options.value) {
			options.value.predefinedLayer = null;
			options.value.south = '';
			options.value.west = '';
			options.value.north = '';
			options.value.east = '';
		}

		if (options.olMap) {
			options.olMap.selectionVisible(false);
		}

		jQuery('#' + southFieldId).val('');
		jQuery('#' + westFieldId).val('');
		jQuery('#' + northFieldId).val('');
		jQuery('#' + eastFieldId).val('');
		jQuery('#' + topSearchFieldId).val('');
		inControl.clearLocationLabel();

		resetPredefinedLayerDropdowns();
		clearPredefinedLayerSelection();
		if (predefinedLayers.length) {
			resetPredefinedLayerDropdownsWithoutSelection();
		} else {
			resetPredefinedLayerDropdowns();
		}
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

	var setPredefinedLayersListMessage = function(message, className) {
		var messageDiv = jQuery('#predefinedLayersMessage');
		if (!messageDiv.length) {
			return;
		}
		messageDiv.attr('class', className || 'predefined-layers-message');
		messageDiv.text(message).show();
		jQuery('#predefinedGroupSelect, #predefinedAreaSelect').prop('disabled', true);
	};

	var hidePredefinedLayersListMessage = function() {
		jQuery('#predefinedLayersMessage').hide().text('');
		jQuery('#predefinedGroupSelect, #predefinedAreaSelect').prop('disabled', false);
	};

	var resetPredefinedLayerDropdowns = function() {
		jQuery('#predefinedGroupSelect').empty();
		jQuery('#predefinedAreaSelect').empty();
	};

	var hasUngroupedLayers = function(layers) {
		return (layers || predefinedLayers).some(function(layer) {
			return !(layer.group || '').trim();
		});
	};

	var populateGroupSelect = function(selectedGroupKey) {
		var groupSelect = jQuery('#predefinedGroupSelect');
		groupSelect.empty();

		if (!predefinedLayers.length) {
			return;
		}

		var groupsByKey = buildGroupIndex(predefinedLayers);
		var groupKeys = sortedGroupKeys(groupsByKey);
		var hasUngrouped = hasUngroupedLayers(predefinedLayers);

		if (hasUngrouped) {
			groupSelect.append(jQuery('<option>', { value: '', text: '' }));
		}

		groupKeys.forEach(function(groupKey) {
			groupSelect.append(jQuery('<option>', {
				value: groupKey,
				text: groupDisplayLabel(groupKey)
			}));
		});

		if (selectedGroupKey && groupsByKey[selectedGroupKey]) {
			groupSelect.val(selectedGroupKey);
		} else if (!hasUngrouped && groupKeys.length > 0) {
			groupSelect.val(groupKeys[0]);
		} else {
			groupSelect.val('');
		}
	};

	var resetPredefinedLayerDropdownsWithoutSelection = function() {
		if (!predefinedLayers.length) {
			resetPredefinedLayerDropdowns();
			return;
		}

		var groupsByKey = buildGroupIndex(predefinedLayers);
		var groupKeys = sortedGroupKeys(groupsByKey);
		var hasUngrouped = hasUngroupedLayers(predefinedLayers);

		populateGroupSelect('');
		if (!hasUngrouped && groupKeys.length > 0) {
			populateAreaSelect(groupKeys[0], '');
		} else {
			populateAreaSelect('', '');
		}
	};

	var populateAreaSelect = function(groupKey, selectedLayerName) {
		var areaSelect = jQuery('#predefinedAreaSelect');
		areaSelect.empty();

		var __t = window.__t || function(s) { return s; };
		var noSelection = __t('predefined_no_selection');
		if (noSelection === 'predefined_no_selection') {
			noSelection = '(no selection)';
		}
		areaSelect.append(jQuery('<option>', { value: '', text: noSelection }));

		if (!groupKey) {
			return;
		}

		var groupsByKey = buildGroupIndex(predefinedLayers);
		var layers = groupsByKey[groupKey] || [];

		layers.forEach(function(layer) {
			areaSelect.append(jQuery('<option>', {
				value: layer.name,
				text: layer.title || layer.name
			}));
		});

		if (selectedLayerName) {
			areaSelect.val(selectedLayerName);
		}
	};

	var clearPredefinedLayerSelection = function() {
		if (options.value) {
			options.value.predefinedLayer = null;
		}
		if (shapeLayer != null) {
			resultsMapWidget.removeLayers(shapeLayer);
			shapeLayer = null;
		}
	};

	var applyPredefinedLayerSelection = function(layer) {
		if (!layer) {
			clearPredefinedLayerSelection();
			return;
		}

		if (options.value) {
			options.value.predefinedLayer = layer.name;
		}

		var onlineArray = [];
		var protocol = 'urn:ogc:serviceType:WebMapService:' + options.wmsVersion + ':HTTP';
		onlineArray.push({
			'function': 'download',
			'name': layer.name,
			'title': layer.title,
			'protocol': protocol,
			'url': resolveShapeWmsEndpoint()
		});

		var mapLayers = GIAPI.LayersFactory.layers(onlineArray, 'urn:ogc:serviceType:WebMapService:');

		if (shapeLayer != null) {
			resultsMapWidget.removeLayers(shapeLayer);
		}

		resultsMapWidget.addLayers(mapLayers);
		shapeLayer = mapLayers;
	};

	var restorePredefinedLayerSelection = function() {
		var selectedName = options.value && options.value.predefinedLayer;
		if (!selectedName || !predefinedLayers.length) {
			resetPredefinedLayerDropdownsWithoutSelection();
			return;
		}

		var layer = predefinedLayers.find(function(item) {
			return item.name === selectedName;
		});

		if (!layer) {
			resetPredefinedLayerDropdownsWithoutSelection();
			return;
		}

		var groupKey = layerGroupKey(layer);
		populateGroupSelect(groupKey);
		populateAreaSelect(groupKey, layer.name);
		applyPredefinedLayerSelection(layer);
	};

	var refreshPredefinedLayerDropdowns = function() {
		hidePredefinedLayersListMessage();
		resetPredefinedLayerDropdowns();

		if (!predefinedLayers.length) {
			var __t = window.__t || function(s) { return s; };
			var emptyLabel = __t('predefined_selection_empty');
			if (emptyLabel === 'predefined_selection_empty') {
				emptyLabel = 'No predefined search areas available.';
			}
			setPredefinedLayersListMessage(emptyLabel);
			return;
		}

		restorePredefinedLayerSelection();
	};

	/**
	  * 
	  */
	var downloadWmsLayers = function() {

		var __t = window.__t || function(s) { return s; };
		var loadingLabel = __t('loading');
		if (loadingLabel === 'loading') {
			loadingLabel = 'Loading...';
		}
		setPredefinedLayersListMessage(loadingLabel);

		var endpoint = options.dabNode.endpoint();
		endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

		var servicePath = options.dabNode.servicePath();

		var query;
		if (options.shapeView && typeof window.buildShapeWmsLayersQuery === 'function') {
			query = window.buildShapeWmsLayersQuery(options.wmsVersion);
		} else if (options.wmsEndpoint) {
			query = endpoint + servicePath + '/opensearch/wmslayershandler?request=capabilities&endpoint='
				+ options.wmsEndpoint + '&version=' + options.wmsVersion;
		} else {
			return;
		}

		jQuery.ajax({

			type: 'GET',
			url: query,
			crossDomain: true,
			dataType: 'jsonp',

			success: function(data, status, jqXHR) {

				if (data && data.layers) {

					predefinedLayers = data.layers;
					refreshPredefinedLayerDropdowns();
				} else {
					var emptyLabel = __t('predefined_selection_empty');
					if (emptyLabel === 'predefined_selection_empty') {
						emptyLabel = 'No predefined search areas available.';
					}
					setPredefinedLayersListMessage(emptyLabel);
				}
			},

			error: function() {
				var errorLabel = __t('predefined_selection_load_error');
				if (errorLabel === 'predefined_selection_load_error') {
					errorLabel = 'Failed to load predefined search areas.';
				}
				setPredefinedLayersListMessage(errorLabel, 'predefined-layers-message predefined-layers-error');
			}
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

		var __t = window.__t || function(s){ return s; };
		var south = '<td style="padding-left: 5px;padding-top:15px"><label>'+__t('south')+'</label></td>';
		south += '<td style="padding-top:10px;padding-right: 5px;"><input value="' + southVal + '" style="font-size: 11px;width: 140px" id="' + southFieldId + '" min="-90" max="90" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + south + '</tr>';
		var west = '<td style="padding-left: 5px;padding-top:5px"><label>'+__t('west')+'</label></td>';
		west += '<td style="padding-right: 5px;"><input value="' + westVal + '" style="font-size: 11px;width: 140px" id="' + westFieldId + '" min="-180" max="180" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + west + '</tr>';
		var north = '<td style="padding-left: 5px;padding-top:5px"><label>'+__t('north')+'</label></td>';
		north += '<td style="padding-right: 5px;"><input value="' + northVal + '" style="font-size: 11px;width: 140px"  min="-90" max="90" id="' + northFieldId + '" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + north + '</tr>';
		var east = '<td style="padding-left: 5px;padding-top:5px"><label>'+__t('east')+'</label></td>';
		east += '<td style="padding-right: 5px; padding-bottom:10px"><input value="' + eastVal + '" style="font-size: 11px;width: 140px"  min="-180" max="180" id="' + eastFieldId + '" step="0.1" type="number"/></td>';
		inputTable += '<tr>' + east + '</tr>';

		//
		// Location ---
		//

		var title = __t('location');
		var help = (__t('location_help_line1') || 'Name of a location (town, city, state, continent) such as "florence", "italy", "u.s.a", "africa", etc.<br>');
		help += (__t('location_help_line2') || 'This is a free-form query, so you may search for "rome, italy" or you can also search for a complete address such as "via nomentana, rome, italy". Commas are optional, but improve performance by reducing the complexity of the search<br><br>');
		help += (__t('location_help_line3') || ('Click the "'+__t('search_location')+'" button or the enter key; if the location is found, the selection is updated with the correspondent bounding box'));

		var locLabel = '<td colspan="2" style="padding-left: 5px;padding-top:5px"><label>'+__t('location')+'</label>' + GIAPI.UI_Utils.helpImage(title, help, 'vertical-align: text-bottom;margin-left: 5px;') + '</td>';
		var locField = '<td colspan="2" style="padding-left:5px"><input id="' + topSearchFieldId + '" style="vertical-align: top;" class="cnst-widget-location-field"/>';

		var searchLocation = function() {

			jQuery('#' + locInfoLabelId).css('color', 'blue');
			jQuery('#' + locInfoLabelId).text(__t('searching_location'));

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
						jQuery('#' + locInfoLabelId).text(__t('location_found'));

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
						jQuery('#' + locInfoLabelId).text(__t('location_not_found'));
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
			'attr': [{ name: 'title', value: __t('search_location') }],

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

		if (options.shapeView || options.wmsEndpoint !== undefined) {

			var layersSelectorButton = GIAPI.ButtonsFactory.onOffSwitchButton(__t('layers_select'), __t('layers_hide'), {
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
			var layerSelectorLabel = '<div style="' + style + '"><label>'+__t('predefined')+'</label></div>';

			inputTable += '<tr><td  colspan="2"><div style="margin-top:10px">' + layerSelectorLabel +

				GIAPI.UI_Utils.helpImage(__t('predefined_selection'), help, 'vertical-align: top;margin-left: -15px;padding-right: 5px;') +

				'<div style="display: inline-block;margin-left: 15px;">' + layersSelectorButton + '</div></div></td></tr>';
		}

		//
		// Contains button
		//

		var containsButton = GIAPI.FontAwesomeButton({
			'id': 'containsButton',
			'width': 90,
			'label': __t('contains'),
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
			'label': __t('overlaps'),
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
				'label': __t('apply'),
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
				'label': __t('clear'),
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

		var title = __t('select_spatial_extent_title');

		if (options.dialogMode) {
			help = __t('select_spatial_extent_help');
		} else {
			help = __t('select_spatial_extent_help_apply');
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
