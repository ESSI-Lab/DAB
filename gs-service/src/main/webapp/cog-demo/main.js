import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';


/* =========================================================
   2) VARIABLE/LAYER and Legends configuration
   ---------------------------------------------------------
   - folderUrl: S3 folder
   - filePrefix: real file prefix
   - availableHours: time available
   - minDate: first timestamp available
   - style: Openlayers
   ========================================================= */


const temperatureStyle = {
  color: [
    'case',

    ['<=', ['band', 1], -20], '#313695',
    ['<=', ['band', 1], -13], '#4575b4',
    ['<=', ['band', 1], -6],  '#74add1',
    ['<=', ['band', 1], 1],   '#abd9e9',
    ['<=', ['band', 1], 8],   '#e0f3f8',
    ['<=', ['band', 1], 15],  '#ffffbf',
    ['<=', ['band', 1], 22],  '#fee090',
    ['<=', ['band', 1], 29],  '#fdae61',
    ['<=', ['band', 1], 36],  '#f46d43',
    ['<=', ['band', 1], 43],  '#d73027',
    '#a50026' // > 43
  ],
  opacity: 1.0
};


const shiweStyle = {
  color: [
    'case',
        ['<', ['band', 1], 0.5], '#006837',
        ['<', ['band', 1], 1.0], '#31a354',
        ['<', ['band', 1], 1.5], '#78c679',
        ['<', ['band', 1], 2.0], '#c2e699',
        ['<', ['band', 1], 2.5], '#ffffb2',
        ['<', ['band', 1], 3.0], '#fecc5c',
        ['<', ['band', 1], 3.5], '#fd8d3c',
        ['<', ['band', 1], 4.0], '#f03b20',
        ['<', ['band', 1], 4.5], '#bd0026',
        '#800026'
  ],
  opacity: 1.0
};

const humidityStyle = {
  color: [
    'case',

    ['<', ['band', 1], 0], 'transparent',
    ['<=', ['band', 1], 10], '#ffffcc',  // 0–10%
    ['<=', ['band', 1], 20], '#ffeda0',  // 10–20%
    ['<=', ['band', 1], 30], '#fed976',  // 20–30%
    ['<=', ['band', 1], 40], '#feb24c',  // 30–40%
    ['<=', ['band', 1], 50], '#fd8d3c',  // 40–50%
    ['<=', ['band', 1], 60], '#f03b20',  // 50–60%
    ['<=', ['band', 1], 70], '#bd0026',  // 60–70%
    ['<=', ['band', 1], 80], '#9ecae1',  // 70–80%
    ['<=', ['band', 1], 90], '#4292c6',  // 80–90%
    ['<=', ['band', 1], 100], '#08519c', // 90–100%
    '#08519c'
  ],
  opacity: 1.0
};

const utciStyle = {
  color: [
    'case',
        ['<=', ['band', 1], -50], '#08306b',
        ['<=', ['band', 1], -40], '#2171b5',
        ['<=', ['band', 1], -30], '#6baed6',
        ['<=', ['band', 1], -20], '#bdd7e7',
        ['<=', ['band', 1], -10], '#eff3ff',
        ['<=', ['band', 1], 0], '#ffffbf',
        ['<=', ['band', 1], 10], '#fee090',
        ['<=', ['band', 1], 20], '#fdae61',
        ['<=', ['band', 1], 30], '#f46d43',
        ['<=', ['band', 1], 40], '#d73027',
        '#a50026'
  ],
  opacity: 1.0
};


const legends = {
      shiwe: {
        title: 'SHIWE (Synthetic Healthiness Index of Workplace Exposure)',
        items: [
          { range: '0.0 – 0.5', label: 'No risk',      color: '#006837' },
          { range: '0.5 – 1.0', label: 'Very low',     color: '#31a354' },
          { range: '1.0 – 1.5', label: 'Low',          color: '#78c679' },
          { range: '1.5 – 2.0', label: 'Slight',       color: '#c2e699' },
          { range: '2.0 – 2.5', label: 'Moderate',     color: '#ffffb2' },
          { range: '2.5 – 3.0', label: 'High',         color: '#fecc5c' },
          { range: '3.0 – 3.5', label: 'Very high',    color: '#fd8d3c' },
          { range: '3.5 – 4.0', label: 'Severe',       color: '#f03b20' },
          { range: '4.0 – 4.5', label: 'Extreme',      color: '#bd0026' },
          { range: '4.5 – 5.0', label: 'Critical',     color: '#800026' }
        ]
      },
    '2t': {
        title: 'Air Temperature at 2 m (°C)',
        items: [
          { range: '≤ −20', label: 'Extreme cold', color: '#313695' },
          { range: '−20 – −13', label: 'Very cold', color: '#4575b4' },
          { range: '−13 – −6', label: 'Cold',  color: '#74add1' },
          { range: '−6 – 1',  label: 'Near freezing',  color: '#abd9e9' },
          { range: '1 – 8',  label: 'Cool',   color: '#e0f3f8' },
          { range: '8 – 15', label: 'Mild',   color: '#ffffbf' },
          { range: '15 – 22', label: 'Warm',  color: '#fee090' },
          { range: '22 – 29', label: 'Hot',  color: '#fdae61' },
          { range: '29 – 36', label: 'Very hot',  color: '#f46d43' },
          { range: '36 - 43', label: 'Extreme heat',     color: '#d73027' },
          { range: '>= 43', label: 'Exceptional heat',     color: '#d73027' },
        ]
      },

      utci: {
        title: 'UTCI (°C)',
        items: [
          { range: '≤ −50', label: 'Extreme cold stress',color: '#08306b' },
          { range: '-50 - −40', label: 'Very strong cold stress',color: '#2171b5' },
          { range: '-40 - −30', label: 'Strong cold stress',color: '#6baed6' },
          { range: '−30 – −20', label: 'Moderate cold stress',color: '#bdd7e7' },
          { range: '−20 – 10', label: 'Slight cold stress',color: '#eff3ff' },
          { range: '-10 – 0', label: 'No thermal stress', color: '#ffffbf' },
          { range: '0 – 10', label: 'Moderate heat stress', color: '#fee090' },
          { range: '10 – 20', label: 'Strong heat stress', color: '#fdae61' },
          { range: '20 – 30', label: 'Very strong heat stress',color: '#f46d43' },
          { range: '30 – 40', label: 'Extreme heat stress',color: '#d73027' },
          { range: '> 40', label: 'Exceptional heat stress',color: '#a50026' }
        ]
      },

      '2r': {
        title: 'Relative Humidity (%)',
        items: [
          { range: '0 – 10', label: 'Extremely dry',color: '#ffffcc' },
          { range: '10 – 20', label: 'Very dry', color: '#ffeda0' },
          { range: '20 – 30', label: 'Dry',color: '#fed976' },
          { range: '30 – 40', label: 'Slightly dry',color: '#feb24c' },
          { range: '40 – 50', label: 'Comfortable',color: '#fd8d3c' },
          { range: '50 – 60', label: 'Humid',color: '#f03b20' },
          { range: '60 – 70', label: 'Very humid',color: '#bd0026' },
          { range: '70 – 80', label: 'Oppressive',color: '#9ecae1' },
          { range: '80 – 90', label: 'Extremely humid',color: '#4292c6' },
          { range: '90 – 100', label: 'Near saturation',color: '#08519c' }
        ]
      }

};






const variables = {
  shiwe: {
    folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/shiwe/',
    filePrefix: 'shiwe',
    visible: true,
    style: shiweStyle
  },

  '2t': {
    folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/2t/',
    filePrefix: '2t', //
    visible: false,
    style: temperatureStyle
  },

    utci: {
      folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/utci/',
      filePrefix: 'utci', //
      visible: false,
      style: utciStyle
    },

    '2r': {
          folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/2r/',
          filePrefix: '2r', //
          visible: false,
          style: humidityStyle
        }

};

let activeLegendLayer = null;

const legendDiv = document.getElementById('legend');

function updateLegend(layerName) {
  const legend = legends[layerName];
  if (!legend) {
    return;
  }

  activeLegendLayer = layerName;

  legendDiv.innerHTML = `
    <div class="legend-title">${legend.title}</div>
    ${legend.items.map(item => `
      <div class="legend-item">
        <div class="legend-color" style="background:${item.color}"></div>
        <div>${item.range} (${item.label})</div>
      </div>
    `).join('')}
  `;
}

let currentIndex = 0;

const loadedData = {}; // Store info from index.json
let timelineMaster = [];

async function initData() {
  const promises = Object.entries(variables).map(async ([name, cfg]) => {
    try {
      const response = await fetch(`${cfg.folderUrl}index.json`);
      const data = await response.json();
      loadedData[name] = data;
    } catch (e) {
      console.error(`error loading index.json for ${name}:`, e);
      loadedData[name] = { files: [] };
    }
  });

  await Promise.all(promises);
  buildMasterTimeline();
  initSlider();
  updateFromIndex(0);
}



/* =========================================================
   3) UTILITY
   ========================================================= */


function hoursBetween(a, b) {
  return Math.round((b - a) / 36e5);
}

function formatUtcLabel(dateInput) {

const date = (dateInput instanceof Date) ? dateInput : new Date(dateInput);
  const yyyy = date.getUTCFullYear();
  const mm = String(date.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(date.getUTCDate()).padStart(2, '0');
  const hh = String(date.getUTCHours()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:00 UTC`;
}




function buildMasterTimeline() {
  const timeSet = new Set();
  Object.values(loadedData).forEach(varData => {
    varData.files.forEach(f => timeSet.add(f.time));
  });
  timelineMaster = Array.from(timeSet).sort();
}




function createSource(url) {
  return new GeoTIFF({
    sources: [{ url }],
    normalize: false
  });
}


function pad2(n) {
  return String(n).padStart(2, '0');
}



/* =========================================================
   4) SAFE TEMPORAL CANDIDATE GENERATION
   ---------------------------------------------------------
   If the exact file does not exist:
   - try the closest available time
   - first within the same day
   - then, if necessary, the previous / next day
   - always respecting minDate and maxDate
   ========================================================= */



/* =========================================================
   5) CHECK URL AVAILABILITY (with cache)
   ---------------------------------------------------------
    Uses HEAD to avoid downloading the file.
   If the S3 CORS configuration allows HEAD requests
   (as it should), it works correctly.
   ========================================================= */



const urlExists = (() => {
  const cache = new Map();

  return async function urlExists(url) {
    if (cache.has(url)) {
      return cache.get(url);
    }

    try {
      const res = await fetch(url, { method: 'HEAD' });
      const ok = res.ok;
      cache.set(url, ok);
      return ok;
    } catch {
      cache.set(url, false);
      return false;
    }
  };
})();




/* =========================================================
   7) LAYER
   ========================================================= */

const layers = {};

for (const [name, cfg] of Object.entries(variables)) {
  layers[name] = new WebGLTileLayer({
    style: cfg.style,
    opacity: cfg.opacity,
    visible: cfg.visible
  });
}

/* =========================================================
   8) BASE MAP and OLMAP
   ========================================================= */

const baseMap = new TileLayer({
  source: new OSM()
});

const map = new OLMap({
  target: 'map',
  layers: [baseMap, ...Object.values(layers)],
  view: new View({
    projection: 'EPSG:3857',
    center: [1113194, 7628367],
    zoom: 4
  })
});


const mapEl = map.getTargetElement();

mapEl.appendChild(document.getElementById('layerControls'));
mapEl.appendChild(document.getElementById('legend'));


/* =========================================================
   9) TIME SLIDER
   ========================================================= */


const slider = document.getElementById('forecastSlider');

slider.min = 0;
slider.max = timelineMaster.length - 1;
slider.step = 1;
slider.value = currentIndex;

const timeMain = document.getElementById('timeMain');
const timeLayers = document.getElementById('timeLayers');

let updateRequestId = 0;

async function updateTimeFromDate(isoDateString) {
  timeMain.textContent = isoDateString.replace('T', ' · ').replace('.000Z', ' UTC');

  // Update global
  currentIndex = timelineMaster.indexOf(isoDateString);

  const promises = Object.entries(layers).map(async ([name, layer]) => {
    const data = loadedData[name];
    if (!data || !data.files.length) return { name, layer, url: null };

    const target = new Date(isoDateString).getTime();
    // Find best file
    const bestFile = data.files.reduce((prev, curr) => {
      return Math.abs(new Date(curr.time) - target) < Math.abs(new Date(prev.time) - target)
             ? curr : prev;
    });

    return { name, layer, url: bestFile.url };
  });

  const results = await Promise.all(promises);

  results.forEach(({ name, layer, url }) => {
    if (url) {
      layer.setSource(createSource(url));
      // user layer visibility
      layer.setVisible(variables[name].visible);
    } else {
      layer.setVisible(false);
    }
  });
}

function initSlider() {
  slider.min = 0;
  slider.max = timelineMaster.length - 1;
  slider.value = 0;
}




function updateFromIndex(index) {
  if (index < 0 || index >= timelineMaster.length) return;
  currentIndex = index;
  slider.value = index;
  updateTimeFromDate(timelineMaster[index]);
}

document.getElementById('prevStep')
  .addEventListener('click', () => {
    updateFromIndex(currentIndex - 1);
  });

document.getElementById('nextStep')
  .addEventListener('click', () => {
    updateFromIndex(currentIndex + 1);
  });


/* =========================================================
   11) INIT + LISTENERS
   ========================================================= */


initData();



// show legend for the first visible layer
for (const [name, layer] of Object.entries(layers)) {
  if (layer.getVisible()) {
    updateLegend(name);
    break;
  }
}


window.layers = layers;




document.getElementById('chkShiwe').addEventListener('change', e => {
   variables.shiwe.visible = e.target.checked;
   layers.shiwe.setVisible(e.target.checked);
   if (e.target.checked) updateLegend('shiwe');
});

document.getElementById('chkTemp').addEventListener('change', e => {
   variables['2t'].visible = e.target.checked;
   layers['2t'].setVisible(e.target.checked);
   if (e.target.checked) updateLegend('2t');
});

document.getElementById('chkUtci').addEventListener('change', e => {
   variables['utci'].visible = e.target.checked;
   layers['utci'].setVisible(e.target.checked);
   if (e.target.checked) updateLegend('utci');
});

document.getElementById('chkHumidity').addEventListener('change', e => {
   variables['2r'].visible = e.target.checked;
   layers['2r'].setVisible(e.target.checked);
   if (e.target.checked) updateLegend('2r');
});


slider.style.accentColor = '#666';

slider.addEventListener('input', e => {
  updateFromIndex(Number(e.target.value));
});


slider.addEventListener('mousemove', e => {
  const idx = Number(e.target.value);
  slider.title = formatUtcLabel(timelineMaster[idx]);
});


window.addEventListener('resize', () => {
  map.updateSize();
});




