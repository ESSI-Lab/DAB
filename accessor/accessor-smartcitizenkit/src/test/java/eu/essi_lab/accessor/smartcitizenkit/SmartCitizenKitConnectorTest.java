package eu.essi_lab.accessor.smartcitizenkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.smartcitizenkit.SmartCitizenKitConnector;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import junit.framework.TestCase;

public class SmartCitizenKitConnectorTest {

    private SmartCitizenKitConnector connector;
    private GSSource source;

    @Before
    public void init() {

	this.connector = new SmartCitizenKitConnector();

	this.source = Mockito.mock(GSSource.class);
    }


}
