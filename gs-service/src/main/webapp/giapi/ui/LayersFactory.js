/**
 * 
 */

import { GIAPI } from '../core/GIAPI.js';

GIAPI.LayersFactory = new function() {

	var factory = {};

	/**
	 * 
	 */
	factory.layers = function(online, protocol, opt_options) {

		var out = [];

		var projExtent = ol.proj.get('EPSG:3857').getExtent();
		var startResolution = ol.extent.getWidth(projExtent) / 256;

		var resolutions = new Array(22);
		for (var i = 0, ii = resolutions.length; i < ii; ++i) {
			resolutions[i] = startResolution / Math.pow(2, i);
		}

		var tileGrid = new ol.tilegrid.TileGrid({
			// minZoom: 6,
			extent: projExtent,
			resolutions: resolutions,
			tileSize: [256, 256]
		});

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];
				var prot = on.protocol;

				if (prot && prot.indexOf(protocol) != -1) {

					var name = on.name;
					var url = on.url;
					var title = on.title || on.description || on.name;
					var visible = true;
					if (opt_options.visible !== undefined) {
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

					if (opt_options && opt_options.attributions) {

						out.push(new ol.layer.TileLayer({
							name: name,
							title: title,
							visible: visible,

							source: new ol.source.TileWMS({
								crossOrigin: 'anonymous',
								url: url,
								ratio: 1,
								params: getMapParams,
								attributions: opt_options.attributions
							})
						})); //OpenLayers.Layer.WMS(name, url, getMapParams, initOptions.options));
					} else {

						var source = new ol.source.TileWMS({
							crossOrigin: 'anonymous',
							url: url,
							ratio: 1,
							params: getMapParams
						});

						var layer = new ol.layer.Tile({
							visible: visible,
							name: name,
							title: title,
							source: source
						});

						out.push(layer);
					}
				}
			};
		}

		return out;
	};

	return factory;
}
