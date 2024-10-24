/*
 *     This file is part of GI-cat frontend.
 *
 *     GI-cat frontend is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     GI-cat frontend is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GI-cat frontend.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009-2011 ESSI-Lab <info@essi-lab.eu>
 */

YAHOO.namespace("geoportal.map");

YAHOO.geoportal.map.selectionLayer;
YAHOO.geoportal.map.highlightLayer;
YAHOO.geoportal.map.resultsLayer;

/**
 * The map init function.
 */
YAHOO.geoportal.map.init = function(){
	
	//----------------------- //
    // --- Initialization --- //
    //----------------------- //

    var Dom = YAHOO.util.Dom;
    
    var nField = Dom.get("input-sel-area-n");
    var sField = Dom.get("input-sel-area-s");
    var wField = Dom.get("input-sel-area-w");
    var eField = Dom.get("input-sel-area-e");
    
   /**
    * This layer keeps a single feature ( a vector ) which represents the selection 
    * made by the user. Vector is created with the createSelectionVector method with
    * parameter "selection". 
    */
    YAHOO.geoportal.map.selectionLayer = new OpenLayers.Layer.Vector("User Selection", {
        styleMap: new OpenLayers.StyleMap({
            strokeColor: "red",
            strokeOpacity: 0.5,
            fill: true,
            fillOpacity: 0.2,
            fillColor: "blue"
        
        })
    });
        
    /**
     * This layer keeps a single feature ( a vector ) which represents the bbox area of a given
     * result in the results table. Vector is created with the createSelectionVector method with
     * parameter "highlight".       
     */
    YAHOO.geoportal.map.highlightLayer = new OpenLayers.Layer.Vector("Highlight Layer", {
        styleMap: new OpenLayers.StyleMap({
            strokeColor: "red",
            strokeOpacity: 1.0,
            fill: true,
            fillOpacity: 0.5,
            fillColor: "yellow"   
        })
    });
    
    /**
     * This layer keeps multiple features ( vectors ) which represents all the bbox area of a search
     * result. Vector is created with the createSelectionVector method with parameter "results".
     */
    YAHOO.geoportal.map.resultsLayer = new OpenLayers.Layer.Vector("Results Layer", {
        styleMap: new OpenLayers.StyleMap({
            strokeColor: "yellow",
            strokeOpacity: 0.7,
            fill: false,
        })
    });
    /**
     * This is the default "Gloabal Imagery" map layer.
     */
    var imagery = new OpenLayers.Layer.WMS("Global Imagery", "http://maps.opengeo.org/geowebcache/service/wms", {
        layers: "openstreetmap",
        format: "image/png",
		numZoomLevels : 10
    });
            
    /**
     * The "Blue Marble" map layer.
     */
    var blueMarble = new OpenLayers.Layer.WMS("Blue Marble", "http://maps.opengeo.org/geowebcache/service/wms", {
        layers: 'bluemarble',
        format: 'image/png',
        numZoomLevels : 10
    });
          
    //------------------------------------------------ //
    // Creating map and adding layers and controls --- //
    //------------------------------------------------ //
    
    var map = new OpenLayers.Map('map');
    
    map.addLayers([imagery, blueMarble,  YAHOO.geoportal.map.selectionLayer,YAHOO.geoportal.map.resultsLayer, YAHOO.geoportal.map.highlightLayer]);     
    map.addControl(new OpenLayers.Control.PanZoomBar());
    map.addControl(new OpenLayers.Control.LayerSwitcher({
        'ascending': false
    }));
    
    map.addControl(new OpenLayers.Control.MousePosition());
    
    //----------------------------- //
    // --- Functions definition --- //
    //----------------------------- //
    
    /** 
	 * Clear the selection made by the user.
	 */
    YAHOO.geoportal.map.selectionLayer.clearSelection = function(fields){
    
        if (fields) {
			nField.value = "";
			sField.value = "";
			eField.value = "";
			wField.value = "";
		}
		
		Dom.get('input-bbox').value = "";
        
        if (this.features.length > 0) {
            this.removeFeatures([this.selection]);
        }
        
        YAHOO.geoportal.cancelBboxButton.set('disabled', true);
    };
    
    /**
	 * Set the selection and updates the bbox input fields; this method is called every time the user
	 * makes a selection on the map.
	 * 
	 * @param {Object}
	 *            oBounds
	 */
    YAHOO.geoportal.map.selectionLayer.setSelection = function(oBounds){
    
        // bounds is in pixel
        nw = new OpenLayers.Pixel(oBounds.left, oBounds.top);
        se = new OpenLayers.Pixel(oBounds.right, oBounds.bottom);
        
        // transforming to lat/lon
        nwM = map.getLonLatFromPixel(nw);
        seM = map.getLonLatFromPixel(se);
        
         if (seM.lon>180){
        	seM.lon = 180;
        }
        if (nwM.lon<-180){
        	nwM.lon = -180;
        }
        if (nwM.lat>90){
        	nwM.lat = 90;
        }        
        if (seM.lat<-90){
        	seM.lat = -90;
        }
        
        nwM.lon =  nwM.lon.toFixed(3);
        nwM.lat =  nwM.lat.toFixed(3);
        seM.lon =  seM.lon.toFixed(3);
        seM.lat =  seM.lat.toFixed(3);
        
        // clears the current selection
        this.clearSelection(true);
        
        // adds the new selection
        var lonLatBounds = new OpenLayers.Bounds(nwM.lon, seM.lat, seM.lon, nwM.lat);       
        this.selection = new OpenLayers.Feature.Vector(lonLatBounds.toGeometry());            
        this.addFeatures([this.selection]);
   
        // updating bbox fields
        nField.value = nwM.lat;
        sField.value = seM.lat;
        eField.value = seM.lon;
        wField.value = nwM.lon;
        
        // updating input-bbox value
        Dom.get('input-bbox').value = wField.value + "," + sField.value + "," + eField.value + "," + nField.value;
          
		if (Dom.get("loc-radio").checked == false) {
			
			YAHOO.geoportal.cancelBboxButton.set('disabled', false);
			YAHOO.geoportal.enableRelation(true);
		}
    };
    
	/**
	 * Updates the selection with the bbox input fields value; this method is called every time the user 
	 * edits the bbox input fields.
	 */  
    YAHOO.geoportal.map.selectionLayer.updateSelection = function(){
    
        var n = nField.value;
        var s = sField.value;
        var e = eField.value;
        var w = wField.value;
                      
        if (n != "" && s != "" && e != "" && w != "") {
        
            if (parseInt(n) > 90) {
                n = 90;
            }
            if (parseInt(s) < -90) {
                s = -90;
            }
            if (parseInt(e) > 180) {
                e = 180;
            }
            if (parseInt(w) < -180) {
                w = -180;
            }
			
			YAHOO.geoportal.cancelBboxButton.set('disabled', false);
			YAHOO.geoportal.enableRelation(true);
        }
        
        if (this.features.length > 0) {
            this.removeFeatures([this.selection]);
        }
                
        var bounds = new OpenLayers.Bounds(w, s, e, n);
        
        this.selection = YAHOO.geoportal.map.selectionLayer.createSelectionVector(w,s,e,n,"selection");            
        this.addFeatures([this.selection]);    
        
        // updating input-bbox value
        Dom.get('input-bbox').value = w + "," + s + "," + e + "," + n;

    }
        
    /**
	 * The selection control ( created and added to the map now because of the setSelection and
	 * clearSelection methods references ).
	 */
    var control = new OpenLayers.Control(); // the selection control
    OpenLayers.Util.extend(control, {
        draw: function(){
            this.box = new OpenLayers.Handler.Box(control, {
                "done": this.notice
            }, {
                keyMask: OpenLayers.Handler.MOD_SHIFT
            });
            this.box.activate();
        },
        notice: function(bounds){
        
            if (bounds.left) {
                YAHOO.geoportal.map.selectionLayer.setSelection(bounds);
            } else {
                YAHOO.geoportal.map.selectionLayer.clearSelection(true);
            }
        }
    });
    map.addControl(control);
    
    /**
	 * This function is called when the "Zoom" button of a given search result
	 * is pressed.
	 * 
	 * @param {Object}
	 *            west
	 * @param {Object}
	 *            south
	 * @param {Object}
	 *            east
	 * @param {Object}
	 *            north
	 */
    YAHOO.geoportal.map.zoomToBounds = function(west, south, east, north){
    
        bounds = new OpenLayers.Bounds(west, south, east, north);
        map.zoomToExtent(bounds, true);
    };
    
    YAHOO.geoportal.map.zoomToBounds(-85, -13, 128, 56);
    
    /**
	 * This function is called when the "Add" button of a given WMS 1.1.1 search
	 * result is pressed.
	 * 
	 * @param {Object}
	 *            name
	 * @param {Object}
	 *            endpoint
	 * @param {Object}
	 *            layer
	 */
    YAHOO.geoportal.map.addWms11 = function(name, endpoint, layer){
    
        var newlayer = new OpenLayers.Layer.WMS(name, endpoint, {
            layers: layer,
            transparent: "TRUE"
        }, {
            isBaseLayer: false
        });
        
        if (endpoint.indexOf("jpl.nasa.gov") != -1) {
            newlayer.setTileSize(new OpenLayers.Size(512, 512));
        }
        
        map.addLayers([newlayer]);
    };
    
    /**
	 * This function is called when the "Add" button of a given WMS 1.3.0 search
	 * result is pressed.
	 * 
	 * @param {Object}
	 *            name
	 * @param {Object}
	 *            endpoint
	 * @param {Object}
	 *            layer
	 * @deprecated
	 */
    YAHOO.geoportal.map.addWms13 = function(name, endpoint, layer){
    
        var newlayer = new OpenLayers.Layer.WMS(name, endpoint, {
            layers: layer,
            transparent: "TRUE",
            version: "1.3.0"
        }, {
            isBaseLayer: false
        });
        
        if (endpoint.indexOf("svs.gsfc.nasa.gov") != -1) {
            newlayer.setTileSize(new OpenLayers.Size(1024, 1024));
        }
        
        map.addLayers([newlayer]);
    };
    
    /**
	 * Highlights the given search result on the map by means of a rectangular region with semi-transparent 
	 * yellow background. This method is called every time the user rolls the mouse pointer over a result 
	 * in the results table.
	 */
    YAHOO.geoportal.map.highlightResult = function(west, south, east, north){
       	
    	YAHOO.geoportal.map.clearHiglightResult();
                        
        var resultFeature = YAHOO.geoportal.map.selectionLayer.createSelectionVector(west, south, east, north,"highlight");
        
        YAHOO.geoportal.map.highlightLayer.addFeatures([resultFeature]);
    };
      
    /**
	 * Clears the highlighted result.
	 */
    YAHOO.geoportal.map.clearHiglightResult = function(){
    	
    	YAHOO.geoportal.map.highlightLayer.removeAllFeatures();
    };
    
    /**
     * This function is called at the end of a query. It requires as input
     * an array variable, when each element represents a bbox coordinates in the form [s,w,n,e]. 
     */
    YAHOO.geoportal.map.localizeAll = function(searchResults){
    	
    	YAHOO.geoportal.map.resultsLayer.removeAllFeatures();
    	
        for ( var i = 0; i < searchResults.length; i++) {
        	
        	var markerType = "results";
        	
        	if( searchResults[i].einfo == "true" ){
        		
        		if(searchResults[i].mag == 7){           		
        			markerType = "mag7";
        		}else if(searchResults[i].mag == 6){        			
        			markerType = "mag6";
        		}else if(searchResults[i].mag == 5){        		
        			markerType = "mag5";
        		}else{
        			markerType = "mag4";
        		}		        	
        	}else if( searchResults[i].sinfo == "true" ){
        		markerType = "sResults";
        	}
        					
        	var bbox = searchResults[i].box;
        	
        	var w = bbox.split(" ")[1];
        	var s = bbox.split(" ")[0];
        	var e = bbox.split(" ")[3];
        	var n = bbox.split(" ")[2];
        	  	
        	var resultFeature = YAHOO.geoportal.map.selectionLayer.createSelectionVector(w, s, e, n, markerType);
        	YAHOO.geoportal.map.resultsLayer.addFeatures([resultFeature]);
		}
    }
        
    /**
     * Creates a vector which represents a bbox with the given values. If given values represents a point,
     * than a vector with a marker is created, otherwise a rectangular vector is created.
     */
    YAHOO.geoportal.map.selectionLayer.createSelectionVector = function(west,south,east,north, markerType){
    	
    	var lonLatBounds = new OpenLayers.Bounds(west, south, east, north);
        var vector;
    	
        if(west == east && south == north){
        	
        	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
            renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
            
            var style_mark = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
      
            style_mark.graphicOpacity = 1;
            style_mark.graphicWidth   = 24;
            style_mark.graphicHeight  = 24;
            style_mark.graphicXOffset = -(style_mark.graphicWidth/2);
            style_mark.graphicYOffset = -(style_mark.graphicHeight/2);
                      
            if(markerType == "selection"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_selection.png";
            }else if(markerType == "results"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_results.png";
            
            }else if(markerType == "mag7"){
            	style_mark.externalGraphic = "../gi-portal/images/mag7.png";
         
            }else if(markerType == "mag6"){
            	style_mark.externalGraphic = "../gi-portal/images/mag6.png";
         
            }else if(markerType == "mag5"){
            	style_mark.externalGraphic = "../gi-portal/images/mag5.png";
         
            }else if(markerType == "mag4"){
            	style_mark.externalGraphic = "../gi-portal/images/mag4.png";           
            
            }else if(markerType == "sResults"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_sta_results.png";
                      
            }else if(markerType == "highlight"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_highlight.png";
            }
                                
            var point = new OpenLayers.Geometry.Point(west, south);
             
            vector = new OpenLayers.Feature.Vector(point,null,style_mark);
        	
        }else{
        	
        	vector = new OpenLayers.Feature.Vector(lonLatBounds.toGeometry());
        }	
        
        return vector;
    }
};

/**
 * Verifies that the bbox input fields contain only numbers.
 * 
 * @param {Object}
 *            event
 */
function checkBBOXinput(event){

    var key = window.event ? event.keyCode : event.which;
	
	// special characters admitted
    if (key == null || key == 0 || key == 8 || key == 13 || key == 46 || key == 37 || key == 39) { 
        return true;
    }
    
    var keychar = String.fromCharCode(key);
    
    reg = /[0-9]|\-|\./;
    return result = reg.test(keychar);
}
