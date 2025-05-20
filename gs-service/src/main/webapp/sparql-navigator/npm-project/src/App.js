import React, { useState, useEffect } from 'react';
import SPARQLOntologyNavigator from './SPARQLOntologyNavigator';

const App = () => {
  const [config, setConfig] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const basePath = window.location.pathname.split('/')[1]; // Detects 'context1' or 'context2'
    const configPath = `/${basePath}/config.json`;

    fetch(configPath)
      .then((response) => {
        if (!response.ok) throw new Error(`Failed to load configuration from ${configPath}`);
        return response.json();
      })
      .then((config) => setConfig(config))
      .catch((err) => setError(err.message));
  }, []);

  if (error) {
    return <div>Error: {error}</div>;
  }

  return config ? (
    <div>
      <h1>{config.title}</h1>
      <SPARQLOntologyNavigator endpoint={config.endpoint} title={config.title} />
    </div>
  ) : (
    <div>Loading configuration...</div>
  );
};

export default App;
