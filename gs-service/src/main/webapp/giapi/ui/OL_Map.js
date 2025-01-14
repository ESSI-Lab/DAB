/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

GIAPI.OL_Map = function(options) {

	var obj = {};

	var selVisible = false;
	var selectionVector = new ol.source.Vector();

	var featuresArray = {};

	//
	// markers vector
	// 

	var markersVector = new ol.source.Vector({
		features: []
	});

	var markerURL = function() {

		if (options.markerURL) {

			return options.markerURL;

		} else {

			return GIAPI.UI_Utils.markerIcon(options.markerColor);
		}
	};

	var iconStyle = new ol.style.Style({
		image: new ol.style.Icon(({
			anchor: [0.5, 1],
			src: markerURL()
		}))
	});

	var markersLayer = new ol.layer.Vector({
		title: 'Markers',
		source: markersVector,
		style: iconStyle
	});

	//
	// bboxes vector
	// 

	var bboxesVector = new ol.source.Vector({
		features: []
	});

	var bboxesLayer = new ol.layer.Vector({
		title: 'Bounding box',
		source: bboxesVector
	});

	//
	// 
	// 

	var flagLayer;
	var flagSource;

	var extent = [-30, -30, -30, -30];
	var where = { south: -30, west: -30, east: -30, north: -30 };

	//
	// selection feature
	//

	var polygon = ol.geom.Polygon.fromExtent(extent);
	var feature = new ol.Feature({
		geometry: polygon
	});

	var hexColor = '#0000FF';
	var color = ol.color.asArray(hexColor);
	color = color.slice();
	color[3] = 0.2;

	var userSelectionFeatureStyle = new ol.style.Style({
		fill: new ol.style.Fill({
			color: color,
			weight: 10
		}),
		stroke: new ol.style.Stroke({
			color: hexColor,
			width: 1.5
		}),
	});

	feature.setStyle(userSelectionFeatureStyle);

	selectionVector.clear();
	selectionVector.addFeature(feature);

	//
	// results feature style
	//

	hexColor = '#FFFD55';
	color = ol.color.asArray(hexColor);
	color = color.slice();
	color[3] = 0.05;

	var bboxesFeatureStyle = new ol.style.Style({
		fill: new ol.style.Fill({
			color: color,
			weight: 10
		}),
		stroke: new ol.style.Stroke({
			color: hexColor,
			width: 1.5
		}),
	});

	//
	//
	//

	var controls = [ //
		new ol.control.Attribution(),//
		new ol.control.MousePosition({//
			//undefinedHTML : 'outside',
			projection: 'EPSG:3857',
			coordinateFormat: function(coordinate) {
				return ol.coordinate.format(coordinate, '{x}, {y}', 3);
			}
		}),//
		new ol.control.ScaleLine(),  //
		// new ol.control.Zoom({ className: 'custom-zoom' }), //
		new ol.control.Rotate({//
			autoHide: true
		})];

	if (options.fullscreenControl) {
		controls.push(new ol.control.FullScreen());
	}

	var attributions = '<a href="https://www.arcgis.com/home/item.html?id=10df2279f9684e4a9f6a7f08febac2a9" target="_blank">ArcGIS</a>';

	var map = new ol.Map({

		target: options.divId,

		layers: [],

		interactions: ol.interaction.defaults.defaults({

			mouseWheelZoom: options.scrollwheel
		}),

		controls: controls,

		view: new ol.View({
			projection: 'EPSG:3857',
			constrainResolution: false,

			center: ol.proj.transform([options.longitude, options.latitude], 'EPSG:4326', 'EPSG:3857'),
			//minZoom: options.minZoom,
			//maxZoom: options.maxZoom,
			zoom: options.zoom
			
		})
	});
	
	//
	// base maps group
	//

	var baseMapsGroup = new ol.layer.Group({

		'title': 'Base maps',

		layers: [

			//			 new ol.layer.Tile({
			//	         	  	title : 'Satellite',
			//	        	  	type : 'base',
			//	            	visible : false,
			//					tileSize: 512,	
			//	          		source: new ol.source.XYZ({
			//	          			attributions: attributions,	
			//	    		    	url: 'https://api.maptiler.com/tiles/satellite/{z}/{x}/{y}.jpg?key=SxEF6iVAIInskgqkJKps'
			//	    		    })
			//	    		 })			 
			//			 
			//			 ,new ol.layer.Tile({
			//	         	  	title : 'Streets',
			//	        	  	type : 'base',
			//	            	visible : false,
			//	          	    
			//					source: new ol.source.TileJSON({
			//	            		// attributions: attributions,	
			//	 					url: 'https://maps.gnosis.earth/ogcapi/collections/NaturalEarth:raster:HYP_HR_SR_OB_DR/map/tiles/WebMercatorQuad?f=tilejson',
			//		 				tileSize: 512,
			//		 				crossOrigin: 'anonymous'      		    
			//	 	     	})
			//	 		 }),

			new ol.layer.Tile({

				title: 'Satellite',
				type: 'base',
				visible: false,

				source: new ol.source.XYZ({
					attributions: attributions,
					url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
					//maxZoom: 19
				})
			}),

			new ol.layer.Tile({
				title: 'OSM',
				type: 'base',
				visible: true,
				source: new ol.source.OSM(),
				tileSize: 512,
				// wrapX : false
			})

		]
	});

	//
	// overlay group
	//

	var overlayGroup = new ol.layer.Group({
		title: 'Overlays',
		layers: []
	});

	//
	// results group with the bboxes and the markers
	//

	var resultsGroup = new ol.layer.Group({
		title: 'Results',
		layers: []
	});

	resultsGroup.getLayers().push(markersLayer);
	resultsGroup.getLayers().push(bboxesLayer);

	//
	// adds the layer groups
	//

	map.addLayer(baseMapsGroup);

	map.addLayer(resultsGroup);

	map.addLayer(overlayGroup);

	//
	//
	//

	var olLayerV = new ol.layer.Vector({
		projection: 'EPSG:4326',
		source: selectionVector
	});

	var interaction = new ol.interaction.DragBox({
		condition: ol.events.condition.platformModifierKeyOnly,
		style: new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: [0, 0, 255, 1]
			})
		})
	});

	/**
	 * 
	 */
	obj.select = function(_where) {

		where = _where;

		if (!obj.selectionVisible()) {
			obj.selectionVisible(true);
		}

		var extent = [where.west, where.south, where.east, where.north];

		var polygon = ol.geom.Polygon.fromExtent(extent);
		var feature = new ol.Feature({
			geometry: polygon
		});

		feature.setStyle(userSelectionFeatureStyle);

		selectionVector.clear();
		selectionVector.addFeature(feature);
	};

	/**
	 *  
	 */
	obj.where = function() {

		return where;
	};

	/**
	 * 
	 */
	obj.selectionVisible = function(set) {

		if (set != undefined && set != null) {

			selVisible = set;

			switch (set) {

				case true:

					map.addLayer(olLayerV);
					break;

				case false:

					map.removeLayer(olLayerV);
					break;
			}
		}

		return selVisible;
	};

	/**
	 * 
	 */
	obj.markerIcon = function(node, options) {

		var features = markersVector.getFeatures();

		features.forEach(function(feature, index) {

			if (feature.getProperties().name === node.uiId) {

				if (options.url) {

					//mousein
					flagSource = new ol.source.Vector({
						features: []
					});

					var iconFeature = new ol.Feature({
						geometry: feature.getGeometry(),
						name: feature.getProperties().name
					});

					flagSource.addFeature(iconFeature);

					feature.icon = options.url;
					var iconStyle = new ol.style.Style({

						image: new ol.style.Icon(({
							anchor: [0.5, 1],
							src: feature.icon
						}))
					});

					flagLayer = new ol.layer.Vector({
						source: flagSource,
						style: iconStyle
					});

					map.addLayer(flagLayer);

				} else {
					//mouseout
					flagSource.clear();
					map.removeLayer(flagLayer);
				}
			}
		});
	};

	/**
	 * 
	 */
	obj.addLayers = function(layers) {

		layers.forEach(function(layer) {

			/*var group = map.getLayerGroup().getLayers();
		 var isOverlay = false;
		 
		 group.forEach(function(l) {
			 var title = l.get('title');
			 if (title === 'Overlays') {
				 isOverlay = true;
			 }
		 });
		 
		 if (!isOverlay) {
			 map.addLayer(overlayGroup);
		 }*/

			var curLayers = overlayGroup.getLayers();
			var addLayer = true;

			curLayers.forEach(function(curLayer) {
				if (curLayer.get('name') === layer.get('name')) {
					addLayer = false;
					return;
				}
			});

			if (addLayer) {
				overlayGroup.getLayers().push(layer);
			}
		});
	};

	/**
	 * 
	 */
	obj.removeLayers = function(layers) {

		layers.forEach(function(layer) {

			var curLayers = overlayGroup.getLayers();
			var index = -1;

			curLayers.forEach(function(curLayer) {
				if (curLayer.get('name') === layer.get('name')) {
					index = curLayers.getArray().indexOf(curLayer);
					return;
				}
			});

			if (index > -1) {

				overlayGroup.getLayers().removeAt(index);
			}
		});
	};

	/**
	 * 
	 */
	obj.markers = function(options) {

		// removes all the markers from the map
		markersVector.clear();
		// map.removeLayer(markersLayer);

		while (options.page.hasNext()) {

			var node = options.page.next();
			var title = node.report().title;

			// set the current marker on the map
			if (node.report().where) {

				var where = node.report().where[0];

				var ext = convert(where);

				var lonlat = new ol.extent.getCenter(ext);

				var iconFeature = new ol.Feature({
					geometry: new ol.geom.Point([lonlat[0], lonlat[1]]),
					name: node.uiId
				});

				markersVector.addFeature(iconFeature);

				featuresArray[iconFeature.getProperties().name] = node;
			}
		}

		if (options.onMarkerClick) {

			map.on('pointermove', function(e) {

				var pixel = widget.map.getEventPixel(e.originalEvent);
				var hit = widget.map.hasFeatureAtPixel(pixel);
				widget.map.getTargetElement().style.cursor = hit ? 'pointer' : '';
			});

			// display detailed report on click
			map.on('click', function(evt) {

				var feature = widget.map.forEachFeatureAtPixel(evt.pixel, function(feature, layer) {
					return feature;
				});

				if (feature) {
					var n = featuresArray[feature.getProperties().name];
					options.onMarkerClick(n);
				}
			});
		}
	};

	/**
	 * 
	 */
	obj.bboxes = function(options) {

		// removes all the bboxes from the map
		bboxesVector.clear();
		// map.removeLayer(bboxesLayer);

		while (options.page.hasNext()) {

			var node = options.page.next();
			var title = node.report().title;

			if (node.report().where) {

				var where = node.report().where[0];

				var ext = convert(where);

				var polygon = ol.geom.Polygon.fromExtent(ext);
				var feature = new ol.Feature({
					geometry: polygon
				});

				feature.setStyle(bboxesFeatureStyle);

				bboxesVector.addFeature(feature);
			}
		}

	};

	var convert = function(where) {

		var minlatLon = ol.proj.transform([where.west, where.south], 'EPSG:4326', 'EPSG:3857');
		var maxlatLon = ol.proj.transform([where.east, where.north], 'EPSG:4326', 'EPSG:3857');

		return [minlatLon[0], minlatLon[1], maxlatLon[0], maxlatLon[1]];
	};

	/**
	 * 
	 */
	obj.fitBounds = function(where) {

		map.getView().fit([where.west, where.south, where.east, where.north], map.getSize());
	};

	/**
	 * 
	 */
	obj.inputControl = function(inControl) {

		interaction.on('boxend', function(evt) {

			var geom = evt.target.getGeometry();
			var ext = geom.getExtent();

			obj.select({ west: ext[0], south: ext[1], east: ext[2], north: ext[3] });

			var ext = geom.getExtent();
			inControl.updateWhereFields(ext);
		});

		map.addInteraction(interaction);
	};

	/**
	 * 
	 */
	obj.map = function() {

		return map;
	};

	return obj;
};