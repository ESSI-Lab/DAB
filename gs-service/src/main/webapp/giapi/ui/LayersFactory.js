/**
 * 
 */

import { GIAPI } from '../core/GIAPI.js';

GIAPI.LayersFactory = new function() {

	var factory = {};

	function resolveWmsLayerExtent(opt_options) {
		var bbox = (opt_options && opt_options.wmsLayerExtent) ||
			(typeof window !== 'undefined' && window.config && window.config.wmsLayerExtent);
		if (!bbox) {
			return null;
		}

		var south = Number(bbox.south);
		var west = Number(bbox.west);
		var north = Number(bbox.north);
		var east = Number(bbox.east);
		if (isNaN(south) || isNaN(west) || isNaN(north) || isNaN(east)) {
			return null;
		}

		return ol.proj.transformExtent([west, south, east, north], 'EPSG:4326', 'EPSG:3857');
	}

	function createTileGrid(extent) {
		var startResolution = ol.extent.getWidth(extent) / 256;
		var resolutions = new Array(22);
		for (var i = 0, ii = resolutions.length; i < ii; ++i) {
			resolutions[i] = startResolution / Math.pow(2, i);
		}

		return new ol.tilegrid.TileGrid({
			extent: extent,
			resolutions: resolutions,
			tileSize: [256, 256]
		});
	}

	/**
	 * 
	 */
	factory.layers = function(online, protocol, opt_options) {

		var out = [];

		var projExtent = ol.proj.get('EPSG:3857').getExtent();
		var limitedExtent = resolveWmsLayerExtent(opt_options);
		var tileGridExtent = limitedExtent || projExtent;
		var tileGrid = createTileGrid(tileGridExtent);

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];
				var prot = on.protocol;

				if (prot && prot.indexOf(protocol) != -1) {

					var name = on.name;
					var url = on.url;
					var title = on.title || on.description || on.name;
					var visible = true;
					if (opt_options && opt_options.visible !== undefined) {
						visible = opt_options.visible;
					}


					var getMapParams = {
						'LAYERS': name,
						'TRANSPARENT': 'true'

					};
					if (opt_options && opt_options.params) {
						for (key in opt_options.params) {
							getMapParams[key] = opt_options.params[key];
						}
					}

					var sourceOptions = {
						crossOrigin: 'anonymous',
						url: url,
						ratio: 1,
						params: getMapParams,
						tileGrid: tileGrid
					};
					if (limitedExtent) {
						sourceOptions.extent = limitedExtent;
					}
					if (opt_options && opt_options.attributions) {
						sourceOptions.attributions = opt_options.attributions;
					}

					var layerOptions = {
						visible: visible,
						name: name,
						title: title,
						source: new ol.source.TileWMS(sourceOptions)
					};
					if (limitedExtent) {
						layerOptions.extent = limitedExtent;
					}

					if (opt_options && opt_options.attributions) {
						out.push(new ol.layer.TileLayer(layerOptions));
					} else {
						out.push(new ol.layer.Tile(layerOptions));
					}
				}
			};
		}

		return out;
	};

	return factory;
}
