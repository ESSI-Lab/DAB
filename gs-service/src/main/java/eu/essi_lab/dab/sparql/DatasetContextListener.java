package eu.essi_lab.dab.sparql;

import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class DatasetContextListener implements ServletContextListener {

    private static final String RDF_GZ_URL = "https://www.eionet.europa.eu/gemet/latest/gemet.rdf.gz";

	
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	GSLoggerFactory.getLogger(getClass()).info("Downloading GEMET rdf");
    	
    	 try {
             // Step 1: Download and decompress the .ttl.gz file
             InputStream in = new URL(RDF_GZ_URL).openStream();
             GZIPInputStream gzipIn = new GZIPInputStream(in);

             // Step 2: Load into Jena
             Dataset dataset = DatasetFactory.createTxnMem();
             Model defaultModel = dataset.getDefaultModel();
             
             System.out.println("Model size: " + defaultModel.size());

             defaultModel.read(gzipIn, null, "RDF/XML");
             
             System.out.println("Model size: " + defaultModel.size());


             // Step 3: Store in context
             sce.getServletContext().setAttribute("dataset", dataset);


             System.out.println("Dataset loaded from compressed RDF at " + RDF_GZ_URL);
             
             Object ds = sce.getServletContext().getAttribute("dataset");
             System.out.println("SPARQL servlet sees dataset: " + ds);
             
             

         } catch (Exception e) {
             throw new RuntimeException("Failed to initialize RDF dataset", e);
         }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Dataset dataset = (Dataset) sce.getServletContext().getAttribute("org.apache.jena.fuseki.Dataset");
        if (dataset != null) dataset.close();
    }
}
