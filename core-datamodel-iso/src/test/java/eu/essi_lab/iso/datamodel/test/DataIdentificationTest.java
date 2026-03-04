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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
	assertEquals("abs", dataId.getAbstract());

	String characterSetCode = dataId.getCharacterSetCode();
	assertEquals("characterSetCode", characterSetCode);

	assertEquals("altTitle", dataId.getCitationAlternateTitle());
	assertEquals("DATE", dataId.getCitationEditionDate());
	assertEquals("DATE", dataId.getCitationCreationDate());
	ArrayList<String> dates = Lists.newArrayList(dataId.getCitationDates("creation"));
	assertEquals("My date", dates.get(1));
	assertEquals(2, dates.size());
	dates = Lists.newArrayList(dataId.getCitationDates());
	assertEquals(5, dates.size());
	assertEquals("DATE", dataId.getCitationPublicationDate());
	assertEquals("DATE", dataId.getCitationRevisionDate());
	// Assert.assertEquals(dataId.getCitationCreationDateTime(), calendar);

	assertEquals(1, Lists.newArrayList(dataId.getLanguages()).size());
	assertEquals("eng", Lists.newArrayList(dataId.getLanguages()).getFirst());

	assertEquals("Individual", dataId.getPointOfContact().getIndividualName());

	assertEquals("Individual", dataId.getPointOfContact("custodian").getIndividualName());

	assertEquals("CitationParty", dataId.getCitationResponsibleParties().getFirst().getOrganisationName());

	// Assert.assertEquals(dataId.getCitationPublicationDateTime(), calendar);
	//
	// Assert.assertEquals(dataId.getCitationRevisionDateTime(), calendar);

	assertEquals("TITLE", dataId.getCitationTitle());

	assertEquals("ID", dataId.getResourceIdentifier());

	// ----------------------------
	//
	// spatial
	//

	Iterator<GeographicBoundingBox> boxesIterator = dataId.getGeographicBoundingBoxes();
	GeographicBoundingBox next = boxesIterator.next();
	assertEquals(Double.valueOf(0), next.getSouth());
	next = boxesIterator.next();
	assertEquals(Double.valueOf(1), next.getSouth());
	next = boxesIterator.next();
	assertEquals(Double.valueOf(2), next.getSouth());

	GeographicBoundingBox geographicBoundingBox = dataId.getGeographicBoundingBox();
	assertEquals(Double.valueOf(0), geographicBoundingBox.getSouth());

	assertEquals(Double.valueOf(0), Double.valueOf(dataId.getEN()[0]));
	assertEquals(Double.valueOf(0), Double.valueOf(dataId.getWS()[0]));

	BoundingPolygon polygon = dataId.getBoundingPolygons().next();

	ArrayList<Double> coordinates = Lists.newArrayList(polygon.getCoordinates());

	assertEquals(coordinates, Arrays.asList(new Double[] { 0.0, 0.1 }));

	// ----------------------------
	//
	// temporal
	//
	Iterator<TemporalExtent> temporalExtents = dataId.getTemporalExtents();
	TemporalExtent next2 = temporalExtents.next();
	assertEquals("BEGIN", next2.getBeginPosition());
	next2 = temporalExtents.next();
	assertEquals("BEGIN2", next2.getBeginPosition());
	next2 = temporalExtents.next();
	assertEquals("BEGIN3", next2.getBeginPosition());

	assertEquals("BEGIN", dataId.getTemporalExtent().getBeginPosition());

	// ----------------------------
	//
	// keywords
	//
	Iterator<Keywords> kwdIterator = dataId.getKeywords();
	Keywords kwd = kwdIterator.next();
	assertEquals("kwd1", kwd.getKeywords().next());
	kwd = kwdIterator.next();
	assertEquals("kwd2", kwd.getKeywords().next());
	assertEquals("thesaurus", kwd.getThesaurusNameCitationTitle());

	Iterator<String> kwdIterator2 = dataId.getKeywords("thesaurus");
	assertEquals("kwd2", kwdIterator2.next());

	ArrayList<String> keys = Lists.newArrayList(dataId.getKeywordsValues());
	assertEquals(2, keys.size());

	// -----------------------------
	//
	// vertical extent
	//

	assertEquals(Double.valueOf(23), dataId.getVerticalExtent().getMaximumValue());
	assertEquals(Double.valueOf(-18.0), dataId.getVerticalExtent().getMinimumValue());

	Assert.assertTrue(dataId.getVerticalExtents().hasNext());

	assertEquals(2, Lists.newArrayList(dataId.getVerticalExtents()).size());

	// ----------------------------
	//
	// topic category
	//
	assertEquals(MDTopicCategoryCodeType.BIOTA.value(), dataId.getTopicCategoryString());
	assertEquals(MDTopicCategoryCodeType.BIOTA.value(), dataId.getTopicCategoriesStrings().next());
	assertEquals(MDTopicCategoryCodeType.BIOTA, dataId.getTopicCategory());
	assertEquals(MDTopicCategoryCodeType.BIOTA, dataId.getTopicCategories().next());

	// spatial representation
	assertEquals("vector", dataId.getSpatialRepresentationTypeCodeListValue());

	assertEquals("My Constraints", dataId.getLegalConstraints().next().getOtherConstraint());
	assertEquals("My Constraints", dataId.getLegalConstraintsOthers().next());
	assertEquals("patent", dataId.getLegalConstraints().next().getAccessConstraintCode());
	assertEquals("patent", dataId.getLegalConstraintsAccessCodes().next());
	assertEquals("trademark", dataId.getLegalConstraints().next().getUseConstraintsCode());
	assertEquals("trademark", dataId.getLegalConstraintsUseCodes().next());
	assertEquals("My Use Limitation", dataId.getLegalConstraints().next().getUseLimitation());
	assertEquals("My Use Limitation", dataId.getLegalConstraintsUseLimitations().next());

	Assert.assertTrue(dataId.hasAccessLegalConstraints());
	Assert.assertTrue(dataId.hasOtherLegalConstraints());
	Assert.assertTrue(dataId.hasUseLegalConstraints());

	assertEquals("filename.png", dataId.getGraphicOverview().getFileName());
	assertEquals("filename.png", dataId.getGraphicOverviews().next().getFileName());
	assertEquals("supplemental info", dataId.getSupplementalInformation());

	MDResolution res = dataId.getSpatialResolution();
	assertEquals("m", res.getDistanceUOM());
	assertEquals(10., res.getDistanceValue(), 10 ^ -7);
	assertEquals(new BigInteger("1000"), res.getEquivalentScale());
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
	assertFalse(boxesIterator.hasNext());
	Assert.assertNull(dataId.getGeographicBoundingBox());
	Assert.assertNull(dataId.getWS());
	Assert.assertNull(dataId.getEN());

	Iterator<BoundingPolygon> polyIterator = dataId.getBoundingPolygons();
	assertFalse(polyIterator.hasNext());

	// ----------------------------
	//
	// temporal
	//

	Iterator<TemporalExtent> temporalExtents = dataId.getTemporalExtents();
	assertFalse(temporalExtents.hasNext());
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
