/**
 * 
 */
package eu.essi_lab.accessor.aviso.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.essi_lab.accessor.aviso.AVISOMapper;
import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.OriginalMetadata;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author Fabrizio
 */
public class AVISOConnectorAndMapperTest {

    /**
     * 
     */
    public AVISOConnectorAndMapperTest() {
    }

    public static void main(String[] args) {

	FTPDownloader ftpDownloader = new FTPDownloader();
	List<String> names = ftpDownloader.//
		downloadFileNames("ftp://ftp.aviso.altimetry.fr/pub/oceano/AVISO/indicators/msl/").//
		stream().filter(n -> n.endsWith("nc")).//
		collect(Collectors.toList());

	for (int i = 0; i < names.size(); i++) {

	    String fileName = names.get(i);

	    String storedFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + fileName;

	    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + storedFileName);

	    file = ftpDownloader.downloadToFile("ftp://ftp.aviso.altimetry.fr/pub/oceano/AVISO/indicators/msl/", fileName, file, false);

	    try {

		NetcdfDataset dataset = NetcdfDataset.openDataset(file.toString());

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(AVISOMapper.METADATA_SCHEMA);
		metadata.setMetadata(dataset.toString());
		
		try {
		    dataset.release();
		    dataset.close();

		} catch (IOException e) {
		    GSLoggerFactory.getLogger(AVISOMapper.class).error(e.getMessage(), e);
		}

		GSPropertyHandler propertyHandler = new GSPropertyHandler();
		propertyHandler.add(new GSProperty<File>(AVISOMapper.NET_CDF_FILE_PROPERTY, file));
 
		metadata.setAdditionalInfo(propertyHandler);

		AVISOMapper avisoMapper = new AVISOMapper();
		GSSource source = new GSSource();
		source.setLabel("AVISO");
		source.setEndpoint("ftp://ftp.aviso.altimetry.fr/pub/oceano/AVISO/indicators/msl/");

		avisoMapper.map(metadata, source);

	    } catch (Exception ex) {

		ex.printStackTrace();
	    }

	}
    }

}
