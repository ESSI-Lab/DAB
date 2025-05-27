import React, { useState, useEffect } from 'react';

let SPARQL_ENDPOINT = '';

const fetchTopConcept = async () => {
  const query = `
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    SELECT ?concept ?prefLabel ?altLabel (lang(?prefLabel) as ?prefLang) (lang(?altLabel) as ?altLang) WHERE {
      ?concept a skos:Concept .
      ?concept skos:topConceptOf ?scheme .
      ?concept skos:prefLabel ?prefLabel .
      OPTIONAL { ?concept skos:altLabel ?altLabel }
    } ORDER BY ?prefLabel
  `;

  const response = await fetch(SPARQL_ENDPOINT, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/sparql-query',
      'Accept': 'application/sparql-results+json',
    },
    body: query,
  });

  if (!response.ok) throw new Error('Failed to fetch the top concept');

  const data = await response.json();

  const conceptMap = new Map();
  const languages = new Set();

  data.results.bindings.forEach((binding) => {
    const uri = binding.concept.value;
    if (!conceptMap.has(uri)) {
      conceptMap.set(uri, {
        uri,
        prefLabel: binding.prefLabel.value,
        prefLang: binding.prefLang.value,
        altLabels: [],
      });
    }
    languages.add(binding.prefLang.value);
    if (binding.altLabel) {
      conceptMap.get(uri).altLabels.push({
        label: binding.altLabel.value,
        lang: binding.altLang.value,
      });
      languages.add(binding.altLang.value);
    }
  });

  return { concepts: Array.from(conceptMap.values()), languages: Array.from(languages) };
};

const fetchConcepts = async (parentURI) => {
  const query = `
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    SELECT ?concept ?prefLabel ?altLabel (lang(?prefLabel) as ?prefLang) (lang(?altLabel) as ?altLang) WHERE {
      ?concept skos:broader <${parentURI}> .
      ?concept skos:prefLabel ?prefLabel .
      OPTIONAL { ?concept skos:altLabel ?altLabel }
    } ORDER BY ?prefLabel
  `;

  const response = await fetch(SPARQL_ENDPOINT, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/sparql-query',
      'Accept': 'application/sparql-results+json',
    },
    body: query,
  });

  if (!response.ok) throw new Error('Failed to fetch concepts');

  const data = await response.json();

  const conceptMap = new Map();
  const newLanguages = new Set();

  data.results.bindings.forEach((binding) => {
    const uri = binding.concept.value;
    if (!conceptMap.has(uri)) {
      conceptMap.set(uri, {
        uri,
        prefLabel: binding.prefLabel.value,
        prefLang: binding.prefLang.value,
        altLabels: [],
      });
    }
    newLanguages.add(binding.prefLang.value);
    if (binding.altLabel) {
      conceptMap.get(uri).altLabels.push({
        label: binding.altLabel.value,
        lang: binding.altLang.value,
      });
      newLanguages.add(binding.altLang.value);
    }
  });

  return { concepts: Array.from(conceptMap.values()), newLanguages: Array.from(newLanguages) };
};

const ConceptNode = ({ concept, selectedLang, addLanguages }) => {
  const [narrowerConcepts, setNarrowerConcepts] = useState([]);
  const [expanded, setExpanded] = useState(false);

  const handleExpand = async () => {
    if (!expanded) {
      const { concepts: fetchedConcepts, newLanguages } = await fetchConcepts(concept.uri);
      setNarrowerConcepts(fetchedConcepts);
      addLanguages(newLanguages);
    }
    setExpanded(!expanded);
  };

  const handleUriClick = (e) => {
    e.preventDefault();
    	const message = { selectedConcept: concept.prefLabel,selectedConceptId: concept.uri};   
    window.parent.postMessage(message, '*');
  };

  const filterLabels = (labels) =>
    selectedLang === 'All'
      ? labels.map((l) => `${l.label} [${l.lang}]`)
      : labels.filter((l) => l.lang === selectedLang).map((l) => l.label);

  return (
    <div style={{ paddingLeft: '20px' }}>
      <button onClick={handleExpand} style={{ marginRight: '8px' }}>
        {expanded ? '-' : '+'}
      </button>
      <span>
        <strong>{concept.prefLabel}</strong> [{concept.prefLang}]
        {concept.altLabels.length > 0 && (
          <span style={{ color: 'gray', marginLeft: '8px' }}>
            ({filterLabels(concept.altLabels).join(' ')})
          </span>
        )}
        <span
          style={{ color: 'blue', marginLeft: '10px', fontStyle: 'italic', cursor: 'pointer', textDecoration: 'underline' }}
          onClick={handleUriClick}
        >
          {concept.uri}
        </span>
      </span>
      {expanded && narrowerConcepts.length > 0 && (
        <div style={{ marginLeft: '20px', marginTop: '8px' }}>
          {narrowerConcepts.map((nConcept) => (
            <ConceptNode key={nConcept.uri} concept={nConcept} selectedLang={selectedLang} addLanguages={addLanguages} />
          ))}
        </div>
      )}
    </div>
  );
};

const SPARQLOntologyNavigator = ({ endpoint, title }) => {
  const [topConcepts, setTopConcepts] = useState([]);
  const [languages, setLanguages] = useState([]);
  const [selectedLang, setSelectedLang] = useState('All');
  const [endpointReady, setEndpointReady] = useState(false);

  const addLanguages = (newLangs) => {
    setLanguages((prevLangs) => Array.from(new Set([...prevLangs, ...newLangs])).sort());
  };

  useEffect(() => {
    if (endpoint) {
      SPARQL_ENDPOINT = endpoint;
      setEndpointReady(true);
    }
  }, [endpoint]);

  useEffect(() => {
    const fetchTop = async () => {
      if (endpointReady) {
        const { concepts, languages } = await fetchTopConcept();
        setTopConcepts(concepts);
        setLanguages(['All', ...languages.sort()]);
      }
    };
    fetchTop();
  }, [endpointReady]);

  return (
    <div style={{ padding: '20px' }}>
      <h1>{title}</h1>
      <div style={{ marginBottom: '10px' }}>
        <label htmlFor="language-selector">Language: </label>
        <select id="language-selector" value={selectedLang} onChange={(e) => setSelectedLang(e.target.value)}>
          {languages.map((lang) => (
            <option key={lang} value={lang}>
              {lang}
            </option>
          ))}
        </select>
      </div>
      {topConcepts.map((concept) => (
        <ConceptNode key={concept.uri} concept={concept} selectedLang={selectedLang} addLanguages={addLanguages} />
      ))}
    </div>
  );
};

export default SPARQLOntologyNavigator;
