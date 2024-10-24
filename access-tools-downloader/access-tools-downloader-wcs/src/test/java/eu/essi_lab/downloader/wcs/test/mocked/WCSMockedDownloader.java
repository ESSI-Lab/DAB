package eu.essi_lab.downloader.wcs.test.mocked;

import java.util.AbstractMap.SimpleEntry;

public interface WCSMockedDownloader {
    public SimpleEntry<String, String> getCoverageUrlFile();

    public String getCapabilitiesFile();

    public String getCoverageDescription();
}
