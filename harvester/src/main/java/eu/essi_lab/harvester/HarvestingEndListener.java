package eu.essi_lab.harvester;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

/**
 * Listener notified at the end of each metadata harvesting procedure, registered with
 * {@link HarvestingNotifier#addListener(HarvestingEndListener)}.<br> Listeners are invoked synchronously in the
 * harvesting thread, so long-running activities should be executed asynchronously. Exceptions thrown by listeners are
 * logged and ignored
 *
 * @author boldrini
 */
@FunctionalInterface
public interface HarvestingEndListener {

    /**
     * @param summary the summary of the ended harvesting procedure
     */
    void harvestingEnded(HarvestingNotifier summary);
}
