import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';

/* =========================================================
   1) GLOBAL TIMELINE
   ---------------------------------------------------------
   Here define the tempral extent of the slide.
   In this case start from 2026-05-11T18:00:00Z
   ========================================================= */

function generateTimeline(startIso, endIso, stepHours = 1) {
  const out = [];
  const start = new Date(startIso);
  const end = new Date(endIso);

  for (
    let d = new Date(start.getTime());
    d <= end;
    d = new Date(d.getTime() + stepHours * 3600 * 1000)
  ) {
    out.push(d.toISOString());
  }

  return out;
}

const timeline = generateTimeline(
  '2026-05-12T06:00:00Z',
  '2026-05-22T00:00:00Z',
  1
);

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
    availableHours: [0, 6, 12, 18],
    minDate: '2026-05-12T06:00:00Z',
    maxDate: '2026-05-15T18:00:00Z',
    availability: [
        {
          untilDays: 4,
          stepHours: 6
        }
      ],

    visible: true,
    style: shiweStyle
  },

  '2t': {
    folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/2t/',
    filePrefix: '2t', //
    minDate: '2026-05-12T01:00:00Z',
    maxDate: '2026-05-22T00:00:00Z',
    availability: [
        {
          untilDays: 4,
          stepHours: 1
        },
        {
          untilDays: 6,
          stepHours: 3
        },
        {
          untilDays: 10,
          stepHours: 6
        }
      ],

    visible: false,
    style: temperatureStyle
  },

    utci: {
      folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/utci/',
      filePrefix: 'utci', //
      minDate: '2026-05-12T01:00:00Z',
      maxDate: '2026-05-22T00:00:00Z',
      availability: [
          {
            untilDays: 4,
            stepHours: 1
          },
          {
            untilDays: 6,
            stepHours: 3
          },
          {
            untilDays: 10,
            stepHours: 6
          }
        ],

      visible: false,
      style: utciStyle
    },

    '2r': {
          folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/2r/',
          filePrefix: '2r', //
          minDate: '2026-05-12T01:00:00Z',
          maxDate: '2026-05-22T00:00:00Z',
          availability: [
              {
                untilDays: 4,
                stepHours: 1
              },
              {
                untilDays: 6,
                stepHours: 3
              },
              {
                untilDays: 10,
                stepHours: 6
              }
            ],

          visible: false,
          style: humidityStyle
        }

};



const legendDiv = document.getElementById('legend');

function updateLegend(layerName) {
  const legend = legends[layerName];
  if (!legend) {
    legendDiv.innerHTML = '';
    return;
  }

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



const timelineMaster = buildMasterTimeline(variables);
let currentIndex = 0;

const modelStart = new Date(
  Math.min(...Object.values(variables).map(v => new Date(v.minDate)))
);


/* =========================================================
   3) UTILITY
   ========================================================= */


function hoursBetween(a, b) {
  return Math.round((b - a) / 36e5);
}

function formatUtcLabel(date) {
  const yyyy = date.getUTCFullYear();
  const mm = String(date.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(date.getUTCDate()).padStart(2, '0');
  const hh = String(date.getUTCHours()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:00 UTC`;
}


function buildVariableTimeline(cfg) {
  const out = [];
  const start = new Date(cfg.minDate);
  const end = new Date(cfg.maxDate);

  let current = new Date(start);

  for (const rule of cfg.availability) {
    const ruleEnd = new Date(
      start.getTime() + rule.untilDays * 24 * 3600e3
    );
    const effectiveEnd = ruleEnd < end ? ruleEnd : end;

    while (current <= effectiveEnd) {
      out.push(new Date(current));
      current = new Date(
        current.getTime() + rule.stepHours * 3600e3
      );
    }
  }

  return out;
}


function buildMasterTimeline(variables) {
  const map = new Map();

  for (const cfg of Object.values(variables)) {
    for (const d of buildVariableTimeline(cfg)) {
      map.set(d.toISOString(), d);
    }
  }

  return Array.from(map.values()).sort((a, b) => a - b);
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

function buildFilename(filePrefix, date) {
  const yyyy = date.getUTCFullYear();
  const mm = pad2(date.getUTCMonth() + 1);
  const dd = pad2(date.getUTCDate());
  const hh = pad2(date.getUTCHours());

  return `${filePrefix}_${yyyy}${mm}${dd}${hh}.tif`;
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


function getStepHours(cfg, requestedDate) {
  const t0 = new Date(cfg.minDate).getTime();
  const dtHours = (requestedDate.getTime() - t0) / 36e5;

  for (const rule of cfg.availability) {
    if (dtHours <= rule.untilDays * 24) {
      return rule.stepHours;
    }
  }

  return null; // outside range
}



function buildCandidateDates(targetDate, cfg, searchDays = 2) {
  const minDate = new Date(cfg.minDate);
  const maxDate = new Date(cfg.maxDate);

  const stepHours = getStepHours(cfg, targetDate);
  if (!stepHours) return [];

  const candidates = [];

  for (let dayOffset = -searchDays; dayOffset <= searchDays; dayOffset++) {
    const base = new Date(Date.UTC(
      targetDate.getUTCFullYear(),
      targetDate.getUTCMonth(),
      targetDate.getUTCDate() + dayOffset,
      0, 0, 0
    ));

    for (let h = 0; h < 24; h += stepHours) {
      const d = new Date(base.getTime() + h * 3600e3);
      if (d < minDate || d > maxDate) continue;
      candidates.push(d);
    }
  }

  candidates.sort(
    (a, b) =>
      Math.abs(a - targetDate) - Math.abs(b - targetDate)
  );

  return candidates;
}




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
   6) BEST URL RESOLUTION
   ---------------------------------------------------------
   Given a layer and a requested date:
   - generates nearby candidates
   - tries them in order
   - returns the first URL that actually exists
   ========================================================= */

async function resolveBestUrl(cfg, requestedDate) {
  const candidates = buildCandidateDates(requestedDate, cfg, 2);

  for (const d of candidates) {
    const file = buildFilename(cfg.filePrefix, d);
    const url = cfg.folderUrl + file;

    const exists = await urlExists(url);
    if (exists) {
      return {
        url,
        resolvedDate: d,
        file
      };
    }
  }

  return null;
}

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
    center: [1113194, 7628367], // Europa circa, puoi modificare
    zoom: 4
  })
});

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

async function updateTimeFromDate(requestedDate) {
  const requestId = ++updateRequestId;


  const lead = hoursBetween(modelStart, requestedDate);
  const yyyy = requestedDate.getUTCFullYear();
  const mm = String(requestedDate.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(requestedDate.getUTCDate()).padStart(2, '0');
  const hh = String(requestedDate.getUTCHours()).padStart(2, '0');

  timeMain.textContent =
    `${yyyy}-${mm}-${dd} · ${hh}:00 UTC   (T+${lead}h)`;


  const promises = Object.entries(layers).map(async ([name, layer]) => {
    const cfg = variables[name];
    const result = await resolveBestUrl(cfg, requestedDate);
    return { name, layer, result };
  });

  const results = await Promise.all(promises);
  if (requestId !== updateRequestId) return;

  const parts = [];

  for (const { name, layer, result } of results) {
    if (!result) {
      layer.setVisible(false);
      parts.push(`${name.toUpperCase()} ✕`);
      continue;
    }

    layer.setSource(createSource(result.url));

    const layerLead = hoursBetween(modelStart, result.resolvedDate);
    const snap = Math.abs(
      hoursBetween(requestedDate, result.resolvedDate)
    );

    parts.push(
      snap > 0
        ? `${name.toUpperCase()} ✓ T+${layerLead}h (±${snap}h)`
        : `${name.toUpperCase()} ✓ T+${layerLead}h`
    );
  }

  timeLayers.textContent = parts.join('   ');
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

updateFromIndex(0);


// show legend
for (const [name, cfg] of Object.entries(variables)) {
  if (cfg.visible) {
    updateLegend(name);
    break; // only one
  }
}

window.layers = layers;


document.getElementById('chkShiwe')
  .addEventListener('change', e => {
    layers.shiwe.setVisible(e.target.checked);
    if (e.target.checked) updateLegend('shiwe');
  });

document.getElementById('chkTemp')
  .addEventListener('change', e => {
    layers['2t'].setVisible(e.target.checked);
     if (e.target.checked) updateLegend('2t');
  });

document.getElementById('chkUtci')
  .addEventListener('change', e => {
    layers['utci'].setVisible(e.target.checked);
     if (e.target.checked) updateLegend('utci');
  });

document.getElementById('chkHumidity')
  .addEventListener('change', e => {
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



