/**
 * @module UI
 **/

/**
 * This widget localizes a <a href="../classes/GINode.html" class="crosslink">node</a> in a small map
 * and it can also show, when available, the <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">Google Maps layers</a> or the <a href="../classes/GINode.html#method_ol3WMS_Layer" class="crosslink">Openlayers 3 layers</a>
 * associated to the <a href="../classes/GINode.html" class="crosslink">node</a>. See <a href="#mode" class="crosslink"><code>mode</code></a> option for more info.<br><br>
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
 *
 *<table>
 *               <tbody>
 *                   <tr>
 *                       <td class="search-div" style="width:50%">                           
 *                           <center>
 *                               <h3 class="h3-color">Google Maps</h3>
 *                           </center>
 *                           <center>
 *                               <img style="border: none;" src="../assets/img/node-map-widget.png" />
 *                           </center>   
 *                       </td>                                                   
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Openlayers 3</h3>
 *                           </center>
 *                           <center>
 *                              <img style="border: none;" src="../assets/img/ol-node-map-widget.png" />       
 *                           </center>
 *                       </td>
 *                    </tr>
 *                </tbody>
 *</table>  
 *
 * The images above show the widget of a <a href="../classes/GINode.html" class="crosslink">GINode</a> localized in a rectangular area
 *  (see <code>options.areaColor</code> option).
 * When the mouse pointer is on the map, a dialog with the coordinates is shown (see <code>options.coordinatesDialog</code> options).
 *
 * <table>
 *               <tbody>
 *                   <tr>
 *                       <td class="search-div" style="width:50%">                           
 *                           <center>
 *                               <h3 class="h3-color">Google Maps</h3>
 *                           </center>
 *                           <center>
 *                               <img style="border: none;" src="../assets/img/node-map-widget-2.png" />
 *                           </center>   
 *                       </td>                                                   
 *                       <td class="search-div" style="width:50%">
 *                           <center>
 *                               <h3 class="h3-color">Openlayers 3</h3>
 *                           </center>
 *                           <center>
 *                              <img style="border: none;" src="../assets/img/ol-node-map-widget-2.png" />        
 *                           </center>
 *                       </td>
 *                    </tr>
 *                </tbody>
 *</table>    
 * 
 * 
 * The images above show the widget of a <a href="../classes/GINode.html" class="crosslink">GINode</a> localized in a point (see <code>options.marker</code> options).<br>
 * For additional personalization of the widget, see the <code>map-widget</code> class of the <i>giapi.css</i> file
 *
 *
 * @class NodeMapWidget
 * @constructor
 *
 * @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the widget is inserted
 * @param {GINode} node
 * @param {Object} [options]
 *
 * @param {String} [options.mapType='google'] <a name="mapType">type of the map</a>. Possible values are: "google" (or "gmaps") and "openlayers" (or "ol"). Default value: 'google'
 * 
 * @param {Object} [options.zoom]
 *
 * @param {String} [options.mode="a"] <a name="mode">possible values</a>:<ul>
 <li>"a": shows only the localization area/marker</li><li>"l": shows only the node layer/s (if any)</li><li>"al": shows both the localization area/marker and the node layer/s (if any)</li></ul>
 * <br>See also {{#crossLink "GINode/googleImageMapType:method"}}{{/crossLink}} method for Google Maps or {{#crossLink "GINode/ol3WMS_Layer:method"}}{{/crossLink}} method for Openlayers 3
 * @param {Integer} [options.width=]
 * @param {Integer} [options.height=180]
 *
 * @param {String} [options.areaColor="#0000FF"]
 *
 * @param {String} [options.markerColor="red"] possible values: "red","yellow","green","red"
 * @param {String} [options.markerIcon=""] URL of an image to use as marker icon (overrides the <code>options.markerColor</code> option)
 *
 * @param {Boolean} [options.coordinatesDialog=true]
 * @param {String} [options.coordinatesDialogPosition="left"] possible values: "left","right","top","bottom"
 *
 */
GIAPI.NodeMapWidget = function(id, node, options) {

    var widget = {};
    /**
     * The <a target=_blank href="https://hpneo.github.io/gmaps/">gmaps.js</a> instance or <a target=_blank href="http://openlayers.org/en/v3.15.1/apidoc/">Openlayers 3 API</a> instance
     *
     * @property {<a target=_blank href="https://hpneo.github.io/gmaps/">gmaps.js</a>/<a target=_blank href="http://openlayers.org/en/v3.15.1/apidoc/">Openlayers 3 API</a>} gmap
     */
    widget.map;

    if (!options) {
        options = {};
    }

    if (!options.mode) {
        options.mode = GIAPI.Common_UINode.mapMode || 'a';
    }

    if (!options.height) {
        options.height = 180;
    }

    if (!options.coordinatesDialog) {
        options.coordinatesDialog = true;
    }

    if (!options.coordinatesDialogPosition) {
        options.coordinatesDialogPosition = 'left';
    }

    if (!options.areaColor) {
        options.areaColor = '#0000FF';
    }

    if (!options.markerColor) {
        options.markerColor = 'red';
    }
    
    if(!options.mapType){
    	options.mapType = GIAPI.Common_UINode.mapType || GIAPI.ui.mapType ||'google';
    }
    
    if(!options.mapTypeId){    	
    	options.mapTypeId = GIAPI.Common_UINode.mapTypeId || GIAPI.ui.mapTypeId || google.maps.MapTypeId.ROADMAP;
    }

    if (jQuery('#coordinatesDialog').length === 0) {

        jQuery('head').append('<div id="coordinatesDialog"/>');
    }

    /**
     * Initializes the widget
     *
     * @method init
     */
    widget.init = function() {

        if (node.report().where) {

            var divId = GIAPI.random();
            var div = '<div class="map-widget-div">';
            var w = options.width ? ' width: ' + options.width + 'px;' : '';
            div += '<div id="' + divId + '" style="position: relative; ' + w + ' height: ' + options.height + 'px"></div>';
            div += '</div>';
            jQuery('#' + id).append(div);

            createNodeMap(node, divId, options);

            // adds the layers
            if (options.mapType.toLowerCase() === 'ol' || options.mapType.toLowerCase().indexOf('openlayer') != -1) {
                
                var layers = node.ol3WMS_Layer({
                    // options : {
                    // isBaseLayer : false,
                    // rendererOptions : {
                    // zIndexing : true
                    // }
                    //}
                });
                if (layers && (options.mode === 'l' || options.mode === 'al')) {
                    layers.forEach(function(layer) {
                        widget.map.addLayer(layer);
                    });
                }
            } else {
                var layers = node.googleImageMapType(widget.map.map);
                if (layers && (options.mode === 'l' || options.mode === 'al')) {
                    layers.forEach(function(layer) {
                        widget.map.map.overlayMapTypes.push(layer);
                    });
                }
            }

        }
    };

    var createNodeMap = function(node, id, options) {

        var where = node.report().where[0];

        var info = '<div class="node-map-widget-coordinates-div">';
        info += '<table>';
        info += '<tr><td>South:</td><td>' + where.south.toFixed(3) + '</td></tr>';
        info += '<tr><td>West:</td><td>' + where.west.toFixed(3) + '</td></tr>';
        info += '<tr><td>North:</td><td>' + where.north.toFixed(3) + '</td></tr>';
        info += '<tr><td>East:</td><td>' + where.east.toFixed(3) + '</td></tr>';
        info += '</table>';
        info += '</div>';

        if (options.mapType.toLowerCase() === 'ol' || options.mapType.toLowerCase().indexOf('openlayer') != -1) {
           
        	var controls = [new ol.control.Attribution(), new ol.control.MousePosition({
                undefinedHTML : 'outside',
                projection : 'EPSG:4326',
                coordinateFormat : function(coordinate) {
                    return ol.coordinate.format(coordinate, '{x}, {y}', 4);
                }
            }), new ol.control.OverviewMap({
                collapsed : false
            }), new ol.control.Rotate({
                autoHide : true
            }), new ol.control.ScaleLine(), new ol.control.Zoom(), new ol.control.ZoomSlider(), new ol.control.ZoomToExtent(), new ol.control.FullScreen()];

            widget.map = new ol.Map({
                target : id,
                layers : [new ol.layer.Tile({
                    source : new ol.source.OSM(),
                    wrapX : false
                })],
                interactions : ol.interaction.defaults({
                    mouseWheelZoom : false
                }),
                //controls : controls,
                view : new ol.View({
                    projection : 'EPSG:4326',
                })
            });

            if (options.zoom) {
                widget.map.getView().setZoom(options.zoom);
            }
          
            var extent = [where.west, where.south, where.east, where.north];

            if (where.south === where.north && where.west === where.east) {

                var iconFeature = new ol.Feature({
                    geometry : new ol.geom.Point([where.west, where.south]),
                });

                var vectorSource = new ol.source.Vector({
                    features : [iconFeature]
                });

                var marker = function() {
                    if (options.markerURL) {
                        return options.markerURL;
                    } else {
                        switch(options.markerColor) {
                        case 'red':
                            return 'https://api.geodab.eu/docs/assets/img/red-marker.png';
                            break;
                        case 'blue':
                            return 'https://api.geodab.eu/docs/assets/img/blue-marker.png';
                            break;
                        case 'green':
                            return 'https://api.geodab.eu/docs/assets/img/green-marker.png';
                            break;
                        case 'yellow':
                            return 'https://api.geodab.eu/docs/assets/img/yellow-marker.png';
                            break;
                        }
                    }
                };

                var iconStyle = new ol.style.Style({
                    image : new ol.style.Icon(( {
                        anchor : [0.5, 1],
                        src : marker()
                    }))
                });

                var vectorLayer = new ol.layer.Vector({
                    source : vectorSource,
                    style : iconStyle
                });

                widget.map.getView().fit(extent, widget.map.getSize());

                if (options.zoom) {
                    widget.map.getView().setZoom(options.zoom);
                }
                widget.map.addLayer(vectorLayer);

                return;
            }

            widget.map.getView().fit(extent, widget.map.getSize());

            if (where.west > -180 && where.east < 180 && where.south > -90 && where.north < 90) {

                var polygon = ol.geom.Polygon.fromExtent(extent);
                var feature = new ol.Feature({
                    geometry : polygon
                });

                var vectorSrc = new ol.source.Vector();
                var olLayerV = new ol.layer.Vector({
                    projection : 'EPSG:4326',
                    source : vectorSrc
                });

                var hexColor = options.areaColor;
                var color = ol.color.asArray(hexColor);
                color = color.slice();
                color[3] = 0.2;
                feature.setStyle(new ol.style.Style({
                    fill : new ol.style.Fill({
                        color : color,
                        weight : 10
                    }),
                    stroke : new ol.style.Stroke({
                        color : hexColor,
                        width : 1.5
                    }),

                }));

                vectorSrc.addFeature(feature);
                widget.map.addLayer(olLayerV);

            }
        } else {

            var bounds = new google.maps.LatLngBounds(new google.maps.LatLng(where.south, where.west), new google.maps.LatLng(where.north, where.east));

            var center = bounds.getCenter();

            widget.map = new GMaps({
                div : '#' + id,
                lat : center.lat(),
                lng : center.lng(),
                panControl : false,
                panControlOptions : false,
                streetViewControl : false,
                overviewMapControl : false,
                mapTypeControl : false,
                scrollwheel : false,
                mapTypeId : options.mapTypeId,
 
            });

            if (options.zoom) {
                widget.map.setZoom(options.zoom);
            }
           
            if (where.south === where.north && where.west === where.east) {

                var opt = {
                    lat : center.lat(),
                    lng : center.lng(),
                    title : node.report().title,
                    clickable : false
                };

                if (options.markerURL) {
                    opt.icon = markerURL;
                } else {
                    switch(options.markerColor) {
                    case 'red':
                        opt.icon = 'https://api.geodab.eu/docs/assets/img/red-marker.png';
                        break;
                    case 'blue':
                        opt.icon = 'https://api.geodab.eu/docs/assets/img/blue-marker.png';
                        break;
                    case 'green':
                        opt.icon = 'https://api.geodab.eu/docs/assets/img/green-marker.png';
                        break;
                    case 'yellow':
                        opt.icon = 'https://api.geodab.eu/docs/assets/img/yellow-marker.png';
                        break;
                    }
                }

                widget.map.addMarker(opt);

                return;
            }

            widget.map.fitBounds(bounds);

            var path = [[where.south, where.west], [where.north, where.west], [where.north, where.east], [where.south, where.east, where.south, where.west]];

            if (where.west > -180 && where.east < 180 && where.south > -90 && where.north < 90) {

                widget.map.drawPolygon({
                    paths : path,
                    strokeColor : options.areaColor,
                    strokeOpacity : 1,
                    strokeWeight : 1,
                    fillColor : options.areaColor,
                    fillOpacity : 0.3
                });
            }
        }
        
        jQuery('#' + id).mouseover(function() {

            if (options.coordinatesDialog) {

                openCoordinatesDialog(info, id);
            }
        });

        jQuery('#' + id).mouseout(function() {

            jQuery('#coordinatesDialog').dialog('close');
        });

        if (options.mode === 'l') {
            return;
        }
    };

    var openCoordinatesDialog = function(info, divId) {

        var pos = GIAPI.position(options.coordinatesDialogPosition);

        jQuery('#coordinatesDialog').empty();
        jQuery('#coordinatesDialog').append(info);
        jQuery('#coordinatesDialog').dialog({
            dialogClass : 'no-titlebar',
            height : 120,
            width : 60,
            modal : false,
            position : {
                of : '#' + divId,
                my : pos.my,
                at : pos.at,
                collision : 'none'
            }
        });

        jQuery('#coordinatesDialog').css('background', 'transparent');
        jQuery('#coordinatesDialog').css('padding','0px');
        jQuery('[aria-describedby="coordinatesDialog"]').css('background', 'none');
        jQuery('[aria-describedby="coordinatesDialog"]').css('border', 'none');
        
    };

    return widget;
};
