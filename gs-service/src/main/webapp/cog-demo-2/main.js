import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';



// 1. WBGT Style
const wbgtStyle = {
  color: [
    'case',
    ['<=', ['band', 1], -5.0], '#313695',
    ['<=', ['band', 1], 0.0],  '#4575b4',
    ['<=', ['band', 1], 5.0],  '#74add1',
    ['<=', ['band', 1], 10.0], '#abd9e9',
    ['<=', ['band', 1], 15.0], '#fdae61',
    ['<=', ['band', 1], 20.0], '#f46d43',
    '#d73027'
  ],
  opacity: 1.0
};

// 2. Map & Layer Init
const wbgtLayer = new WebGLTileLayer({ style: wbgtStyle });
const map = new OLMap({
  target: 'map',
  layers: [new TileLayer({ source: new OSM() }), wbgtLayer],
  view: new View({ projection: 'EPSG:3857', center: [1113194, 7628367], zoom: 4 })
});

// 3. State
let timeline = [];
const slider = document.getElementById('forecastSlider');
const timeLabel = document.getElementById('timeMain');

// 4. Load Data
async function initData() {
  try {
    const response = await fetch('https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/wbgt/index.json');
    const data = await response.json();
    timeline = data.files;

    slider.max = timeline.length - 1;
    updateMap(0);
  } catch (e) { console.error("Data load error:", e); }
}

function updateLegend() {
  const legendDiv = document.getElementById('legend');
  const items = [
    { range: '≤ -5.0', label: 'Freezing / Cold Stress', color: '#313695' },
    { range: '-5.0 – 0.0', label: 'Very Cold', color: '#4575b4' },
    { range: '0.0 – 5.0', label: 'Cold', color: '#74add1' },
    { range: '5.0 – 10.0', label: 'Cool', color: '#abd9e9' },
    { range: '10.0 – 15.0', label: 'Mild', color: '#fdae61' },
    { range: '15.0 – 20.0', label: 'Warm', color: '#f46d43' },
    { range: '> 20.0', label: 'Heat Stress Threshold', color: '#d73027' }
  ];

  legendDiv.innerHTML = items.map(item => `
    <div class="legend-item">
      <div class="legend-color" style="background:${item.color}"></div>
      <div>${item.range} - ${item.label}</div>
    </div>
  `).join('');
}

// 5. Update UI
function updateMap(index) {
  const file = timeline[index];
  if (!file) return;

  wbgtLayer.setSource(new GeoTIFF({ sources: [{ url: file.url }], normalize: false }));
  timeLabel.textContent = file.time.replace('T', ' · ');
}

slider.addEventListener('input', (e) => updateMap(Number(e.target.value)));

initData().then(updateLegend);