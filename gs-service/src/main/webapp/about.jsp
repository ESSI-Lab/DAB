<%@ page import="java.util.Properties, java.io.InputStream" %>
<%
  Properties buildProps = new Properties();
  try (InputStream is = getServletContext().getClassLoader().getResourceAsStream("META-INF/build.properties")) {
    if (is != null) {
      buildProps.load(is);
    }
  } catch (Exception e) {
    // Handle error if needed
  }

  String timestamp = buildProps.getProperty("build.timestamp", "N/A");
  String gitCommit = buildProps.getProperty("git.commit.id", "N/A");
%>

<h2>About DAB</h2>
<ul>  
  <li><strong>Description:</strong> <%= buildProps.getProperty("project.description", "N/A") %></li>
  <li><strong>Project Name:</strong> <%= buildProps.getProperty("project.name", "N/A") %></li>
  <li><strong>Group ID:</strong> <%= buildProps.getProperty("project.groupId", "N/A") %></li>
  <li><strong>Artifact ID:</strong> <%= buildProps.getProperty("project.artifactId", "N/A") %></li>
  <li><strong>Version:</strong> <%= buildProps.getProperty("project.version", "N/A") %></li>
  <li><strong>Build Timestamp:</strong> <%= buildProps.getProperty("build.timestamp", "N/A") %></li>
  <li><strong>Git Commit:</strong> <%= buildProps.getProperty("git.commit.id", "N/A") %></li>
</ul>
