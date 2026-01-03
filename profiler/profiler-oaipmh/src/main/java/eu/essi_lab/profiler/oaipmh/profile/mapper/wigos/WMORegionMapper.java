package eu.essi_lab.profiler.oaipmh.profile.mapper.wigos;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class WMORegionMapper {

    public enum WMORegion {
        I("africa", 1),
        II("asia", 2),
        III("southAmerica", 3),
        IV("northCentralAmericaCaribbean", 4),
        V("southWestPacific", 5),
        VI("europe", 6),
        VII("antarctica", 7);

        private final String description;
        private final int code;

        WMORegion(String description, int code) {
            this.description = description;
            this.code = code; 
        }

        public String getDescription() {
            return description;
        }
        
        public int getCode() {
            return code;
        }

        public static WMORegion fromCode(String code) {
            return WMORegion.valueOf(code);
        }
    }
    
    
    private static Map<String, WMORegion> iso3ToRegion;

    private static synchronized void loadMapping() {
        if (iso3ToRegion != null) return; // already loaded
        iso3ToRegion = new HashMap<>();
        try (InputStream in = WMORegionMapper.class.getResourceAsStream("/wmo_regions.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());
            for (String iso3 : json.keySet()) {
                String regionCode = json.getString(iso3);
                iso3ToRegion.put(iso3.toUpperCase(), WMORegion.fromCode(regionCode));
            }

        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load WMO region mapping JSON", e);
        }
    }

    public static WMORegion getRegion(String iso3Code) {
        if (iso3ToRegion == null) {
            loadMapping();
        }
        if (iso3Code == null || iso3Code.length() != 3) {
            return null;
        }
        WMORegion region = iso3ToRegion.get(iso3Code.toUpperCase());
        if (region == null) {
            return null;
        }
        return region;
    }
    
    
    public static void main(String[] args) {
        System.out.println(getRegion("ITA").getCode()); // 1
        System.out.println(getRegion("USA").getDescription()); // Region IV
        System.out.println(getRegion("AUS").getDescription()); // Region V
    }

}
