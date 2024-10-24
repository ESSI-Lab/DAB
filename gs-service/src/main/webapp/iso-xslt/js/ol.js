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


function getSource()
{
  location.href = "view-source:" + location.href;
  return;
}

function isCorrect(w,s,e,n){
	return !(n>90 || s<-90 || e>180 || e <-180 || w>180 || w < -180);
}
    
var map, layer, layer2;

function init(multiboxString,markerType,track) {


	map = new OpenLayers.Map( 'map' );
	/*layer = new OpenLayers.Layer.WMS( "OpenLayers WMS",
			"http://vmap0.tiles.osgeo.org/wms/vmap0",
			{layers: 'basic'} );*/
			

	layer2 = new OpenLayers.Layer.XYZ("Blue Marble Next",["http://tiles.geodab.eu/geodab-tiles/tile/bluemarblenext/${z}/${x}/${y}.jpg"], {numZoomLevels: 9});			
	map.addLayer(layer2);

	
    layer = new OpenLayers.Layer.XYZ("DAB Base Map",["http://tiles.geodab.eu/geodab-tiles/tile/bluemarble/${z}/${x}/${y}.png"], {numZoomLevels: 9});			
	map.addLayer(layer);
	

	map.zoomToMaxExtent();

	var highlightLayer = new OpenLayers.Layer.Vector("Highlight Layer", {
		styleMap: new OpenLayers.StyleMap({
			strokeColor: "red",
			strokeOpacity: 1.0,
			fill: true,
			fillOpacity: 0.5,
			fillColor: "yellow"   
		})
	});

	var multibox = multiboxString.split("#");

	var w = -181;
	var s = -91;
	var e = 181;
	var n = 91;

	for ( var j = 0; j < multibox.length-1; j++) {

		var bbox = multibox[j];

		var west = parseFloat(bbox.split(",")[0]);
		var south = parseFloat(bbox.split(",")[1]);
		var east = parseFloat(bbox.split(",")[2]);
		var north = parseFloat(bbox.split(",")[3]);

		if (isCorrect(west,south,east,north)){
			if (w==-181){
				w = west;
			}
			if (west<w){
				w = west;
			}        		
			if (s==-91){
				s = south;
			}
			if (south<s){
				s = south;
			}
			if (e==181){
				e = east;
			}
			if (east>e){
				e = east;
			}
			if (n==91){
				n = north;
			}
			if (north>n){
				n = north;
			}
			var resultFeature = createSelectionVector(west, south, east, north,markerType);                            
			highlightLayer.addFeatures([resultFeature]);
		}
	}

	map.addControl(new OpenLayers.Control.LayerSwitcher({
		'ascending': false
	}));


	map.addControl(new OpenLayers.Control.MousePosition());

	map.zoomToBounds = function(w, s, e, n){

		bounds = new OpenLayers.Bounds(w, s, e, n);
		map.zoomToExtent(bounds, true);
	};
	w = w-5;
	s = s-5;
	e = e+5;
	n = n+5;

	map.zoomToBounds(w, s, e, n);

	map.addLayer(highlightLayer);
	
	// POLYGONS
	
	if (track && track.indexOf(' ') > -1){
		
		var newTrack = track.replace("  "," ").trim();
		
		while (newTrack!=track){
			track = newTrack;
			newTrack = track.replace("  "," ");
		}
		track = newTrack;
		
		
		var trackSplit = track.split(" ");		
		
		if (trackSplit.length>1){
		
			var lineLayer = new OpenLayers.Layer.Vector("Line Layer"); 
		
			map.addLayer(lineLayer);                    
			map.addControl(new OpenLayers.Control.DrawFeature(lineLayer, OpenLayers.Handler.Path));                                     
			var points = [];
			
			var markers = new OpenLayers.Layer.Markers( "Markers" );
		    map.addLayer(markers);
		    var s = 5
		    var size = new OpenLayers.Size(s,s);
		    var offset = new OpenLayers.Pixel(-s, -s-2);
		    var icon = new OpenLayers.Icon('../gi-portal/iso-xslt/img/red-circle.png', size, offset);
		    
		
			
				for (i = 0; i < trackSplit.length; i=i+2) {
					points.push(new OpenLayers.Geometry.Point(trackSplit[i], trackSplit[i+1]));
					//alert(trackSplit[i]+' '+trackSplit[i+1]);
					markers.addMarker(new OpenLayers.Marker(new OpenLayers.LonLat(trackSplit[i],trackSplit[i+1]),icon.clone()));
				}			   
			
		
			var line1 = new OpenLayers.Geometry.LineString(points);
			var line2 = new OpenLayers.Geometry.LineString(points);
			
			var style1 = { 
			  strokeColor: '#0000ff', 
			  strokeOpacity: 0.5,
			  strokeWidth: 2
			};
			var style2 = { 
			  strokeColor: '#ffffff', 
			  strokeOpacity: 1,
			  strokeWidth: 1
			};
		
			var lineFeature1 = new OpenLayers.Feature.Vector(line1, null, style1);
			lineLayer.addFeatures([lineFeature1]);
			var lineFeature2 = new OpenLayers.Feature.Vector(line2, null, style2);
			lineLayer.addFeatures([lineFeature2]);
			
		    
		}
	}
}

function createSelectionVector(west,south,east,north, markerType) {

	var lonLatBounds = new OpenLayers.Bounds(west, south, east, north);
	var vector;

	if(west == east) {
		if (south == north) {

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
            
            }else if(markerType == "sResults"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_sta_results.png";
                      
            }else if(markerType == "highlight"){
            	style_mark.externalGraphic = "../gi-portal/images/marker_highlight.png";
          
            }else if(markerType >= 7){
            	
            	style_mark.externalGraphic = "../gi-portal/images/mag7.png";
         
            }else if(markerType >= 6){
            	
            	style_mark.externalGraphic = "../gi-portal/images/mag6.png";
         
            }else if(markerType >= 5){
            	
            	style_mark.externalGraphic = "../gi-portal/images/mag5.png";
         
            }else if(markerType >= 4){
            	
            	style_mark.externalGraphic = "../gi-portal/images/mag4.png";                     
            }
			
			var point = new OpenLayers.Geometry.Point(west, south);

			vector = new OpenLayers.Feature.Vector(point,null,style_mark);
		} else {

			vector = new OpenLayers.Feature.Vector(lonLatBounds.toGeometry());
		}

	} else {

		vector = new OpenLayers.Feature.Vector(lonLatBounds.toGeometry());
	}	

	return vector;

}