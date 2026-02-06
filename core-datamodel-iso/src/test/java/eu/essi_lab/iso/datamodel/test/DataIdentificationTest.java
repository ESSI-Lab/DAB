package eu.essi_lab.iso.datamodel.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDDataIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

public class DataIdentificationTest extends MetadataTest<DataIdentification, AbstractMDIdentificationType> {

    public DataIdentificationTest() {
	super(DataIdentification.class, MDDataIdentificationType.class);
    }

    // private XMLGregorianCalendar calendar;

    @Override
    public void init() throws Exception {
	// this.calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
    }

    @Override
    public void setProperties(DataIdentification dataId) {
	dataId.setAbstract("abs");
	dataId.setCharacterSetCode("characterSetCode");
	dataId.addCitationAlternateTitle("fakeTitle");

	dataId.setCitationAlternateTitle("altTitle");
	dataId.setCitationEditionDate("DATE");
	dataId.setCitationCreationDate("DATE");
	dataId.addCitationDate("My date", "creation");
	dataId.setCitationPublicationDate("DATE");
	dataId.setCitationRevisionDate("DATE");

	dataId.addLanguage("eng");
	// dataId.setCitationCreationDateTime(calendar);
	// dataId.setCitationPublicationDateTime(calendar);
	// dataId.setCitationRevisionDateTime(calendar);
	ResponsibleParty contact = new ResponsibleParty();
	contact.setIndividualName("Individual");
	contact.setRoleCode("custodian");
	dataId.addPointOfContact(contact);
	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("CitationParty");
	dataId.addCitationResponsibleParty(party);
	dataId.setCitationTitle("TITLE");
	dataId.setResourceIdentifier("ID");
	try {
	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
	    dataId.addCitationDateTime(calendar);
	} catch (DatatypeConfigurationException e) {
	    e.printStackTrace();
	}

	// ----------------------------
	//
	// spatial
	//
	dataId.addGeographicBoundingBox(0, 0, 0, 0);
	dataId.addGeographicBoundingBox("desc", 1, 1, 1, 1);
	GeographicBoundingBox box = new GeographicBoundingBox();
	box.setEast(2.);
	box.setWest(2.);
	box.setNorth(2.);
	box.setSouth(2.);
	dataId.addGeographicBoundingBox(box);

	BoundingPolygon polygon = new BoundingPolygon();
	polygon.setCoordinates(Arrays.asList(new Double[] { 0.0, 0.1 }));
	dataId.addBoundingPolygon(polygon);

	// ----------------------------
	//
	// temporal
	//
	dataId.addTemporalExtent("period", "BEGIN", "END");
	dataId.addTemporalExtent("BEGIN2", "END2");
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition("BEGIN3");
	temporalExtent.setEndPosition("END3");
	dataId.addTemporalExtent(temporalExtent);

	// ----------------------------
	//
	// keywords
	//
	dataId.addKeyword("kwd1");

	Keywords keywords = new Keywords();
	keywords.addKeyword("kwd2");
	keywords.setThesaurusNameCitationTitle("thesaurus");
	dataId.addKeywords(keywords);

	// -----------------------------
	//
	// vertical extent
	//

	VerticalExtent verticalExtent = new VerticalExtent();
	verticalExtent.setMinimumValue(-18.);
	verticalExtent.setMaximumValue(23.);

	dataId.addVerticalExtent(verticalExtent);

	dataId.addVerticalExtent(0, 5);

	// ----------------------------
	//
	// topic category
	//
	dataId.addTopicCategory(MDTopicCategoryCodeType.BIOTA);

	// spatial representation
	dataId.setSpatialRepresentationType("vector");

	LegalConstraints legal = new LegalConstraints();
	legal.addOtherConstraints("My Constraints");
	legal.addAccessConstraintsCode("patent");
	legal.addUseConstraintsCode("trademark");
	legal.addUseLimitation("My Use Limitation");
	dataId.addLegalConstraints(legal);

	BrowseGraphic browseGraphic = new BrowseGraphic();
	browseGraphic.setFileName("filename.png");
	dataId.addGraphicOverview(browseGraphic);
	dataId.setSupplementalInformation("supplemental info");

	MDResolution res = new MDResolution();
	res.setDistance("m", 10.);
	res.setEquivalentScale(new BigInteger("1000"));
	dataId.setSpatialResolution(res);
    }

    @Override
    public void checkProperties(DataIdentification dataId) {
	Assert.assertEquals(dataId.getAbstract(), "abs");

	String characterSetCode = dataId.getCharacterSetCode();
	Assert.assertEquals("characterSetCode", characterSetCode);

	Assert.assertEquals(dataId.getCitationAlternateTitle(), "altTitle");
	Assert.assertEquals(dataId.getCitationEditionDate(), "DATE");
	Assert.assertEquals(dataId.getCitationCreationDate(), "DATE");
	ArrayList<String> dates = Lists.newArrayList(dataId.getCitationDates("creation"));
	Assert.assertEquals("My date", dates.get(1));
	Assert.assertEquals(2, dates.size());
	dates = Lists.newArrayList(dataId.getCitationDates());
	Assert.assertEquals(5, dates.size());
	Assert.assertEquals(dataId.getCitationPublicationDate(), "DATE");
	Assert.assertEquals(dataId.getCitationRevisionDate(), "DATE");
	// Assert.assertEquals(dataId.getCitationCreationDateTime(), calendar);

	Assert.assertEquals(1, Lists.newArrayList(dataId.getLanguages()).size());
	Assert.assertEquals("eng", Lists.newArrayList(dataId.getLanguages()).get(0));

	Assert.assertEquals("Individual", dataId.getPointOfContact().getIndividualName());

	Assert.assertEquals("Individual", dataId.getPointOfContact("custodian").getIndividualName());

	Assert.assertEquals("CitationParty", dataId.getCitationResponsibleParties().get(0).getOrganisationName());

	// Assert.assertEquals(dataId.getCitationPublicationDateTime(), calendar);
	//
	// Assert.assertEquals(dataId.getCitationRevisionDateTime(), calendar);

	Assert.assertEquals(dataId.getCitationTitle(), "TITLE");

	Assert.assertEquals(dataId.getResourceIdentifier(), "ID");

	// ----------------------------
	//
	// spatial
	//

	Iterator<GeographicBoundingBox> boxesIterator = dataId.getGeographicBoundingBoxes();
	GeographicBoundingBox next = boxesIterator.next();
	Assert.assertEquals(next.getSouth(), new Double(0));
	next = boxesIterator.next();
	Assert.assertEquals(next.getSouth(), new Double(1));
	next = boxesIterator.next();
	Assert.assertEquals(next.getSouth(), new Double(2));

	GeographicBoundingBox geographicBoundingBox = dataId.getGeographicBoundingBox();
	Assert.assertEquals(geographicBoundingBox.getSouth(), new Double(0));

	Assert.assertEquals(new Double(dataId.getEN()[0]), new Double(0));
	Assert.assertEquals(new Double(dataId.getWS()[0]), new Double(0));

	BoundingPolygon polygon = dataId.getBoundingPolygons().next();
	ArrayList<Double> coordinates = Lists.newArrayList(polygon.getCoordinates());
	Assert.assertEquals(coordinates, Arrays.asList(new Double[] { 0.0, 0.1 }));

	// ----------------------------
	//
	// temporal
	//
	Iterator<TemporalExtent> temporalExtents = dataId.getTemporalExtents();
	TemporalExtent next2 = temporalExtents.next();
	Assert.assertEquals(next2.getBeginPosition(), "BEGIN");
	next2 = temporalExtents.next();
	Assert.assertEquals(next2.getBeginPosition(), "BEGIN2");
	next2 = temporalExtents.next();
	Assert.assertEquals(next2.getBeginPosition(), "BEGIN3");

	Assert.assertEquals(dataId.getTemporalExtent().getBeginPosition(), "BEGIN");

	// ----------------------------
	//
	// keywords
	//
	Iterator<Keywords> kwdIterator = dataId.getKeywords();
	Keywords kwd = kwdIterator.next();
	Assert.assertEquals(kwd.getKeywords().next(), "kwd1");
	kwd = kwdIterator.next();
	Assert.assertEquals(kwd.getKeywords().next(), "kwd2");
	Assert.assertEquals(kwd.getThesaurusNameCitationTitle(), "thesaurus");

	Iterator<String> kwdIterator2 = dataId.getKeywords("thesaurus");
	Assert.assertEquals(kwdIterator2.next(), "kwd2");

	ArrayList<String> keys = Lists.newArrayList(dataId.getKeywordsValues());
	Assert.assertEquals(2, keys.size());

	// -----------------------------
	//
	// vertical extent
	//

	Assert.assertEquals(dataId.getVerticalExtent().getMaximumValue(), new Double(23));
	Assert.assertEquals(dataId.getVerticalExtent().getMinimumValue(), new Double(-18.0));

	Assert.assertTrue(dataId.getVerticalExtents().hasNext());

	Assert.assertEquals(Lists.newArrayList(dataId.getVerticalExtents()).size(), 2);

	// ----------------------------
	//
	// topic category
	//
	Assert.assertEquals(dataId.getTopicCategoryString(), MDTopicCategoryCodeType.BIOTA.value());
	Assert.assertEquals(dataId.getTopicCategoriesStrings().next(), MDTopicCategoryCodeType.BIOTA.value());
	Assert.assertEquals(dataId.getTopicCategory(), MDTopicCategoryCodeType.BIOTA);
	Assert.assertEquals(dataId.getTopicCategories().next(), MDTopicCategoryCodeType.BIOTA);

	// spatial representation
	Assert.assertEquals("vector", dataId.getSpatialRepresentationTypeCodeListValue());

	Assert.assertEquals("My Constraints", dataId.getLegalConstraints().next().getOtherConstraint());
	Assert.assertEquals("My Constraints", dataId.getLegalConstraintsOthers().next());
	Assert.assertEquals("patent", dataId.getLegalConstraints().next().getAccessConstraintCode());
	Assert.assertEquals("patent", dataId.getLegalConstraintsAccessCodes().next());
	Assert.assertEquals("trademark", dataId.getLegalConstraints().next().getUseConstraintsCode());
	Assert.assertEquals("trademark", dataId.getLegalConstraintsUseCodes().next());
	Assert.assertEquals("My Use Limitation", dataId.getLegalConstraints().next().getUseLimitation());
	Assert.assertEquals("My Use Limitation", dataId.getLegalConstraintsUseLimitations().next());

	Assert.assertTrue(dataId.hasAccessLegalConstraints());
	Assert.assertTrue(dataId.hasOtherLegalConstraints());
	Assert.assertTrue(dataId.hasUseLegalConstraints());

	Assert.assertEquals("filename.png", dataId.getGraphicOverview().getFileName());
	Assert.assertEquals("filename.png", dataId.getGraphicOverviews().next().getFileName());
	Assert.assertEquals("supplemental info", dataId.getSupplementalInformation());

	MDResolution res = dataId.getSpatialResolution();
	Assert.assertEquals("m", res.getDistanceUOM());
	Assert.assertEquals(10., res.getDistanceValue(), 10 ^ -7);
	Assert.assertEquals(new BigInteger("1000"), res.getEquivalentScale());
    }

    @Override
    public void clearProperties(DataIdentification dataId) {
	dataId.setAbstract(null);
	dataId.setCharacterSetCode(null);
	dataId.clearCitationAlternateTitles();
	dataId.setCitationEditionDate(null);
	dataId.setCitationCreationDate(null);
	dataId.setCitationPublicationDate(null);
	dataId.setCitationRevisionDate(null);
	dataId.setCitationCreationDateTime(null);
	dataId.clearPointOfContacts();
	dataId.clearLanguages();
	dataId.setCitationPublicationDateTime(null);
	dataId.setCitationRevisionDateTime(null);
	dataId.setCitationTitle(null);
	dataId.setResourceIdentifier(null);
	dataId.clearCitationDates();
	dataId.clearExtents();
	dataId.clearKeywords();
	dataId.clearTopicCategories();
	dataId.setSpatialRepresentationType(null);
	dataId.clearResourceConstraints();
	dataId.clearGraphicOverviews();
	dataId.setSupplementalInformation(null);
	dataId.clearSpatialResolution();
    }

    @Override
    public void checkNullProperties(DataIdentification dataId) {
	Assert.assertNull(dataId.getAbstract());
	Assert.assertNull(dataId.getCharacterSetCode());
	Assert.assertNull(dataId.getCitationAlternateTitle());
	Assert.assertNull(dataId.getCitationEditionDate());
	Assert.assertNull(dataId.getCitationCreationDate());
	Assert.assertNull(dataId.getCitationPublicationDate());
	Assert.assertNull(dataId.getCitationRevisionDate());
	Assert.assertNull(dataId.getCitationCreationDateTime());
	Assert.assertNull(dataId.getCitationPublicationDateTime());
	Assert.assertNull(dataId.getCitationRevisionDateTime());
	Assert.assertNull(dataId.getTopicCategoryString());
	Assert.assertNull(dataId.getTopicCategory());

	Assert.assertFalse(dataId.getTemporalExtents().hasNext());
	Assert.assertFalse(dataId.getGeographicDescriptionCodes().hasNext());
	Assert.assertFalse(dataId.getKeywordTypes().hasNext());

	Assert.assertNull(dataId.getPointOfContact());
	Assert.assertNull(dataId.getPointOfContact("custodian"));
	Assert.assertNull(dataId.getCitationTitle());
	Assert.assertNull(dataId.getResourceIdentifier());

	Assert.assertNull(dataId.getCitationCreationDate());
	Assert.assertNull(dataId.getCitationPublicationDate());
	Assert.assertNull(dataId.getCitationRevisionDate());
	Assert.assertNull(dataId.getCitationCreationDateTime());
	Assert.assertNull(dataId.getCitationPublicationDateTime());
	Assert.assertNull(dataId.getCitationRevisionDateTime());

	Iterator<String> languagesIterator = dataId.getLanguages();
	Assert.assertFalse(languagesIterator.hasNext());

	// ----------------------------
	//
	// spatial
	//

	Iterator<GeographicBoundingBox> boxesIterator = dataId.getGeographicBoundingBoxes();
	Assert.assertEquals(boxesIterator.hasNext(), false);
	Assert.assertNull(dataId.getGeographicBoundingBox());
	Assert.assertNull(dataId.getWS());
	Assert.assertNull(dataId.getEN());

	Iterator<BoundingPolygon> polyIterator = dataId.getBoundingPolygons();
	Assert.assertEquals(polyIterator.hasNext(), false);

	// ----------------------------
	//
	// temporal
	//

	Iterator<TemporalExtent> temporalExtents = dataId.getTemporalExtents();
	Assert.assertEquals(temporalExtents.hasNext(), false);
	Assert.assertNull(dataId.getTemporalExtent());

	// ----------------------------
	//
	// keywords
	//

	Iterator<Keywords> kwdIterator = dataId.getKeywords();
	Assert.assertFalse(kwdIterator.hasNext());
	Assert.assertFalse(dataId.getKeywordsValues().hasNext());

	// -----------------------------
	//
	// vertical extent
	//

	Assert.assertFalse(dataId.getVerticalExtents().hasNext());

	Assert.assertFalse(dataId.getTopicCategories().hasNext());

	Assert.assertNull(dataId.getVerticalExtent());

	// spatial representation
	Assert.assertNull(dataId.getSpatialRepresentationTypeCodeListValue());

	Assert.assertFalse(dataId.getLegalConstraints().hasNext());
	Assert.assertFalse(dataId.getLegalConstraintsAccessCodes().hasNext());
	Assert.assertFalse(dataId.getLegalConstraintsOthers().hasNext());
	Assert.assertFalse(dataId.getLegalConstraintsUseCodes().hasNext());
	Assert.assertFalse(dataId.getLegalConstraintsUseLimitations().hasNext());

	Assert.assertFalse(dataId.hasAccessLegalConstraints());
	Assert.assertFalse(dataId.hasOtherLegalConstraints());
	Assert.assertFalse(dataId.hasUseLegalConstraints());
	Assert.assertFalse(dataId.hasSecurityConstraints());

	Assert.assertFalse(dataId.getGraphicOverviews().hasNext());
	Assert.assertNull(dataId.getGraphicOverview());
	Assert.assertNull(dataId.getSupplementalInformation());

	Assert.assertNull(dataId.getSpatialResolution());

    }

}
