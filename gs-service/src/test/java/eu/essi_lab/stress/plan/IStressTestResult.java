package eu.essi_lab.stress.plan;

/**
 * @author Mattia Santoro
 */
public interface IStressTestResult {
    void setCode(Integer code);

    void setRequest(String requestString);

    void setStart(Long start);

    void setEnd(Long end);

    void setTest(IStressTest test);

    void setResponseFile(String filePath);
}
