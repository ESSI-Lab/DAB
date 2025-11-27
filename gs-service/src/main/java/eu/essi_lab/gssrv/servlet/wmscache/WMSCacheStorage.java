package eu.essi_lab.gssrv.servlet.wmscache;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

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

public interface WMSCacheStorage {

    public List<String>getViews();
    
    public List<String>getLayers(String view);
    
    public Response getCachedResponse(String view, String layer, String hash);

    public void putCachedResponse(String view, String layer, String hash, File file);

    public Date getCachedResponseDate(String view, String layer, String hash);

    public void deleteCachedResponse(String view, String layer, String hash);

    public Integer getSize();
    
    public Integer getMaxSize();

    public void setMaxSize(Integer size);

    public Integer getSize(String view, String layer);
}
