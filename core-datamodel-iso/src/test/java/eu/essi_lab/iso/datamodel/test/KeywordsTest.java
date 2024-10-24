package eu.essi_lab.iso.datamodel.test;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordsType;

public class KeywordsTest extends MetadataTest<Keywords, MDKeywordsType> {

    public KeywordsTest() {
	super(Keywords.class, MDKeywordsType.class);
    }

    @Override
    public void setProperties(Keywords keywords) {

	keywords.addKeyword("kwd1");
	keywords.addKeyword("kwd2");
	keywords.addKeyword("kwd3");

	keywords.addKeywords(Arrays.asList(new String[] { "kwd1", "kwd2" }));

	keywords.setThesaurusNameCitationTitle("title");
	keywords.addThesaurusNameCitationAlternateTitle("altTitle");

	keywords.setTypeCode("keywordType");

    }

    @Override
    public void checkProperties(Keywords keywords) {

	Iterator<String> keywords3 = keywords.getKeywords();
	String next = keywords3.next();
	Assert.assertEquals(next, "kwd1");
	next = keywords3.next();
	Assert.assertEquals(next, "kwd2");
	next = keywords3.next();
	Assert.assertEquals(next, "kwd3");

	next = keywords3.next();
	Assert.assertEquals(next, "kwd1");
	next = keywords3.next();
	Assert.assertEquals(next, "kwd2");

	Assert.assertEquals(keywords.getThesaurusNameCitationAlternateTitles().next(), "altTitle");
	Assert.assertEquals(keywords.getThesaurusNameCitationTitle(), "title");

	Assert.assertEquals("keywordType", keywords.getTypeCode());

    }

    @Override
    public void clearProperties(Keywords keywords) {

	keywords.clearKeywords();

    }

    @Override
    public void checkNullProperties(Keywords keywords) {

	Iterator<String> keywords2 = keywords.getKeywords();
	Assert.assertEquals(keywords2.hasNext(), false);

    }
}
