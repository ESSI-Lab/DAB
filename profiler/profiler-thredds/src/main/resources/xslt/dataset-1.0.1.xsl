<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">   
    <xsl:param name="id">urn-uuid-99cef850-6802-481a-9b66-02ecb64c2c6e</xsl:param>
    
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
                <title> Catalog Services</title>
                <!--link rel='stylesheet' href='/thredds/tds.css' type='text/css' /-->
                <style>
                    body{
                    font-weight: normal;
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    text-decoration: none;
                    margin-top: 0px;
                    }
                    
                    td {
                    font-weight: normal;
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    text-decoration: none
                    }
                    
                    
                    
                    A:link {
                    font-family: Arial, Helvetica, sans-serif;
                    color: #0000FF
                    
                    }
                    
                    A:visited {
                    font-family: Arial, Helvetica, sans-serif;
                    color: #A020F0
                    }
                    
                    A:active {
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FF0000;
                    }
                    
                    a:hover {
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FF0000;
                    text-decoration: underline
                    }
                    
                    
                    A { text-decoration: none }
                    
                    
                    b {
                    font-weight: bold;
                    font-family: Arial, Helvetica, sans-serif;
                    }
                    
                    i {
                    font-style: italic;
                    font-family: Arial, Helvetica, sans-serif;
                    }
                    
                    
                    
                    a.navtitle:link {
                    font-size: 10pt;
                    font-weight: bold;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    }
                    
                    
                    a.navtitle:hover {
                    font-size: 10pt;
                    font-weight: bold;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    text-decoration: underline
                    }
                    
                    a.navtitle:visited {
                    font-size: 10pt;
                    font-weight: bold;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    }
                    
                    
                    a.bnav:link {
                    font-size: 8pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    }
                    
                    
                    a.bnav:hover {
                    font-size: 8pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    text-decoration: underline
                    }
                    
                    a.bnav:visited {
                    font-size: 8pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    }
                    
                    
                    a.menu:link {
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    }
                    
                    a.menu:hover {
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    text-decoration: underline
                    }
                    
                    a.menu:visited {
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    }
                    
                    
                    
                    .menutitle {
                    font-size: 9pt;
                    font-weight: bold;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    text-decoration: none;
                    }
                    
                    .menutitle2 {
                    font-size: 9pt;
                    font-weight: normal;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    text-decoration: none;
                    }
                    
                    
                    td.greentab {
                    background-image: url('/img/tabgreen.gif');
                    background-repeat: no-repeat;
                    background-position: center;
                    }
                    
                    
                    td.bluetab {
                    background-image: url('/img/tabblue.gif');
                    background-repeat: no-repeat;
                    background-position: center;
                    }
                    
                    
                    
                    td.t {
                    background-image: url('/img/horizontal.gif');
                    background-repeat: no-repeat;
                    background-position: left;
                    }
                    
                    .cat_title {
                    font-weight: bold;
                    font-size: 10pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #FFFFFF;
                    
                    }
                    
                    
                    .sub_g {
                    font-weight: bold;
                    font-size: 12pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    border-bottom:4px solid #CCCCCC
                    }
                    
                    .sub_b {
                    font-weight: bold;
                    font-size: 12pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    border-bottom:4px solid #006699
                    }
                    
                    
                    h2 {
                    font-size: 17pt;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #9C9C9C;
                    }
                    
                    small {
                    font-size: 9pt;
                    font-weight: normal;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    }
                    
                    
                    
                    small.imageinc {
                    font-size: 9pt;
                    font-weight: normal;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #000000;
                    text-align: justify;
                    }
                 </style>            
            </head>
            <body>
                <table width='100%'>
                    <tr>
                        <td>
                            <img src='http://api.geodab.eu/docs/assets/img/iia.png' alt='CNR-IIA' align='left' valign='top' hspace='10' vspace='2'/>
                            <h3><strong><a href='catalog.html'>Discovery and Access Broker (DAB)</a></strong></h3>
                            <!--h3><strong><a href='https://www.unidata.ucar.edu/software/tds/current/TDS.html'>THREDDS Data Server</a></strong></h3-->
                    </td></tr>
                </table>
                <h2>Catalog ${URL}</h2>
        
              <xsl:apply-templates select="//*:dataset[@ID=$id]"></xsl:apply-templates>
            </body>            
        </html>        
    </xsl:template>  
    
    <xsl:template match="*:dataset">        
      
        <h2><xsl:value-of select="@name"/></h2>
        <ul>
            <li><em>Data format: </em><xsl:value-of select="*:dataFormat"/>NetCDF</li>
            <li><em>Data type: </em><xsl:value-of select="*:dataType"/></li>
            <!--li><em>Naming Authority: </em>edu.ucar.unidata</li-->
            <li><em>ID: </em><xsl:value-of select="@ID"/></li>
        </ul>
        <h3>Documentation:</h3>
        <ul>
            <xsl:for-each select="*:documentation">
                <xsl:choose>
                    <xsl:when test="@type">
                        <li><strong><xsl:value-of select="@type"/>:</strong> <xsl:value-of select="."/></li>        
                    </xsl:when>
                    <xsl:when test="@*:title">
                        <li><xsl:element name="a">
                            <xsl:attribute name="href">
                                <xsl:value-of select="@*:href"/>
                            </xsl:attribute>
                            <xsl:value-of select="@*:title"/>
                        </xsl:element></li>
                    </xsl:when>
                </xsl:choose>                    
            </xsl:for-each>
        </ul>
        
        <h3>Access:</h3>
        <ol>
            <xsl:variable name="path" >
                <xsl:value-of select="@urlPath"/>
            </xsl:variable>
                
            <xsl:for-each select="ancestor-or-self::*[*:service]">  
                
                <xsl:for-each select=".//*:service[@serviceType!='Compound'][@serviceType!='OPENDAP']">
                    <xsl:variable name="url">
                        <xsl:text>${HOSTNAME}</xsl:text>
                        <xsl:value-of select="@base"/><xsl:value-of select="$path"/>
                    </xsl:variable>
                    <li> <b><xsl:value-of select="@serviceType"/>:&#160;</b> 
                        <xsl:element name="a">
                            <xsl:attribute name="href"><xsl:value-of select="$url"/>
                                <xsl:choose>
                                    <xsl:when test="@serviceType='WMS'"><xsl:text disable-output-escaping="yes">?service=WMS&amp;request=GetCapabilities</xsl:text></xsl:when>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:value-of select="$url"/>   
                        </xsl:element>
                    </li>    
                </xsl:for-each>
                
                
            </xsl:for-each>
            
        </ol>
        
        <xsl:if test="*:creator">
        <h3>Creators:</h3>
        <ul>            
            <xsl:for-each select="*:creator">
                <li><strong><xsl:value-of select="*:name"/></strong><ul>
                    <li><em>email: </em><xsl:value-of select="*:contact/@email"/></li>
                    <li> <em>
                        <xsl:element name="a">
                            <xsl:attribute name="href"><xsl:value-of select="*:contact/@url"/></xsl:attribute>
                            <xsl:value-of select="*:contact/@url"/>
                        </xsl:element>
                    </em></li>                        
                </ul></li>    
            </xsl:for-each>            
        </ul>
        </xsl:if>
        
        <xsl:if test="*:publisher">
        <h3>Publishers:</h3>
        <ul>
            <xsl:for-each select="*:publisher">
                <li><strong><xsl:value-of select="*:name"/></strong><ul>
                    <li><em>email: </em><xsl:value-of select="*:contact/@email"/></li>
                    <li> <em>
                        <xsl:element name="a">
                            <xsl:attribute name="href"><xsl:value-of select="*:contact/@url"/></xsl:attribute>
                            <xsl:value-of select="*:contact/@url"/>
                        </xsl:element>
                    </em></li>                        
                </ul></li>    
            </xsl:for-each>            
        </ul>
        </xsl:if>
        
        <xsl:if test="*:variableMap">
        <h3>Variables:</h3>
        <ul>
        <xsl:for-each select="*:variableMap">
            <li>                
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:value-of select="@href"/>
                    </xsl:attribute>
                    <xsl:text>VariableMap</xsl:text>
                </xsl:element>
            </li>
        </xsl:for-each>            
        </ul>       
        </xsl:if>

        <h3>GeospatialCoverage:</h3>
        <ul>
            <li><em> Longitude: </em> <xsl:value-of select="*:metadata/*:geospatialCoverage/*:eastwest/*:start"/> to <xsl:value-of select="*:metadata/*:geospatialCoverage/*:eastwest/*:size"/> <xsl:if test="*:metadata/*:geospatialCoverage/*:eastwest/*:resolution">Resolution=<xsl:value-of select="*:metadata/*:geospatialCoverage/*:eastwest/*:resolution"/></xsl:if> degrees_east</li>
            <li><em> Latitude: </em> <xsl:value-of select="*:metadata/*:geospatialCoverage/*:northsouth/*:start"/> to <xsl:value-of select="*:metadata/*:geospatialCoverage/*:northsouth/*:size"/> <xsl:if test="*:metadata/*:geospatialCoverage/*:eastwest/*:resolution">Resolution=-<xsl:value-of select="*:metadata/*:geospatialCoverage/*:northsouth/*:resolution"/></xsl:if> degrees_north</li>                        
        </ul>
        <h3>TimeCoverage:</h3>
        <ul>
            <li><em>  Start: </em> <xsl:value-of select="*:metadata/*:timeCoverage/*:start"/></li>
            <li><em>  End: </em> <xsl:value-of select="*:metadata/*:timeCoverage/*:end"/></li>
        </ul>
        <h3>Properties:</h3>
        <ul>
            <xsl:for-each select=".//*:property">                
                <li><xsl:value-of select="@name"/> = &quot;<xsl:value-of select="@value"/>"</li>
            </xsl:for-each>
        </ul>
        <!--h3>Viewers:</h3>
                <ul>
                    <li> <a href='/thredds/godiva2/godiva2.html?server=https://thredds.ucar.edu/thredds/wms/grib/NCEP/GEFS/Global_1p0deg_Ensemble/members-analysis/TP'>Godiva2 (browser-based)</a></li>
                    <li> <a href='/thredds/view/ToolsUI.jnlp?catalog=https://thredds.ucar.edu/thredds/catalog/grib/NCEP/GEFS/Global_1p0deg_Ensemble/members-analysis/catalog.xml&amp;dataset=grib/NCEP/GEFS/Global_1p0deg_Ensemble/members-analysis/TP'>NetCDF-Java ToolsUI (webstart)</a></li>
                    <li> <a href='/thredds/view/idv.jnlp?url=https://thredds.ucar.edu/thredds/dodsC/grib/NCEP/GEFS/Global_1p0deg_Ensemble/members-analysis/TP'>Integrated Data Viewer (IDV) (webstart)</a></li>
                </ul-->
      
    </xsl:template>    
    
</xsl:stylesheet>

