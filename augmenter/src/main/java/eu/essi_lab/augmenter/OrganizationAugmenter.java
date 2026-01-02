package eu.essi_lab.augmenter;

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

import java.util.Optional;

import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * sets the point of contact organization metadata element
 * 
 * @author boldrini
 */
public class OrganizationAugmenter extends ResourceAugmenter<OrganizationNameSetting> {

    /**
     * 
     */
    public OrganizationAugmenter() {
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource STARTED");

	String organization = getSetting().getOrganizationName().orElse(null);

	if (organization != null) {

	    ResponsibleParty poc = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getPointOfContact();

	    if (poc != null) {
		resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearPointOfContacts();
	    }

	    ResponsibleParty party = new ResponsibleParty();
	    party.setOrganisationName(organization);
	    party.setRoleCode("pointOfContact");
	    resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addPointOfContact(party);
	}

	GSLoggerFactory.getLogger(getClass()).warn("Metadata augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public String getType() {

	return "OrganizationAugmenter";
    }

    @Override
    protected OrganizationNameSetting initSetting() {

	return new OrganizationNameSetting();
    }

    @Override
    protected String initName() {

	return "Organization augmenter";
    }
}
