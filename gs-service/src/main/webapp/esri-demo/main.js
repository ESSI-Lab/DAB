import 'ol/ol.css';
import EsriJSON from 'ol/format/EsriJSON';
import Map from 'ol/Map';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import XYZ from 'ol/source/XYZ';
import {Fill, Stroke, Style} from 'ol/style';
import {Tile as TileLayer, Vector as VectorLayer} from 'ol/layer';
import {createXYZ} from 'ol/tilegrid';
import {fromLonLat} from 'ol/proj';
import {tile as tileStrategy} from 'ol/loadingstrategy';

var serviceUrl =
  'http://localhost:9090/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/FeatureServer/';
var layer = '0';

var esrijsonFormat = new EsriJSON();

var styleCache = {
  'ABANDONED': new Style({
    fill: new Fill({
      color: 'rgba(225, 225, 225, 255)',
    }),
    stroke: new Stroke({
      color: 'rgba(0, 0, 0, 255)',
      width: 0.4,
    }),
  }),
  'GAS': new Style({
    fill: new Fill({
      color: 'rgba(255, 0, 0, 255)',
    }),
    stroke: new Stroke({
      color: 'rgba(110, 110, 110, 255)',
      width: 0.4,
    }),
  }),
  'OIL': new Style({
    fill: new Fill({
      color: 'rgba(56, 168, 0, 255)',
    }),
    stroke: new Stroke({
      color: 'rgba(110, 110, 110, 255)',
      width: 0,
    }),
  }),
  'OILGAS': new Style({
    fill: new Fill({
      color: 'rgba(168, 112, 0, 255)',
    }),
    stroke: new Stroke({
      color: 'rgba(110, 110, 110, 255)',
      width: 0.4,
    }),
  }),
};

var vectorSource = new VectorSource({
  loader: function (extent, resolution, projection) {
    var url =
      serviceUrl +
      layer +
      '/query/?f=json&' +
      'returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry=' +
      encodeURIComponent(
        '{"xmin":' +
          extent[0] +
          ',"ymin":' +
          extent[1] +
          ',"xmax":' +
          extent[2] +
          ',"ymax":' +
          extent[3] +
          ',"spatialReference":{"wkid":102100}}'
      ) +
      '&geometryType=esriGeometryEnvelope&inSR=102100&outFields=*' +
      '&outSR=102100';
    $.ajax({
      url: url,
      dataType: 'jsonp',
      success: function (response) {
        if (response.error) {
          alert(
            response.error.message + '\n' + response.error.details.join('\n')
          );
        } else {
          // dataProjection will be read from document
          var features = esrijsonFormat.readFeatures(response, {
            featureProjection: projection,
          });
          if (features.length > 0) {
            vectorSource.addFeatures(features);
          }
        }
      },
    });
  },
  strategy: tileStrategy(
    createXYZ({
      tileSize: 512,
    })
  ),
});

var vector = new VectorLayer({
  source: vectorSource,
  style: function (feature) {
    var classify = feature.get('activeprod');
    return styleCache[classify];
  },
});

var raster = new TileLayer({
  source: new XYZ({
    attributions:
      'Tiles © <a href="https://services.arcgisonline.com/ArcGIS/' +
      'rest/services/World_Topo_Map/MapServer">ArcGIS</a>',
    url:
      'https://server.arcgisonline.com/ArcGIS/rest/services/' +
      'World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
  }),
});

var map = new Map({
  layers: [raster, vector],
  target: document.getElementById('map'),
  view: new View({
    center: fromLonLat([-97.6114, 38.8403]),
    zoom: 7,
  }),
});

var displayFeatureInfo = function (pixel) {
  var features = [];
  map.forEachFeatureAtPixel(pixel, function (feature) {
    features.push(feature);
  });
  if (features.length > 0) {
    var info = [];
    var i, ii;
    for (i = 0, ii = features.length; i < ii; ++i) {
      info.push(features[i].get('field_name'));
    }
    document.getElementById('info').innerHTML = info.join(', ') || '(unknown)';
    map.getTarget().style.cursor = 'pointer';
  } else {
    document.getElementById('info').innerHTML = '&nbsp;';
    map.getTarget().style.cursor = '';
  }
};

map.on('pointermove', function (evt) {
  if (evt.dragging) {
    return;
  }
  var pixel = map.getEventPixel(evt.originalEvent);
  displayFeatureInfo(pixel);
});

map.on('click', function (evt) {
  displayFeatureInfo(evt.pixel);
});
