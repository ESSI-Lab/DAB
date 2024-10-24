//package eu.essi_lab.accessor.waf;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.Test;
//
//import eu.essi_lab.accessor.waf.onamet.ONAMETConnector;
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.lib.utils.IOStreamUtils;
//
///**
// * @author Fabrizio
// */
//public class MergeTimeTest {
//
//    @Test
//    public void test1() throws IOException, InterruptedException {
//
//	File workingDir = new File("/tmp/onamet-nc");
//
//	//
//	//
//	//
//	StringBuilder builder = new StringBuilder();
//
//	String mergedFileName = "pippo.nc";
//
//	builder.append("cdo -W mergetime ");
//
//	List<File> files = Arrays.asList(workingDir.listFiles(new FileFilter() {
//
//	    @Override
//	    public boolean accept(File pathname) {
//
//		return pathname.getName().endsWith(".nc");
//	    }
//	}));
//
////	files = files.subList(0, 9); // max 8
//
//	for (File file : files) {
//
//	    builder.append(" " + file.getName());
//	}
//
//	builder.append(" " + mergedFileName);
//
//	String command = builder.toString();
//
//	GSLoggerFactory.getLogger(getClass()).debug("Command:");
//	GSLoggerFactory.getLogger(getClass()).debug("{}", command);
//
//	//
//	//
//	//
//
//	Runtime rt = Runtime.getRuntime();
//
//	Process ps = rt.exec(command, null, workingDir);
//
//	int exitVal = ps.waitFor();
//
//	GSLoggerFactory.getLogger(getClass()).debug("Exit val: {}", exitVal);
//
//	if (exitVal > 0) {
//
//	    GSLoggerFactory.getLogger(getClass()).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));
//	}
//
//	ps.destroy();
//    }
//    
////    @Test
//    public void test2() throws IOException, InterruptedException {
//
//	File workingDir = new File("/tmp/onamet-nc");
//
//	//
//	//
//	//
//	 
//	String command = "/bin/bash -c /tmp/onamet-nc/script.sh";
//
//	GSLoggerFactory.getLogger(getClass()).debug("Command:");
//	GSLoggerFactory.getLogger(getClass()).debug("{}", command);
//
//	//
//	//
//	//
//
//	Runtime rt = Runtime.getRuntime();
//
//	Process ps = rt.exec(command, null, workingDir);
//
//	int exitVal = ps.waitFor();
//
//	GSLoggerFactory.getLogger(getClass()).debug("Exit val: {}", exitVal);
//
//	if (exitVal > 0) {
//
//	    GSLoggerFactory.getLogger(getClass()).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));
//	}
//
//	ps.destroy();
//    }
//
//    // @Test
//    // public void test2() {
//    //
//    // Runtime rt = Runtime.getRuntime();
//    //
//    // // String[] s = new String[] { "/bin/bash -c", "cdo mergetime *.nc " + fileName };
//    //
//    // // chmod +x script.sh
//    //
//    // // String command = "/bin/bash -c \""+workingDir.getAbsolutePath() + "/script.sh " + fileName+"\"";
//    //
//    // StringBuilder builder = new StringBuilder();
//    //
//    // builder.append("cdo mergetime ");
//    //
//    // List<File> files = Arrays.asList(workingDir.listFiles(new FileFilter() {
//    //
//    // @Override
//    // public boolean accept(File pathname) {
//    //
//    // return pathname.getName().endsWith(".nc");
//    // }
//    // }));
//    //
//    // files = files.subList(0, 15);
//    //
//    // for (File file : files) {
//    //
//    // // builder.append(" " + file.getName());
//    // }
//    //
//    // builder.append(" " + mergedOutput.getName());
//    //
//    // // String command = builder.toString();
//    //
//    // String command = "cdo mergetime /tmp/onamet-nc/*.nc /tmp/onamet-nc/" + mergedOutput.getName();
//    //
//    // // /tmp/onamet-nc/script.sh out.nc
//    //
//    // // /bin/bash -c /tmp/onamet-nc/script.sh /tmp/onamet-nc/2022022500_d01_.merged.nc
//    //
//    // GSLoggerFactory.getLogger(getClass()).debug("Command:");
//    // GSLoggerFactory.getLogger(getClass()).debug("{}", command);
//    //
//    // // String[] s = new String[] { command };
//    //
//    // // Process ps = rt.exec(command, null, workingDir);
//    //
//    // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", command);
//    //
//    // Process ps = processBuilder.start();
//    //
//    // // while (!mergedOutput.exists()) {
//    // //
//    // // Thread.sleep(1000);
//    // //
//    // // GSLoggerFactory.getLogger(getClass()).debug("Sleeping");
//    // // }
//    //
//    // // GSLoggerFactory.getLogger(getClass()).debug("!!!! DONE !!!!!");
//    //
//    // int exitVal = ps.waitFor();
//    //
//    // GSLoggerFactory.getLogger(getClass()).debug("Exit val: {}", exitVal);
//    //
//    // if (exitVal > 0) {
//    //
//    // GSLoggerFactory.getLogger(ONAMETConnector.class).error(IOStreamUtils.asUTF8String(ps.getErrorStream()));
//    // }
//    //
//    // ps.destroy();
//    // }
//}
