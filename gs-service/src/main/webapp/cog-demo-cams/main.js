import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';


// The COGs store the concentration multiplied by 10 as integers (UInt16): 137 = 13.7 µg/m³. For this reason the thresholds in the
// styles below are multiplied by 10, while the legends show the real values in µg/m³. 65535 is the NoData value.
const VALUE_SCALE = 0.1;
const NODATA_VALUE = 65535;

// NoData: OpenLayers adds an alpha band (band 2: 0 = NoData, 255 = data). A custom 'case' style ignores that alpha band,
// so every style must test it first
const NODATA_CASE = ['<', ['band', 2], 128];

const ATTRIBUTION = 'Generated using Copernicus Atmosphere Monitoring Service information';

// European Air Quality Index (EEA, 2024 revision): same colors for every pollutant, different thresholds (µg/m³).
// 'max' is the upper bound of each class, used to classify the value under the mouse
const CONFIG = {
  pm25: {
    title: "PM2.5 (Particulate Matter < 2.5 µm)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CAMS/pm25/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 50],   '#50f0e6',   //   5 µg/m³
        ['<', ['band', 1], 150],  '#50ccaa',   //  15 µg/m³
        ['<', ['band', 1], 500],  '#f0e641',   //  50 µg/m³
        ['<', ['band', 1], 900],  '#ff5050',   //  90 µg/m³
        ['<', ['band', 1], 1400], '#960032',   // 140 µg/m³
        '#7d2181'
      ]
    },
    legend: [
      { max: 5,   range: '< 5 µg/m³',      label: 'Good',           color: '#50f0e6' },
      { max: 15,  range: '5 – 15 µg/m³',   label: 'Fair',           color: '#50ccaa' },
      { max: 50,  range: '15 – 50 µg/m³',  label: 'Moderate',       color: '#f0e641' },
      { max: 90,  range: '50 – 90 µg/m³',  label: 'Poor',           color: '#ff5050' },
      { max: 140, range: '90 – 140 µg/m³', label: 'Very poor',      color: '#960032' },
      {           range: '≥ 140 µg/m³',    label: 'Extremely poor', color: '#7d2181' }
    ]
  },
  pm10: {
    title: "PM10 (Particulate Matter < 10 µm)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CAMS/pm10/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 150],  '#50f0e6',   //  15 µg/m³
        ['<', ['band', 1], 450],  '#50ccaa',   //  45 µg/m³
        ['<', ['band', 1], 1200], '#f0e641',   // 120 µg/m³
        ['<', ['band', 1], 1950], '#ff5050',   // 195 µg/m³
        ['<', ['band', 1], 2700], '#960032',   // 270 µg/m³
        '#7d2181'
      ]
    },
    legend: [
      { max: 15,  range: '< 15 µg/m³',      label: 'Good',           color: '#50f0e6' },
      { max: 45,  range: '15 – 45 µg/m³',   label: 'Fair',           color: '#50ccaa' },
      { max: 120, range: '45 – 120 µg/m³',  label: 'Moderate',       color: '#f0e641' },
      { max: 195, range: '120 – 195 µg/m³', label: 'Poor',           color: '#ff5050' },
      { max: 270, range: '195 – 270 µg/m³', label: 'Very poor',      color: '#960032' },
      {           range: '≥ 270 µg/m³',     label: 'Extremely poor', color: '#7d2181' }
    ]
  },
  no2: {
    title: "NO₂ (Nitrogen Dioxide)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CAMS/no2/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 100],  '#50f0e6',   //  10 µg/m³
        ['<', ['band', 1], 250],  '#50ccaa',   //  25 µg/m³
        ['<', ['band', 1], 600],  '#f0e641',   //  60 µg/m³
        ['<', ['band', 1], 1000], '#ff5050',   // 100 µg/m³
        ['<', ['band', 1], 1500], '#960032',   // 150 µg/m³
        '#7d2181'
      ]
    },
    legend: [
      { max: 10,  range: '< 10 µg/m³',      label: 'Good',           color: '#50f0e6' },
      { max: 25,  range: '10 – 25 µg/m³',   label: 'Fair',           color: '#50ccaa' },
      { max: 60,  range: '25 – 60 µg/m³',   label: 'Moderate',       color: '#f0e641' },
      { max: 100, range: '60 – 100 µg/m³',  label: 'Poor',           color: '#ff5050' },
      { max: 150, range: '100 – 150 µg/m³', label: 'Very poor',      color: '#960032' },
      {           range: '≥ 150 µg/m³',     label: 'Extremely poor', color: '#7d2181' }
    ]
  },
  o3: {
    title: "O₃ (Ozone)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CAMS/o3/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 600],  '#50f0e6',   //  60 µg/m³
        ['<', ['band', 1], 1000], '#50ccaa',   // 100 µg/m³
        ['<', ['band', 1], 1200], '#f0e641',   // 120 µg/m³
        ['<', ['band', 1], 1600], '#ff5050',   // 160 µg/m³
        ['<', ['band', 1], 1800], '#960032',   // 180 µg/m³
        '#7d2181'
      ]
    },
    legend: [
      { max: 60,  range: '< 60 µg/m³',      label: 'Good',           color: '#50f0e6' },
      { max: 100, range: '60 – 100 µg/m³',  label: 'Fair',           color: '#50ccaa' },
      { max: 120, range: '100 – 120 µg/m³', label: 'Moderate',       color: '#f0e641' },
      { max: 160, range: '120 – 160 µg/m³', label: 'Poor',           color: '#ff5050' },
      { max: 180, range: '160 – 180 µg/m³', label: 'Very poor',      color: '#960032' },
      {           range: '≥ 180 µg/m³',     label: 'Extremely poor', color: '#7d2181' }
    ]
  },
  so2: {
    title: "SO₂ (Sulphur Dioxide)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CAMS/so2/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 200],  '#50f0e6',   //  20 µg/m³
        ['<', ['band', 1], 400],  '#50ccaa',   //  40 µg/m³
        ['<', ['band', 1], 1250], '#f0e641',   // 125 µg/m³
        ['<', ['band', 1], 1900], '#ff5050',   // 190 µg/m³
        ['<', ['band', 1], 2750], '#960032',   // 275 µg/m³
        '#7d2181'
      ]
    },
    legend: [
      { max: 20,  range: '< 20 µg/m³',      label: 'Good',           color: '#50f0e6' },
      { max: 40,  range: '20 – 40 µg/m³',   label: 'Fair',           color: '#50ccaa' },
      { max: 125, range: '40 – 125 µg/m³',  label: 'Moderate',       color: '#f0e641' },
      { max: 190, range: '125 – 190 µg/m³', label: 'Poor',           color: '#ff5050' },
      { max: 275, range: '190 – 275 µg/m³', label: 'Very poor',      color: '#960032' },
      {           range: '≥ 275 µg/m³',     label: 'Extremely poor', color: '#7d2181' }
    ]
  }
};

// Application State Context
let activeVariable = 'pm25';
let timeline = [];
let forecastRun = null;
let currentIndex = 0;
let playTimer = null;

const geoTiffCache = new Map();
const MAX_CACHE_SIZE = 48;

// DOM Selectors
const pollutantSelector = document.getElementById('pollutantSelector');
const runLabel = document.getElementById('runLabel');
const timeLabel = document.getElementById('timeMain');
const slider = document.getElementById('forecastSlider');
const prevBtn = document.getElementById('prevBtn');
const playBtn = document.getElementById('playBtn');
const nextBtn = document.getElementById('nextBtn');
const speedSelector = document.getElementById('speedSelector');
const opacitySlider = document.getElementById('opacitySlider');
const pixelValue = document.getElementById('pixelValue');
const legendTitle = document.getElementById('legendTitle');

// Initialize base OpenLayers layer placeholder
const mapLayer = new WebGLTileLayer({ style: CONFIG[activeVariable].style, opacity: opacitySlider.value / 100 });
const map = new OLMap({
  target: 'map',
  layers: [new TileLayer({ source: new OSM() }), mapLayer],
  view: new View({ projection: 'EPSG:3857', center: [1113194, 6800000], zoom: 4 })
});

// Load the forecast of the selected pollutant
async function loadPollutant(variableKey) {
  try {
    stopAnimation();
    activeVariable = variableKey;
    runLabel.textContent = "Loading forecast...";

    const response = await fetch(CONFIG[variableKey].url, { cache: 'no-cache' });
    const data = await response.json();

    // The COGs are in the same folder of the index.json
    const folder = CONFIG[variableKey].url.replace('index.json', '');
    timeline = data.files
      .map(file => ({ time: file.time, leadTimeHour: file.leadTimeHour, url: folder + file.filename }))
      .sort((a, b) => new Date(a.time) - new Date(b.time));

    forecastRun = data.forecastReferenceTime;
    runLabel.textContent = `Forecast run: ${formatDate(forecastRun)} (CAMS ensemble)`;

    mapLayer.setStyle(CONFIG[variableKey].style);
    legendTitle.textContent = CONFIG[variableKey].title + " (µg/m³)";
    buildLegend(variableKey);

    slider.max = timeline.length - 1;
    updateMap(findClosestToNow());
  } catch (e) {
    runLabel.textContent = `Error loading pollutant: ${variableKey}`;
    console.error(e);
  }
}

// The forecast starts at 00 UTC of the run day: open the map on the hour closest to the current time
function findClosestToNow() {
  const now = Date.now();
  let closest = 0;
  timeline.forEach((file, index) => {
    if (Math.abs(new Date(file.time) - now) < Math.abs(new Date(timeline[closest].time) - now)) {
      closest = index;
    }
  });
  return closest;
}

function updateMap(index) {
  if (!timeline[index]) return;
  currentIndex = index;
  const file = timeline[index];

  mapLayer.setSource(getOrCreateSource(file.url));
  slider.value = index;
  timeLabel.textContent = `${formatDate(file.time)} (+${file.leadTimeHour}h)`;

  // Prepare the next hour in advance, so that the animation does not flicker
  if (timeline[index + 1]) {
    getOrCreateSource(timeline[index + 1].url);
  }
}

function getOrCreateSource(fileUrl) {
  if (geoTiffCache.has(fileUrl)) return geoTiffCache.get(fileUrl);
  const source = new GeoTIFF({
    sources: [{ url: fileUrl, nodata: NODATA_VALUE }],
    normalize: false,
    transition: 0,
    attributions: ATTRIBUTION
  });
  if (geoTiffCache.size >= MAX_CACHE_SIZE) geoTiffCache.delete(geoTiffCache.keys().next().value);
  geoTiffCache.set(fileUrl, source);
  return source;
}

function buildLegend(variableKey) {
  const legendDiv = document.getElementById('legend');
  legendDiv.innerHTML = CONFIG[variableKey].legend.map(item => `
    <div class="legend-item">
      <div class="legend-color" style="background:${item.color}"></div>
      <div><strong>${item.range}</strong> — ${item.label}</div>
    </div>
  `).join('');
}

function formatDate(isoTime) {
  return new Date(isoTime).toLocaleString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', timeZone: 'UTC'
  }) + ' UTC';
}

// --- Animation ---
function startAnimation() {
  playBtn.innerHTML = '&#10074;&#10074; Pause';
  playTimer = setInterval(() => {
    updateMap((currentIndex + 1) % timeline.length);
  }, Number(speedSelector.value));
}

function stopAnimation() {
  clearInterval(playTimer);
  playTimer = null;
  playBtn.innerHTML = '&#9654; Play';
}

// --- Event Triggers ---
pollutantSelector.addEventListener('change', (e) => {
  loadPollutant(e.target.value);
});

slider.addEventListener('input', (e) => {
  stopAnimation();
  updateMap(parseInt(e.target.value, 10));
});

prevBtn.addEventListener('click', () => {
  stopAnimation();
  updateMap(Math.max(0, currentIndex - 1));
});

nextBtn.addEventListener('click', () => {
  stopAnimation();
  updateMap(Math.min(timeline.length - 1, currentIndex + 1));
});

playBtn.addEventListener('click', () => {
  if (playTimer) {
    stopAnimation();
  } else {
    startAnimation();
  }
});

// Restart the animation with the new speed
speedSelector.addEventListener('change', () => {
  if (playTimer) {
    stopAnimation();
    startAnimation();
  }
});

opacitySlider.addEventListener('input', (e) => {
  mapLayer.setOpacity(e.target.value / 100);
});

// Concentration under the mouse: getData returns [band 1, alpha band]
map.on('pointermove', (evt) => {
  if (evt.dragging) return;

  const data = mapLayer.getData(evt.pixel);
  if (!data || data[1] === 0) {
    pixelValue.textContent = 'No data';
    return;
  }

  const value = data[0] * VALUE_SCALE;
  const item = CONFIG[activeVariable].legend.find(item => item.max === undefined || value < item.max);
  pixelValue.innerHTML = `<strong>${value.toFixed(1)} µg/m³</strong> — ${item.label}`;
});

// Kickstart with the PM2.5 forecast
loadPollutant('pm25');
