/**
 * 
 */
package eu.essi_lab.cdk.utils.test;

import java.net.URL;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;

/**
 * @author Fabrizio
 */
public class WAFClientExternalTestIT {

    @Test
    public void deepListFilesTest() throws Exception {

	String url = "https://essilab.eu/schemas/GI-cat/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<URL> listFiles = client.deepListFiles();

	Assert.assertEquals(18, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.deepListFiles(new WAF_URL(new URL(url)));

	Assert.assertEquals(18, listFiles.size());

    }

    @Test
    public void deepListFilesFileFilterTest() throws Exception {

	String url = "https://essilab.eu/schemas/GI-cat/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<URL> listFiles = client.deepListFiles(u -> u.toString().endsWith(".txt"));

	Assert.assertEquals(1, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.deepListFiles(new WAF_URL(new URL(url)), u -> u.toString().endsWith(".txt"));

	Assert.assertEquals(1, listFiles.size());

    }

    @Test
    public void deepListFilesFileAndFoldersFilterTest() throws Exception {

	String url = "https://essilab.eu/schemas/GI-cat/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<URL> listFiles = client.deepListFiles(//
		fileUrl -> fileUrl.toString().contains("DataModel"), //
		wafUrl -> wafUrl.toString().endsWith("3.0/")); //

	Assert.assertEquals(2, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.deepListFiles(//
		new WAF_URL(new URL(url)), //
		fileUrl -> fileUrl.toString().contains("DataModel"), //
		wafUrl -> wafUrl.toString().endsWith("3.0/"));

	Assert.assertEquals(2, listFiles.size());
    }

    @Test
    public void deepListFolders() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<WAF_URL> listFolders = client.deepListFolders(); //

	Assert.assertEquals(75, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.deepListFolders(new WAF_URL(new URL(url))); // );

	Assert.assertEquals(75, listFolders.size());
    }

    @Test
    public void deepListFoldersFoldersFilter() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<WAF_URL> listFolders = client.deepListFolders(wafUrl -> wafUrl.toString().contains("xlink")); //

	Assert.assertEquals(2, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.deepListFolders(new WAF_URL(new URL(url)), wafUrl -> wafUrl.toString().contains("xlink"));

	Assert.assertEquals(2, listFolders.size());
    }

    @Test
    public void listFilesFileFilterTest() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/csw/2.0.2/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<URL> listFiles = client.listFiles(fileUrl -> fileUrl.toString().endsWith(".txt")); //

	Assert.assertEquals(1, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.listFiles(new WAF_URL(new URL(url)), fileUrl -> fileUrl.toString().endsWith(".txt")); // );

	Assert.assertEquals(1, listFiles.size());

    }

    @Test
    public void listFilesTest() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/csw/2.0.2/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<URL> listFiles = client.listFiles(); //

	Assert.assertEquals(6, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.listFiles(new WAF_URL(new URL(url))); // );

	Assert.assertEquals(6, listFiles.size());
    }

    @Test
    public void listFoldersTest() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/csw/2.0.2/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<WAF_URL> listFolders = client.listFolders(); //

	Assert.assertEquals(2, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.listFolders(new WAF_URL(new URL(url))); // );

	Assert.assertEquals(2, listFolders.size());

    }

    @Test
    public void listRootFoldersTestWithAbsolutePathReference() throws Exception {

	String url = "https://onamet.gov.do/ema/";

	int year = Calendar.getInstance().get(Calendar.YEAR);

	int expectedFolders = year - 2019 + 2;

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<WAF_URL> listFolders = client.listFolders(); //

	Assert.assertEquals(expectedFolders, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.listFolders(new WAF_URL(new URL(url)), true);

	Assert.assertEquals(expectedFolders, listFolders.size());
    }

    @Test
    public void listFoldersTestWithAbsolutePathReference() throws Exception {

	String url = "https://onamet.gov.do/ema/2020/";

	int expectedFolders = 341;

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<WAF_URL> listFolders = client.listFolders(); //

	Assert.assertEquals(expectedFolders, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.listFolders(new WAF_URL(new URL(url)), true);

	Assert.assertEquals(expectedFolders, listFolders.size());
    }

    @Test
    public void listFilesTestWithAbsolutePathReference() throws Exception {

	String url = "https://onamet.gov.do/ema/2020/001/";

	int expectedFiles = 9;

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<URL> listFiles = client.listFiles(); //

	Assert.assertEquals(expectedFiles, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.listFiles(new WAF_URL(new URL(url)), true);

	Assert.assertEquals(expectedFiles, listFiles.size());
    }

    @Test
    public void deepListFilesTestWithAbsolutePathReference() throws Exception {

	String url = "https://onamet.gov.do/ema/2020";

	int minExpectedFiles = 2900;// there are 341 folders, some with 9 files, some others with 8 files
	int maxExpectedFiles = 3100;

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<URL> listFiles = client.deepListFiles(); //

	int filesCount = listFiles.size();

	//
	//
	//

	listFiles = WAFClient.deepListFiles(new WAF_URL(new URL(url)), true);

	Assert.assertTrue(filesCount <= maxExpectedFiles && filesCount >= minExpectedFiles);
    }

    @Test
    public void deepListFilesTestWithAbsolutePathReferenceAnd_CSV_Filter() throws Exception {

	String url = "https://onamet.gov.do/ema/";

	int expectedFiles = 2;// inside the metadatos folder

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<URL> listFiles = client.deepListFiles(u -> u.toString().endsWith(".csv")); //

	Assert.assertEquals(expectedFiles, listFiles.size());

	//
	//
	//

	listFiles = WAFClient.deepListFiles(new WAF_URL(new URL(url)), u -> u.toString().endsWith(".csv"), true);

	Assert.assertEquals(expectedFiles, listFiles.size());
    }

    @Test
    public void deepListFilesTestWithAbsolutePathReferenceAnd_CVS_Filter() throws Exception {

	String url = "https://onamet.gov.do/ema/";

	int expectedMinimumFiles = 5840;//

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<URL> listFiles = client.deepListFiles(u -> u.toString().endsWith(".cvs")); //

	Assert.assertTrue(listFiles.size() >= expectedMinimumFiles);

	//
	//
	//

	listFiles = WAFClient.deepListFiles(new WAF_URL(new URL(url)), u -> u.toString().endsWith(".cvs"), true);

	Assert.assertTrue(listFiles.size() >= expectedMinimumFiles);
    }

    @Test
    public void deepListFoldersTestWithAbsolutePathReference() throws Exception {

	String url = "https://onamet.gov.do/ema/";

	int expectedMinimumFolders = 655;//

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<WAF_URL> listFolders = client.deepListFolders(); //

	Assert.assertTrue(listFolders.size() >= expectedMinimumFolders);

	//
	//
	//

	listFolders = WAFClient.deepListFolders(new WAF_URL(new URL(url)), true);

	Assert.assertTrue(listFolders.size() >= expectedMinimumFolders);
    }

    @Test
    public void deepListFoldersTestWithAbsolutePathReferenceAndFilter() throws Exception {

	String url = "https://onamet.gov.do/ema/";

	int expectedMinimumFolders = 650;//

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	client.setUseAbsolutePathReference(true);

	List<WAF_URL> listFolders = client.deepListFolders(u -> !u.toString().contains("metadatos")); //

	Assert.assertTrue(listFolders.size() >= expectedMinimumFolders);

	//
	//
	//

	listFolders = WAFClient.deepListFolders(new WAF_URL(new URL(url)), u -> !u.toString().contains("metadatos"), true);

	Assert.assertTrue(listFolders.size() >= expectedMinimumFolders);
    }

    @Test
    public void listFoldersFoldersFilterTest() throws Exception {

	String url = "https://essilab.eu/schemas/sdi-services/4.0/";

	//
	//
	//

	WAFClient client = new WAFClient(new URL(url));

	List<WAF_URL> listFolders = client.listFolders(wafUrl -> wafUrl.toString().contains("beta")); //

	Assert.assertEquals(1, listFolders.size());

	//
	//
	//

	listFolders = WAFClient.listFolders(new WAF_URL(new URL(url)), wafUrl -> wafUrl.toString().contains("beta")); // );

	Assert.assertEquals(1, listFolders.size());
    }

    @Test
    public void test() throws Exception {

	String demoUrl = "https://essi-lab.eu/projects/demo/";

	WAFClient dl = new WAFClient(new URL(demoUrl));

	List<URL> listFiles = dl.listFiles();
	Assert.assertEquals(10, listFiles.size());

	List<WAF_URL> directories = dl.listFolders(); // 15
	Assert.assertEquals(15, directories.size());

	for (WAF_URL wAF_URL : directories) {

	    if (wAF_URL.toString().contains("lobster-pivot")) {

		List<URL> lobsterPivotFiles = WAFClient.listFiles(wAF_URL);// 3
		Assert.assertEquals(3, lobsterPivotFiles.size());

		List<WAF_URL> lobsterPivotDirectories = WAFClient.listFolders(wAF_URL); // 5
		Assert.assertEquals(5, lobsterPivotDirectories.size());
	    }
	}

	String docsUrl = "https://essi-lab.eu/documents/reference-data/";

	dl = new WAFClient(new URL(docsUrl));

	List<URL> listAllFiles = dl.deepListFiles();
	Assert.assertEquals(6335, listAllFiles.size());
    }
}
