<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">   
    
    <xsl:output
        method="html"
        doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
        doctype-system="http://www.w3.org/TR/html4/loose.dtd"
        omit-xml-declaration="yes"
        encoding="UTF-8"
        indent="yes" />
    
    <xsl:template match="/*:catalog">
        <html>
            <head>
                <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>
                <title>Catalog ${URL}</title>
                <!--link rel='stylesheet' href='/thredds/tdsCat.css' type='text/css' /-->
                <style>
                 BODY {
                 font-family: Tahoma, Arial, sans-serif;
                 color: black;
                 background-color: white;
                 }
                 
                 H1 {
                 font-family: Tahoma, Arial, sans-serif;
                 color: white;
                 background-color: #525D76;
                 font-size: 26px;
                 }
                 
                 H2 {
                 font-family: Tahoma, Arial, sans-serif;
                 color: white;
                 background-color: #525D76;
                 font-size: 16px;
                 }
                 
                 H3 {
                 font-family: Tahoma, Arial, sans-serif;
                 color: white;
                 background-color: #525D76;
                 font-size: 14px;
                 }
                 
                 B {
                 font-family: Tahoma, Arial, sans-serif;
                 color: white;
                 background-color: #525D76;
                 }
                 
                 P {
                 font-family: Tahoma,Arial,sans-serif;
                 background: white;
                 color: black;
                 font-size: 12px;
                 }
                 
                 A {
                 color : black;
                 }
                 
                 A.name {
                 color : black;
                 }
                 
                 HR {
                 color : #525D76;
                 }                
                 </style>            
            </head>
            <body>
                <h1><img src='http://api.geodab.eu/docs/assets/img/iia.png' alt='CNR-IIA' align='left' valign='top' />&#160;Catalog ${URL}</h1>
                <HR size='1' noshade='noshade'/>
                <table width='100%' cellspacing='0' cellpadding='5' align='center'>
                    <tr>
                        <th align='left'><font size='+1'>Dataset</font></th>
                        <th align='center'><font size='+1'>Size</font></th>
                        <th align='right'><font size='+1'>Last Modified</font></th>
                    </tr>
                    <xsl:apply-templates select="*:dataset | *:catalogRef" ></xsl:apply-templates>
                </table>
                <HR size='1' noshade='noshade' />
                <h3>
                    <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:text>${URL}</xsl:text>
                        </xsl:attribute>
                        <xsl:text>THREDDS Data Server</xsl:text>
                    </xsl:element>
                    <xsl:text> at </xsl:text><a href='http://essi-lab.eu/'>ESSI-Lab of CNR-IIA</a> see <a href='http://essi-lab.eu'> Info </a><br/>Discovery and Access Broker (DAB)<a href='https://www.geodab.net/'> Documentation</a></h3>
            </body>            
        </html>        
    </xsl:template>  
    
    <xsl:template match="*:dataset | *:catalogRef">        
        <xsl:variable name="spaces"><xsl:for-each select="ancestor::*"><xsl:text>&#160;&#160;&#160;&#160;</xsl:text></xsl:for-each></xsl:variable>
        <tr>
            <td align='left'><xsl:value-of select="$spaces"/>
                <xsl:text>&#xa;</xsl:text>
                <xsl:choose>
                    <xsl:when test="local-name(.)='catalogRef' or *:dataset or *:catalogRef"><img src='/gs-service/icons/folder.png' alt='Folder' /> &#160;</xsl:when>
                    <xsl:otherwise><img src='/gs-service/icons/file.png' alt='File' /> &#160;</xsl:otherwise>
                </xsl:choose>
                <xsl:variable name="link">                
                <xsl:choose>                    
                    <xsl:when test="@*:href">
                                <xsl:value-of select="replace(@*:href,'.xml','.html')"/>
                    </xsl:when>
                    <xsl:when test="@urlPath">
                                <xsl:text>catalog.html?id=</xsl:text><xsl:value-of select="@urlPath"/>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
                    
                </xsl:variable>
                <xsl:element name="a">                    
                    <xsl:attribute name="href">
                        <xsl:value-of select="$link"/>
                    </xsl:attribute>
                    <tt><xsl:value-of select="@*:title | @name"/></tt>
                </xsl:element>        
                    </td>
            <td align='right'><tt>&#160;</tt></td>
            <td align='right'><tt>--</tt></td>
        </tr>
        
        <xsl:apply-templates select="*:dataset | *:catalogRef" ></xsl:apply-templates>
    </xsl:template>    
    
</xsl:stylesheet>

