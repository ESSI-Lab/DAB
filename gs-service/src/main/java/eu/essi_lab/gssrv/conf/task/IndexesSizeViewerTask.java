package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.indexes.IndexedResourceElements;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class IndexesSizeViewerTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Indexes size viewer task STARTED");

	DecimalFormat format = new DecimalFormat();
	format.setMaximumFractionDigits(3);
	format.setGroupingUsed(true);
	format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ITALIAN));

	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);
	MarkLogicDatabase dataBase = ((MarkLogicDatabase) dbReader.getDatabase());

	List<IndexedElementInfo> supportedIndexes = IndexedMetadataElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC);
	supportedIndexes.addAll(IndexedElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));
	supportedIndexes.addAll(IndexedResourceElements.getIndexesInfo(DatabaseImpl.MARK_LOGIC));

	supportedIndexes = supportedIndexes.stream()

		.filter(i -> !i.getElementName().equals(MetadataElement.ANY_TEXT.getName())
			&& !i.getElementName().equals(MetadataElement.ONLINE_LINKAGE.getName())
			&& !i.getElementName().equals(ResourceProperty.PRIVATE_ID.getName()))
		.collect(Collectors.toList());

	String[][] table = new String[supportedIndexes.size() + 1][2];
	table[0][0] = "Index";
	table[0][1] = "Size";

	List<String> info = new ArrayList<>();

	for (int i = 0; i < supportedIndexes.size(); i++) {

	    String elementName = supportedIndexes.get(i).getElementName();

	    String query = "count(cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','" + elementName + "')))";

	    String size = dataBase.execXQuery(query).asString();

	    info.add("- [" + elementName + "][" + size + "]");

	    GSLoggerFactory.getLogger(getClass()).debug("- [" + elementName + "][" + size + "]");
	}

	info.sort((v1, v2) -> Integer.valueOf(v2.split("\\]")[1].replace("[", ""))
		.compareTo(Integer.valueOf(v1.split("\\]")[1].replace("[", ""))));

	for (int i = 0; i < info.size(); i++) {

	    String msg = info.get(i);

	    status.addInfoMessage(msg);

	    String elementName = msg.split("\\]")[0].replace("[", "").replace("-", "").trim();
	    String size = msg.split("\\]")[1].replace("[", "");

	    table[i + 1][0] = elementName;
	    table[i + 1][1] = size;
	}

	String htmlTable = createHTMLTable(table, format);

	File target = new File(System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis() + "_indexesSize.html");

	log(status, "- Writing table to:\n " + target.getAbsolutePath());

	FileOutputStream outputStream = new FileOutputStream(target);
	outputStream.write(htmlTable.getBytes(StandardCharsets.UTF_8));
	outputStream.flush();
	outputStream.close();

	CustomTaskSetting setting = retrieveSetting(context);
	Optional<String> taskOptions = setting.getTaskOptions();

	if (taskOptions.isPresent()) {

	    String option = taskOptions.get();
	    String browserPath = option.replace("browserPath:", "").trim();

	    Runtime.getRuntime().exec(new String[] { browserPath, target.getAbsolutePath() });
	}

	log(status, "Indexes size viewer task ENDED");
    }

    @Override
    public String getName() {

	return "Indexes size viewer";
    }

    /**
     * @param table
     * @return
     */
    private String createHTMLTable(String[][] table, DecimalFormat format) {

	StringBuilder builder = new StringBuilder();

	builder.append("<html>");
	builder.append("<head>");
	builder.append("<style>");
	builder.append("#table {");
	builder.append("  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;");
	builder.append("  border-collapse: collapse;");
	builder.append("  width: 100%;");
	builder.append("}");
	builder.append("#table td, #table th {");
	builder.append(" border: 1px solid #ddd;");
	builder.append("  padding: 8px;");
	builder.append("}");
	builder.append("#table tr:nth-child(even){background-color: #f2f2f2;}");
	builder.append("#table tr:hover {background-color: #ddd;}");
	builder.append("#table th {");
	builder.append("  padding-top: 12px;");
	builder.append("  padding-bottom: 12px;");
	builder.append("  text-align: left;");
	builder.append("  background-color: #4CAF50;");
	builder.append("  color: white;");
	builder.append("}");
	builder.append("</style>");
	builder.append("</head>");

	builder.append("<body>");

	builder.append("<script>");
	builder.append("var asc = true;");
	builder.append("function sortTable(selectedCol) {");
	builder.append("  var table, rows, switching, i, x, y, shouldSwitch;");
	builder.append("  table = document.getElementById('table');");
	builder.append("  switching = true;");
	builder.append(" while (switching) {");
	builder.append("   switching = false;");
	builder.append("    rows = table.rows;");
	builder.append("    for (i = 1; i < (rows.length - 1); i++) {");
	builder.append("      shouldSwitch = false;");
	builder.append("      x = rows[i].getElementsByTagName('TD')[selectedCol];");
	builder.append("      y = rows[i + 1].getElementsByTagName('TD')[selectedCol];");
	builder.append("      if(asc){");
	builder.append("        if (parseInt(x.innerHTML.replace(/[.]/g,'')) > parseInt(y.innerHTML.replace(/[.]/g,''))) {");
	builder.append("          shouldSwitch = true;");
	builder.append("          break;");
	builder.append("        }");
	builder.append("      }else{");
	builder.append("       if (parseInt(x.innerHTML.replace(/[.]/g,'')) < parseInt(y.innerHTML.replace(/[.]/g,''))) {");
	builder.append("         shouldSwitch = true;");
	builder.append("         break;");
	builder.append("       }");
	builder.append("      }");
	builder.append("    }");
	builder.append("    if (shouldSwitch) {");
	builder.append("      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);");
	builder.append("      switching = true;");
	builder.append("    }");
	builder.append("  }");
	builder.append("  asc = !asc;");
	builder.append("}");
	builder.append("</script>");

	builder.append("<table id='table' style='width:100%'>");

	int rows = table.length;
	int cols = table[0].length;

	for (int row = 0; row < rows; row++) {

	    builder.append("<tr>");

	    for (int col = 0; col < cols; col++) {

		String value = table[row][col];
		if (row > 0 && col == 1) {
		    value = format.format(Double.valueOf(value));
		}
		if (row == 0 && col == 1) {
		    builder.append("<th><button style='width: 100%;' onclick='sortTable(" + col + ")'>Sort</button><br>");
		} else if (row == 0 && col == 0) {
		    builder.append("<th>");
		} else {
		    builder.append("<td>");
		}

		builder.append(value);
		builder.append(row == 0 ? "</th>" : "</td>");
	    }

	    builder.append("</tr>");
	}

	builder.append("</table>");

	builder.append("</table></body></html>");

	return builder.toString();
    }
}
