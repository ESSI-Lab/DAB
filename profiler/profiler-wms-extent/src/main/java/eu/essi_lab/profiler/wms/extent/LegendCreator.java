package eu.essi_lab.profiler.wms.extent;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.awt.Color;
import java.util.List;
import java.util.Random;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.profiler.wms.extent.legend.InfoLegend;

public abstract class LegendCreator {
    public abstract List<InfoLegend> getLegend(String layers, StationRecord station);
    
    public Color getColor(String layers) {
	int seed = layers.hashCode();
	Random random = new Random(seed);
	float hue = randomInt(random, 0, 360);
	float saturation = randomInt(random, 42, 98);
	float luminance = randomInt(random, 40, 90);
	final Color color = Color.getHSBColor(hue, saturation, luminance);
	return color;
    }

    private static int randomInt(Random random, Integer min, Integer max) {
	return (int) (Math.floor(random.nextDouble() * (max - min + 1)) + min);
    }
}
