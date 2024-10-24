/**
 * @module UI
 **/

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
	 // map options
	 'mapType' : 'google',
	 'zoom': 6,
	 'scrollwheel' : true,
	 'panControl' : true,
	 'panControlOptions' : true,
	 'streetViewControl' : false,
	 'overviewMapControl' : true,
	 'mapTypeControl' : false,
	 'mapTypeControlOptions' : { style: google.maps.MapTypeControlStyle.DROPDOWN_MENU },
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
 * The images above show the widget with some <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">Google Maps layers</a> or <a href="../classes/GINode.html#method_ol3WMS_Layer" class="crosslink">Openlayers 3 layers</a>
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
 * @param {String} [options.mapType='google'] type of the map. Possible values are: "google" (or "gmaps") and "openlayers" (or "ol"). Default value: 'google'

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
 * @param {String} [options.wmsEndpoint]
 * @param {String} [options.wmsVersion='1.3.0']

 *
 */

GIAPI.ResultsMapWidget = function(id, latitude, longitude, options) {

    var widget = {};
    var selection;
    var noResultsOverlay;
    var ol3Map;
    var showRectangles = true;
    var rectanglesArray = [];
    var _inputControl;

    if (!options) {
        options = {};
    }
    
    if(!options.mapType){
    	
    	options.mapType = GIAPI.ui.mapType || 'google';
    }
    var mapType = options.mapType;

    if (!options.width) {
        options.width = '368px';
    }else if(!jQuery.isNumeric(options.width) && options.width.indexOf('%') === 0){
    	options.width += 'px';
    }

    if (!options.height) {
        options.height = '300px';
    }else if( !jQuery.isNumeric(options.height) && options.height.indexOf('%') === 0){
    	options.height += 'px';
    }
    
    if(options.showSelectionControl === undefined){
    	options.showSelectionControl = true;
    }
        
    if (options.showNoResultsMsg === undefined) {
        options.showNoResultsMsg = false;
    }
    if (!options.noResultsMsg) {
        options.noResultsMsg = "No geolocalized results to show";
    }
    
    if(options.showSpatialRelationControl === undefined || showSpatialRelationControl === true){
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
    if (options.mapTypeControl === undefined) {
        options.mapTypeControl = true;
    }
    if (!options.mapTypeControlOptions && mapType === 'google') {
        options.mapTypeControlOptions = {
            style : google.maps.MapTypeControlStyle.HORIZONTAL_BAR
        };
        options.mapTypeControlOptions.position = google.maps.ControlPosition.TOP_LEFT;
    }
    
    if (options.navigationControl === undefined) {
        options.navigationControl = false;
    }
    if (options.fullscreenControl === undefined) {
        options.fullscreenControl = true;
    }
    if (!options.fullscreenControlOptions && mapType === 'google') {
        options.fullscreenControlOptions = {
            position : google.maps.ControlPosition.RIGHT_BOTTOM
        };
    }
    if(!options.mapTypeId && mapType === 'google'){    	
    	options.mapTypeId = GIAPI.ui.mapTypeId || google.maps.MapTypeId.ROADMAP;
    }
    if (options.zoomControl === undefined) {
        options.zoomControl = true;
    }

    if (!options.zoomControlOptions && mapType === 'google') {
    	options.zoomControlOptions = {
    			position: google.maps.ControlPosition.RIGHT_BOTTOM
    	}
    }    
    
    if(options.showLocationControl === undefined || options.showLocationControl === null ){
    	options.showLocationControl = true;
	}
	
	if(!options.wmsVersion){
	  	options.wmsVersion = '1.3.0';
	}

    var divId = GIAPI.random();
    var div = '';
    switch(mapType){
    case 'ol':
    	div = '<div style="width: ' + options.width + '; height: ' + options.height + 'px" class="map-widget-div"><div id="' + divId + '"  class="ol-fullscreen-map"></div></div>';	
    	break;
    case 'google':
    	div = '<div style="width:' + options.width + '; " class="map-widget-div"><div id="' + divId + '" style="position: relative; width: ' + options.width + 'px; height: ' + options.height + 'px"></div></div>';    	
    	break;
    }
    
    jQuery('#' + id).append(div);

    options.longitude = longitude;
    options.latitude = latitude;
    options.divId = divId;
    options.dialogMode = false;

    switch(mapType){
    case 'ol':
    	           
        // creates the ol3 map
        ol3Map = GIAPI.OL3_Map(options);
 
        /**
         * The <a target=_blank href="http://openlayers.org/en/v3.15.1/apidoc/">Openlayers 3 API</a> instance
         *
         * @property {<a target=_blank href="http://openlayers.org/en/v3.15.1/apidoc/">Openlayers 3 API</a>} olmap
         */
        widget.map = ol3Map.map();
        
   	    options.widgetMap = widget.map;
        options.ol3Map = ol3Map;
        
        if(options.showSelectionControl){        
	        // creates the input control
	        _inputControl = GIAPI._whereInputControl(widget,options);
	        _inputControl.updateWhereFields();
	        
	           // add the input control to the map
	           _inputControl.add(widget.map);
        }
                       
        break;
        
    case 'google':
        /**
         * The <a target=_blank href="https://hpneo.github.io/gmaps/">gmaps.js</a> instance
         *
         * @property {<a target=_blank href="https://hpneo.github.io/gmaps/">gmaps.js</a>} gmap
         */
        widget.map = new GMaps({
            div : '#' + divId,
            lat : latitude,
            lng : longitude,
            minZoom: 2,

            fullscreenControl : options.fullscreenControl,
            fullscreenControlOptions : options.fullscreenControlOptions,

            zoomControl : options.zoomControl,
            zoomControlOptions : options.zoomControlOptions,

            scrollwheel : options.scrollwheel,
            panControl : options.panControl,
            panControlOptions : options.panControlOptions,
            streetViewControl : options.streetViewControl,
            overviewMapControl : options.overviewMapControl,
            mapTypeId : options.mapTypeId,
            mapTypeControl : options.mapTypeControl,
            mapTypeControlOptions : options.mapTypeControlOptions,

            navigationControl : options.navigationControl,
            //      navigationControlOptions: { style: google.maps.NavigationControlStyle.ZOOM_PAN },
        });

        widget.map.setZoom(options.zoom);
        
//        options.value = {'south': -30, 'west': -30, 'north': 30, 'east': 30 };
          
        selection = new google.maps.Rectangle({

            strokeColor : '#0000FF',
            strokeOpacity : 1,
            strokeWeight : 3,
            fillColor : '#0000FF',
            fillOpacity : 0.3,

            bounds : options.value,
            editable : true,
            draggable : true
        });

        selection.setMap(widget.map.map);
        
        selection.setVisible(options.showSelectionControl);
        
        options.controlPosition = google.maps.ControlPosition.LEFT_TOP;
        
        options.widgetMap = widget.map;
        options.selection = selection;
        
        if(options.showSelectionControl){   
        
	        _inputControl = GIAPI._whereInputControl(widget,options);
	        _inputControl.updateWhereFields();
	        
	        google.maps.event.addListener(selection, 'bounds_changed', function() {
	        	
	        	_inputControl.updateWhereFields();
	        });        
	        
	           // add the input control to the map
	           _inputControl.add(widget.map.map);
        }
        break;
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
        arrayFeatures = new Object();
        
        switch(mapType){
	    case 'ol':
	    	
	    	if (options.showNoResultsMsg && count === 0) {
	             jQuery('#no-results-div').css('display', 'inline');
	        }else{
	             jQuery('#no-results-div').css('display', 'none');
	        }
	    
	    	options.page = page;
	    	
	    	// set the map markers
	    	ol3Map.markers(options);
	    	
	     	// resets the page in order to make it reusable
	    	page.reset();

			// set the bboxes
	        ol3Map.bboxes(options);
	    	
	    	// resets the page in order to make it reusable
	    	page.reset();
	    	
            while (page.hasNext()) {

                var node = page.next();
                var title = node.report().title;

                // adds the layers
                var layers = node.ol3WMS_Layer({
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
           
            break;
            
	    case 'google':
	    	
            //remove markers and rectangles
            widget.map.removeMarkers();
            
            for(var i = 0; i < rectanglesArray.length;i++){
            	rectanglesArray[i].setMap(null);
            }
            rectanglesArray = [];

            var count = 0;

            while (page.hasNext()) {

                var node = page.next();
                var title = node.report().title;

                // adds the layers
                
     	    	widget.map.map.overlayMapTypes.clear();

                updateLayersTable();
                
                var layers = node.googleImageMapType(widget.map.map);
                if (layers.length > 0 && options.addLayers) {
                    addLayers(layers);
                }

                // set the current marker on the map
                if (node.report().where) {

                    var where = node.report().where[0];

                    var bounds = new google.maps.LatLngBounds(
                    		new google.maps.LatLng(where.south, where.west), 
                    		new google.maps.LatLng(where.north, where.east));

                    var center = bounds.getCenter();

                    var opt = {
                        lat : center.lat(),
                        lng : center.lng(),
                        title : options.markerTitle(node),
                        uiId: node.uiId
                    };

                    if (options.markerURL) {
                        opt.icon = markerURL;

                    } else {
                    	opt.icon = GIAPI.UI_Utils.markerIcon(options.markerColor);
                    }
                    
                    var clickable = false;
                    if (options.onMarkerClick) {
                    	clickable = true;
                        opt.click = ((function(n) {
                                return function() {
                                    options.onMarkerClick(n);
                                };

                            })(node));
                    } 
                    
                    if(options.onMarkerMouseOver){
                    	clickable = true;
                    	opt.mouseover = ((function(n) {
                            return function() {
                                options.onMarkerMouseOver(n);
                            };

                        })(node));
                    }
                    
                    if(options.onMarkerMouseOut){
                    	clickable = true;
                    	opt.mouseout = ((function(n) {
                            return function() {
                                options.onMarkerMouseOut(n);
                            };

                        })(node));
                    }
                    
                    opt.clickable = clickable;

                    widget.map.addMarker(opt);
                                      
                    var rect = new google.maps.Rectangle({
                        strokeColor : '#ffff00',
                        strokeOpacity : 1,
                        strokeWeight : 1,
                        fillColor : '#ffff00',
                        fillOpacity : 0.05,

                        bounds : bounds,
                        editable : false,
                        draggable : false,
                        clickable: false
                    });

                    rect.setMap(widget.map.map);                   
                    rect.setVisible(showRectangles);
                    
                    rectanglesArray.push(rect);

                    count++;
                }
            }

            if (options.showNoResultsMsg && count === 0) {
                jQuery('#no-results-div').css('display', 'inline');
            } else {
                jQuery('#no-results-div').css('display', 'none');
            }

            // resets the page in order to make it reusable
            page.reset();
            
            break;
        }
    };
    
   /**
    *
    * @method selection
    * @return {BBox}
    */
   widget.where = function() {
	      	   	
   	   switch(mapType){
       case 'ol':   	 
    	 if(!ol3Map.selectionVisible()){
       		 return null;
       	 } 
         break;
       case 'google':  	 
    	 if(!selection.getVisible()){
    		 return null;
    	 }
    	 break;
       }
   	 
   	   return _inputControl.where(true);
    };

    
   /**
    *
    * @method spatialRelation
    */
    widget.spatialRelation = function(){
   	     
   	     if(!_inputControl){
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
        var layers;
        switch(mapType){
	    case 'ol':
	    	layers = node.ol3WMS_Layer({});
	    	break;
	    case 'google':
	    	layers = node.googleImageMapType(widget.map.map);
	    	break;
        }

        if (layers.length > 0) {
            
	   	     var button  = GIAPI.FontAwesomeButton({   
   			    'width': 24,
   			    'label':'',
   		        'icon':'fa-map-o',
   		        'handler': function(){
   		        	 addLayers(layers);	
   		             return false;
   		        },
   				'attr':[{ name:'title',value:'Add layers'  }]	        
	   	    });
	   	    button.css('div','padding','4px');
	   	    button.css('div','background',' #2196f3');
	   	    button.css('icon','font-size','12.5px');
	   	    
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
    widget.markerIcon = function(node,options){
    	 
    	 switch(mapType){
 	     case 'ol':

    		 ol3Map.markerIcon(node, options);    		 
    		 break;
    		 
 	     case 'google':
        	 var markers = widget.map.markers;    	
        	 markers.forEach(function(mark,index){
        		 if(mark.uiId === node. uiId){
        			
        			 mark.icon = options.color ? GIAPI.UI_Utils.markerIcon(options.color) : options.url;
        			 
        			 markers.splice(index, 1);
        	    	 widget.map.addMarker(mark);
        	    	 
        	    	 return;
        		 }
        	 });    
        	 
        	 break;
    	 }   	
   };
   
   /**
    * Applies the given bounding box to the user selection
    */
   widget.select = function(where) {
   	
	   	switch(mapType){
	   	case 'ol':
	   		
		   		if(!where){
		   			ol3Map.selectionVisible(false);  	
		   		}else{
		   			ol3Map.select(where);  	
				}
		   		
		   		break;
	   		
	   	case 'google':
	   		
		   	   if (!where) {
		            selection.setVisible(false);            	 
	                return;
	           }
	         
	           selection.setBounds(where);
	           selection.setVisible(true);
	           selection.setDraggable(false);
	           selection.setEditable(false);
	
	           widget.map.fitBounds(where);
	   	}
	   	
	   	if(options.showSelectionControl){
	   		_inputControl.updateWhereFields();
	   	}
   };

   /**
    * Returns the map type
    *
    * @method mapType
    * 
    * @return the map type
    */
   widget.mapType = function() {
       
	   return mapType;
   };
   
   /**
	* 
    */
   widget.addLayers = function(layers) {
	
	   addLayers(layers);
   };
    
   var addLayers = function(layers) {
    	
    	 switch(mapType){
 	     case 'ol':
          
        	ol3Map.addLayers(layers);
        	
        	break;

 	     case 'google':
 	    	 
 	    	widget.map.map.overlayMapTypes.clear();

            layers.forEach(function(layer) {
            	
            	// all layers are hidden
            	layer.setOpacity(0);

            	widget.map.map.overlayMapTypes.push(layer);
            });

            updateLayersTable();
            
            break;
        }
    };  

    var createNoResultsOverlay = function() {

        var div = document.createElement('div');
        div.id = 'no-results-div';
       
        jQuery(div).attr('class', 'results-map-widget-no-results-div');
        div.innerHTML = '<label style="font-weight: bold;font-style: italic">' + options.noResultsMsg + '</label>';
        
        switch(mapType){
        case 'ol':
        	
        	 var popup = new ol.Overlay({
                 element : document.getElementById('no-results-div')
             });

             widget.map.addOverlay(popup);
             
             popup.setPosition([longitude, latitude]);
             popup.setPositioning('top-right');
        	
        	 break;
        case 'google':
            
        	 widget.map.map.controls[google.maps.ControlPosition.CENTER].push(div);

        	 break;
        }
    };

    var createLayersControl = function() {

       switch(mapType){
       case 'ol':
    	   
            var layerSwitcher = new ol.control.LayerSwitcher({
                tipLabel : 'Legend' // Optional label for button
            });
            
            widget.map.addControl(layerSwitcher);
            
            break;
      
       case 'google':

            var controlDiv = document.createElement('div');
            controlDiv.style.padding = '5px';

            // control button
            var controlButton = document.createElement('div');
            var controlButtonId = 'control-button';
            controlButton.id = controlButtonId;

            jQuery(controlButton).addClass('layers-control-button');
            jQuery(controlButton).attr('title', 'Layers control');
            jQuery(controlButton).attr('pressed', false);

            jQuery(controlDiv).append(controlButton);

            // control table div (used to get the scrollbar)
            var controlTableDiv = document.createElement('div');
            jQuery(controlTableDiv).addClass('layers-control-table-div');

            jQuery(controlDiv).append(controlTableDiv);

            // control table
            var controlTable = document.createElement('table');
            jQuery(controlTable).addClass('layers-control-table');

            var controlTableId = 'layers-table';
            controlTable.id = controlTableId;

            jQuery(controlTableDiv).append(controlTable);
            jQuery(controlTableDiv).css('visibility', 'hidden');

            jQuery(controlTable).append(userSelectionControl());
            jQuery(controlTable).append(showRectanglesControl());

            // put the control on the top-right position
            widget.map.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(controlDiv);

            // control button listener
            jQuery(controlButton).click(function() {

                if (jQuery(this).attr('pressed') === 'true') {
                    jQuery(controlTableDiv).css('visibility', 'hidden');

                    jQuery(controlButton).removeClass('layers-control-button-pressed');
                    jQuery(controlButton).attr('pressed', false);
                } else {
                    jQuery(controlTableDiv).css('visibility', 'visible');

                    jQuery(controlButton).addClass('layers-control-button-pressed');
                    jQuery(controlButton).attr('pressed', true);
                }
            });
            
            break;
        }
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
    	
    	var checked = showRectangles ? ' checked':'';
        var content = '<tr>';
        content += '<td><input class="layers-control-check" type="checkbox" id="rectangles" '+checked+'/></td>';
        content += '<td>Results area</td>';
        content += '</tr>';

        jQuery(document).on('click', '#rectangles', function() {
        	 
        	 showRectangles = this.checked;
        	 
        	 for(var i = 0; i < rectanglesArray.length;i++){
             	  rectanglesArray[i].setVisible(this.checked);
             }      
        });

        return content;
    };

    var updateLayersTable = function() {
    	
    	var normalizeLayerName = function(name){
    		
    		return name.replace(/:/g,'','_');
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
              
                var checked = visible ? ' checked':'';
                layer.setOpacity(checked ? 1 : 0);
                visible = false;
                
                content += '<tr>';
                content += '<td><input class="layers-control-check" type="checkbox" id="layer-check_' + name + '"'+checked+'/></td>';
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

    if(options.showNoResultsMsg){
    	createNoResultsOverlay();
    }

    return widget;
};
