<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    /**
    *
    * This file is part of SeaDataNet metadata profile of ISO 19115.
    *
    * Copyright (C) 2012-2019 Enrico Boldrini, Stefano Nativi
    * CNR - Institute of Atmospheric Pollution Research
    *
    * schematron rules version 12.1.1
    */
-->
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
    <sch:title>SeaDatanet profile schematron</sch:title>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gml32" uri="http://www.opengis.net/gml/3.2"/>
    <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#" />
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink" />
    <sch:ns prefix="sdn" uri="http://www.seadatanet.org" />
    <sch:ns prefix="dc" uri="http://purl.org/dc/elements/1.1/" />
        
    <!-- Metadata information -->
    <sch:pattern>        
        <sch:title>fileIdentifier (2) -> mandatory; restricted textual domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:fileIdentifier">fileIdentifier missing</sch:assert>
            <sch:assert test="starts-with(gmd:fileIdentifier/*/text(),'urn:SDN:CDI:')">fileIdentifier does not start with urn:SDN:CDI:</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>language (3) -> mandatory; modified datatype; restricted domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:language">language missing</sch:assert>
            <sch:assert test="gmd:language/gmd:LanguageCode">language datatype must be LanguageCode</sch:assert>
            <sch:assert test="gmd:language/gmd:LanguageCode/@codeListValue='eng'">language is not "eng"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>characterSet (4) -> mandatory; restricted textual domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:characterSet">characterSet missing</sch:assert>
            <sch:assert test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'">characterSet is not "utf8"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>hierarchyLevel (6) -> mandatory; restricted cardinality; restricted domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:hierarchyLevel">hierarchyLevel missing</sch:assert>
            <sch:assert test="count(gmd:hierarchyLevel)=1">hierarchyLevel cardinality differs from 1</sch:assert>
            <sch:assert test="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset'">hierarchyLevel is not "dataset"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>hierarchyLevelName (7) -> mandatory; restricted cardinality; restricted domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:hierarchyLevelName">hierarchyLevelName missing</sch:assert>
            <sch:assert test="count(gmd:hierarchyLevelName)=1">hierarchyLevel cardinality differs from 1</sch:assert>
            <sch:assert test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode">hierarchyLevel codelist missing</sch:assert>
            <sch:assert test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode/@codeListValue='CDI'">hierarchyLevelName is not "Common Data Index record"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>contact (8) -> restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="count(gmd:contact)=1">contact cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>metadataStandardName (10) -> mandatory; restricted textual domain</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:metadataStandardName">metadataStandardName missing</sch:assert>
            <sch:assert test="gmd:metadataStandardName/*/text()='ISO 19115/SeaDataNet profile'">metadataStandardName is not "ISO 19115/SeaDatanet profile"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>metadataStandardVersion (11) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:metadataStandardVersion">metadataStandardVersion missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>referenceSystemInfo (13) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:referenceSystemInfo">referenceSystemInfo missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>metadataExtensionInfo (14) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:metadataExtensionInfo">metadataExtensionInfo missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>identificationInfo (15) -> restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="count(gmd:identificationInfo)=1">identificationInfo cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>distributionInfo (17) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:distributionInfo">distributionInfo missing</sch:assert>
            <sch:assert test="count(gmd:distributionInfo)=1">distributionInfo cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>dataQualityInfo (18) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:dataQualityInfo">dataQualityInfo missing</sch:assert>
            <sch:assert test="count(gmd:dataQualityInfo)=1">dataQualityInfo cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Identification information -->
    <sch:pattern>
        <sch:title>pointOfContact (29) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">            
            <sch:assert test="gmd:pointOfContact">pointOfContact missing</sch:assert>
            <sch:assert test="count(gmd:pointOfContact)=1">pointOfContact cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>descriptiveKeywords (33) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">
            <sch:assert test="gmd:descriptiveKeywords">descriptiveKeywords missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>resourceConstraints (35) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:DataIdentification">            
            <sch:assert test="gmd:resourceConstraints">resourceConstraints missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>spatialRepresentationType (37) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:DataIdentification">            
            <sch:assert test="gmd:spatialRepresentationType">spatialRepresentationType missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>language (39) -> restricted cardinality; modified datatype; restricted domain</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">            
            <sch:assert test="count(gmd:language)=1">language cardinality differs from 1</sch:assert>
            <sch:assert test="gmd:language/gmd:LanguageCode">language datatype must be LanguageCode</sch:assert>
            <sch:assert test="gmd:language/gmd:LanguageCode/@codeListValue='eng'">language is not "eng"</sch:assert>                        
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>characterSet (40) -> mandatory; restricted textual domain</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">            
            <sch:assert test="gmd:characterSet">characterSet missing</sch:assert>
            <sch:assert test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'">characterSet is not "utf8"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>topicCategory (41) -> mandatory; restricted cardinality; restricted textual domain</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">            
            <sch:assert test="gmd:topicCategory">topicCategory missing</sch:assert>
            <sch:assert test="count(gmd:topicCategory)=1">topicCategory cardinality differs from 1</sch:assert>
            <sch:assert test="gmd:topicCategory/gmd:MD_TopicCategoryCode='oceans'">topicCategory is not "oceans"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>extent (45) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification">            
            <sch:assert test="gmd:extent">extent missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Keyword information -->
    <sch:pattern>
        <sch:title>keyword (53) -> restricted domain</sch:title>
        <sch:rule context="gmd:keyword">            
            <sch:assert test="gco:CharacterString or sdn:SDN_DeviceCategoryCode or sdn:SDN_PlatformCategoryCode or sdn:SDN_ParameterDiscoveryCode or sdn:SDN_EDMERPCode">keyword type must be one of CharacterString or SDN_DeviceCategoryCode or SDN_PlatformCategoryCode or SDN_ParameterDiscoveryCode or SDN_EDMERPCode</sch:assert>
        </sch:rule>
    </sch:pattern>    
    
    <!-- Resolution information -->
    <!-- e.g. uom = https://www.seadatanet.org/urnurl/SDN:P06::ULAA -->
    <!-- e.g. uomValue = ULAA -->
    <!-- e.g. length = 4-->
    <!--sch:let name="catalog" value="document('../vocabularies/P06.xml')"/-->
    <sch:pattern>
        <sch:title>distance (61) -> mandatory; restricted domain</sch:title>
        <sch:rule context="gmd:MD_Resolution">            
            <sch:let name="list" value="'https://www.seadatanet.org/urnurl/SDN:P06::'"/>
            <sch:let name="uom" value="gmd:distance/*/@uom"/>        
            <sch:let name="uomValue" value="substring-after($uom,$list)"/>
            <sch:let name="length" value="string-length($uomValue)"/>
            <!--sch:let name="catalog" value="document('https://www.seadatanet.org/urnurl/SDN:P06')"/-->
            <sch:let name="catalog" value="document($uom)"/>            
            <sch:assert test="gmd:distance">distance missing</sch:assert>            
            <sch:assert test="starts-with($uom,$list)">distance uom must start with https://www.seadatanet.org/urnurl/SDN:P06::</sch:assert>
            <sch:assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]">the given uomValue is not included in P06 list</sch:assert>            
        </sch:rule>
    </sch:pattern>
        
    <!-- Aggregation information -->
    <sch:pattern>
        <sch:title>MD_AggregateInformation (66.1)</sch:title>
        <sch:rule context="gmd:MD_AggregateInformation">            
            <sch:assert test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier">aggregateDataSetName or aggregateDataSetIdentifier missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>aggregateDataSetName (66.2) -> restricted domain</sch:title>
        <sch:rule context="gmd:aggregateDataSetName">            
            <sch:assert test="gmd:CI_Citation/gmd:title">title missing</sch:assert>
            <sch:assert test="gmd:CI_Citation/gmd:alternateTitle">alternateTitle missing</sch:assert>
            <sch:assert test="gmd:CI_Citation/gmd:date">date missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>inititativeType (66.5) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_AggregateInformation">            
            <sch:assert test="gmd:initiativeType">inititativeType missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Constraint information -->
    <sch:pattern>
        <sch:title>useLimitation (68) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Constraints">            
            <sch:assert test="gmd:useLimitation">useLimitation missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Data quality information -->
    <sch:pattern>
        <sch:title>report (80) -> mandatory</sch:title>
        <sch:rule context="gmd:DQ_DataQuality">            
            <sch:assert test="gmd:report">report  missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>lineage (81) -> mandatory</sch:title>
        <sch:rule context="gmd:DQ_DataQuality">            
            <sch:assert test="gmd:lineage">lineage missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Lineage information -->
    <sch:pattern>
        <sch:title>statement (83) -> mandatory</sch:title>
        <sch:rule context="gmd:LI_Lineage">            
            <sch:assert test="gmd:statement">statement missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Spatial representation information -->
    <sch:pattern>
        <sch:title>geometricObjects (178) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_VectorSpatialRepresentation">            
            <sch:assert test="gmd:geometricObjects">geometricObjects missing</sch:assert>
            <sch:assert test="count(gmd:geometricObjects) = 1">geometricObjects cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Dimenstion information -->
    <!--sch:let name="catalog" value="document('../vocabularies/P06.xml')"/-->
    <sch:pattern>
        <sch:title>resolution (182) -> mandatory; restricted domain</sch:title>
        <sch:rule context="gmd:MD_Dimension">            
            <sch:let name="list" value="'https://www.seadatanet.org/urnurl/SDN:P06::'"/>
            <sch:let name="uom" value="gmd:resolution/*/@uom"/>
            <sch:let name="uomValue" value="substring-after($uom,$list)"/>
            <sch:let name="length" value="string-length($uomValue)"/>            
            <!--sch:let name="catalog" value="document('https://www.seadatanet.org/urnurl/SDN:P06')"/-->            
            <sch:let name="catalog" value="document($uom)"/>
            <sch:assert test="gmd:resolution">resolution missing</sch:assert>
            <sch:assert test="gmd:resolution/gco:Measure">resolution must be of type measure</sch:assert>           
            <sch:assert test="starts-with($uom,$list)">distance uom must start with https://www.seadatanet.org/urnurl/SDN:P06::</sch:assert>
            <sch:assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]">the given uomValue is not included in P06 list</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Identifier information -->
    <sch:pattern>
        <sch:title>code (205) -> restricted domain</sch:title>
        <sch:rule context="gmd:MD_Identifier">            
            <sch:assert test="gmd:code/gco:CharacterString or gmd:code/sdn:SDN_EDMEDCode or gmd:code/sdn:SDN_CSRCode">CharacterString or EDMED code or CSR code missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>code (208) -> restricted domain</sch:title>
        <sch:rule context="gmd:RS_Identifier">            
            <sch:assert test="gmd:code/sdn:SDN_CRSCode">SDN_CRSCode code list missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Distribution information -->
    <sch:pattern>
        <sch:title>distributionFormat (271) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Distribution">            
            <sch:assert test="gmd:distributionFormat">distributionFormat missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>distributor (272) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:MD_Distribution">            
            <sch:assert test="gmd:distributor">distributor missing</sch:assert>
            <sch:assert test="count(gmd:distributor)=1">distributor cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>transferOptions (273) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_Distribution">            
            <sch:assert test="gmd:transferOptions">transferOptions missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Digital transfer options information -->
    <sch:pattern>
        <sch:title>onLine (277) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_DigitalTransferOptions">            
            <sch:assert test="gmd:onLine">onLine missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Format information -->
    <sch:pattern>
        <sch:title>name (285) -> restricted domain</sch:title>
        <sch:rule context="gmd:MD_Format">            
            <sch:assert test="gmd:name/sdn:SDN_FormatNameCode">New CodeList missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Metadata extension information -->
    <sch:pattern>
        <sch:title>extensionOnLineResource (304) -> mandatory</sch:title>
        <sch:rule context="gmd:MD_MetadataExtensionInformation">            
            <sch:assert test="gmd:extensionOnLineResource">extensionOnLineResource missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Extent information -->
    <sch:pattern>
        <sch:title>geographicElement (336) -> mandatory</sch:title>
        <sch:rule context="gmd:EX_Extent">            
            <sch:assert test="gmd:geographicElement">geographicElement missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>temporalElement (337) -> mandatory</sch:title>
        <sch:rule context="gmd:EX_Extent">            
            <sch:assert test="gmd:temporalElement">temporalElement missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>EX_BoundingPolygon (341) -> restricted domain</sch:title>
        <sch:rule context="gmd:EX_BoundingPolygon">            
            <sch:assert test="not(gmd:extentTypeCode)">extentTypeCode is not permitted</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>EX_GeographicBoundingBox (343) -> restricted domain</sch:title>
        <sch:rule context="gmd:EX_GeographicBoundingBox">            
            <sch:assert test="not(gmd:extentTypeCode)">extentTypeCode is not permitted</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>westBoundLongitude (344) -> restricted domain</sch:title>
        <sch:rule context="gmd:westBoundLongitude">            
            <sch:assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) &gt; 1">precision of two decimal places is required</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>eastBoundLongitude (345) -> restricted domain</sch:title>
        <sch:rule context="gmd:eastBoundLongitude">            
            <sch:assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) &gt; 1">precision of two decimal places is required</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>southBoundLatitude (346) -> restricted domain</sch:title>
        <sch:rule context="gmd:southBoundLatitude">            
            <sch:assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) &gt; 1">precision of two decimal places is required</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>northBoundLatitude (347) -> restricted domain</sch:title>
        <sch:rule context="gmd:northBoundLatitude">            
            <sch:assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) &gt; 1">precision of two decimal places is required</sch:assert>
        </sch:rule>
    </sch:pattern>    
    <sch:pattern>
        <sch:title>geographicIdentifier (349) -> restricted domain</sch:title>
        <sch:rule context="gmd:geographicIdentifier">            
            <sch:assert test="gmd:MD_Identifier">identifier if present must be MD_Identifier</sch:assert>
        </sch:rule>
    </sch:pattern> 
    <sch:pattern>
        <sch:title>extent (351) -> restricted domain</sch:title>
        <sch:rule context="gmd:EX_TemporalExtent/gmd:extent">            
            <sch:assert test="gml:TimePeriod">extent if present must be gml:TimePeriod</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Vertical extent information -->
    <!--sch:let name="catalog" value="document('../vocabularies/L11.xml')"/-->
    <sch:pattern>
        <sch:title>verticalDatum (358) -> restricted domain</sch:title>
        <sch:rule context="gmd:EX_VerticalExtent">
            <sch:let name="list" value="'https://www.seadatanet.org/urnurl/SDN:L11::'"/>
            <sch:let name="identifier" value="gmd:verticalCRS/gml:VerticalCRS/gml:verticalDatum/gml:VerticalDatum/gml:identifier"/>            
            <sch:let name="uom" value="gmd:verticalCRS/gml:VerticalCRS/gml:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@gml:uom"/>
            <sch:let name="identifierValue" value="substring-after($identifier,$list)"/>            
            <sch:let name="length" value="string-length($identifierValue)"/>            
            <!--sch:let name="catalog" value="document('https://www.seadatanet.org/urnurl/SDN:L11')"/-->
            <sch:let name="catalog" value="document($identifier)"/>
            <sch:assert test="starts-with($identifier,$list)">vertical datum identifier must start with https://www.seadatanet.org/urnurl/SDN:L11::</sch:assert>           
            <sch:assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $identifierValue]">the given vertical datum identifier is not included in L11 list</sch:assert>
            <sch:assert test="$uom='metres'">unit of measure must be 'metres'</sch:assert>
        </sch:rule>
    </sch:pattern>   
    
    <!-- Citation information -->
    <sch:pattern>
        <sch:title>identifier (365)</sch:title>
        <sch:rule context="gmd:identifier">            
            <sch:assert test="gmd:MD_Identifier or gco:nilReason">identifier if present must be MD_Identifier</sch:assert>
        </sch:rule>
    </sch:pattern>   
    <sch:pattern>
        <sch:title>individualName (375), organisationName (376) -> obligation restriction</sch:title>
        <sch:rule context="gmd:CI_ResponsibleParty">            
            <sch:assert test="gmd:individualName or gmd:organisationName">individualName or organisationName missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>organisationName (376) -> restricted domain</sch:title>
        <sch:rule context="gmd:CI_ResponsibleParty">            
            <sch:assert test="../../../../sdn:additionalDocumentation or gmd:organisationName/sdn:SDN_EDMOCode">SDN_EDMOCode code list missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>contactInfo (378) -> mandatory; restricted cardinality</sch:title>
        <sch:rule context="gmd:CI_ResponsibleParty">            
            <sch:assert test="gmd:contactInfo">contactInfo missing</sch:assert>            
            <sch:assert test="count(gmd:contactInfo)=1">contactInfo cardinality differs from 1</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Address information -->
    <sch:pattern>
        <sch:title>country (385) -> restricted data type and domain</sch:title>
        <sch:rule context="gmd:country">            
            <sch:assert test="sdn:SDN_CountryCode">country must be of type SDN_CountryCode</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>electronicMailAddress (386) -> mandatory</sch:title>
        <sch:rule context="gmd:CI_Address">            
            <sch:assert test="gmd:electronicMailAddress">electronicMailAddress missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Contact information -->
    <sch:pattern>
        <sch:title>address (389) -> mandatory</sch:title>
        <sch:rule context="gmd:CI_Contact">            
            <sch:assert test="gmd:address">address missing</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- OnLine resource information -->
    <sch:pattern>
        <sch:title>linkage (397) -> restricted domain</sch:title>
        <sch:rule context="gmd:linkage">
            <sch:assert test="starts-with(gmd:URL,'http://') or starts-with(gmd:URL,'https://') or starts-with(gmd:URL,'ftp://')">linkage should start with http://, https://, ftp://</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Relevant ISO 19139 Table A.1 constraints not already checked-->    
    <sch:pattern>
        <sch:title>otherConstraints (72) -> conditional</sch:title>
        <sch:rule context="gmd:MD_LegalConstraints">            
            <sch:let name="restrictionCode" value="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>            
            <sch:assert test="gmd:otherConstraints or $restrictionCode!='otherRestrictions'">otherConstraints missing while accessConstraints="otherRestrictions"</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>shortName (308) -> conditional</sch:title>
        <sch:rule context="gmd:MD_ExtendedElementInformation">      
            <sch:let name="datatypeCode" value="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue"/>            
            <sch:assert test="gmd:shortName or $datatypeCode='codelistElement'">if dataType notEqual 'codelistElement' then shortName is mandatory</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>shortName (309) -> conditional</sch:title>
        <sch:rule context="gmd:MD_ExtendedElementInformation">            
            <sch:let name="datatypeCode" value="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue"/>
            <sch:assert test="gmd:domainCode or $datatypeCode!='codelistElement'">if dataType = 'codelistElement' then domainCode is mandatory</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>condition (312) -> conditional</sch:title>
        <sch:rule context="gmd:MD_ExtendedElementInformation">
            <sch:let name="obligationCode" value="gmd:obligation/gmd:MD_ObligationCode/@codeListValue"/>            
            <sch:assert test="gmd:condition or $obligationCode!='conditional'">if obligation = 'conditional' then condition is mandatory</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>obligation (311), maximumOccurrence (314), domainValue (315) -> conditional</sch:title>
        <sch:rule context="gmd:MD_ExtendedElementInformation">            
            <sch:let name="datatypeCode" value="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue"/>
            <sch:assert test="gmd:obligation or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then obligation is mandatory</sch:assert>
            <sch:assert test="gmd:maximumOccurrence or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then maximumOccurrence is mandatory</sch:assert>
            <sch:assert test="gmd:domainValue or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then domainValue is mandatory</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Additional ISO 19139 constrained not listed in Table A.1 and not enforced by the schema-->
    <sch:pattern>
        <sch:title>EX_GeographicBoundingBox (343) -> restricted domain</sch:title>
        <sch:rule context="gmd:EX_GeographicBoundingBox">            
            <sch:let name="west" value="number(gmd:westBoundLongitude/gco:Decimal)"/>
            <sch:let name="east" value="number(gmd:eastBoundLongitude/gco:Decimal)"/>
            <sch:let name="south" value="number(gmd:southBoundLatitude/gco:Decimal)"/>
            <sch:let name="north" value="number(gmd:northBoundLatitude/gco:Decimal)"/>
            <sch:assert test="-180.0 &lt;= $west and $west &lt;= 180.0">westBoundLongitude must be in -180; 180</sch:assert>
            <sch:assert test="-180.0 &lt;= $east and $east &lt;= 180.0">eastBoundLongitude must be in -180; 180</sch:assert>
            <sch:assert test="-90.0 &lt;= $south and $south &lt;= $north and $south &lt;=90.0">southBoundLatitude must be in -90; 90 and less than northBoundLatitude</sch:assert>
            <sch:assert test="-90.0 &lt;= $north and $south &lt;= $north and $north &lt;= 90.0">northBoundLatitude must be in -90; 90 and greater than southBoundLatitude</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Check on null elements without a null reason -->
    <sch:pattern>
        <sch:title>null elements are not allowed</sch:title>
        <sch:rule context="*">            
            <sch:assert test="* or normalize-space(text()) or @gco:nilReason or @xlink:href">null objects are not permitted unless for optional elements - in that case a reason for the null must be provided - a xlink pointer is also allowed</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Additional SeaDataNet constraints -->
    <sch:pattern>
        <sch:title>associationType (66.5) -> A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="count(//gmd:associationType[gmd:DS_AssociationTypeCode/@codeListValue='source']) &lt; 2">A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>associationType (66.5) -> At least one keyword with type 'platform_class' should be documented</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']) &gt; 0">At least one keyword with type 'platform_class' should be documented</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>associationType (66.5) -> At least one keyword with type 'parameter' should be documented</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']) &gt; 0">At least one keyword with type 'parameter' should be documented</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>RS_Identifier -> L101 thesaurus should be documented</sch:title>
        <sch:rule context="gmd:RS_identifier">            
            <sch:assert test="gmd:authority/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L10'">L10 should be referenced</sch:assert>
            <sch:assert test="gmd:authority/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L10'">L10 should be referenced</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>MD_Keywords -> SDN thesaurus should be documented</sch:title>
        <sch:rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_ParameterDiscoveryCode]">            
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='P02'">P02 should be referenced</sch:assert>
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:P02'">P02 should be referenced</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>MD_Keywords -> SDN thesaurus should be documented</sch:title>
        <sch:rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_DeviceCategoryCode]">            
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L05'">L05 should be referenced</sch:assert>
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L05'">L05 should be referenced</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>MD_Keywords -> SDN thesaurus should be documented</sch:title>
        <sch:rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_PlatformCategoryCode]">            
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L06'">L06 should be referenced</sch:assert>
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L06'">L06 should be referenced</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>MD_Keywords -> SDN thesaurus should be documented</sch:title>
        <sch:rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_EDMERPCode]">            
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='EDMERP'">EDMERP should be referenced</sch:assert>
            <sch:assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:EDMERP'">EDMERP should be referenced</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    
    <!-- Additional INSPIRE constraints from the implementing rules -->
    <sch:pattern>
        <sch:title>INSPIRE SC7</sch:title>
        <sch:rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification">            
            <sch:assert test="count(gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='creation'])&lt;2">[INSPIRE SC7] more than one creation date is not permitted</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>INSPIRE SC8</sch:title>
        <sch:rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification">            
            <sch:assert test="gmd:citation/gmd:CI_Citation/gmd:identifier">[INSPIRE SC8] the resource identifier is mandatory</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>INSPIRE SC10</sch:title>
        <sch:rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification">            
            <sch:assert test="count(gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox)&gt;0">[INSPIRE SC10] at least one geographic bounding box must be provided</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>INSPIRE SC16</sch:title>
        <sch:rule context="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode">            
            <sch:assert test="@codeListValue='pointOfContact'">[INSPIRE SC16] the role code of the contact responsibleParty must be 'pointOfContact'</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>INSPIRE SC17</sch:title>
        <sch:rule context="gmd:MD_DataIdentification">            
            <sch:assert test="contains(gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString,'GEMET')">[INSPIRE SC17] at least one keyword from the GEMET thesaurus must be documented</sch:assert>
        </sch:rule>
    </sch:pattern>    
    <sch:pattern>
        <sch:title>INSPIRE conformance report as regards metadata</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata'">[INSPIRE conformance report] conformance report against INSPIRE regulation as regards metadata must be documented</sch:assert>
        </sch:rule>
        <sch:rule context="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata']">            
            <sch:assert test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2008-12-04'">[INSPIRE conformance report] date must be 2008-12-04</sch:assert>
            <sch:assert test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'">[INSPIRE conformance report] pass should be true</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>INSPIRE conformance report as regards interoperability of spatial data sets and services</sch:title>
        <sch:rule context="gmd:MD_Metadata">            
            <sch:assert test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services'">[INSPIRE conformance report] conformance report against INSPIRE regulation as regards interoperability of spatial data sets and services must be documented</sch:assert>
        </sch:rule>
        <sch:rule context="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services']">            
            <sch:assert test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2010-12-08'">[INSPIRE conformance report] date must be 2010-12-08</sch:assert>
            <sch:assert test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'">[INSPIRE conformance report] pass should be true</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Codelists validation -->
    <sch:pattern>
        <sch:title>Validation of codelists</sch:title>
        <sch:rule context="*[@codeList]">
            <sch:let name="catalog" value="document(substring-before(@codeList,'#'))"/>
            <sch:let name="codeList" value="substring-after(@codeList,'#')"/>            
            <sch:let name="codeListValue" value="@codeListValue"/>
            <sch:assert test="$catalog//gmx:codelistItem/*[@gml:id=$codeList or @gml32:id=$codeList]/gmx:codeEntry/*[gml:identifier=$codeListValue or gml32:identifier=$codeListValue]">the value is not allowed by the codelist catalogue</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- add check thesaurus reference!! -->

</sch:schema>
