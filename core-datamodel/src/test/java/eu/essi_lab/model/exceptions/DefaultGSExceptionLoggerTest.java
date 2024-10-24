//package eu.essi_lab.model.exceptions;
//
//import java.io.IOException;
//
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//
///**
// * @author ilsanto
// */
//public class DefaultGSExceptionLoggerTest {
//
//    private static final String UNKNOWN_ERROR_TEST = "UNKNOWN_ERROR_TEST";
//
//    /**
//     * This is ignored because when running from maven the stack trace is actuallky different from the one used here.
//     */
//    @Test
//    @Ignore
//    public void testPrintToString() {
//
//	GSException gse = GSException.createException(DefaultGSExceptionLoggerTest.class, "Unknown error", null,
//		ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, UNKNOWN_ERROR_TEST, new IOException());
//
//	gse.log();
//
//	System.out.println("");
//	System.out.println("%%");
//	System.out.println("");
//
//	String s = DefaultGSExceptionLogger.printToString(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(gse)));
//	System.out.println(s);
//	Assert.assertEquals("Found Alien Exception (java.io.IOException) with message null\n" + "java.io.IOException\n"
//		+ "\tat eu.essi_lab.model.exceptions.DefaultGSExceptionLoggerTest.testPrintToString(DefaultGSExceptionLoggerTest"
//		+ ".java:18)\n" + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
//		+ "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n"
//		+ "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
//		+ "\tat java.lang.reflect.Method.invoke(Method.java:497)\n"
//		+ "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)\n"
//		+ "\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)\n"
//		+ "\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)\n"
//		+ "\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)\n"
//		+ "\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)\n"
//		+ "\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)\n"
//		+ "\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)\n"
//		+ "\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)\n"
//		+ "\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)\n"
//		+ "\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)\n"
//		+ "\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)\n"
//		+ "\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)\n"
//		+ "\tat org.junit.runners.ParentRunner.run(ParentRunner.java:363)\n"
//		+ "\tat org.junit.runner.JUnitCore.run(JUnitCore.java:137)\n"
//		+ "\tat com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)\n"
//		+ "\tat com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)\n"
//		+ "\tat com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)\n"
//		+ "\tat com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)\n"
//		+ "Error Message: Unknown errorError CODE: 1;eu.essi_lab.model.exceptions"
//		+ ".DefaultGSExceptionLoggerTest:UNKNOWN_ERROR_TEST", s);
//
//    }
//}