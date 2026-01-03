package eu.essi_lab.gssrv.servlet.wmscache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Map.Entry;

public interface WMSCacheStats {

    public List<String>getViews();
    
    public List<String>getLayers(String view);
    
    /**
     * Increments usage counter for a given reqid.
     */
    public long incrementUsage(String view, String layer, String reqid);

    /**
     * Adds one usage point to the global leaderboard.
     */
    public void updateLeaderboard(String view, String layer, String reqid);

    /**
     * Stores metadata related to a reqid (e.g. canonical JSON, timestamps).
     */
    public void storeRequest(String view, String layer, String reqid, String request);

    /**
     * Fetch metadata for a reqid.
     */
    public String loadRequest(String view, String layer, String reqid);

    /**
     * Retrieves top N most-used reqids from the leaderboard.
     */
    public List<Entry<String, Double>> getTopRequests(String view, String layer, int limit);
}
