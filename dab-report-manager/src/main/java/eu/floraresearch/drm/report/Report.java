package eu.floraresearch.drm.report;

import java.util.List;

import eu.essi_lab.model.GSSource;

public interface Report {

    public int[] refineRecordsCount();

    void init(String sourceID, List<GSSource> list);

    String getShortName();

    String getCompleteName();

    boolean underTest();

    void setUnderTest(boolean value);

    int getCategory_1_Value() throws Exception;

    int getCategory_2_Value() throws Exception;

    int getCategory_3_Value() throws Exception;

    int getCategory_4_Value() throws Exception;

    /**
     * Source type
     */
    String getCategory_5_Value() throws Exception;

    /**
     * Valid
     */
    int getCategory_6_Value() throws Exception;

    /**
     * Not valid
     */
    int getCategory_7_Value() throws Exception;

    /**
     * Repaired
     */
    int getCategory_8_Value() throws Exception;

    /**
     * Not repaired
     */
    int getCategory_9_Value() throws Exception;

    /**
     * New From
     */
    int getCategory_10_Value() throws Exception;

    /**
     * Deleted
     */
    int getCategory_11_Value() throws Exception;

    /**
     * Not Validated
     */
    int getCategory_12_Value() throws Exception;

    String getComments();

}
