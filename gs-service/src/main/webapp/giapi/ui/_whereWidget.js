GIAPI._whereWidget = function(dabNode, whereOpt, inputId) {

	var widget = {};
	var initialized;
	var selection;
	var vectorSrc;
	var widgetMap;
	var mapDiv;

	var mouseOver = false;
	var padding = 30;

	var _inputControl;

	var dialogId = GIAPI.random();
	jQuery('head').append('<div id="' + dialogId + '"/>');

	/**
	 * Shows the widget dialog
	 */
	widget.show = function() {

		if (!initialized) {

			initialized = true;

			var pos = GIAPI.position(whereOpt.widgetPosition);

			jQuery('#' + dialogId).dialog({

				height: whereOpt.widgetHeight + padding,
				width: whereOpt.widgetWidth + padding,
				modal: whereOpt.widgetModal,
				autoOpen: false,
				resizable: false,
				dialogClass: "no-titlebar",

				close: function(e) {

					_inputControl.clearLocationLabel();

					switch (jQuery('#' + dialogId).attr('apply')) {

						case 'true':

							var where = _inputControl.where();

							if (whereOpt.resultsMapWidget) {
								whereOpt.resultsMapWidget.select(where);
							}

							jQuery('#' + inputId).val(where.south + ',' + where.west + ',' + where.north + ',' + where.east);
							jQuery('#' + inputId).blur();

						case 'false':
					}

					if (GIAPI.UI_Utils.isFullScreen()) {
						// using this instead of GIAPI.UI_Utils.exitFullScreen() since it works
						// also with Firefox and Edge
						jQuery('#' + dialogId + ' .gm-fullscreen-control')[0].click();
					};


					return false;
				},

				position: {
					of: '#' + inputId,
					my: pos.my,
					at: pos.at,
					collision: 'none'
				}
			});

			jQuery('#' + dialogId).parent().css('box-shadow', '0px 0px 6px 4px rgba(0,0,0,0.3)');

			// the first time the dialog must be opened before the init call
			// in order to see the map correctly centered
			jQuery('#' + dialogId).dialog('open');

			// init the widget
			widget.init();

			return;
		}

		jQuery('#' + dialogId).dialog('open');

		// this will refresh the map since after closing the dialog
		// in full screen mode, when the dialog is reopened the map has some glitches
		// if not refreshed

		//TODO: openlayers resize

	};

	/**
	 * Initializes the widget creating the madpDiv and inserting it in the given target div. If not specified inserts the mapDiv in the dialog div
	 */
	widget.init = function(target) {

		var mapDivId = GIAPI.random();

		// ------------------------------------------------
		// creates and appends the mapDiv to the target div
		//
		mapDiv = '<div style="width: ' + whereOpt.widgetWidth + 'px; height: ' + whereOpt.widgetHeight + 'px" class="map-widget-div">';

		var dialogMode = false;

		if (!target) {
			dialogMode = true;
			target = dialogId;
		}

		jQuery('#' + target).append(mapDiv);

		whereOpt.longitude = whereOpt.lng;
		whereOpt.latitude = whereOpt.lat;
		whereOpt.divId = mapDivId;
		whereOpt.dabNode = dabNode;
		whereOpt.dialogMode = dialogMode;
		whereOpt.dialogId = dialogId;

		// -------------------
		// initializes the map
		//
		
		var olMap = GIAPI.OL_Map(whereOpt);

		widgetMap = olMap.map();

		whereOpt.widgetMap = widgetMap;
		whereOpt.olMap = olMap;

		// creates the input control
		_inputControl = GIAPI._whereInputControl(whereOpt);

		// add the input control to the map
		_inputControl.add(widgetMap);

		widgetMap.on('pointermove', function() {

			mouseOver = true;
		});

		widgetMap.getViewport().addEventListener('mouseout', function() {

			mouseOver = false;
		});

		if (target === dialogId && !whereOpt.widgetModal) {
			// it the dialog is not modal, set the listener which closes
			// the dialog after a click outside the dialog
			addOnClickListener();
		}
	};

	/**
	 * Returns the mapDiv. Must be called after the init method
	 */
	widget.mapDiv = function() {

		return mapDiv;
	};

	/**
	 * Refreshes the map and fits the bounds 
	 */
	widget.refreshMap = function() {

		google.maps.event.trigger(widgetMap, 'resize');

		var north = selection.bounds.getNorthEast().lat().toFixed(2);
		var east = selection.bounds.getNorthEast().lng().toFixed(2);
		var south = selection.bounds.getSouthWest().lat().toFixed(2);
		var west = selection.bounds.getSouthWest().lng().toFixed(2);

		widgetMap.fitBounds(new google.maps.LatLngBounds(new google.maps.LatLng(south, west), new google.maps.LatLng(north, east)));
	};

	/**
	 * Returns the input control
	 */
	widget.inputControl = function() {

		return _inputControl;
	};

	var addOnClickListener = function() {

		jQuery(document).click(function(e) {

			var parents = jQuery(e.target).parents();
			// an element of the input control
			for (i = 0; i < parents.length; i++) {

				var cl = jQuery(parents[i]).attr('class');

				if (cl && cl.indexOf('input-control')) {
					return true;
				}
			};

			// spatial relation check
			if (jQuery(e.target).hasClass('medium-onoffswitch-inner') ||
				jQuery(e.target).hasClass('medium-onoffswitch-checkbox')) {
				return true;
			}

			// input control div when the control is closed
			if (jQuery(e.target).children() && jQuery(e.target).children()[0] && jQuery(e.target).children()[0].id === 'input-control') {
				return true;
			}

			// help dialog
			if (GIAPI.UI_Utils.dialog('isOpen') ||
				// close icon
				jQuery(e.target).hasClass('ui-icon')) {
				return true;
			}

			if (e.target.id === inputId || e.target.id === 'helpDialog' || jQuery(e.target).hasClass('cnst-widget-where-input-control-button') ||
				// input control button
				GIAPI.UI_Utils.isFullScreen() ||
				// full screen control
				jQuery(e.target).attr('class') === 'gm-fullscreen-control' ||
				// ...
				jQuery(e.target).attr('class') === 'gmnoprint gm-bundled-control' ||
				// zoom control
				jQuery(e.target).parents('.gmnoprint').length > 0) {

				return true;
			}

			if (!mouseOver) {
				jQuery('#' + dialogId).attr('apply', 'false');
				jQuery('#' + dialogId).dialog('close');
			}
		});
	};

	return widget;
};
