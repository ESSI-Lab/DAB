import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';


// The COGs store the index values in °C (Float32); -9999 is the NoData value (sea and outside the WRF domain)
const NODATA_VALUE = -9999;

// NoData: OpenLayers adds an alpha band (band 2: 0 = NoData, 255 = data). A custom 'case' style ignores that alpha band,
// so every style must test it first
const NODATA_CASE = ['<', ['band', 2], 128];

const ATTRIBUTION = 'CIMA Research Foundation - WRF operational forecast';

// Same classes of cog-demo-2. 'min' and 'max' are the bounds of each class, used to classify the value under the mouse
const CONFIG = {
  utci: {
    title: "Universal Thermal Climate Index (UTCI)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/forecast/utci/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], -40.0], '#000066',
        ['<', ['band', 1], -27.0], '#0000ff',
        ['<', ['band', 1], -13.0], '#007fff',
        ['<', ['band', 1], 0.0],   '#add8e6',
        ['<', ['band', 1], 9.0],   '#e0f3f8',
        ['<', ['band', 1], 26.0],  '#2ca25f',
        ['<', ['band', 1], 32.0],  '#fee090',
        ['<', ['band', 1], 38.0],  '#fdae61',
        ['<', ['band', 1], 46.0],  '#f46d43',
        '#d73027'
      ]
    },
    legend: [
      {            max: -40, range: '< -40.0 °C',         label: 'Extreme cold stress',     color: '#000066' },
      { min: -40,  max: -27, range: '-40.0 to -27.0 °C',  label: 'Very strong cold stress', color: '#0000ff' },
      { min: -27,  max: -13, range: '-27.0 to -13.0 °C',  label: 'Strong cold stress',      color: '#007fff' },
      { min: -13,  max: 0,   range: '-13.0 to 0.0 °C',    label: 'Moderate cold stress',    color: '#add8e6' },
      { min: 0,    max: 9,   range: '0.0 to 9.0 °C',      label: 'Slight cold stress',      color: '#e0f3f8' },
      { min: 9,    max: 26,  range: '9.0 to 26.0 °C',     label: 'No thermal stress',       color: '#2ca25f' },
      { min: 26,   max: 32,  range: '26.0 to 32.0 °C',    label: 'Moderate heat stress',    color: '#fee090' },
      { min: 32,   max: 38,  range: '32.0 to 38.0 °C',    label: 'Strong heat stress',      color: '#fdae61' },
      { min: 38,   max: 46,  range: '38.0 to 46.0 °C',    label: 'Very strong heat stress', color: '#f46d43' },
      { min: 46,             range: '≥ 46.0 °C',          label: 'Extreme heat stress',     color: '#d73027' }
    ]
  },
  at: {
    title: "Apparent Temperature (AT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/forecast/at/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 26.7],  '#e0f3f8',
        ['<', ['band', 1], 32.2],  '#ffff00',
        ['<', ['band', 1], 39.4],  '#ffb300',
        ['<', ['band', 1], 51.7],  '#ff6600',
        '#d73027'
      ]
    },
    legend: [
      {             max: 26.7, range: '< 26.7 °C',        label: 'Normal / No Stress', color: '#e0f3f8' },
      { min: 26.7,  max: 32.2, range: '26.7 to 32.2 °C',  label: 'Caution',            color: '#ffff00' },
      { min: 32.2,  max: 39.4, range: '32.2 to 39.4 °C',  label: 'Extreme Caution',    color: '#ffb300' },
      { min: 39.4,  max: 51.7, range: '39.4 to 51.7 °C',  label: 'Danger',             color: '#ff6600' },
      { min: 51.7,             range: '≥ 51.7 °C',        label: 'Extreme Danger',     color: '#d73027' }
    ]
  },
  wbgt: {
    title: "Wet Bulb Globe Temperature (WBGT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/forecast/wbgt/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], 26.7],  '#e0f3f8',
        ['<', ['band', 1], 29.4],  '#008000',
        ['<', ['band', 1], 31.1],  '#ffff00',
        ['<', ['band', 1], 32.2],  '#ff0000',
        '#000000'
      ]
    },
    legend: [
      {             max: 26.7, range: '< 26.7 °C',        label: 'Normal Conditions',     color: '#e0f3f8' },
      { min: 26.7,  max: 29.4, range: '26.7 to 29.4 °C',  label: 'Green Flag Condition',  color: '#008000' },
      { min: 29.4,  max: 31.1, range: '29.4 to 31.1 °C',  label: 'Yellow Flag Condition', color: '#ffff00' },
      { min: 31.1,  max: 32.2, range: '31.1 to 32.2 °C',  label: 'Red Flag Condition',    color: '#ff0000' },
      { min: 32.2,             range: '≥ 32.2 °C',        label: 'Black Flag Condition',  color: '#000000' }
    ]
  },
  wct: {
    title: "Wind Chill Temperature (WCT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/forecast/wct/index.json",
    style: {
      color: [
        'case',
        NODATA_CASE, 'transparent',
        ['<', ['band', 1], -45.0], '#8b008b',
        ['<', ['band', 1], -40.0], '#ff00ff',
        ['<', ['band', 1], -35.0], '#7f00ff',
        ['<', ['band', 1], -27.0], '#00008b',
        ['<', ['band', 1], -10.0], '#0000ff',
        '#add8e6'
      ]
    },
    legend: [
      { min: -10,            range: '≥ -10.0 °C',         label: 'Low cold stress',                        color: '#add8e6' },
      { min: -27, max: -10,  range: '-27.0 to -10.0 °C',  label: 'Uncomfortable cold',                     color: '#0000ff' },
      { min: -35, max: -27,  range: '-35.0 to -27.0 °C',  label: 'Risk of frostbite / prolonged exposure', color: '#00008b' },
      { min: -40, max: -35,  range: '-40.0 to -35.0 °C',  label: 'Frostbite possible in 10–15 min',        color: '#7f00ff' },
      { min: -45, max: -40,  range: '-45.0 to -40.0 °C',  label: 'Frostbite possible in < 10 min',         color: '#ff00ff' },
      {           max: -45,  range: '< -45.0 °C',         label: 'Frostbite within minutes',               color: '#8b008b' }
    ]
  }
};

// Application State Context
let activeVariable = 'utci';
let timeline = [];
let forecastRun = null;
let currentIndex = 0;
let playTimer = null;

const geoTiffCache = new Map();
const MAX_CACHE_SIZE = 48;

// While the slider is dragged the hour is loaded only when it stops: a source for every 'input' event starts dozens
// of parallel Range requests, and the browser rejects some of them
const SLIDER_DEBOUNCE_MS = 150;
const RETRY_DELAY_MS = 1000;
let sliderTimer = null;
// Time of the hour already retried after a load error (one retry per hour)
let retriedTime = null;

// DOM Selectors
const indexSelector = document.getElementById('indexSelector');
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

// Initialize base OpenLayers layer placeholder, centered on Italy (WRF domain d03)
const mapLayer = new WebGLTileLayer({ style: CONFIG[activeVariable].style, opacity: opacitySlider.value / 100 });
const map = new OLMap({
  target: 'map',
  layers: [new TileLayer({ source: new OSM() }), mapLayer],
  view: new View({ projection: 'EPSG:3857', center: [1391493, 5160979], zoom: 6 })
});

// Load the forecast of the selected index
async function loadIndicator(variableKey) {
  try {
    stopAnimation();
    activeVariable = variableKey;
    runLabel.textContent = "Loading forecast...";

    retriedTime = null;
    const data = await fetchIndex(variableKey);
    if (activeVariable !== variableKey) return;
    setTimeline(variableKey, data);

    mapLayer.setStyle(CONFIG[variableKey].style);
    legendTitle.textContent = CONFIG[variableKey].title;
    buildLegend(variableKey);

    updateMap(findClosestTo(Date.now()));
  } catch (e) {
    runLabel.textContent = `Error loading indicator: ${variableKey}`;
    console.error(e);
  }
}

async function fetchIndex(variableKey) {
  const response = await fetch(CONFIG[variableKey].url, { cache: 'no-cache' });
  if (!response.ok) throw new Error(`HTTP ${response.status} loading ${CONFIG[variableKey].url}`);
  return response.json();
}

function setTimeline(variableKey, data) {
  // The COGs are in the same folder of the index.json
  const folder = CONFIG[variableKey].url.replace('index.json', '');
  timeline = data.files
    .map(file => ({ time: file.time, leadTimeHour: file.leadTimeHour, url: folder + file.filename }))
    .sort((a, b) => new Date(a.time) - new Date(b.time));

  forecastRun = data.forecastReferenceTime;
  runLabel.textContent = `Forecast run: ${formatDate(forecastRun)} (CIMA WRF)`;
  slider.max = timeline.length - 1;
}

// The task may publish a new run while the page is open, deleting the hours of the old one: reload the index.json and,
// if the run has changed, rebuild the timeline keeping the selected time
async function refreshRun(variableKey) {
  try {
    const data = await fetchIndex(variableKey);
    if (activeVariable !== variableKey || data.forecastReferenceTime === forecastRun) return;
    const selectedTime = new Date(timeline[currentIndex].time).getTime();
    setTimeline(variableKey, data);
    geoTiffCache.clear();
    currentIndex = findClosestTo(selectedTime);
  } catch (e) {
    console.error(e);
  }
}

// Index of the forecast hour closest to the given time (the map opens on the hour closest to the current time)
function findClosestTo(time) {
  let closest = 0;
  timeline.forEach((file, index) => {
    if (Math.abs(new Date(file.time) - time) < Math.abs(new Date(timeline[closest].time) - time)) {
      closest = index;
    }
  });
  return closest;
}

function updateMap(index) {
  if (!timeline[index]) return;
  currentIndex = index;
  const file = timeline[index];

  if (file.time !== retriedTime) retriedTime = null;

  mapLayer.setSource(getOrCreateSource(file.url));
  showTime(index);

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
  // A failed source must not stay in the cache, otherwise its hour stays empty until the page is reloaded
  source.on('change', () => {
    if (source.getState() !== 'error') return;
    if (geoTiffCache.get(fileUrl) === source) geoTiffCache.delete(fileUrl);
    if (mapLayer.getSource() === source) handleLoadError(source);
  });
  if (geoTiffCache.size >= MAX_CACHE_SIZE) geoTiffCache.delete(geoTiffCache.keys().next().value);
  geoTiffCache.set(fileUrl, source);
  return source;
}

function showTime(index) {
  const file = timeline[index];
  slider.value = index;
  timeLabel.textContent = `${formatDate(file.time)} (+${file.leadTimeHour}h)`;
}

// The hour shown failed to load: check whether a new run has been published, then retry once
async function handleLoadError(source) {
  const variableKey = activeVariable;
  const file = timeline[currentIndex];
  const retry = retriedTime !== file.time;
  const message = retry ? 'Unable to load this hour – retrying' : 'Unable to load this hour';
  timeLabel.textContent = `${formatDate(file.time)} (+${file.leadTimeHour}h) – ${message}`;
  pixelValue.textContent = message;
  if (!retry) return;
  retriedTime = file.time;

  await refreshRun(variableKey);
  setTimeout(() => {
    // Retry only if the user is still on the failed hour
    if (activeVariable !== variableKey || mapLayer.getSource() !== source) return;
    updateMap(currentIndex);
  }, RETRY_DELAY_MS);
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
  clearTimeout(sliderTimer);
  playTimer = null;
  playBtn.innerHTML = '&#9654; Play';
}

// --- Event Triggers ---
indexSelector.addEventListener('change', (e) => {
  loadIndicator(e.target.value);
});

// While dragging only the label follows the slider: the hour is loaded when the slider stops or is released
slider.addEventListener('input', (e) => {
  stopAnimation();
  const index = parseInt(e.target.value, 10);
  showTime(index);
  sliderTimer = setTimeout(() => updateMap(index), SLIDER_DEBOUNCE_MS);
});

slider.addEventListener('change', (e) => {
  clearTimeout(sliderTimer);
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

// Value under the mouse: getData returns [band 1, alpha band]
map.on('pointermove', (evt) => {
  if (evt.dragging) return;

  const data = mapLayer.getData(evt.pixel);
  if (!data || data[1] === 0) {
    pixelValue.textContent = 'No data';
    return;
  }

  const value = data[0];
  const item = CONFIG[activeVariable].legend.find(item =>
    (item.min === undefined || value >= item.min) && (item.max === undefined || value < item.max));
  pixelValue.innerHTML = `<strong>${value.toFixed(1)} °C</strong> — ${item.label}`;
});

// Kickstart with the UTCI forecast
loadIndicator('utci');
