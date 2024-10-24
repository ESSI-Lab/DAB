<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:gmi="http://www.isotc211.org/2005/gmi"
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0"
    xmlns:gsr="http://www.isotc211.org/2005/gsr"
    xmlns:msg="http://imaa.cnr.it/sdi/services/7.0/messages/schema"
    xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
    xmlns:gmx="http://www.isotc211.org/2005/gmx"
   
    xmlns:wrs="http://www.opengis.net/cat/wrs/1.0" 
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:dct="http://purl.org/dc/terms/" 
    xmlns:ows="http://www.opengis.net/ows"
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:gml="http://www.opengis.net/gml"
    xmlns:gml32="http://www.opengis.net/gml/3.2"
    
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/csw:GetRecordByIdResponse[not(gmd:MD_Metadata) and not(gmi:MI_Metadata)]">
	<xsl:copy-of select="."></xsl:copy-of>
	</xsl:template>

    <xsl:template match="/csw:GetRecordByIdResponse[gmd:MD_Metadata or gmi:MI_Metadata]/*[1]">
        <html>

            <head>
                <link rel="stylesheet" type="text/css" href="/gs-service/iso-xslt/source/style-2.css"/>
               
                <!--  <script type="text/javascript" src="../../iso-xslt/js/ol.js"></script>
                <script type="text/javascript" src="../../iso-xslt/js/openlayers/OpenLayers.js"></script>
                 -->
                
                <title>Metadata Record Viewer</title>
                
            </head>
            
            
            <body>
            
            	<xsl:if
                        test="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement">
                        <xsl:variable name="multibox">
                        <xsl:for-each
                            select="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement">
                            <xsl:variable name="s" select="gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"></xsl:variable>
			                <xsl:variable name="n" select="gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"></xsl:variable>
            			    <xsl:variable name="e" select="gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"></xsl:variable>
                			<xsl:variable name="w" select="gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"></xsl:variable>
                			<xsl:value-of select="$w"></xsl:value-of>
                			<xsl:text>,</xsl:text>
                			<xsl:value-of select="$s"></xsl:value-of>
                			<xsl:text>,</xsl:text>
                			<xsl:value-of select="$e"></xsl:value-of>
                			<xsl:text>,</xsl:text>
                			<xsl:value-of select="$n"></xsl:value-of>
                			<xsl:text>#</xsl:text>
                        </xsl:for-each>
                        </xsl:variable>
                        <xsl:variable name="track" select="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:MultiCurve/gml:curveMember/gml:LineString/gml:posList">
                        </xsl:variable>
				
				<xsl:variable name="markerType">
				    <xsl:choose>
				        <xsl:when test="contains(gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString,'Channel')">sResults</xsl:when>
				        <xsl:otherwise>results</xsl:otherwise>
				    </xsl:choose>
				</xsl:variable>
				
                <!--   <xsl:attribute name="onload">
                    init('<xsl:value-of select="$multibox"/>','<xsl:value-of select="$markerType"/>','<xsl:value-of select="$track"/>')
                </xsl:attribute>
                -->
                
                </xsl:if>
            
            
                <div id="page">
                    <div id="header">


                        <div id="headertext">

                            <h1 class="pagetitle">
                                <xsl:value-of select="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"></xsl:value-of>
                            </h1>
                            <h2 class="pagesubtitle">ISO 19115 Overview </h2>

                        </div>
                        <div id="poweredby">
                            <a class="link" href="https://github.com/ESSI-Lab/DAB">Powered by <img src="https://github.com/ESSI-Lab/DAB/raw/main/DAB.png" width="40"/></a> 
                           
                        </div>
                    </div>

                    <br/>
                    <br/>
                    <br/>
                    <br/>


					<xsl:if test ="gmd:identificationInfo/*/gmd:graphicOverview">					
						<center>
							<div>
							<xsl:element name="img">
								<xsl:attribute name="src">
									<xsl:value-of select="gmd:identificationInfo/*/gmd:graphicOverview/*/gmd:fileName/*"></xsl:value-of>
								</xsl:attribute>
							    
							    <xsl:attribute name="height">
							        <xsl:value-of select="'50px'"></xsl:value-of>
							    </xsl:attribute>
							    
							</xsl:element>
							<div>Dataset preview</div>			
							</div>				
						</center>
					</xsl:if>

                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Record</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:call-template name="metadata"/>
                        </xsl:with-param>
                    </xsl:call-template>

                    <xsl:if test="//gmd:contact/gmd:CI_ResponsibleParty">

                        <xsl:call-template name="createTable">
                            <xsl:with-param name="title">Contact Information</xsl:with-param>
                            <xsl:with-param name="tbody">
                                <xsl:apply-templates
                                    select="//gmd:contact/gmd:CI_ResponsibleParty"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>

					<xsl:if test="//gmd:pointOfContact/gmd:CI_ResponsibleParty">

                        <xsl:call-template name="createTable">
                            <xsl:with-param name="title">Dataset Responsible party</xsl:with-param>
                            <xsl:with-param name="tbody">
                                <xsl:apply-templates
                                    select="//gmd:pointOfContact/gmd:CI_ResponsibleParty"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>



                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Identification Information</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:apply-templates
                                select="gmd:identificationInfo"
                            />
                        </xsl:with-param>
                    </xsl:call-template>
                    
                    <!-- Constraints -->
                    
                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Constraints</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:call-template name="const"/>
                        </xsl:with-param>
                    </xsl:call-template>  
                    
                    
                    
                    <!-- Legal constraints -->
                    
                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Legal constraints</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:call-template name="legal_const"/>
                        </xsl:with-param>
                    </xsl:call-template>  
                    
                    
                    <!-- Security constraints -->
                    
                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Security constraints</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:call-template name="sec_const"/>
                        </xsl:with-param>
                    </xsl:call-template>  
                    
                    
                    <xsl:call-template name="createTable">
                        <xsl:with-param name="title">Content Information</xsl:with-param>
                        <xsl:with-param name="tbody">
                            <xsl:apply-templates
                                select="//gmd:contentInfo"
                            />
                        </xsl:with-param>
                    </xsl:call-template>



                    <!-- Extent  -->
                    <!-- Geogrpahic -->
                    <xsl:if
                        test="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement">

			
						<xsl:variable name="bboxes" select="count(gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement)"></xsl:variable>
						<xsl:variable name="toshow" select="3"></xsl:variable>
                        <xsl:for-each
                            select="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement">
                        <xsl:variable name="pos" select="position()"></xsl:variable>
                        
                        <xsl:if test="$pos &lt; $toshow + 1">
                            
                            <xsl:if test="gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal">
								                            
                                <xsl:call-template name="createTable">
                                    <xsl:with-param name="title">Geographic Extent (Bounding Box)</xsl:with-param>
                                    <xsl:with-param name="tbody">
                                        <xsl:apply-templates select="."/>
                                    </xsl:with-param>
                                </xsl:call-template>
                                
                            </xsl:if>
                            
                            
                            <xsl:if test="gmd:EX_GeographicDescription[gmd:geographicIdentifier/gmd:MD_Identifier/gmd:authority/gmd:CI_Citation/gmd:title/gco:CharacterString='what3words']">
								                            
                                <xsl:call-template name="createTable">
                                    <xsl:with-param name="title">Geographic Extent (What3Words)</xsl:with-param>
                                    <xsl:with-param name="tbody">
                                        <xsl:apply-templates select="."/>
                                    </xsl:with-param>
                                </xsl:call-template>                            
                            </xsl:if>
                            
                         </xsl:if>
                            
                        </xsl:for-each>
                        
                        <xsl:if test="$bboxes &gt; $toshow">
                        <xsl:variable name="title">Geographic Extent ... (<xsl:value-of select="number($bboxes - $toshow)" /> more not shown)</xsl:variable>
                                <xsl:call-template name="createTable">
                                    <xsl:with-param name="title" select="$title"></xsl:with-param>                                    
                                </xsl:call-template>
                        </xsl:if>
                        
                     
                            
                        <!--  <div id="map"></div>  -->
                    </xsl:if>
                    
                    

                    <!-- Temporal -->
                    <xsl:if
                        test="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement">
                        <xsl:for-each
                            select="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement">
                            <xsl:if test="gmd:EX_TemporalExtent">
                                <xsl:call-template name="createTable">
                                    <xsl:with-param name="title">Temporal Extent</xsl:with-param>
                                    <xsl:with-param name="tbody">
                                        <xsl:apply-templates select="."/>
                                    </xsl:with-param>
                                </xsl:call-template>
                             </xsl:if>
                        </xsl:for-each>
                    </xsl:if>
                    
                    <!-- Distribution info -->
                    <xsl:if test="gmd:distributionInfo">

                        <xsl:call-template name="createTable">
                            <xsl:with-param name="title">Distribution Information</xsl:with-param>
                            <xsl:with-param name="tbody">
                                <xsl:apply-templates
                                    select="gmd:distributionInfo"
                                />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>

                    <!-- Quality info -->
                    <xsl:if test="gmd:dataQualityInfo/gmd:DQ_DataQuality">
                        
                        <xsl:call-template name="createTable">
                            <xsl:with-param name="title">Data Quality Information</xsl:with-param>
                            <xsl:with-param name="tbody">
                                <xsl:apply-templates
                                    select="gmd:dataQualityInfo/gmd:DQ_DataQuality"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>


                    <!-- Acquisition info -->
                    <xsl:if test="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation">
                        
                        <xsl:call-template name="createTable">
                            <xsl:with-param name="title">Acquisition Information</xsl:with-param>
                            <xsl:with-param name="tbody">
                                <xsl:apply-templates
                                    select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>

                    <div id="footer">
                        <p>- <a class="link" href="https://github.com/ESSI-Lab/DAB">Powered by DAB</a>
                            -</p>
                    </div>
                </div>
            </body>

        </html>
    </xsl:template>
    
    <xsl:template match="gmd:dataQualityInfo/gmd:DQ_DataQuality">
        
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Statement</xsl:with-param>
            <xsl:with-param name="value" select="gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"></xsl:with-param>
        </xsl:call-template>
        
        <xsl:for-each select="gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmi:LE_ProcessStep">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Process Step</xsl:with-param>
                <xsl:with-param name="value" select="gmd:description/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Process Step Source</xsl:with-param>
                <xsl:with-param name="value" select="gmd:source/gmi:LE_Source/gmd:description/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Processing Info ID</xsl:with-param>
                <xsl:with-param name="value" select="gmi:processingInformation/gmi:LE_Processing/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Processing Info Description</xsl:with-param>
                <xsl:with-param name="value" select="gmi:processingInformation/gmi:LE_Processing/gmi:procedureDescription/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Algorithm ID</xsl:with-param>
                <xsl:with-param name="value" select="gmi:processingInformation/gmi:LE_Processing/gmi:algorithm/gmi:LE_Algorithm/gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Algorithm Description</xsl:with-param>
                <xsl:with-param name="value" select="gmi:processingInformation/gmi:LE_Processing/gmi:algorithm/gmi:LE_Algorithm/gmi:description/gco:CharacterString/text()"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Process Step  Output</xsl:with-param>
                <xsl:with-param name="value" select="gmi:output/gmi:LE_Source/gmd:description/gco:CharacterString/text()"/>
            </xsl:call-template>
            
        </xsl:for-each>
        
    </xsl:template>
    
    <xsl:template match="gmd:distributionInfo">
        <xsl:for-each
            select="gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/*[1]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Distribution format</xsl:with-param>
                <xsl:with-param name="value" select="."/>
            </xsl:call-template>
        </xsl:for-each>


        <!-- Transfer options -->
        <xsl:for-each
            select="gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">


            <xsl:variable name="function" select="gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue"/>
            <xsl:variable name="functionDesc" select="gmd:function/gmd:CI_OnLineFunctionCode/text()"/>
            <xsl:variable name="name" select="gmd:name/gco:CharacterString"/>
            <xsl:variable name="description" select="gmd:description/gco:CharacterString"/>
            <xsl:variable name="link" select="gmd:linkage/gmd:URL"/>

			<xsl:call-template name="create_tr">
                <xsl:with-param name="type">Online resource</xsl:with-param>
                <xsl:with-param name="value" select="'-----------------------------'"/>
            </xsl:call-template>
           
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Description</xsl:with-param>
                <xsl:with-param name="value" select="$description"/>
            </xsl:call-template>

            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Name</xsl:with-param>
                <xsl:with-param name="value" select="$name"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Linkage</xsl:with-param>
                <xsl:with-param name="copy">true</xsl:with-param>
                <xsl:with-param name="value">
                    
                    <!--xsl:choose>
                        <xsl:when test="contains($function,'download')"-->
                    <xsl:call-template name="createHREF">
                        <xsl:with-param name="link" select="$link"/>
                        <xsl:with-param name="title" select="$link"/>
                    </xsl:call-template>
                    <!--/xsl:when>
                        <xsl:otherwise>
                        <xsl:value-of select="$link"/>
                        </xsl:otherwise>
                        
                        </xsl:choose-->
                    
                </xsl:with-param>
            </xsl:call-template>           
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Protocol</xsl:with-param>
                <xsl:with-param name="value" select="gmd:protocol/gco:CharacterString"/>
            </xsl:call-template>
                   
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Function</xsl:with-param>
                <xsl:with-param name="value" select="$function"/>
            </xsl:call-template>
            
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Function Description</xsl:with-param>
                <xsl:with-param name="value" select="$functionDesc"/>
            </xsl:call-template>

        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation">
               
        <xsl:for-each select="gmi:instrument/gmi:MI_Instrument">          
            <xsl:if test="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensor Identifier</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>      
            
            <xsl:if test="gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensor Name</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>      
                              
            <xsl:if test="gmi:type/gmi:MI_SensorTypeCode/@id">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensor type</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:type/gmi:MI_SensorTypeCode/@id"/>                        
                </xsl:call-template>                
            </xsl:if>
            
            <xsl:if test="gmi:type/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensor type</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:type/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>            
            
            <xsl:if test="gmi:description/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensor description</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:description/gco:CharacterString"/>                        
                </xsl:call-template>               
            </xsl:if>
            
        </xsl:for-each>
        
        
        <xsl:for-each select="gmi:objective/gmi:MI_Objective">          

            <xsl:if test="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Observed Object</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>
            
            <xsl:if test="gmi:function/gco:CharacterString">
                <xsl:call-template name="create_tr">
                <!-- The name of this table is this because of the mapping from SOS 2.0 to ISO 19115-2 -->
                <!-- Actually this table shows the function field of an MI_Objective, Observable property might not be good in all cases -->
                    <xsl:with-param name="type">Observable Property</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:function/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>
            
        <xsl:for-each select="gmi:sensingInstrument/gmi:MI_Instrument">          
            
            <xsl:if test="gmi:type/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensing Instrument</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:type/gco:CharacterString"/>                        
                </xsl:call-template>                
            </xsl:if>
            
            <xsl:if test="gmi:description/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Sensing Instrument Description</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:description/gco:CharacterString"/>                        
                </xsl:call-template>               
            </xsl:if>
            
        </xsl:for-each>
            
            
        </xsl:for-each>        
        
        <xsl:for-each select="gmi:platform/gmi:MI_Platform">
            
            <xsl:if test="gmi:description/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Platform description</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:description/gco:CharacterString"/>                        
                </xsl:call-template>
            </xsl:if>
            
            <xsl:if test="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Platform code</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>                        
                </xsl:call-template>
            </xsl:if>
            
            <xsl:if test="gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Instrument code</xsl:with-param>
                    <xsl:with-param name="value" select="gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>                        
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>
             
    </xsl:template>
    
    
    <xsl:template name="createHREF">
        <xsl:param name="link"/>
        <xsl:param name="title"/>
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="$link"></xsl:value-of>
            </xsl:attribute>
            <xsl:value-of select="$title"/>
        </a>
    </xsl:template>
    <xsl:template match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement">
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Start</xsl:with-param>
            <xsl:with-param name="value">
                
                <!-- Time period... (gml) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition/@indeterminatePosition='now'">[NOW]</xsl:if>
                <!-- ...or time instant...(gml) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/@indeterminatePosition='now'">[NOW]</xsl:if>
                
                <!-- Time period... (gml 3.2) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition/@indeterminatePosition='now'">[NOW]</xsl:if>
                <!-- ...or time instant...(gml 3.2) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:begin/gml32:TimeInstant/gml32:timePosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:begin/gml32:TimeInstant/gml32:timePosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:begin/@indeterminatePosition='now'">[NOW]</xsl:if>  
                
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">End</xsl:with-param>
            <xsl:with-param name="value">
                
                <!-- Time period... (gml) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition">
                    <xsl:value-of  select="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition/@indeterminatePosition='now'">[NOW]</xsl:if>
                <!-- ...or time instant... (gml) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/@indeterminatePosition='now'">[NOW]</xsl:if>
                
                <!-- Time period... (gml 3.2) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition">
                    <xsl:value-of  select="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition/@indeterminatePosition='now'">[NOW]</xsl:if>                
                <!-- ...or time instant... (gml 3.2) -->
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:end/gml32:TimeInstant/gml32:timePosition">
                    <xsl:value-of select="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:end/gml32:TimeInstant/gml32:timePosition"/>
                </xsl:if>
                <xsl:if test="./gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:end/@indeterminatePosition='now'">[NOW]</xsl:if>      
                
                
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>



	<xsl:template
        match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement[gmd:EX_GeographicDescription]">
        <xsl:variable name="w3wbox">
        	<xsl:value-of select="./gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"></xsl:value-of>
        </xsl:variable>
        <xsl:variable name="w3wboxClean">
        	<xsl:value-of select="substring-before(substring-after($w3wbox,'BOX2D('),')')"></xsl:value-of>
        </xsl:variable>
        <xsl:variable name="lc">
        	<xsl:value-of select="substring-before($w3wboxClean,',')"></xsl:value-of>
        </xsl:variable>
        <xsl:variable name="uc">
        	<xsl:value-of select="substring-after($w3wboxClean,',')"></xsl:value-of>
        </xsl:variable>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Lower corner</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of select="$lc"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Upper corner</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of select="$uc"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>


    <xsl:template
        match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement[gmd:EX_GeographicBoundingBox]">
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">South</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="./gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">West</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="./gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">North</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="./gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">East</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="./gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="gmd:identificationInfo">

        <!-- Title -->
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Title</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
                />
            </xsl:with-param>
        </xsl:call-template>

        <!-- Creation date -->
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Resource Identifier</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"
                />
            </xsl:with-param>
        </xsl:call-template>
        
        <!-- Creation date -->
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Creation date</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of
                    select="*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date"
                />
            </xsl:with-param>
        </xsl:call-template>

        <!-- Description -->
        <xsl:call-template name="create_tr">
            <xsl:with-param name="type">Description</xsl:with-param>
            <xsl:with-param name="value">
                <xsl:value-of select="*/gmd:abstract/gco:CharacterString"/>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Language -->
        <xsl:if test="*/gmd:language/gco:CharacterString">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Language</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of
                        select="*/gmd:language/gco:CharacterString"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- Descriptive keywords  -->
        <xsl:for-each
            select="*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/*[1]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Descriptive keyword</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="./text()"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
      
        <xsl:for-each
            select="*/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Endpoint</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>
    
    <xsl:template match="gmd:contentInfo">

	 	<xsl:if test="*/gmd:attributeDescription/gco:RecordType">
	      
	        <!-- attribute -->
	        <xsl:call-template name="create_tr">
	            <xsl:with-param name="type">Attribute Description</xsl:with-param>
	            <xsl:with-param name="value">
	                <xsl:value-of
	                    select="*/gmd:attributeDescription/gco:RecordType"
	                />
	            </xsl:with-param>
	        </xsl:call-template>
	               </xsl:if>
	               
	               	<xsl:if test="*/gmd:dimension/gmd:MI_Band">
	        <!-- Band -->
	        <xsl:call-template name="create_tr">
	            <xsl:with-param name="type">Band name</xsl:with-param>
	            <xsl:with-param name="value">
	                <xsl:value-of
	                    select="*/gmd:dimension/gmd:MI_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString"
	                />
	            </xsl:with-param>
	        </xsl:call-template>
	        <xsl:call-template name="create_tr">
	            <xsl:with-param name="type">Min value</xsl:with-param>
	            <xsl:with-param name="value">
	                <xsl:value-of
	                    select="*/gmd:dimension/gmd:MI_Band/gmd:minValue"
	                />
	            </xsl:with-param>
	        </xsl:call-template>
	        <xsl:call-template name="create_tr">
	            <xsl:with-param name="type">Max Value</xsl:with-param>
	            <xsl:with-param name="value">
	                <xsl:value-of
	                    select="*/gmd:dimension/gmd:MI_Band/gmd:maxValue/gco:Real"
	                />
	            </xsl:with-param>
	        </xsl:call-template>
	        
         </xsl:if>
    </xsl:template>

    <xsl:template match="gmd:CI_ResponsibleParty">
        <tbody>

            <!-- Individual name  -->
            <xsl:for-each select="gmd:individualName/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Individual name</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="."/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>

            <!-- Organization name  -->
            <xsl:for-each select="gmd:organisationName/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Organization name</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="."/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>

            <!-- Position name  -->
            <xsl:for-each select="gmd:positionName/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Position name</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="."/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>

            <!-- Role -->
            <xsl:for-each select="gmd:role/gmd:CI_RoleCode/@codeListValue">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Role</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="."/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>

            <!-- Address -->
            <xsl:if test="gmd:contactInfo/gmd:CI_Contact/gmd:address">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Delivery point</xsl:with-param>
                    <xsl:with-param name="value"
                        select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"
                    />
                </xsl:call-template>
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">City</xsl:with-param>
                    <xsl:with-param name="value"
                        select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"
                    />
                </xsl:call-template>
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Postal code</xsl:with-param>
                    <xsl:with-param name="value"
                        select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"
                    />
                </xsl:call-template>
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Country</xsl:with-param>
                    <xsl:with-param name="value"
                        select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"
                    />
                </xsl:call-template>
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Email</xsl:with-param>
                    <xsl:with-param name="value"
                        select="gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"
                    />
                </xsl:call-template>
            </xsl:if>
        </tbody>

    </xsl:template>

    <xsl:template name="const" >
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Use limitation</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="sec_const">
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Use limitation</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Classification</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classificationSystem/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Classification system</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:handlingDescription/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Handling description</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
    </xsl:template>
    
    <xsl:template name="legal_const" >
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Use limitation</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Access contraints</xsl:with-param>
                <xsl:with-param name="value">
                   
                        <xsl:choose>
                            <xsl:when test="string-length(.) > 0">
                                <xsl:value-of select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="notFound">Code list value missing</xsl:variable>
                                <xsl:value-of select="$notFound"/>                                
                            </xsl:otherwise>
                        </xsl:choose>                   
                    
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Access contraints (textual content)</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Use contraints</xsl:with-param>
                <xsl:with-param name="value">
                   
                        <xsl:choose>
                            <xsl:when test="string-length(.) > 0">
                                <xsl:value-of select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="notFound">Code list value missing</xsl:variable>
                                <xsl:value-of select="$notFound"/>                                
                            </xsl:otherwise>
                        </xsl:choose>                   
                   
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Use contraints (textual content)</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/*[text()]">
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Other constraints</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="."/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
    </xsl:template>
    
    
    

    <xsl:template name="metadata" match="gmd:MD_Metadata">

        <tbody>

            <!-- File Identifier -->
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">File identifier</xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="gmd:fileIdentifier"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Parent Identifier -->
            <xsl:if test="gmd:parentIdentifier">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Parent identifier</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="gmd:parentIdentifier"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Hierarchy level  -->
            <xsl:call-template name="create_tr">
                <xsl:with-param name="type">Hierarchy level </xsl:with-param>
                <xsl:with-param name="value">
                    <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
                </xsl:with-param>
            </xsl:call-template>

            <!-- Date stamp  -->
            <xsl:if test="gmd:dateStamp/gco:Date">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Date stamp</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="gmd:dateStamp/gco:Date"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <!-- Reference system info  -->
            <xsl:for-each
                select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString">
                <xsl:call-template name="create_tr">
                    <xsl:with-param name="type">Reference system info</xsl:with-param>
                    <xsl:with-param name="value">
                        <xsl:value-of select="."/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </tbody>

    </xsl:template>
    
    <xsl:template name="createTable">
        <xsl:param name="title"/>
        <xsl:param name="tbody" as="node()"/>

        <xsl:if test="string-length(string($tbody))">
            <table id="box-table-a">
                
                <thead>
                    <tr>
                        <th scope="col" width="150">
                            <xsl:value-of select="$title"/>
                        </th>
                        <th scope="col"/>
                    </tr>
                </thead>
                
                <xsl:copy-of select="$tbody"/>
                
            </table>
        </xsl:if>
        
    </xsl:template>

    <xsl:template name="create_tr">
        <xsl:param name="type"/>
        <xsl:param name="value"/>
        <xsl:param name="copy"/>
        <xsl:if test="$value!=''">
        <tr>
            <td>
                <xsl:value-of select="$type"/>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="string-length($copy)>0">
                        <xsl:copy-of select="$value"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$value"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
        </xsl:if>


    </xsl:template>


</xsl:stylesheet>
