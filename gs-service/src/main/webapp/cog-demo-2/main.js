import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';


// Global Data Matrix: Curated European Historical Events (Jan 2023 - July 2025)
const CONFIG = {
  wct: {
    title: "Wind Chill Temperature (WCT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/wct/index.json",
    style: {
      color: [
        'case',
        ['<', ['band', 1], -45.0], '#8b008b',
        ['<', ['band', 1], -40.0], '#ff00ff',
        ['<', ['band', 1], -35.0], '#7f00ff',
        ['<', ['band', 1], -27.0], '#00008b',
        ['<', ['band', 1], -10.0], '#0000ff',
        '#add8e6'
      ],
      opacity: 1.0
    },
    legend: [
      { range: '≥ -10.0 °C', label: 'Low cold stress', color: '#add8e6' },
      { range: '-27.0 to -10.0 °C', label: 'Uncomfortable cold', color: '#0000ff' },
      { range: '-35.0 to -27.0 °C', label: 'Risk of frostbite / prolonged exposure', color: '#00008b' },
      { range: '-40.0 to -35.0 °C', label: 'Frostbite possible in 10–15 min', color: '#7f00ff' },
      { range: '-45.0 to -40.0 °C', label: 'Frostbite possible in < 10 min', color: '#ff00ff' },
      { range: '< -45.0 °C', label: 'Frostbite within minutes', color: '#8b008b' }
    ],
    events: [
      { value: "2024-01-05", text: "Jan 2024: Historic Nordic Deep Freeze (-43°C Ambient)" },
      { value: "2023-02-05", text: "Feb 2023: Southern European / Aegean Cold Wave" },
      { value: "2023-11-28", text: "Nov 2023: Alpine Blizzard & Early-Winter Baltic Gale" },
      { value: "2025-01-18", text: "Jan 2025: Eastern European Continental Wind-Chill Surge" }
    ]
  },
  at: {
    title: "Apparent Temperature (AT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/wct/index.json",
    style: {
      color: [
        'case',
        ['<', ['band', 1], 26.7],  '#e0f3f8',
        ['<', ['band', 1], 32.2],  '#ffff00',
        ['<', ['band', 1], 39.4],  '#ffb300',
        ['<', ['band', 1], 51.7],  '#ff6600',
        '#d73027'
      ],
      opacity: 1.0
    },
    legend: [
      { range: '< 26.7 °C', label: 'Normal / No Stress', color: '#e0f3f8' },
      { range: '26.7 to 32.2 °C', label: 'Caution', color: '#ffff00' },
      { range: '32.2 to 39.4 °C', label: 'Extreme Caution', color: '#ffb300' },
      { range: '39.4 to 51.1 °C', label: 'Danger', color: '#ff6600' },
      { range: '≥ 51.7 °C', label: 'Extreme Danger', color: '#d73027' }
    ],
    events: [
      { value: "2023-07-18", text: "July 2023: Mediterranean 'Charon' Heat Dome" },
      { value: "2024-08-12", text: "Aug 2024: Iberian Peninsula Humid Apparent Heat Spike" },
      { value: "2023-08-23", text: "Aug 2023: Western Europe Late-Summer Heat Wave" }
    ]
  },
  wbgt: {
    title: "Wet Bulb Globe Temperature (WBGT)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/wct/index.json",
    style: {
      color: [
        'case',
        ['<', ['band', 1], 26.7],  '#e0f3f8',
        ['<', ['band', 1], 29.4],  '#008000',
        ['<', ['band', 1], 31.1],  '#ffff00',
        ['<', ['band', 1], 32.2],  '#ff0000',
        '#000000'
      ],
      opacity: 1.0
    },
    legend: [
      { range: '< 26.7 °C', label: 'Normal Conditions', color: '#e0f3f8' },
      { range: '26.7 to 29.4 °C', label: 'Green Flag Condition', color: '#008000' },
      { range: '29.4 to 31.1 °C', label: 'Yellow Flag Condition', color: '#ffff00' },
      { range: '31.1 to 32.2 °C', label: 'Red Flag Condition', color: '#ff0000' },
      { range: '≥ 32.2 °C', label: 'Black Flag Condition', color: '#000000' }
    ],
    events: [
      { value: "2023-07-24", text: "July 2023: Greek Islands Critical Radiant Exposure (Black Flag)" },
      { value: "2024-07-11", text: "July 2024: Balkan Peninsula High-Humidity Solar Maxima" },
      { value: "2024-08-05", text: "Aug 2024: Italian/Adriatic Coastline Labor Safety Warning" }
    ]
  },
  utci: {
    title: "Universal Thermal Climate Index (UTCI)",
    url: "https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/CIMA/wct/index.json",
    style: {
      color: [
        'case',
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
      ],
      opacity: 1.0
    },
    legend: [
      { range: '< -40.0 °C', label: 'Extreme cold stress', color: '#000066' },
      { range: '-40.0 to -27.0 °C', label: 'Very strong cold stress', color: '#0000ff' },
      { range: '-27.0 to -13.0 °C', label: 'Strong cold stress', color: '#007fff' },
      { range: '-13.0 to 0.0 °C', label: 'Moderate cold stress', color: '#add8e6' },
      { range: '0.0 to 9.0 °C', label: 'Slight cold stress', color: '#e0f3f8' },
      { range: '9.0 to 26.0 °C', label: 'No thermal stress', color: '#2ca25f' },
      { range: '26.0 to 32.0 °C', label: 'Moderate heat stress', color: '#fee090' },
      { range: '32.0 to 38.0 °C', label: 'Strong heat stress', color: '#fdae61' },
      { range: '38.0 to 46.0 °C', label: 'Very strong heat stress', color: '#f46d43' },
      { range: '≥ 46.0 °C', label: 'Extreme heat stress', color: '#d73027' }
    ],
    events: [
      { value: "2023-08-22", text: "Aug 2023: Alpine/Central Euro High-Altitude Heat Load" },
      { value: "2024-01-08", text: "Jan 2024: Fenno-Scandian Extreme Cold Biometeorological Load" },
      { value: "2024-07-18", text: "July 2024: Eastern European Extended Extreme Heat Stress" }
    ]
  }
};

// Application State Context
let activeVariable = 'wct';
let timeline = [];
let currentDayHours = []; // Holds file indices matching the current active day layout
let selectedDateStr = "";
let currentIndex = 0;

const geoTiffCache = new Map();
const MAX_CACHE_SIZE = 48;

// DOM Selectors
const timeLabel = document.getElementById('timeMain');
const datePicker = document.getElementById('datePicker');
const eventSelector = document.getElementById('eventSelector');
const hourSelector = document.getElementById('hourSelector');
const layerSelector = document.getElementById('layerSelector');
const legendTitle = document.getElementById('legendTitle');

// Initialize base OpenLayers layer placeholder
const mapLayer = new WebGLTileLayer({ style: CONFIG[activeVariable].style });
const map = new OLMap({
  target: 'map',
  layers: [new TileLayer({ source: new OSM() }), mapLayer],
  view: new View({ projection: 'EPSG:3857', center: [1113194, 7628367], zoom: 4 })
});

// Load Active Target Pipeline Data
async function loadIndicator(variableKey) {
  try {
    activeVariable = variableKey;
    timeLabel.textContent = "Loading index catalog...";

    const response = await fetch(CONFIG[variableKey].url);
    const data = await response.json();

    timeline = data.files.sort((a, b) => new Date(a.time) - new Date(b.time));

    if (timeline.length > 0) {
      datePicker.min = timeline[0].time.split('T')[0];
      datePicker.max = timeline[timeline.length - 1].time.split('T')[0];

      mapLayer.setStyle(CONFIG[variableKey].style);
      legendTitle.textContent = CONFIG[variableKey].title;

      buildDropdownEvents(variableKey);
      buildLegend(variableKey);

      // Handle default loading to first valid frame entry parameters
      const initialDate = timeline[0].time.split('T')[0];
      handleDateChange(initialDate);
    }
  } catch (e) {
    timeLabel.textContent = `Error mapping indicators: ${variableKey}`;
    console.error(e);
  }
}

// Logic dealing with dynamic date changes and rewriting the hour lists
function handleDateChange(dateString) {
  selectedDateStr = dateString;
  if (datePicker) datePicker.value = dateString;

  // Filter out the true indices from global workspace array matching the date
  currentDayHours = [];
  timeline.forEach((file, index) => {
    if (file.time.startsWith(dateString)) {
      currentDayHours.push({ globalIndex: index, time: file.time });
    }
  });

  // Re-generate the hour dropdown selector choices
  if (currentDayHours.length > 0) {
    hourSelector.innerHTML = currentDayHours.map(item => {
      const parsedHour = new Date(item.time).getUTCHours().toString().padStart(2, '0') + ":00";
      return `<option value="${item.globalIndex}">${parsedHour}</option>`;
    }).join('');

    // Default update map frame render straight to the first hour step available
    updateMap(currentDayHours[0].globalIndex);
  } else {
    hourSelector.innerHTML = `<option value="">No hours</option>`;
  }
}

function updateMap(index) {
  if (!timeline[index]) return;
  const file = timeline[index];

  mapLayer.setSource(getOrCreateSource(file.url));

  // Visual text formatting updates string parser elements
  const dateObj = new Date(file.time);
  timeLabel.textContent = dateObj.toLocaleString('en-US', {
    weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit', timeZoneName: 'short', timeZone: 'UTC'
  });

  // Update hour selection alignment dropdown box position accurately
  if (hourSelector) {
    hourSelector.value = index;
  }
}

function getOrCreateSource(fileUrl) {
  if (geoTiffCache.has(fileUrl)) return geoTiffCache.get(fileUrl);
  const source = new GeoTIFF({ sources: [{ url: fileUrl }], normalize: false, transition: 0 });
  if (geoTiffCache.size >= MAX_CACHE_SIZE) geoTiffCache.delete(geoTiffCache.keys().next().value);
  geoTiffCache.set(fileUrl, source);
  return source;
}

function buildDropdownEvents(variableKey) {
  const events = CONFIG[variableKey].events;
  let html = `<option value="">-- Choose a Significant Outbreak --</option>`;
  html += events.map(ev => `<option value="${ev.value}">${ev.text}</option>`).join('');
  eventSelector.innerHTML = html;
}

function buildLegend(variableKey) {
  const legendDiv = document.getElementById('legend');
  legendDiv.innerHTML = CONFIG[variableKey].legend.map(item => `
    <div class="legend-item" style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
      <div class="legend-color" style="background:${item.color}; width: 18px; height: 18px; border: 1px solid #ccc; border-radius: 2px; flex-shrink: 0;"></div>
      <div style="font-size: 12px; color: #333;"><strong>${item.range}</strong> — ${item.label}</div>
    </div>
  `).join('');
}

// --- Event Triggers ---
if (layerSelector) {
  layerSelector.addEventListener('change', (e) => {
    if (e.target.value) loadIndicator(e.target.value);
  });
}

if (datePicker) {
  datePicker.addEventListener('change', (e) => {
    if (e.target.value) {
      if (eventSelector) eventSelector.value = "";
      handleDateChange(e.target.value); // Triggers initial hour map render
    }
  });
}

if (eventSelector) {
  eventSelector.addEventListener('change', (e) => {
    if (e.target.value) {
      if (datePicker) datePicker.value = e.target.value;
      handleDateChange(e.target.value); // Triggers initial hour map render
    }
  });
}

if (hourSelector) {
  hourSelector.addEventListener('change', (e) => {
    const targetIndex = parseInt(e.target.value, 10);
    // Only update if the user manually changed the hour to a different index
    if (!isNaN(targetIndex) && targetIndex !== currentIndex) {
      updateMap(targetIndex);
    }
  });
}

// Kickstart deployment targeting WCT indicator patterns
loadIndicator('wct');