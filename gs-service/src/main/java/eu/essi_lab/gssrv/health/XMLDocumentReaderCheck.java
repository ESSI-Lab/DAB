package eu.essi_lab.gssrv.health;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class XMLDocumentReaderCheck {

    private static Integer errorsCount = 0;

    public static int getErrorsCount() {
	return errorsCount;
    }

    public static void check() {
	Thread t = new Thread() {
	    @Override
	    public void run() {
		TaskListExecutor<Boolean> tle = new TaskListExecutor<Boolean>(1);
		tle.addTask(new Callable<Boolean>() {

		    @Override
		    public Boolean call() throws Exception {
			String xml = "<test/>";
			try {
			    new XMLDocumentReader(xml);
			    return true;
			} catch (Exception e) {
			    e.printStackTrace();
			}
			return false;
		    }
		});
		List<Future<Boolean>> ret = tle.executeAndWait(120);
		if (ret.isEmpty()) {

		} else {
		    try {
			Boolean result = ret.get(0).get();
			if (result) {
			    return;
			}
		    } catch (Exception e) {
		    }
		}
		errorsCount++;
		return;
	    }
	};
	t.start();

    }

}
