package eu.essi_lab.downloader.hiscentral;

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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataProperty;
import eu.essi_lab.wml._2.WML2QualityCategory;

/**
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataDownloaderTest {

    @Test
    public void validityTest() {

	LombardiaOpenDataProperty level = LombardiaOpenDataProperty.LIVELLO_IDROMETRICO;
	Assert.assertTrue(HISCentralLombardiaOpenDataDownloader.isValid(level, new BigDecimal("-50")));
	Assert.assertTrue(HISCentralLombardiaOpenDataDownloader.isValid(level, new BigDecimal("888")));
	Assert.assertFalse(HISCentralLombardiaOpenDataDownloader.isValid(level, new BigDecimal("-9999")));
	Assert.assertFalse(HISCentralLombardiaOpenDataDownloader.isValid(level, new BigDecimal("-999")));

	LombardiaOpenDataProperty direction = LombardiaOpenDataProperty.DIREZIONE_VENTO;
	Assert.assertTrue(HISCentralLombardiaOpenDataDownloader.isValid(direction, new BigDecimal("270")));
	Assert.assertFalse(HISCentralLombardiaOpenDataDownloader.isValid(direction, new BigDecimal("777")));
	Assert.assertFalse(HISCentralLombardiaOpenDataDownloader.isValid(direction, new BigDecimal("8888")));
    }

    @Test
    public void qualityTest() {

	Assert.assertEquals(WML2QualityCategory.GOOD, HISCentralLombardiaOpenDataDownloader.getQuality("VA"));
	Assert.assertEquals(WML2QualityCategory.POOR, HISCentralLombardiaOpenDataDownloader.getQuality("NA"));
	Assert.assertEquals(WML2QualityCategory.SUSPECT, HISCentralLombardiaOpenDataDownloader.getQuality("NI"));
	Assert.assertNull(HISCentralLombardiaOpenDataDownloader.getQuality("1"));
    }
}
