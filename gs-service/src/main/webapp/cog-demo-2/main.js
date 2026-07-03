import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';

// 1. Define Color Ramp (Cold = Extreme Purple/Blue Danger)
const wctStyle = {
  color: [
    'case',
    ['<=', ['band', 1], -45.0], '#4d004d', // Extreme Frostbite Hazard
    ['<=', ['band', 1], -25.0], '#313695', // Severe Cold
    ['<=', ['band', 1], -10.0], '#4575b4', // Very Cold
    ['<=', ['band', 1], 0.0],   '#74add1', // Freezing Threshold
    ['<=', ['band', 1], 10.0],  '#abd9e9', // Cool
    ['<=', ['band', 1], 20.0],  '#fe9929', // Mild
    '#cc4c02'                              // Warm
  ],
  opacity: 1.0
};

// 2. Map Elements setup
const wctLayer = new WebGLTileLayer({ style: wctStyle });
const map = new OLMap({
  target: 'map',
  layers: [new TileLayer({ source: new OSM() }), wctLayer],
  view: new View({ projection: 'EPSG:3857', center: [1113194, 7628367], zoom: 4 })
});

// 3. Application State Context
let timeline = [];
let currentIndex = 0;

// Persistent RAM Source Cache Map
const geoTiffCache = new Map();
const MAX_CACHE_SIZE = 48; // Keeps up to 2 full days of sequential hourly files hot in memory

// DOM Selectors
const timeLabel = document.getElementById('timeMain');
const datePicker = document.getElementById('datePicker');
const eventSelector = document.getElementById('eventSelector');

// 4. Load Data Pipeline
async function initData() {
  try {
    const response = await fetch('https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/wct/index.json');
    const data = await response.json();

    // Sort chronologically just in case entries are mixed up
    timeline = data.files.sort((a, b) => new Date(a.time) - new Date(b.time));

    // Constrain HTML Date picker to data calendar limits
    if (timeline.length > 0) {
      datePicker.min = timeline[0].time.split('T')[0];
      datePicker.max = timeline[timeline.length - 1].time.split('T')[0];

      // A. Immediate First Load: Render the first index right away
      updateMap(0);

      // B. Proactive 2-Day Preloading Engine: Warm up the upcoming 48 hours of files
      console.log("Warming up look-ahead cache for the next 48 hours...");
      for (let i = 1; i <= 48; i++) {
        if (timeline[i]) {
          getOrCreateSource(timeline[i].url);
        }
      }
    }
    buildLegend();
  } catch (e) {
    timeLabel.textContent = "Error loading timeline data.";
    console.error("Timeline error:", e);
  }
}

// 5. High-Performance Source Management (Cache Layer)
function getOrCreateSource(fileUrl) {
  // If already compiled, return instantly from RAM (Bypasses network connection overhead)
  if (geoTiffCache.has(fileUrl)) {
    return geoTiffCache.get(fileUrl);
  }

  // Create clean standalone source structure
  const source = new GeoTIFF({
    sources: [{ url: fileUrl }],
    normalize: false,
    transition: 0 // Drops opacity fade timers to maximize rendering speed
  });

  // Manage cache memory footprint dynamically (LRU Eviction)
  if (geoTiffCache.size >= MAX_CACHE_SIZE) {
    const firstKey = geoTiffCache.keys().next().value;
    geoTiffCache.delete(firstKey);
  }

  geoTiffCache.set(fileUrl, source);
  return source;
}

// 6. Updated Core Update Function
function updateMap(index) {
  if (!timeline[index]) return;

  currentIndex = index;
  const file = timeline[index];

  // Instantly hot-swap the layer source via local RAM cache
  const activeSource = getOrCreateSource(file.url);
  wctLayer.setSource(activeSource);

  // Keep the Calendar Date selection aligned as dates are chosen
  if (datePicker) {
    datePicker.value = file.time.split('T')[0];
  }

  // LOOK-AHEAD BACKGROUND PREFETCHING (Pre-loads browser file network headers)
  // While rendering the current frame, download the metadata bytes for the next 4 files
  for (let i = 1; i <= 4; i++) {
    const nextFile = timeline[index + i];
    if (nextFile) {
      const prefetcher = document.createElement('link');
      prefetcher.rel = 'prefetch';
      prefetcher.href = nextFile.url;
      prefetcher.as = 'fetch';

      document.head.appendChild(prefetcher);
      setTimeout(() => prefetcher.remove(), 2000); // Strip DOM element safely
    }
  }

  // Visual date text parsing adjustments
  const dateObj = new Date(file.time);
  timeLabel.textContent = dateObj.toLocaleString('en-US', {
    weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit', timeZoneName: 'short', timeZone: 'UTC'
  });
}

// 7. Legend Generator
function buildLegend() {
  const legendDiv = document.getElementById('legend');
  const schema = [
    { range: '≤ -45.0 °C', label: 'Extreme Hazard (Frostbite < 10m)', color: '#4d004d' },
    { range: '-45.0 to -25.0 °C', label: 'High Danger', color: '#313695' },
    { range: '-25.0 to -10.0 °C', label: 'Moderate Cold', color: '#4575b4' },
    { range: '-10.0 to 0.0 °C', label: 'Freezing Zone', color: '#74add1' },
    { range: '0.0 to 10.0 °C', label: 'Cool / Chilly', color: '#abd9e9' },
    { range: '10.0 to 20.0 °C', label: 'Mild', color: '#fe9929' },
    { range: '> 20.0 °C', label: 'Warm', color: '#cc4c02' }
  ];

  legendDiv.innerHTML = schema.map(item => `
    <div class="legend-item">
      <div class="legend-color" style="background:${item.color};"></div>
      <div><strong>${item.range}</strong> — ${item.label}</div>
    </div>
  `).join('');
}

// 8. Interaction Event Listeners
if (datePicker) {
  datePicker.addEventListener('change', (e) => {
    const selectedDate = e.target.value;
    if (!selectedDate) return;

    if (eventSelector) eventSelector.value = ""; // Avoid selector mismatch text conflicts

    const matchedIndex = timeline.findIndex(file => file.time.startsWith(selectedDate));
    if (matchedIndex !== -1) {
      updateMap(matchedIndex);
    }
  });
}

if (eventSelector) {
  eventSelector.addEventListener('change', (e) => {
    const targetDate = e.target.value;
    if (!targetDate) return;

    const eventIndex = timeline.findIndex(file => file.time.startsWith(targetDate));
    if (eventIndex !== -1) {
      updateMap(eventIndex);
    }
  });
}

// Run application
initData();