import { GIAPI } from '../core/GIAPI.js';

GIAPI.OntologiesSelector = (() => {
	
	const endpointUrl = "../services/config-api/list";
	const selectedIds = new Set();

	let modal, tableContainer, rootDiv;

	// =============================
	//  Public API
	// =============================

	/**
	 * 
	 */
	async function showDialog() {
		initDialog();
		modal.style.display = "flex";

		const basicSearch = $('#basicsearch').is(":checked")
		if (basicSearch) {
			tableContainer.innerHTML = "";
		
		} else {
			await loadOntologies();
		}
	}

	/**
	 * 
	 */
	function getSelectedIds() {

		return Array.from(selectedIds);
	}

	/**
	 * 
	 */
	function div() {

		initDialog();

		return rootDiv;
	}

	// =============================
	//  Private functions
	// =============================

	function initDialog() {

		if (rootDiv) return;  
		 
		rootDiv = document.createElement("div");
		rootDiv.className = "sw-content";
		rootDiv.innerHTML = `
	        <h3 id='select-search-mode'>Select search mode</h3>
	        <label><input id='basicsearch' type="radio" name="searchMode" value="basic" checked> Classic search</label><br>
	        <label><input id='semantic' type="radio" name="searchMode" value="semantic"> Semantic search</label>
	        <div id="sw-table-container"></div>
	    `;
		 
		modal = document.createElement("div");
		modal.className = "sw-modal";
		modal.innerHTML = `
	      <div class="sw-modal-content">
	        ${rootDiv.outerHTML}
	        <div style="margin-top:15px; text-align:right;">
	          <div id="sw-close-btn"/>
	        </div>
	      </div>
	    `;
		
		var closeButton = GIAPI.FontAwesomeButton({
			'width': 70,
			'label': 'Close',
			'icon': 'fa-close',
			'handler': function() {
				
				modal.style.display = "none";
			}
		});
		
		document.body.appendChild(modal);
		
		$('#sw-close-btn').append(closeButton.div());
	
		const style = document.createElement("style");
		style.textContent = 
		`
	      .sw-modal {
	        display: none;
	        position: fixed;
	        top: -450px; 
			left: 0; 
			right: 0; 
			bottom: 0;
	        background: rgba(0, 0, 0, 0.3);
	        justify-content: center;
	        align-items: center;
	        z-index: 9999;
	      }
		  
	      .sw-modal-content {
	        background: #c2bebe;
	        padding: 10px;
			padding-top: 0px;     
	        width: 700px;
	        box-shadow: 0 4px 10px rgba(0,0,0,0.3);     
	      }
		  
		  #select-search-mode{
			 background-color: #1f2b38;
			 padding: 10px;
			 color: white;
		  }
		  
		  #ontology-table {
			  background-color: white;
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
             background-color: #9a98905e;
          }
	    `;
		modal.appendChild(style);
		
		tableContainer = modal.querySelector("#sw-table-container");
		 
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

	/**
	 * 
	 */
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
			console.error("Error occurred during fetch:", err);
			tableContainer.innerHTML = "<p style='color:red'>Error loading data</p>";
		}
	}

	/**
	 * 
	 */
	function renderTable(data) {
		if (!Array.isArray(data) || data.length === 0) {
			tableContainer.innerHTML = "<p>No ontologies found</p>";
			return;
		}

		const table = document.createElement("table");
		table.id = "ontology-table";

		const thead = document.createElement("thead");
		thead.innerHTML = "<tr><th></th><th>Name</th><th>Description</th><th>Endpoint</th></tr>";
		table.appendChild(thead);

		const tbody = document.createElement("tbody");
		data.forEach(item => {
			
			const tr = document.createElement("tr");
			const checkbox = document.createElement("input");
			
			checkbox.type = "checkbox";
			checkbox.checked = selectedIds.has(item.id);
			checkbox.addEventListener("change", () => {
				if (checkbox.checked){  					
					selectedIds.add(item.id);									
				}else{					
					selectedIds.delete(item.id);				
				}				
			});

			tr.innerHTML = `
	        <td></td>
	        <td>${item.name}</td>
			<td>${item.description ? item.description : ''}</td>
	        <td>${item.endpoint}</td>
	      
	        `;
			tr.cells[0].appendChild(checkbox);
			tbody.appendChild(tr);
		});

		table.appendChild(tbody);
		tableContainer.innerHTML = "";
		tableContainer.appendChild(table);
	}
 
	return {
		showDialog,
		getSelectedIds,
		div
	};
})();
