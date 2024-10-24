/**
 * 
 */
GIAPI.LayersFactory = new function() {
	
	var factory = {};
	
	/**
	 * 
	 */
	factory.googleImageMapType = function(map, online, initOptions) {
    	
		var out = [];
		
		if(!initOptions){
			initOptions = {};
		}
		
		if(!initOptions.tileSize){
			initOptions.tileSize = new google.maps.Size(256, 256);
		}

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];
				var protocol = on.protocol;
				var url = on.url;
				if(url){
					url = on.url.endsWith('?') ? url : url+ '?';
				}

				if (protocol && url
						&&  protocol.toLowerCase().indexOf('axe') === -1
						&&  url.toLowerCase().indexOf('axe') === -1
						&&  protocol.toLowerCase().indexOf('urn:ogc:servicetype:webmapservice:') != -1) {
					
					var name = on.name;
					// ignoring nasa svs!
					if(url.indexOf('http://svs.gsfc.nasa.gov/') == 0){
						return [];
					}
					
					var version = protocol.toLowerCase().replace('urn:ogc:servicetype:webmapservice:','').replace(':http','');
					if(version !== '1.3.0' && version !== '1.1.1'){
						version = '1.3.0';
					}
				    var code = version === '1.3.0' ? 'CRS':'SRS';
					
				    out.push(new google.maps.ImageMapType({
						
				    	 layerName: on.name, 
				    	
			             getTileUrl: function (coord, zoom) {

				             	var proj = map.getProjection();
				                var zfactor = Math.pow(2, zoom);
				                
				                coord = getNormalizedCoord(coord, zoom);
				                if (!coord) {
				                    return null;
				                }
				                
				                var bbox;
				                var crs;
				                
				                if(true){
				                	// at the moment only requests in mercator are performed since the mapping 
				                	// in 4326 stretches the layers with low zoom levels 
				                	crs = 'EPSG:3857';
				                	
			                	    var minX = -20026376.39;
					                var minY = -20048966.10;
					                var maxX = 20026376.39;
					                var maxY = 20048966.10;
					                
					                var yExtent = maxY - minY;
					                var xExtent = maxX - minX;

					            	var tileSpanX = xExtent / zfactor;
					            	var tileSpanY = yExtent / zfactor;

					            	var minx = minX + coord.x * tileSpanX;
					            	var miny = maxY - (coord.y + 1) * tileSpanY;
					            	var maxy = maxY - coord.y * tileSpanY;
					            	var maxx = minX + (coord.x  + 1) * tileSpanX;
					                				                
					                // coord represents the top left hand (NW)
					                var upperLeftPoint = new google.maps.Point(minx,maxy);
					                var lowerRightPoint = new google.maps.Point(maxx,miny);
				                 							
								    // creates the Bounding box string
								    bbox = minx + ',' + miny + ',' +  maxx + ',' + maxy;
		                		    
				                }else{
				                	
				                	crs = 'EPSG:4326';
				                
					                // coord represents the top left hand (NW)
					                var upperLeftPoint = new google.maps.Point(coord.x*256 / zfactor,(coord.y+1)*256 / zfactor);
					                var lowerRightPoint = new google.maps.Point((coord.x+1)*256 / zfactor,coord.y*256 / zfactor);
				                 
								    var upperLeft = proj.fromPointToLatLng(upperLeftPoint);
								    var lowerRight = proj.fromPointToLatLng(lowerRightPoint);
								    
								    //corrections for the slight shift of the SLP (mapserver)
								    var deltaX = 0.0013;
								    var deltaY = 0.00058;
								    
								    var minY = Math.min(upperLeft.lat(), lowerRight.lat());
								    var minX = Math.min(upperLeft.lng(), lowerRight.lng());
								    var maxY = Math.max(upperLeft.lat(), lowerRight.lat());
								    var maxX = Math.max(upperLeft.lng(), lowerRight.lng());							    
								
								    // creates the Bounding box string
								    var bbox_1 = (minX + deltaX) + "," +
								    	       (minY + deltaY) + "," +
								    	       (maxX + deltaX) + "," +
								    	       (maxY + deltaY);
								    
								    var bbox_3 = (minY + deltaY) + "," +
							    			   (minX + deltaX) + "," +
							    	           (maxY + deltaY) + "," +
				                               (maxX  + deltaX);
								    
								    bbox = version === '1.3.0' ? bbox_3:bbox_1;
							    }
							    
							    var wms = this._url;
							    // some url containts a ? followed by a part od query string; in that case the ?
							    // at the end MUST not be added (e.g: http://wms.pcn.minambiente.it/ogc?map=/ms_ogc/WMS_v1.3/Vettoriali/Bacini_idrografici.map&)
							    if(!wms.endsWith('?') && wms.indexOf('?') === -1 ){
							    	wms += '?';
							    }
							    	
							    wms += "REQUEST=GetMap";  
							    wms += "&SERVICE=WMS";     
							    wms += "&VERSION="+ version;   
							    wms += "&LAYERS=" + this.layerName;  
							    wms += "&FORMAT=image/png" ;  
							    wms += "&BGCOLOR=0xFFFFFF";  
							    wms += "&TRANSPARENT=TRUE";
							    wms += "&"+this._code+"="+crs;     
							    wms += "&BBOX=" + bbox;       
							    wms += "&WIDTH=256";         
							    wms += "&HEIGHT=256";
							    wms += "&STYLES=";  
							    
							    return wms;                 		    
						    },
						    _url: on.url,
						    _code: code,
						    name: name,
						    tileSize: initOptions.tileSize,
						    isPng: true
			    	}));										
				}
			};
		}
    	
		return out;
    };
    
     //-------------------------------------------------------------------------
    // Normalizes the coords that tiles repeat across the x axis (horizontally)
    // like the standard Google map tiles
    //------------------------------------------------------------------------
    var getNormalizedCoord = function(coord, zoom) {
    	
	      var y = coord.y;
	      var x = coord.x;
	
	      // tile range in one direction range is dependent on zoom level
	      // 0 = 1 tile, 1 = 2 tiles, 2 = 4 tiles, 3 = 8 tiles, etc
	      var tileRange = 1 << zoom;
	
	      // don't repeat across y-axis (vertically)
	      if (y < 0 || y >= tileRange) {
	        return null;
	      }
	
	      // repeat across x-axis
	      if (x < 0 || x >= tileRange) {
	        x = (x % tileRange + tileRange) % tileRange;
	      }
	
	      return {x: x, y: y};
    };
		
	/**
	 * 
	 */
	factory.ol_Layer = function(online, protocol, initOptions) {
  	  
		var out = [];

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];
				var prot = on.protocol;
				
				if (prot && prot.indexOf(protocol) != -1) {
					

					var name = on.name;
					var url = on.url;

					var getMapParams = {
						layers : name,
						transparent :"TRUE"
						
					};
					if (initOptions && initOptions.params) {
						for (key in initOptions.params) {
							getMapParams[key] = initOptions.params[key];
						}
					}

					if (initOptions && initOptions.options) {
						out.push(new OpenLayers.Layer.WMS(name, url, getMapParams, initOptions.options));
					} else {
						out.push(new OpenLayers.Layer.WMS(name, url, getMapParams));
					}
				}
			};
		}
		
		return out;
    };
    
    /**
	 * 
	 */
    factory.ol3_Layer = function(online, protocol, opt_options) {
      
        var out = [];

        if (online) {

            for (var i = 0; i < online.length; i++) {
                var on = online[i];
                var prot = on.protocol;
                
                if (prot && prot.indexOf(protocol) != -1) {
                    
                    var name = on.name;
                    var url = on.url;
					var title = on.title || on.description || on.name;

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
                        out.push(new ol.layer.Tile({
                            name: name,
                            title: title,
                            source: new ol.source.TileWMS({
                                url: url,
                                ratio: 1,
                                params: getMapParams,
                                attributions: opt_options.attributions
                            })    
                        })); //OpenLayers.Layer.WMS(name, url, getMapParams, initOptions.options));
                    } else {
                        out.push(new ol.layer.Tile({
                            name: name,
                            title: title,
                            source: new ol.source.TileWMS({
                                url: url,
                                ratio: 1,
                                params: getMapParams
                            })    
                        }));
                    }
                }
            };
        }
        
        return out;
    };
    
    return factory;
}
