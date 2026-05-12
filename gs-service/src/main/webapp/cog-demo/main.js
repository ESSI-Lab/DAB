import OLMap from 'ol/Map.js';
import View from 'ol/View.js';
import TileLayer from 'ol/layer/Tile.js';
import OSM from 'ol/source/OSM.js';
import WebGLTileLayer from 'ol/layer/WebGLTile.js';
import GeoTIFF from 'ol/source/GeoTIFF.js';

/* =========================================================
   1) TIMELINE GLOBALE
   ---------------------------------------------------------
   Qui definisci l'intervallo temporale che vuoi mostrare
   nello slider.
   In questo esempio parte da 11 maggio 2026 ore 18 UTC.
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
  '2026-05-11T18:00:00Z',
  '2026-05-22T00:00:00Z',
  1
);

/* =========================================================
   2) CONFIGURAZIONE VARIABILI / LAYER
   ---------------------------------------------------------
   - folderUrl: cartella S3
   - filePrefix: prefisso reale del file
   - availableHours: orari disponibili nel giorno
   - minDate: primo timestamp realmente disponibile
   - style: stile OpenLayers
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
  ]
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

  ]
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

  ]
};



const variables = {
  shiwe: {
    folderUrl: 'https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/shiwe/',
    filePrefix: 'shiwe',
    availableHours: [0, 6, 12, 18],
    minDate: '2026-05-11T06:00:00Z',
    maxDate: '2026-05-14T18:00:00Z',
    availability: [
        {
          untilDays: 4,
          stepHours: 6
        }
      ],

    visible: true,
    opacity: 0.75,
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
    opacity: 0.80,
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
      opacity: 0.80,
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
          opacity: 0.80,
          style: humidityStyle
        }

};



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

function formatForecastHeader(requestedDate, modelStart) {
  const lead = hoursBetween(modelStart, requestedDate);
  const day = Math.floor(lead / 24) + 1;
  return `T+${lead}h | Day ${day} | ${formatUtcLabel(requestedDate)}`;
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
   4) GENERAZIONE CANDIDATI TEMPORALI "SAFE"
   ---------------------------------------------------------
   Se il file esatto non esiste:
   - prova l'orario disponibile più vicino
   - prima nello stesso giorno
   - poi eventualmente giorno precedente / successivo
   - sempre rispettando minDate e maxDate
   ========================================================= */


function getStepHours(cfg, requestedDate) {
  const t0 = new Date(cfg.minDate).getTime();
  const dtHours = (requestedDate.getTime() - t0) / 36e5;

  for (const rule of cfg.availability) {
    if (dtHours <= rule.untilDays * 24) {
      return rule.stepHours;
    }
  }

  return null; // fuori orizzonte
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
   5) CHECK ESISTENZA URL (con cache)
   ---------------------------------------------------------
   Usa HEAD per evitare download del file.
   Se il CORS S3 permette HEAD (come dovrebbe), funziona bene.
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
   6) RISOLUZIONE URL MIGLIORE
   ---------------------------------------------------------
   Dato un layer e una data richiesta:
   - genera candidati vicini
   - prova in ordine
   - restituisce il primo URL realmente esistente
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
   8) MAPPA DI SFONDO + MAPPA
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

//const slider = document.getElementById('timeSlider');
const label = document.getElementById('timeLabel');
//
//if (!slider || !label) {
//  throw new Error(
//    'HTML missing #timeSlider or #timeLabel. Add them to index.html.'
//  );
//}
//
//slider.min = 0;
//slider.max = timeline.length - 1;
//slider.step = 1;
//slider.value = 0;

/* =========================================================
   10) UPDATE TIME SAFE
   ---------------------------------------------------------
   - aggiorna la label
   - per ogni layer trova il file più vicino disponibile
   - se lo trova, aggiorna il source
   - se non lo trova, lascia il source precedente
   - evita race condition se muovi lo slider velocemente
   ========================================================= */

//let updateRequestId = 0;
//
//async function updateTime(index) {
//  const requestId = ++updateRequestId;
//  const requestedDate = new Date(timeline[index]);
//
//  label.textContent =
//    formatForecastHeader(requestedDate, modelStart) + ' (loading…)';
//
//  const promises = Object.entries(layers).map(async ([name, layer]) => {
//    const cfg = variables[name];
//    const result = await resolveBestUrl(cfg, requestedDate);
//    return { name, layer, cfg, result };
//  });
//
//  const results = await Promise.all(promises);
//
//  // se parte un update più recente, abbandona
//  if (requestId !== updateRequestId) return;
//
//  const infoParts = [];
//
//  for (const { name, layer, cfg, result } of results) {
//    if (!result) {
//      layer.setVisible(false); // opzionale ma forecast-like
//      infoParts.push(`${name}: no forecast`);
//      continue;
//    }
//
//    layer.setSource(createSource(result.url));
//
//    const resolvedLead =
//      hoursBetween(modelStart, result.resolvedDate);
//
//    const snapHours = Math.abs(
//      hoursBetween(requestedDate, result.resolvedDate)
//    );
//
//    const snapNote = snapHours > 0 ? ` (±${snapHours}h)` : '';
//
//    infoParts.push(
//      `${name}: T+${resolvedLead}h${snapNote}`
//    );
//  }
//
//  label.textContent =
//    formatForecastHeader(requestedDate, modelStart) +
//    ' | ' +
//    infoParts.join(' | ');
//}



let updateRequestId = 0;

async function updateTimeFromDate(requestedDate) {
  const requestId = ++updateRequestId;

  label.textContent =
    formatForecastHeader(requestedDate, modelStart) + ' (loading…)';

  const promises = Object.entries(layers).map(async ([name, layer]) => {
    const cfg = variables[name];
    const result = await resolveBestUrl(cfg, requestedDate);
    return { name, layer, cfg, result };
  });

  const results = await Promise.all(promises);
  if (requestId !== updateRequestId) return;

  const infoParts = [];

  for (const { name, layer, result } of results) {
    if (!result) {
      layer.setVisible(false);
      infoParts.push(`${name}: no forecast`);
      continue;
    }

    layer.setSource(createSource(result.url));

    const lead = hoursBetween(modelStart, result.resolvedDate);
    const snap = Math.abs(
      hoursBetween(requestedDate, result.resolvedDate)
    );

    infoParts.push(
      snap > 0
        ? `${name}: T+${lead}h (±${snap}h)`
        : `${name}: T+${lead}h`
    );
  }

  label.textContent =
    formatForecastHeader(requestedDate, modelStart) +
    ' | ' +
    infoParts.join(' | ');
}


function updateFromIndex(index) {
  if (index < 0 || index >= timelineMaster.length) return;
  currentIndex = index;
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
   11) INIT + EVENTI
   ========================================================= */

//updateTime(0);
updateFromIndex(0);


// 👇 espliciti cosa rendi globale
window.layers = layers;


document.getElementById('chkShiwe')
  .addEventListener('change', e => {
    layers.shiwe.setVisible(e.target.checked);
  });

document.getElementById('chkTemp')
  .addEventListener('change', e => {
    layers['2t'].setVisible(e.target.checked);
  });

document.getElementById('chkUtci')
  .addEventListener('change', e => {
    layers['utci'].setVisible(e.target.checked);
  });

document.getElementById('chkHumidity')
  .addEventListener('change', e => {
    layers['2r'].setVisible(e.target.checked);
  });



//slider.addEventListener('input', (e) => {
//  updateTime(Number(e.target.value));
//});

