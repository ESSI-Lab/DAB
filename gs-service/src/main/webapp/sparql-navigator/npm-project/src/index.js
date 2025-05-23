import React from 'react';
import ReactDOM from 'react-dom/client';
import SPARQLOntologyNavigator from './components/SparqlOntologyNavigator';
const path = window.location.pathname;

const config = {
  '/whos/': { cfg: '/whos/config.json' },
  '/his-central/': { cfg: '/his-central/config.json' },
};

const endpoint = config[path]?.cfg || 'config.json';

fetch(endpoint)
  .then((response) => response.json())
  .then((configData) => {
    const root = ReactDOM.createRoot(document.getElementById('root'));
    root.render(
      <SPARQLOntologyNavigator endpoint={configData.endpoint} title={configData.title} />
    );
  })
  .catch((err) => console.error('Failed to load configuration:', err));
