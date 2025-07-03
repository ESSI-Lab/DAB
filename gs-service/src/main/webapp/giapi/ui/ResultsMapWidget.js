/**
* @module UI
**/
import { GIAPI } from '../core/GIAPI.js';

/**
 * This widget localizes all the <a href="../classes/GINode.html" class="crosslink">nodes</a> of the current
 * <a href="../classes/ResultSet.html" class="crosslink">result set</a> <a href="../classes/Page.html" class="crosslink">page</a>
 *  in a map using a marker for each <a href="../classes/GINode.html" class="crosslink">node</a>.<br>
 * This widget can also show, when available, the <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layers</a>
 *  associated to the <a href="../classes/GINode.html" class="crosslink">nodes</a>. See the constructor options for more info.<br><br>
 * The map is realized by default using <a target=_blank href="https://developers.google.com/maps/">Google Maps API</a>
 * and <a target=_blank href="https://hpneo.github.io/gmaps/">gmaps.js</a>. The map can be realized using <a target=_blank href="http://openlayers.org/">Openlayers 3</a> also.<br><br>
 *  Required <a target=_blank href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a>:<pre><code>
 &lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
 </code></pre>
 * The following scripts and CSS are required for <a target=_blank href="https://developers.google.com/maps/">Google Maps API</a>:<pre><code>
 &lt;!-- Google Maps --&gt;
 &lt;script type="text/javascript" src="http://maps.google.com/maps/api/js?" /&gt;
 &lt;!-- Gmap.js --&gt;
 &lt;script type="text/javascript" src="https://raw.githubusercontent.com/HPNeo/gmaps/master/gmaps.js" /&gt;<br>
 </code></pre>
 * The following scripts and CSS are required for <a target=_blank href="http://openlayers.org/">Openlayers 3</a> :<pre><code>
 &lt;!-- Openlayers 3 API --&gt;
 &lt;script type="text/javascript" src="http://openlayers.org/en/v3.15.1/build/ol.js" /&gt;
 &lt;!-- Openlayers 3 API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="http://openlayers.org/en/v3.15.1/css/ol.css" /&gt;<br>
 &lt;!-- Openlayers 3 Layer Switcher --&gt;
 &lt;script type="text/javascript" src="http://cdn.rawgit.com/walkermatt/ol3-layerswitcher/master/src/ol3-layerswitcher.js" /&gt;
 &lt;link rel="stylesheet" type="text/css" href="http://cdn.rawgit.com/walkermatt/ol3-layerswitcher/master/src/ol3-layerswitcher.css" /&gt;<br>
 </code></pre>

 *<pre><code>
 * // creates the widget with the default options
 * var resMapWidget = GIAPI.ResultsMapWidget(id, 10, 10, {
 *      'width': 370,
	 'height': 300,<br>
	 // marker and selection options
	 'markerColor':'red',
	 'markerTitle':function(node){return node.report().title},
	 'selectionColor: '#0000FF',<br>
	 // layers options
	 'addLayers': false,
	 'showLayersControl': false,
	 'layersControlWidth': 155,
	 'layersControlHeight': 100,
	 'layersControlOpacity': 0.8,<br>
	
	 'zoom': 6,
	 'scrollwheel' : true,
	 'panControl' : true,
	 'panControlOptions' : true,
	 'streetViewControl' : false,
	 'overviewMapControl' : true,
	 'navigationControl': false,
	 'fullscreenControl': true,
	 'fullscreenControlOptions': { position: google.maps.ControlPosition.RIGHT_BOTTOM }
 });<br>
 // ...<br>
 var onDiscoverResponse = function(result, response) {
	 var resultSet = response[0];
	 ...
	 // updates the widget with the current result set
	 resMapWidget.update(resultSet);
	 ...
 }</pre></code>
 *
 *<table>
 *               <tbody>
 *                   <tr>
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Google Maps</h3>
 *                           </center>
 *                           <center>
 *                               <img style="border: none;" src="../assets/img/results-map-widget.png" />
 *                           </center>
 *                       </td>
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Openlayers 3</h3>
 *                           </center>
 *                           <center>
 *                              <img style="border: none;" src="../assets/img/ol-results-map-widget.png" />
 *                           </center>
 *                       </td>
 *                    </tr>
 *                </tbody>
 *</table>
 *
 * The images above show the widget with all the default options. For additional personalization of the widget, see the
 *  <code>map-widget</code> class of the <i>giapi.css</i> file
 *
 *<table>
 *               <tbody>
 *                   <tr>
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Google Maps</h3>
 *                           </center>
 *                           <center>
 *                               <img style="border: none;" src="../assets/img/results-map-widget-layers-1.png" />
 *                               <img style="border: none;" src="../assets/img/results-map-widget-layers.png" />
 *                           </center>
 *                       </td>
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Openlayers 3</h3>
 *                           </center>
 *                           <center>
 *                              <img style="border: none;" src="../assets/img/ol-results-map-widget-layers-1.png" />
 *                              <img style="border: none;" src="../assets/img/ol-results-map-widget-layers.png" />
 *                           </center>
 *                       </td>
 *                    </tr>
 *                </tbody>
 *</table>
 *
 * The images above show the widget with some <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">Google Maps layers</a> or <a href="../classes/GINode.html#method_olMWS_Layer" class="crosslink">Openlayers 3 layers</a>
 *  and with the <a href="#layersControl" class="crosslink">layers control</a> activated. For additional personalization of the layers control, see the
 *  <code>layers-control</code> class of the <i>giapi.css</i> file
 *
 *
 * @class ResultsMapWidget
 * @constructor
 *
 * @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the widget is inserted
 * @param {Double} latitude latitude of the initial map center
 * @param {Double} longitude longitude of the initial map center
 * @param {Object} [options] all the available <a href="https://developers.google.com/maps/documentation/javascript/reference#MapOptions" target=_blank>map options</a> are also allowed
 * 
 * @param {Integer} [options.width=370]
 * @param {Integer} [options.height=300]
 *
 * @param {Boolean} [options.showNoResultsMsg=true]
 * @param {Boolean} [options.noResultsMsg="No geolocalized results to show"]

 * @param {String} [options.markerColor="red"] possible values: "red", "yellow", "green", "red"
 * @param {String} [options.markerIcon] URL of an image to use as marker icon (overrides the <code>options.markerColor</code> option)
 * @param {Function} [options.markerTitle="return node.report().title"]
 * 
 * @param {DAB} dabNode
 *
 * @param {Function} [options.onMarkerClick]
 * @param {Function} [options.onMarkerMouseOver]
 * @param {Function} [options.onMarkerMouseOut]

 * @param {String} [options.selectionColor='#0000FF']

 * @param {Boolean} [options.addLayers=false] if <code>true</code> <a name="addLayers">adds automatically</a> all the <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layers</a>
 * available in the <a href="../classes/Page.html" class="crosslink">page</a> of the current <a href="../classes/ResultSet.html" class="crosslink">result set</a>.<br>
 * See also <a href="#method_addLayersButton" class="crosslink">addLayersButton</a> method
 * @param {Boolean} [options.showLayersControl=false] if set to <code>true</code> a <a name="layersControl">map control</a> is showed on the right-top corner. The control allows to select/deselect the
 * <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layers</a> added to the map
 * @param {Boolean} [options.layersControlWidth=155]
 * @param {Boolean} [options.layersControlHeight=100]
 * @param {Boolean} [options.layersControlOpacity=0.8]
 * 
 * @param {String} [options.wmsVersion='1.3.0']
 * 
 * @param {Boolean} [options.clusterWMS=false]
   @param {String} [options.clusterWMSToken]
   @param {String} [options.clusterWMSView]
   @param {String} [options.clusterWMSLayer]
   @param {String} [options.clusterWMSLayerName]
   @param {String} [options.clusterWMSLayerTitle]
 
 */

GIAPI.ResultsMapWidget = function(id, latitude, longitude, options) {

	var layerSwitcher;
	var widget = {};
	var selection;
	var noResultsOverlay;
	var olMap;
	var showRectangles = true;
	var rectanglesArray = [];
	var _inputControl;
	var wait = false;

	var mouseMoveHandler = null;
	var mouseClickHandler = null;

	if (!options) {
		options = {};
	}

	if (!options.width) {
		options.width = '368px';

	} else if (!jQuery.isNumeric(options.width) && options.width.indexOf('%') === 0) {

		options.width += 'px';
	}

	if (!options.height) {
		options.height = '300px';

	} else if (!jQuery.isNumeric(options.height) && options.height.indexOf('%') === 0) {

		options.height += 'px';
	}

	if (options.showSelectionControl === undefined) {
		options.showSelectionControl = true;
	}

	if (options.showNoResultsMsg === undefined) {
		options.showNoResultsMsg = false;
	}
	if (!options.noResultsMsg) {
		options.noResultsMsg = "No geolocalized results to show";
	}

	if (options.showSpatialRelationControl === undefined || showSpatialRelationControl === true) {
		options.showSpatialRelationControl = true;
	}

	if (!options.markerTitle) {
		options.markerTitle = function(node) {

			return node.report().title;
		};
	}

	if (!options.markerColor) {
		options.markerColor = 'red';
	}
	if (!options.selectionColor) {
		options.selectionColor = '#0000FF';
	}
	if (options.showLayersControl === undefined) {
		options.showLayersControl = false;
	}
	if (options.addLayers === undefined) {
		options.addLayers = false;
	}
	if (!options.layersControlWidth) {
		options.layersControlWidth = 155;
	}
	if (!options.layersControlHeight) {
		options.layersControlHeight = 100;
	}
	if (!options.layersControlOpacity) {
		options.layersControlOpacity = 0.8;
	}

	var style = '';
	style += '.layers-control-table-div{ opacity: ' + options.layersControlOpacity + '; width: ' + options.layersControlWidth + 'px; ';
	style += ' height: ' + options.layersControlHeight + 'px; margin-left: -' + (options.layersControlWidth + 15) + 'px;}';

	GIAPI.UI_Utils.appendStyle(style);

	if (!options.minZoom) {
		options.minZoom = 1;
	}

	if (!options.maxZoom) {
		options.maxZoom = 10;
	}

	if (!options.zoom) {
		options.zoom = 6;
	}

	if (options.scrollwheel === undefined) {
		options.scrollwheel = true;
	}

	if (options.panControl === undefined) {
		options.panControl = true;
	}

	if (options.panControlOptions === undefined) {
		options.panControlOptions = true;
	}

	if (options.streetViewControl === undefined) {
		options.streetViewControl = false;
	}

	if (options.overviewMapControl === undefined) {
		options.overviewMapControl = true;
	}

	if (options.navigationControl === undefined) {
		options.navigationControl = false;
	}

	if (options.fullscreenControl === undefined) {
		options.fullscreenControl = true;
	}

	if (options.zoomControl === undefined) {
		options.zoomControl = true;
	}

	if (options.showLocationControl === undefined || options.showLocationControl === null) {
		options.showLocationControl = true;
	}

	if (!options.wmsVersion) {
		options.wmsVersion = '1.3.0';
	}

	var divId = GIAPI.random();
	var div = '<div style="width: ' + options.width + '; height: ' + options.height + 'px" class="map-widget-div"><div id="' + divId + '"  class="ol-fullscreen-map"></div></div>';

	jQuery('#' + id).append(div);

	options.longitude = longitude;
	options.latitude = latitude;
	options.divId = divId;
	options.dialogMode = false;

	// creates the ol map
	olMap = GIAPI.OL_Map(options);

	widget.map = olMap.map();

	options.widgetMap = widget.map;
	options.olMap = olMap;

	if (options.showSelectionControl) {
		// creates the input control
		_inputControl = GIAPI._whereInputControl(widget, options);
		_inputControl.updateWhereFields();

		// add the input control to the map
		_inputControl.add(widget.map);
	}

	function createMouseMoveHandler(wmsLayer) {
		return function mouseMoveFunction(evt) {

			if (evt === undefined) {
				return;
			}

			if (evt.dragging) {
				return;
			}

			if (wmsLayer === undefined) {
				return;
			}

			const data = wmsLayer.getData(evt.pixel);
			const hit = data && data[3] > 0; // transparent pixels have zero for data[3]
			//const hit = data && data[3] == 255; // transparent pixels have zero for data[3]


			if (!wait) {
				widget.map.getTargetElement().style.cursor = hit ? 'pointer' : '';
			}

		};
	}

	function createMouseClickHandler(wmsLayer, overlay) {
		return function mouseClickFunction(evt) {

			const data = wmsLayer.getData(evt.pixel);

			var hit = data && data[3] > 0; // transparent pixels have zero for data[3]

			if (!hit) {
				return false;
			}

			//console.log(data);
			hit = data && data[3] == 255; // transparent pixels have zero for data[3]
			if (!hit) {
				const clickedCoordinates = evt.coordinate;

				const view = widget.map.getView();
				view.setCenter(clickedCoordinates);
				const currentZoom = view.getZoom();
				if (currentZoom !== undefined) {
					view.setZoom(currentZoom + 1); // Increase zoom by 1
				}
				return true;
			}


			widget.map.getTargetElement().style.cursor = 'wait';
			wait = true;

			document.getElementById('stationInfo').innerHTML = '';

			const coordinate = evt.coordinate;

			const viewResolution = /** @type {number} */ (widget.map.getView().getResolution());
			const url = wmsLayer.getSource().getFeatureInfoUrl(
				evt.coordinate,
				viewResolution,
				'EPSG:3857',
				{ 'INFO_FORMAT': 'text/html' },
			);

			if (url) {
				fetch(url)
					.then((response) => response.text())
					.then((html) => {

						document.getElementById(options.stationInfoId).innerHTML = html;
						overlay.setPosition(coordinate);

						widget.map.getTargetElement().style.cursor = 'pointer';
						wait = false;

						// closes the station info popup on a X click
						jQuery('#closePopup').on("click", function() {

							overlay.setPosition(undefined);
							return false;
						});

					});
			}
		};
	}

	var createWMSCLusterLayer = function(options, constraints) {


		if (!constraints) {
			constraints = {};
		}

		var query = 'what=' + (constraints.what || '') + '&';

		query += 'from=' + ((constraints.when && constraints.when.from) || '') + '&';

		query += 'to=' + ((constraints.when && constraints.when.to) || '') + '&';

		if (constraints.sources) {
			var value = constraints.sources;
			query += 'sources=' + value + '&';

		}

		if (constraints.observedPropertyURI) {
			var value = constraints.observedPropertyURI;
			query += 'observedPropertyURI=' + value + '&';
		}

		if (constraints.organisationName) {
			var value = constraints.organisationName;
			query += 'organizationName=' + value + '&';

		}

		if (constraints.platformTitle) {
			var value = constraints.platformTitle;
			query += 'platformTitle=' + value + '&';

		}

		if (constraints.keyword) {
			var value = constraints.keyword;
			query += 'keyword=' + value + '&';

		}

		if (constraints.sources) {
			var sources = constraints.sources;
			query += 'sources=' + sources + '&';

		}

		if (constraints.attributeTitle) {
			var value = constraints.attributeTitle;
			query += 'attributeTitle=' + value + '&';

		}

		if (constraints.timeInterpolation) {
			var value = constraints.timeInterpolation;
			query += 'timeInterpolation=' + value + '&';

		}

		if (constraints.intendedObservationSpacing) {
			var value = constraints.intendedObservationSpacing;
			query += 'intendedObservationSpacing=' + value + '&';

		}

		if (constraints.aggregationDuration) {
			var value = constraints.aggregationDuration;
			query += 'aggregationDuration=' + value + '&';

		}

		if (constraints.where) {
			if (constraints.where.predefinedLayer) {
				query += 'predefinedLayer=' + constraints.where.predefinedLayer + '&';
			}
			if (constraints.where.south && constraints.where.west && constraints.where.north && constraints.where.east) {
				var where = constraints.where;
				query += 'where=' + where.south + ',' + where.west + ',' + where.north + ',' + where.east + '&';
			}
			var spatialOp = constraints.spatialOp;
			query += 'spatialOp=' + spatialOp + '&';

		}

		if (constraints.ontology) {

			var ontology = constraints.ontology;
			query += 'ontology=' + ontology + '&';
		}


		if (constraints.kvp) {

			constraints.kvp.forEach(function(item) {

				query += item.key + '=' + item.value + '&'
			});
		}

		var layerName = options.clusterWMSLayerName;
		var layerTitle = options.clusterWMSLayerTitle;

		if (options.availability !== undefined && options.availability) {
			query += 'styles=availability&';
			layerName += '.availability';
			layerTitle += ' data availability';

		}

		var onlineArray = [];
		var protocol = 'urn:ogc:serviceType:WebMapService:1.3.0:HTTP';

		var endpoint = options.dabNode.endpoint();
		endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

		var servicePath = options.dabNode.servicePath();

		if (options.clusterWMS !== undefined && options.clusterWMS) {
			var url = endpoint + servicePath + '/token/' + options.clusterWMSToken + '/view/' + options.clusterWMSView + '/wms-cluster?' + query;

			var online = {

				'function': 'download',
				'name': layerName,
				'title': layerTitle,
				'protocol': protocol,
				'url': url
			};

			onlineArray.push(online);

			var array = GIAPI.LayersFactory.layers(onlineArray, 'urn:ogc:serviceType:WebMapService:', options);
			var wmsLayer = array[0];

			if (options.availability === undefined || !options.availability) {
				//
				// set the pointer cursor when over a tile
				//
				if (mouseMoveHandler != null) {
					widget.map.un('pointermove', mouseMoveHandler);
				}

				var mmf = createMouseMoveHandler(wmsLayer);
				widget.map.on('pointermove', mmf);
				mouseMoveHandler = mmf;


				//
				// creates an overlay to anchor the popup to the map
				//		 
				const overlay = new ol.Overlay({
					element: document.getElementById(options.stationInfoId)
				});

				widget.map.addOverlay(overlay);

				if (mouseClickHandler != null) {
					widget.map.un('singleclick', mouseClickHandler);
				}
				var mcf = createMouseClickHandler(wmsLayer, overlay);
				widget.map.on('singleclick', mcf);
				mouseClickHandler = mcf;


			}

		}




		return array;
	};


	/**
	 * 
	 */
	widget.updateWMSClusterLayers = function(constraints) {


		if (config.clusterWMS!==undefined && config.clusterWMS){

			options.availability = false;
			options.visible = true;
			var layerArray = createWMSCLusterLayer(options, constraints);
			options.availability = true;
			options.visible = false;
			var layerArray2 = createWMSCLusterLayer(options, constraints);
	
			olMap.removeLayers(layerArray);
			olMap.addLayers(layerArray);
	
			olMap.removeLayers(layerArray2);
			olMap.addLayers(layerArray2);
	
			layerSwitcher.renderPanel();
		}

	}

	/**
	 * Updates the widget with the first <a href="../classes/Page.html" class="crosslink">page</a> of the current <a href="../classes/ResultSet.html" class="crosslink">result set</a>
	 *
	 * @param {ResultSet} resultSet
	 * @method update
	 */
	widget.update = function(resultSet) {

		var paginator = resultSet.paginator;
		var page = paginator.page();
		var arrayFeatures = new Object();

		if (options.showNoResultsMsg && count === 0) {

			jQuery('#no-results-div').css('display', 'inline');

		} else {

			jQuery('#no-results-div').css('display', 'none');
		}

		options.page = page;

		// set the map markers
		olMap.markers(options);

		// resets the page in order to make it reusable
		page.reset();

		// set the bboxes
		olMap.bboxes(options);

		// resets the page in order to make it reusable
		page.reset();

		while (page.hasNext()) {

			var node = page.next();
			var title = node.report().title;

			// adds the layers
			var layers = node.olMWS_Layer({
				// options : {
				// isBaseLayer : false,
				// rendererOptions : {
				// zIndexing : true
				// }
				//}
			});

			if (layers.length > 0 && options.addLayers) {
				addLayers(layers);
			}
		}

		// resets the page in order to make it reusable
		page.reset();
	};

	/**
	 *
	 * @method selection
	 * @return {BBox}
	 */
	widget.where = function() {
		if (!olMap.selectionVisible()) {
			return null;
		}

		var where = _inputControl.where(true);
		if (options.value && options.value.predefinedLayer) {
			where.predefinedLayer = options.value.predefinedLayer;
		}
		return where;
	};


	/**
	 *
	 * @method spatialRelation
	 */
	widget.spatialRelation = function() {

		if (!_inputControl) {
			//default case: CONTAINS
			return 'CONTAINS';
		}
		return _inputControl.spatialRelation();
	};

	/**
	 * If the given <code><a href="../classes/GINode.html" class="crosslink">node</a></code> has
	 * <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layers</a>, this method returns a
	 * <a href="../classes/FontAwesomeButton.html" class="crosslink">FontAwesomeButton</a> ready for adding the
	 * <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layers</a> to the map.<br>
	 * See also <code><a href="#addLayers" class="crosslink">addLayers</a></code> option
	 *
	 * @param {GINode} node
	 * @method addLayersButton
	 */
	widget.addLayersButton = function(node) {

		// adds the layers
		var layers = node.olMWS_Layer({});

		if (layers.length > 0) {

			var button = GIAPI.FontAwesomeButton({
				'width': 24,
				'label': '',
				'icon': 'fa-map-o',
				'handler': function() {
					addLayers(layers);
					return false;
				},
				'attr': [{ name: 'title', value: 'Add layers' }]
			});

			button.css('div', 'padding', '4px');
			button.css('div', 'background', ' #2196f3');
			button.css('icon', 'font-size', '12.5px');

			return button;
		}

		return null;
	};

	/**
	 * 
	 * @param {GINode} node
	 * @param {Object} options
	 * @param {String} [options.color]
	 * @param {String} [url]
	 * 
	 * @method markerIcon
	 */
	widget.markerIcon = function(node, options) {

		olMap.markerIcon(node, options);
	};

	/**
	 * Applies the given bounding box to the user selection
	 */
	widget.select = function(where) {

		if (!where) {
			olMap.selectionVisible(false);
		} else {
			olMap.select(where);
		}

		if (options.showSelectionControl) {
			_inputControl.updateWhereFields();
		}
	};

	/**
	 * 
	 */
	widget.addLayers = function(layers) {

		addLayers(layers);
	};

	var addLayers = function(layers) {

		olMap.addLayers(layers);
	};

	widget.removeLayers = function(layers) {

		removeLayers(layers);
	};

	var removeLayers = function(layers) {

		olMap.removeLayers(layers);
	};

	var createNoResultsOverlay = function() {

		var div = document.createElement('div');
		div.id = 'no-results-div';

		jQuery(div).attr('class', 'results-map-widget-no-results-div');
		div.innerHTML = '<label style="font-weight: bold;font-style: italic">' + options.noResultsMsg + '</label>';

		var popup = new ol.Overlay({
			element: document.getElementById('no-results-div')
		});

		widget.map.addOverlay(popup);

		popup.setPosition([longitude, latitude]);
		popup.setPositioning('top-right');
	};



	var createLayersControl = function() {


		layerSwitcher = new ol.control.LayerSwitcher({
			tipLabel: 'Show layers control', //
			collapseTipLabel: 'Hide layers control',//
			activationMode: 'click', //

			startActive: options.startActive, //
			groupSelectStyle: 'children', // Can be 'children' [default], 'group' or 'none',

			clusterWMS: options.clusterWMS,
			clusterWMSToken: options.clusterWMSToken,

			clusterWMSView: options.clusterWMSView,
			clusterWMSLayer: options.clusterWMSLayer,
			clusterWMSLayerName: options.clusterWMSLayerName,
			clusterWMSLayerTitle: options.clusterWMSLayerTitle,
			dabEndpoint: options.dabNode.endpoint(),
			servicePath: options.dabNode.servicePath()
		});

		widget.map.addControl(layerSwitcher);

	};

	var userSelectionControl = function() {

		var content = '<caption id="layersCaption" class="layers-caption">LAYERS</caption>';
		content += '<tr><td><input class="layers-control-check" type="checkbox" id="user-selection" checked/></td>';
		content += '<td>User selection</td>';
		content += '</tr>';

		jQuery(document).on('click', '#user-selection', function() {

			selection.setVisible(this.checked);
		});

		return content;
	};

	var showRectanglesControl = function() {

		var checked = showRectangles ? ' checked' : '';
		var content = '<tr>';
		content += '<td><input class="layers-control-check" type="checkbox" id="rectangles" ' + checked + '/></td>';
		content += '<td>Results area</td>';
		content += '</tr>';

		jQuery(document).on('click', '#rectangles', function() {

			showRectangles = this.checked;

			for (var i = 0; i < rectanglesArray.length; i++) {
				rectanglesArray[i].setVisible(this.checked);
			}
		});

		return content;
	};

	var updateLayersTable = function() {

		var normalizeLayerName = function(name) {

			return name.replace(/:/g, '', '_');
		};

		var layers = widget.map.map.overlayMapTypes;
		jQuery('#layers-table').html('');

		// adds the all layer check
		var content = '<tr><td colspan="2"><input class="layers-control-check" type="checkbox" id="layer-check_all" checked/></td></tr>';

		content += userSelectionControl();
		content += showRectanglesControl();

		// only the first layer is visible
		var visible = true;

		// adds a check for each layer
		layers.forEach(function(layer) {

			if (layer.name) {
				var name = normalizeLayerName(layer.name);

				var checked = visible ? ' checked' : '';
				layer.setOpacity(checked ? 1 : 0);
				visible = false;

				content += '<tr>';
				content += '<td><input class="layers-control-check" type="checkbox" id="layer-check_' + name + '"' + checked + '/></td>';
				content += '<td title="' + name + '">' + name + '</td>';
				content += '</tr>';
			}
		});

		jQuery('#layers-table').append(content);

		// adds a listener for each check
		jQuery('[id^="layer-check_"]').click(function() {

			var layerName = this.id.replace('layer-check_', '');
			var opacity = this.checked ? 1 : 0;

			layers.forEach(function(layer) {
				if (layer.name === layerName || layerName === 'all') {
					layer.setOpacity(opacity);
					jQuery('#layer-check_' + normalizeLayerName(layer.name)).prop('checked', opacity ? 'checked' : '');
				}
			});
		});
	};

	if (options.showLayersControl) {

		createLayersControl();
	}

	if (options.showNoResultsMsg) {

		createNoResultsOverlay();
	}

	return widget;
};
