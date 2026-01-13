/**
 * SPARQL Ontology Navigator Library
 * A plain JavaScript library for navigating SKOS ontologies via SPARQL endpoints
 */

class SparqlNavigator {
  constructor(endpoint, containerId, title) {
    this.endpoint = endpoint;
    this.containerId = containerId;
    this.title = title;
    this.languages = ['All'];
    this.selectedLang = 'All';
    this.expandedNodes = new Set();
    this.conceptCache = new Map();
    this.topConcepts = null;
  }

  /**
   * Fetch all distinct languages from all concepts in the ontology
   */
  async fetchAllLanguages() {
    const query = `
      PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
      SELECT DISTINCT (lang(?prefLabel) as ?prefLang) (lang(?altLabel) as ?altLang) WHERE {
        ?concept a skos:Concept .
        ?concept skos:prefLabel ?prefLabel .
        OPTIONAL { ?concept skos:altLabel ?altLabel }
      }
    `;

    const response = await fetch(this.endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/sparql-query',
        'Accept': 'application/sparql-results+json',
      },
      body: query,
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch languages: ${response.statusText}`);
    }

    const data = await response.json();
    const languages = new Set();

    data.results.bindings.forEach((binding) => {
      if (binding.prefLang) {
        languages.add(binding.prefLang.value);
      }
      if (binding.altLang) {
        languages.add(binding.altLang.value);
      }
    });

    return Array.from(languages).sort();
  }

  /**
   * Fetch top concepts from the SPARQL endpoint
   */
  async fetchTopConcepts() {
    const query = `
      PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
      SELECT ?concept ?prefLabel ?altLabel (lang(?prefLabel) as ?prefLang) (lang(?altLabel) as ?altLang) WHERE {
        ?concept a skos:Concept .
        ?concept skos:topConceptOf ?scheme .
        ?concept skos:prefLabel ?prefLabel .
        OPTIONAL { ?concept skos:altLabel ?altLabel }
      } ORDER BY ?prefLabel
    `;

    return this.executeQuery(query, true);
  }

  /**
   * Fetch narrower concepts for a given parent URI
   */
  async fetchNarrowerConcepts(parentURI) {
    const query = `
      PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
      SELECT ?concept ?prefLabel ?altLabel (lang(?prefLabel) as ?prefLang) (lang(?altLabel) as ?altLang) WHERE {
        ?concept skos:broader <${parentURI}> .
        ?concept skos:prefLabel ?prefLabel .
        OPTIONAL { ?concept skos:altLabel ?altLabel }
      } ORDER BY ?prefLabel
    `;

    return this.executeQuery(query, false);
  }

  /**
   * Sort prefLabels so English comes first, then alphabetically
   */
  sortPrefLabels(prefLabels) {
    return prefLabels.sort((a, b) => {
      // English (en) always comes first
      if (a.lang === 'en' && b.lang !== 'en') return -1;
      if (a.lang !== 'en' && b.lang === 'en') return 1;
      // Then sort by language code
      if (a.lang !== b.lang) return a.lang.localeCompare(b.lang);
      // Same language, sort by label
      return a.label.localeCompare(b.label);
    });
  }

  /**
   * Execute a SPARQL query and parse results
   */
  async executeQuery(query, isTopLevel) {
    const response = await fetch(this.endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/sparql-query',
        'Accept': 'application/sparql-results+json',
      },
      body: query,
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch concepts: ${response.statusText}`);
    }

    const data = await response.json();
    const conceptMap = new Map();
    const newLanguages = new Set();

    data.results.bindings.forEach((binding) => {
      const uri = binding.concept.value;
      if (!conceptMap.has(uri)) {
        conceptMap.set(uri, {
          uri,
          prefLabels: [],
          altLabels: [],
        });
      }
      
      const concept = conceptMap.get(uri);
      const prefLabel = binding.prefLabel.value;
      const prefLang = binding.prefLang.value;
      
      // Add prefLabel if not already present
      const prefLabelExists = concept.prefLabels.some(
        pl => pl.label === prefLabel && pl.lang === prefLang
      );
      if (!prefLabelExists) {
        concept.prefLabels.push({
          label: prefLabel,
          lang: prefLang,
        });
        newLanguages.add(prefLang);
      }
      
      if (binding.altLabel) {
        const altLabel = binding.altLabel.value;
        const altLang = binding.altLang.value;
        const altLabelExists = concept.altLabels.some(
          al => al.label === altLabel && al.lang === altLang
        );
        if (!altLabelExists) {
          concept.altLabels.push({
            label: altLabel,
            lang: altLang,
          });
          newLanguages.add(altLang);
        }
      }
    });

    // Sort prefLabels for each concept (English first)
    conceptMap.forEach((concept) => {
      concept.prefLabels = this.sortPrefLabels(concept.prefLabels);
    });

    // Update languages list
    let languagesUpdated = false;
    newLanguages.forEach(lang => {
      if (!this.languages.includes(lang)) {
        this.languages.push(lang);
        languagesUpdated = true;
      }
    });
    
    if (languagesUpdated) {
      this.languages.sort();
      this.updateLanguageSelector();
    }

    return Array.from(conceptMap.values());
  }

  /**
   * Update the language selector dropdown with current languages
   */
  updateLanguageSelector() {
    const langSelect = document.getElementById('language-selector');
    if (!langSelect) return;

    const currentValue = langSelect.value;
    langSelect.innerHTML = '';
    
    this.languages.forEach((lang) => {
      const option = document.createElement('option');
      option.value = lang;
      option.textContent = lang;
      if (lang === currentValue) {
        option.selected = true;
      }
      langSelect.appendChild(option);
    });
  }

  /**
   * Filter labels based on selected language
   */
  filterLabels(labels) {
    if (this.selectedLang === 'All') {
      return labels.map((l) => `${l.label} [${l.lang}]`);
    }
    return labels.filter((l) => l.lang === this.selectedLang).map((l) => l.label);
  }

  /**
   * Handle expand/collapse of concept nodes
   */
  async handleExpand(conceptUri, expandButton, container) {
    const isExpanded = this.expandedNodes.has(conceptUri);

    if (!isExpanded) {
      // Expand: fetch and display narrower concepts
      try {
        expandButton.disabled = true;
        expandButton.textContent = '...';
        
        let concepts;
        if (this.conceptCache.has(conceptUri)) {
          concepts = this.conceptCache.get(conceptUri);
        } else {
          concepts = await this.fetchNarrowerConcepts(conceptUri);
          this.conceptCache.set(conceptUri, concepts);
        }

        const childrenContainer = document.createElement('div');
        childrenContainer.className = 'navigator-children';
        childrenContainer.style.marginLeft = '20px';
        childrenContainer.style.marginTop = '8px';

        if (concepts.length > 0) {
          concepts.forEach((concept) => {
            const childNode = this.createConceptNode(concept);
            childrenContainer.appendChild(childNode);
          });
        } else {
          const noChildren = document.createElement('div');
          noChildren.textContent = '(no narrower concepts)';
          noChildren.style.color = 'gray';
          noChildren.style.fontStyle = 'italic';
          childrenContainer.appendChild(noChildren);
        }

        container.appendChild(childrenContainer);
        this.expandedNodes.add(conceptUri);
        expandButton.textContent = '-';
      } catch (error) {
        console.error('Error fetching narrower concepts:', error);
        expandButton.textContent = '+';
        alert('Failed to fetch narrower concepts: ' + error.message);
      } finally {
        expandButton.disabled = false;
      }
    } else {
      // Collapse: remove children container
      const childrenContainer = container.querySelector('.navigator-children');
      if (childrenContainer) {
        childrenContainer.remove();
      }
      this.expandedNodes.delete(conceptUri);
      expandButton.textContent = '+';
    }
  }

  /**
   * Handle URI click - send message to parent window
   */
  handleUriClick(concept) {
    // Use the first prefLabel (should be English if available)
    const firstPrefLabel = concept.prefLabels.length > 0 
      ? concept.prefLabels[0].label 
      : '';
    const message = {
      selectedConcept: firstPrefLabel,
      selectedConceptId: concept.uri
    };
    window.parent.postMessage(message, '*');
  }

  /**
   * Filter prefLabels based on selected language
   */
  filterPrefLabels(prefLabels) {
    if (this.selectedLang === 'All') {
      return prefLabels;
    }
    return prefLabels.filter((pl) => pl.lang === this.selectedLang);
  }

  /**
   * Create a concept node element
   */
  createConceptNode(concept) {
    const nodeDiv = document.createElement('div');
    nodeDiv.className = 'navigator-concept-node';
    nodeDiv.style.paddingLeft = '20px';
    nodeDiv.style.marginTop = '4px';

    const expandButton = document.createElement('button');
    expandButton.textContent = '+';
    expandButton.style.marginRight = '8px';
    expandButton.style.cursor = 'pointer';

    const contentSpan = document.createElement('span');

    // Display all prefLabels in bold (filtered by selected language)
    const filteredPrefLabels = this.filterPrefLabels(concept.prefLabels);
    
    if (filteredPrefLabels.length > 0) {
      filteredPrefLabels.forEach((prefLabel, index) => {
        const prefLabelStrong = document.createElement('strong');
        prefLabelStrong.textContent = prefLabel.label;
        contentSpan.appendChild(prefLabelStrong);
        
        const langSpan = document.createElement('span');
        langSpan.textContent = ` [${prefLabel.lang}]`;
        contentSpan.appendChild(langSpan);
        
        // Add separator between multiple prefLabels
        if (index < filteredPrefLabels.length - 1) {
          const separator = document.createTextNode(' ');
          contentSpan.appendChild(separator);
        }
      });
    } else {
      // Fallback if no prefLabels match the selected language
      const prefLabelStrong = document.createElement('strong');
      prefLabelStrong.textContent = concept.prefLabels[0]?.label || '(no label)';
      contentSpan.appendChild(prefLabelStrong);
      if (concept.prefLabels[0]) {
        const langSpan = document.createElement('span');
        langSpan.textContent = ` [${concept.prefLabels[0].lang}]`;
        contentSpan.appendChild(langSpan);
      }
    }

    // Only show altLabels in gray parentheses
    if (concept.altLabels.length > 0) {
      const altLabelsSpan = document.createElement('span');
      altLabelsSpan.style.color = 'gray';
      altLabelsSpan.style.marginLeft = '8px';
      const filtered = this.filterLabels(concept.altLabels);
      if (filtered.length > 0) {
        altLabelsSpan.textContent = `(${filtered.join(' ')})`;
        contentSpan.appendChild(altLabelsSpan);
      }
    }

    const uriSpan = document.createElement('span');
    uriSpan.textContent = concept.uri;
    uriSpan.style.color = 'blue';
    uriSpan.style.marginLeft = '10px';
    uriSpan.style.fontStyle = 'italic';
    uriSpan.style.cursor = 'pointer';
    uriSpan.style.textDecoration = 'underline';
    uriSpan.addEventListener('click', (e) => {
      e.preventDefault();
      this.handleUriClick(concept);
    });
    contentSpan.appendChild(uriSpan);

    const childrenContainer = document.createElement('div');
    childrenContainer.className = 'navigator-children-container';

    expandButton.addEventListener('click', async () => {
      await this.handleExpand(concept.uri, expandButton, childrenContainer);
    });

    nodeDiv.appendChild(expandButton);
    nodeDiv.appendChild(contentSpan);
    nodeDiv.appendChild(childrenContainer);

    return nodeDiv;
  }

  /**
   * Handle language selection change
   */
  handleLanguageChange(newLang) {
    this.selectedLang = newLang;
    this.rerenderConcepts();
  }

  /**
   * Re-render concepts without fetching (for language changes)
   */
  rerenderConcepts() {
    const conceptsContainer = document.querySelector('.navigator-concepts');
    if (!conceptsContainer || !this.topConcepts) return;

    conceptsContainer.innerHTML = '';
    if (this.topConcepts.length === 0) {
      const noConcepts = document.createElement('div');
      noConcepts.textContent = 'No top concepts found.';
      noConcepts.style.color = 'gray';
      conceptsContainer.appendChild(noConcepts);
    } else {
      this.topConcepts.forEach((concept) => {
        const node = this.createConceptNode(concept);
        conceptsContainer.appendChild(node);
      });
    }
  }

  /**
   * Render the navigator UI
   */
  async render() {
    const container = document.getElementById(this.containerId);
    if (!container) {
      throw new Error(`Container with id "${this.containerId}" not found`);
    }

    container.innerHTML = '';

    // Create title
    const title = document.createElement('h1');
    title.textContent = this.title;
    container.appendChild(title);

    // Create language selector
    const langContainer = document.createElement('div');
    langContainer.style.marginBottom = '10px';

    const langLabel = document.createElement('label');
    langLabel.htmlFor = 'language-selector';
    langLabel.textContent = 'Language: ';

    const langSelect = document.createElement('select');
    langSelect.id = 'language-selector';
    langSelect.value = this.selectedLang;
    langSelect.addEventListener('change', (e) => {
      this.handleLanguageChange(e.target.value);
    });

    langContainer.appendChild(langLabel);
    langContainer.appendChild(langSelect);
    container.appendChild(langContainer);

    // Create concepts container
    const conceptsContainer = document.createElement('div');
    conceptsContainer.className = 'navigator-concepts';

    try {
      // Show loading state
      const loading = document.createElement('div');
      loading.textContent = 'Loading languages and concepts...';
      conceptsContainer.appendChild(loading);

      // First, fetch all languages from the entire ontology
      const allLanguages = await this.fetchAllLanguages();
      this.languages = ['All', ...allLanguages];

      // Populate language selector with all languages
      langSelect.innerHTML = '';
      this.languages.forEach((lang) => {
        const option = document.createElement('option');
        option.value = lang;
        option.textContent = lang;
        if (lang === this.selectedLang) {
          option.selected = true;
        }
        langSelect.appendChild(option);
      });

      // Fetch and display top concepts
      this.topConcepts = await this.fetchTopConcepts();

      conceptsContainer.innerHTML = '';
      if (this.topConcepts.length === 0) {
        const noConcepts = document.createElement('div');
        noConcepts.textContent = 'No top concepts found.';
        noConcepts.style.color = 'gray';
        conceptsContainer.appendChild(noConcepts);
      } else {
        this.topConcepts.forEach((concept) => {
          const node = this.createConceptNode(concept);
          conceptsContainer.appendChild(node);
        });
      }
    } catch (error) {
      console.error('Error loading concepts:', error);
      conceptsContainer.innerHTML = '';
      const errorDiv = document.createElement('div');
      errorDiv.style.color = 'red';
      errorDiv.textContent = `Error: ${error.message}`;
      conceptsContainer.appendChild(errorDiv);
    }

    container.appendChild(conceptsContainer);
  }

  /**
   * Initialize the navigator
   */
  async init() {
    await this.render();
  }
}

// Export for use in modules or make available globally
if (typeof module !== 'undefined' && module.exports) {
  module.exports = SparqlNavigator;
} else {
  window.SparqlNavigator = SparqlNavigator;
}

