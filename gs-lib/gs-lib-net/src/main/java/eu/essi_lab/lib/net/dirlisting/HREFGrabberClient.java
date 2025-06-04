/**
 * 
 */
package eu.essi_lab.lib.net.dirlisting;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.net.downloader.Downloader;

/**
 * @author Fabrizio
 */
public class HREFGrabberClient {

    protected static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    protected static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

    protected URL url;
    protected String html;
    private String closingTag;
    protected String username;
    protected String password;

    /**
     * 
     */
    public HREFGrabberClient() {

    }

    /**
     * @param url
     * @throws IOException
     */
    public HREFGrabberClient(URL url) {

	this.url = url;
    }

    /**
     * @param url
     * @throws IOException
     */
    public HREFGrabberClient(String html) {

	this.html = html;
    }

    /**
     * Set a closing tag of the &lt;a&gt; element different from &lt;/a&gt;.<br>
     * Use this setting in case the HTML page
     * do not correctly close the &lt;a&gt; element, for example the &lt;a&gt; element is closed with a &lt;/td&gt;
     * element.:<br>
     * &lt;td&gt;&lt;a
     * href="daily/WV03_SWIR_ImageLibraryStrips_Jul25.zip"&gt;WV03_SWIR_ImageLibraryStrips_Jul25.zip&lt;/td&gt;.
     * 
     * @param closingTag
     */
    public void setHREF_A_ClosingTag(String closingTag) {

	this.closingTag = closingTag;
    }

    /**
     * @return
     * @throws Exception
     */
    public List<String> grabLinks() throws Exception {

	return grabLinks(null);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<String> grabLinks(String linkText) throws Exception {

	return grabLinks(this.url, linkText).//
		stream().//
		map(l -> externalizeLink(url, l)).//
		collect(Collectors.toList());
    }

    /**
     * @param url
     * @return
     * @throws Exception
     */
    public List<String> grabLinks(URL url, String linkText) throws Exception {

	Downloader downloader = new Downloader();

	ArrayList<String> list = Lists.newArrayList();

	
	String html_ = "";
	if(this.username != null && this.password != null) {
	    html_ = downloader.downloadOptionalString(url.toExternalForm(), username, password).orElse(null);
	} else {
	    html_ = this.html != null ? this.html : downloader.downloadOptionalString(url.toExternalForm()).orElse(null);
	}
	if (html_ == null) {
	    return list;
	}

	String pattern = HTML_A_TAG_PATTERN;
	if (this.closingTag != null) {
	    pattern = HTML_A_TAG_PATTERN.replace("</a>", closingTag);
	}

	Pattern aPatternTag = Pattern.compile(pattern);
	Pattern hrefPatternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	Matcher matcher = aPatternTag.matcher(html_);

	while (matcher.find()) {

	    String href = matcher.group(1); // href
	    // link text is correct ONLY with closed <a> elements!
	    String linkText_ = matcher.group(2);

	    if (linkText == null || (linkText != null && linkText.equals(linkText_))) {

		Matcher matcherLink = hrefPatternLink.matcher(href);

		while (matcherLink.find()) {

		    String link = matcherLink.group(1).replace("\"", "").replace("'", "");
		    list.add(link);
		}
	    }
	}

	return list;
    }

    /**
     * @param url
     * @param link
     * @param absolutePathReference
     * @return
     */
    protected String externalizeLink(URL url, String link) {

	return externalizeLink(url, link, false);
    }

    /**
     * @param url
     * @param link
     * @param absolutePathReference
     * @return
     */
    protected String externalizeLink(URL url, String link, boolean absolutePathReference) {

	if (url == null) {
	    return link;
	}

	if (!absolutePathReference) {

	    String externalForm = url.toExternalForm();
	    if (!externalForm.endsWith("/")) {

		externalForm += "/";
	    }

	    link = externalForm + link;

	} else {

	    link = url.toString().replace(url.getPath(), "/" + link);
	}

	return link;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
