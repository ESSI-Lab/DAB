import { GIAPI } from '../core/GIAPI.js';

GIAPI.OntologiesSelector = (() => {
	const endpointUrl = "http://localhost:9090/gs-service/services/config-api/list";
	  const selectedIds = new Set();

	  let modal, tableContainer, rootDiv;

	  // =============================
	  //  Public API
	  // =============================

	  async function showDialog() {
	    ensureDialogExists();
	    modal.style.display = "flex";

	    const selectedMode = rootDiv.querySelector("input[name='searchMode']:checked").value;
	    if (selectedMode === "semantic") {
	      await loadOntologies();
	    } else {
	      tableContainer.innerHTML = "";
	    }
	  }

	  function getSelectedIds() {
	    return Array.from(selectedIds);
	  }

	  /**
	   * Ritorna il contenuto HTML del widget (un <div> pronto da inserire altrove)
	   * Non mostra alcun dialog — perfetto per embedding.
	   */
	  function getDiv() {
	    ensureDialogExists();
	    // Restituisce solo il contenuto (non il modale trasparente)
	    return rootDiv;
	  }

	  // =============================
	  //  Private functions
	  // =============================

	  function ensureDialogExists() {
	    if (rootDiv) return; // già creato

	    // Creazione del contenuto principale (riutilizzabile)
	    rootDiv = document.createElement("div");
	    rootDiv.className = "sw-content";
	    rootDiv.innerHTML = `
	        <h3>Select search mode</h3>
	        <label><input type="radio" name="searchMode" value="basic" checked> Basic search</label><br>
	        <label><input type="radio" name="searchMode" value="semantic"> Semantic search</label>
	        <div id="sw-table-container"></div>
	    `;

	    // Contenitore modale (per showDialog)
	    modal = document.createElement("div");
	    modal.className = "sw-modal";
	    modal.innerHTML = `
	      <div class="sw-modal-content">
	        ${rootDiv.outerHTML}
	        <div style="margin-top:15px; text-align:right;">
	          <button id="sw-close-btn">Close</button>
	        </div>
	      </div>
	    `;
	    document.body.appendChild(modal);

	    // CSS
	    const style = document.createElement("style");
	    style.textContent = `
	      .sw-modal {
	        display: none;
	        position: fixed;
	        top: 0; left: 0; right: 0; bottom: 0;
	        background: rgba(0, 0, 0, 0.3);
	        justify-content: center;
	        align-items: center;
	        z-index: 9999;
	      }
	      .sw-modal-content {
	        background: white;
	        padding: 20px;
	        border-radius: 8px;
	        width: 420px;
	        box-shadow: 0 4px 10px rgba(0,0,0,0.3);
	        font-family: sans-serif;
	      }
		  #ontology-table {
		          width: 100%;
		          border-collapse: collapse;
		          margin-top: 10px;
		          font-size: 0.9em;
		        }
		        #ontology-table th,
		        #ontology-table td {
		          border: 1px solid #ddd;
		          padding: 6px;
		          text-align: left;
		        }
		        #ontology-table th {
		          background-color: #f4f4f4;
		        }
	    `;
	    document.head.appendChild(style);

	    // Collegamenti a elementi reali
	    tableContainer = modal.querySelector("#sw-table-container");
	    const closeBtn = modal.querySelector("#sw-close-btn");

	    // Eventi
	    closeBtn.addEventListener("click", () => {
	      modal.style.display = "none";
	    });

	    modal.querySelectorAll("input[name='searchMode']").forEach(radio => {
	      radio.addEventListener("change", async (e) => {
	        if (e.target.value === "semantic") {
	          await loadOntologies();
	        } else {
	          tableContainer.innerHTML = "";
	          selectedIds.clear();
	        }
	      });
	    });
	  }

	  async function loadOntologies() {
	    tableContainer.innerHTML = "<p>Loading ontologies...</p>";
	    try {
	      const response = await fetch(endpointUrl, {
	        method: "POST",
	        headers: { "Content-Type": "application/json" },
	        body: JSON.stringify({ request: "ListOntologies" })
	      });

	      if (!response.ok) throw new Error(`HTTP ${response.status}`);
	      const data = await response.json();
	      renderTable(data);
	    } catch (err) {
	      console.error("Errore durante la fetch:", err);
	      tableContainer.innerHTML = "<p style='color:red'>Errore nel caricamento dei dati.</p>";
	    }
	  }

	  function renderTable(data) {
	    if (!Array.isArray(data) || data.length === 0) {
	      tableContainer.innerHTML = "<p>Nessun dato disponibile.</p>";
	      return;
	    }

	    const table = document.createElement("table");
		table.id = "ontology-table";
		
	    const thead = document.createElement("thead");
	    thead.innerHTML = "<tr><th></th><th>Name</th><th>Endpoint</th><th>ID</th></tr>";
	    table.appendChild(thead);

	    const tbody = document.createElement("tbody");
	    data.forEach(item => {
	      const tr = document.createElement("tr");

	      const checkbox = document.createElement("input");
	      checkbox.type = "checkbox";
	      checkbox.checked = selectedIds.has(item.id);
	      checkbox.addEventListener("change", () => {
	        if (checkbox.checked) selectedIds.add(item.id);
	        else selectedIds.delete(item.id);
	      });

	      tr.innerHTML = `
	        <td></td>
	        <td>${item.name}</td>
	        <td>${item.endpoint}</td>
	        <td>${item.id}</td>
	      `;
	      tr.cells[0].appendChild(checkbox);
	      tbody.appendChild(tr);
	    });

	    table.appendChild(tbody);
	    tableContainer.innerHTML = "";
	    tableContainer.appendChild(table);
	  }

	  // =============================
	  //  Export
	  // =============================
	  return {
	    showDialog,
	    getSelectedIds,
	    getDiv
	  };
})();
