<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:svrl="http://purl.oclc.org/dsdl/svrl" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gml="http://www.opengis.net/gml" xmlns:gml32="http://www.opengis.net/gml/3.2" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:schold="http://www.ascc.net/xml/schematron" xmlns:sdn="http://www.seadatanet.org" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
<!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->

<xsl:param name="archiveDirParameter" />
  <xsl:param name="archiveNameParameter" />
  <xsl:param name="fileNameParameter" />
  <xsl:param name="fileDirParameter" />
  <xsl:variable name="document-uri">
    <xsl:value-of select="document-uri(/)" />
  </xsl:variable>

<!--PHASES-->


<!--PROLOG-->
<xsl:output indent="yes" method="xml" omit-xml-declaration="no" standalone="yes" />

<!--XSD TYPES FOR XSLT2-->


<!--KEYS AND FUNCTIONS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-select-full-path">
    <xsl:apply-templates mode="schematron-get-full-path" select="." />
  </xsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-get-full-path">
    <xsl:apply-templates mode="schematron-get-full-path" select="parent::*" />
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">
        <xsl:value-of select="name()" />
        <xsl:variable name="p_1" select="1+    count(preceding-sibling::*[name()=name(current())])" />
        <xsl:if test="$p_1>1 or following-sibling::*[name()=name(current())]">[<xsl:value-of select="$p_1" />]</xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>*[local-name()='</xsl:text>
        <xsl:value-of select="local-name()" />
        <xsl:text>']</xsl:text>
        <xsl:variable name="p_2" select="1+   count(preceding-sibling::*[local-name()=local-name(current())])" />
        <xsl:if test="$p_2>1 or following-sibling::*[local-name()=local-name(current())]">[<xsl:value-of select="$p_2" />]</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="@*" mode="schematron-get-full-path">
    <xsl:text>/</xsl:text>
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()" />
</xsl:when>
      <xsl:otherwise>
        <xsl:text>@*[local-name()='</xsl:text>
        <xsl:value-of select="local-name()" />
        <xsl:text>' and namespace-uri()='</xsl:text>
        <xsl:value-of select="namespace-uri()" />
        <xsl:text>']</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-2-->
<!--This mode can be used to generate prefixed XPath for humans-->
<xsl:template match="node() | @*" mode="schematron-get-full-path-2">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)" />
      <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text />/@<xsl:value-of select="name(.)" />
    </xsl:if>
  </xsl:template>
<!--MODE: SCHEMATRON-FULL-PATH-3-->
<!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->

<xsl:template match="node() | @*" mode="schematron-get-full-path-3">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="name(.)" />
      <xsl:if test="parent::*">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(self::*)">
      <xsl:text />/@<xsl:value-of select="name(.)" />
    </xsl:if>
  </xsl:template>

<!--MODE: GENERATE-ID-FROM-PATH -->
<xsl:template match="/" mode="generate-id-from-path" />
  <xsl:template match="text()" mode="generate-id-from-path">
    <xsl:apply-templates mode="generate-id-from-path" select="parent::*" />
    <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')" />
  </xsl:template>
  <xsl:template match="comment()" mode="generate-id-from-path">
    <xsl:apply-templates mode="generate-id-from-path" select="parent::*" />
    <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')" />
  </xsl:template>
  <xsl:template match="processing-instruction()" mode="generate-id-from-path">
    <xsl:apply-templates mode="generate-id-from-path" select="parent::*" />
    <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')" />
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-from-path">
    <xsl:apply-templates mode="generate-id-from-path" select="parent::*" />
    <xsl:value-of select="concat('.@', name())" />
  </xsl:template>
  <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
    <xsl:apply-templates mode="generate-id-from-path" select="parent::*" />
    <xsl:text>.</xsl:text>
    <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')" />
  </xsl:template>

<!--MODE: GENERATE-ID-2 -->
<xsl:template match="/" mode="generate-id-2">U</xsl:template>
  <xsl:template match="*" mode="generate-id-2" priority="2">
    <xsl:text>U</xsl:text>
    <xsl:number count="*" level="multiple" />
  </xsl:template>
  <xsl:template match="node()" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number count="*" level="multiple" />
    <xsl:text>n</xsl:text>
    <xsl:number count="node()" />
  </xsl:template>
  <xsl:template match="@*" mode="generate-id-2">
    <xsl:text>U.</xsl:text>
    <xsl:number count="*" level="multiple" />
    <xsl:text>_</xsl:text>
    <xsl:value-of select="string-length(local-name(.))" />
    <xsl:text>_</xsl:text>
    <xsl:value-of select="translate(name(),':','.')" />
  </xsl:template>
<!--Strip characters-->  <xsl:template match="text()" priority="-1" />

<!--SCHEMA SETUP-->
<xsl:template match="/">
    <svrl:schematron-output schemaVersion="" title="SeaDatanet profile schematron">
      <xsl:comment>
        <xsl:value-of select="$archiveDirParameter" />   
		 <xsl:value-of select="$archiveNameParameter" />  
		 <xsl:value-of select="$fileNameParameter" />  
		 <xsl:value-of select="$fileDirParameter" />
      </xsl:comment>
      <svrl:ns-prefix-in-attribute-values prefix="gmd" uri="http://www.isotc211.org/2005/gmd" />
      <svrl:ns-prefix-in-attribute-values prefix="gco" uri="http://www.isotc211.org/2005/gco" />
      <svrl:ns-prefix-in-attribute-values prefix="gmx" uri="http://www.isotc211.org/2005/gmx" />
      <svrl:ns-prefix-in-attribute-values prefix="gml" uri="http://www.opengis.net/gml" />
      <svrl:ns-prefix-in-attribute-values prefix="gml32" uri="http://www.opengis.net/gml/3.2" />
      <svrl:ns-prefix-in-attribute-values prefix="skos" uri="http://www.w3.org/2004/02/skos/core#" />
      <svrl:ns-prefix-in-attribute-values prefix="xlink" uri="http://www.w3.org/1999/xlink" />
      <svrl:ns-prefix-in-attribute-values prefix="sdn" uri="http://www.seadatanet.org" />
      <svrl:ns-prefix-in-attribute-values prefix="dc" uri="http://purl.org/dc/elements/1.1/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">fileIdentifier (2) -&gt; mandatory; restricted textual domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M10" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">language (3) -&gt; mandatory; modified datatype; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M11" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">characterSet (4) -&gt; mandatory; restricted textual domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M12" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">hierarchyLevel (6) -&gt; mandatory; restricted cardinality; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M13" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">hierarchyLevelName (7) -&gt; mandatory; restricted cardinality; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M14" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">contact (8) -&gt; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M15" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">metadataStandardName (10) -&gt; mandatory; restricted textual domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M16" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">metadataStandardVersion (11) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M17" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">referenceSystemInfo (13) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M18" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">metadataExtensionInfo (14) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M19" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">identificationInfo (15) -&gt; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M20" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">distributionInfo (17) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M21" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">dataQualityInfo (18) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M22" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">pointOfContact (29) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M23" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">descriptiveKeywords (33) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M24" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">resourceConstraints (35) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M25" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">spatialRepresentationType (37) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M26" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">language (39) -&gt; restricted cardinality; modified datatype; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M27" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">characterSet (40) -&gt; mandatory; restricted textual domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M28" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">topicCategory (41) -&gt; mandatory; restricted cardinality; restricted textual domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M29" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">extent (45) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M30" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">keyword (53) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M31" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">distance (61) -&gt; mandatory; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M32" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">MD_AggregateInformation (66.1)</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M33" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">aggregateDataSetName (66.2) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M34" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">inititativeType (66.5) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M35" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">useLimitation (68) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M36" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">report (80) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M37" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">lineage (81) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M38" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">statement (83) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M39" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">geometricObjects (178) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M40" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">resolution (182) -&gt; mandatory; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M41" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">code (205) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M42" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">code (208) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M43" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">distributionFormat (271) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M44" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">distributor (272) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M45" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">transferOptions (273) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M46" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">onLine (277) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M47" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">name (285) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M48" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">extensionOnLineResource (304) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M49" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">geographicElement (336) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M50" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">temporalElement (337) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M51" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">EX_BoundingPolygon (341) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M52" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">EX_GeographicBoundingBox (343) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M53" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">westBoundLongitude (344) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M54" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">eastBoundLongitude (345) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M55" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">southBoundLatitude (346) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M56" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">northBoundLatitude (347) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M57" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">geographicIdentifier (349) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M58" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">extent (351) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M59" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">verticalDatum (358) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M60" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">identifier (365)</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M61" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">individualName (375), organisationName (376) -&gt; obligation restriction</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M62" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">organisationName (376) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M63" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">contactInfo (378) -&gt; mandatory; restricted cardinality</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M64" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">country (385) -&gt; restricted data type and domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M65" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">electronicMailAddress (386) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M66" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">address (389) -&gt; mandatory</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M67" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">linkage (397) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M68" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">otherConstraints (72) -&gt; conditional</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M69" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">shortName (308) -&gt; conditional</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M70" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">shortName (309) -&gt; conditional</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M71" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">condition (312) -&gt; conditional</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M72" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">obligation (311), maximumOccurrence (314), domainValue (315) -&gt; conditional</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M73" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">EX_GeographicBoundingBox (343) -&gt; restricted domain</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M74" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">null elements are not allowed</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M75" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">associationType (66.5) -&gt; A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M76" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">associationType (66.5) -&gt; At least one keyword with type 'platform_class' should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M77" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">associationType (66.5) -&gt; At least one keyword with type 'parameter' should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M78" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">RS_Identifier -&gt; L101 thesaurus should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M79" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">MD_Keywords -&gt; SDN thesaurus should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M80" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">MD_Keywords -&gt; SDN thesaurus should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M81" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">MD_Keywords -&gt; SDN thesaurus should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M82" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">MD_Keywords -&gt; SDN thesaurus should be documented</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M83" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE SC7</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M84" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE SC8</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M85" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE SC10</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M86" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE SC16</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M87" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE SC17</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M88" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE conformance report as regards metadata</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M89" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">INSPIRE conformance report as regards interoperability of spatial data sets and services</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M90" select="/" />
      <svrl:active-pattern>
        <xsl:attribute name="document">
          <xsl:value-of select="document-uri(/)" />
        </xsl:attribute>
        <xsl:attribute name="name">Validation of codelists</xsl:attribute>
        <xsl:apply-templates />
      </svrl:active-pattern>
      <xsl:apply-templates mode="M91" select="/" />
    </svrl:schematron-output>
  </xsl:template>

<!--SCHEMATRON PATTERNS-->
<svrl:text>SeaDatanet profile schematron</svrl:text>

<!--PATTERN fileIdentifier (2) -> mandatory; restricted textual domain-->
<svrl:text>fileIdentifier (2) -&gt; mandatory; restricted textual domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M10" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:fileIdentifier" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:fileIdentifier">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>fileIdentifier missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="starts-with(gmd:fileIdentifier/*/text(),'urn:SDN:CDI:')" />
      <xsl:otherwise>
        <svrl:failed-assert test="starts-with(gmd:fileIdentifier/*/text(),'urn:SDN:CDI:')">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>fileIdentifier does not start with urn:SDN:CDI:</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M10" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M10" priority="-1" />
  <xsl:template match="@*|node()" mode="M10" priority="-2">
    <xsl:apply-templates mode="M10" select="*" />
  </xsl:template>

<!--PATTERN language (3) -> mandatory; modified datatype; restricted domain-->
<svrl:text>language (3) -&gt; mandatory; modified datatype; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M11" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:language" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:language">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:language/gmd:LanguageCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:language/gmd:LanguageCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language datatype must be LanguageCode</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:language/gmd:LanguageCode/@codeListValue='eng'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:language/gmd:LanguageCode/@codeListValue='eng'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language is not "eng"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M11" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M11" priority="-1" />
  <xsl:template match="@*|node()" mode="M11" priority="-2">
    <xsl:apply-templates mode="M11" select="*" />
  </xsl:template>

<!--PATTERN characterSet (4) -> mandatory; restricted textual domain-->
<svrl:text>characterSet (4) -&gt; mandatory; restricted textual domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M12" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:characterSet" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:characterSet">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>characterSet missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>characterSet is not "utf8"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M12" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M12" priority="-1" />
  <xsl:template match="@*|node()" mode="M12" priority="-2">
    <xsl:apply-templates mode="M12" select="*" />
  </xsl:template>

<!--PATTERN hierarchyLevel (6) -> mandatory; restricted cardinality; restricted domain-->
<svrl:text>hierarchyLevel (6) -&gt; mandatory; restricted cardinality; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M13" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:hierarchyLevel" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:hierarchyLevel">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevel missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:hierarchyLevel)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:hierarchyLevel)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevel cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevel is not "dataset"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M13" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M13" priority="-1" />
  <xsl:template match="@*|node()" mode="M13" priority="-2">
    <xsl:apply-templates mode="M13" select="*" />
  </xsl:template>

<!--PATTERN hierarchyLevelName (7) -> mandatory; restricted cardinality; restricted domain-->
<svrl:text>hierarchyLevelName (7) -&gt; mandatory; restricted cardinality; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M14" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:hierarchyLevelName" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:hierarchyLevelName">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevelName missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:hierarchyLevelName)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:hierarchyLevelName)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevel cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevel codelist missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode/@codeListValue='CDI'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:hierarchyLevelName/sdn:SDN_HierarchyLevelNameCode/@codeListValue='CDI'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>hierarchyLevelName is not "Common Data Index record"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M14" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M14" priority="-1" />
  <xsl:template match="@*|node()" mode="M14" priority="-2">
    <xsl:apply-templates mode="M14" select="*" />
  </xsl:template>

<!--PATTERN contact (8) -> restricted cardinality-->
<svrl:text>contact (8) -&gt; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M15" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:contact)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:contact)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>contact cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M15" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M15" priority="-1" />
  <xsl:template match="@*|node()" mode="M15" priority="-2">
    <xsl:apply-templates mode="M15" select="*" />
  </xsl:template>

<!--PATTERN metadataStandardName (10) -> mandatory; restricted textual domain-->
<svrl:text>metadataStandardName (10) -&gt; mandatory; restricted textual domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M16" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:metadataStandardName" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:metadataStandardName">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>metadataStandardName missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:metadataStandardName/*/text()='ISO 19115/SeaDataNet profile'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:metadataStandardName/*/text()='ISO 19115/SeaDataNet profile'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>metadataStandardName is not "ISO 19115/SeaDatanet profile"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M16" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M16" priority="-1" />
  <xsl:template match="@*|node()" mode="M16" priority="-2">
    <xsl:apply-templates mode="M16" select="*" />
  </xsl:template>

<!--PATTERN metadataStandardVersion (11) -> mandatory-->
<svrl:text>metadataStandardVersion (11) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M17" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:metadataStandardVersion" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:metadataStandardVersion">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>metadataStandardVersion missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M17" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M17" priority="-1" />
  <xsl:template match="@*|node()" mode="M17" priority="-2">
    <xsl:apply-templates mode="M17" select="*" />
  </xsl:template>

<!--PATTERN referenceSystemInfo (13) -> mandatory-->
<svrl:text>referenceSystemInfo (13) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M18" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:referenceSystemInfo" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:referenceSystemInfo">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>referenceSystemInfo missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M18" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M18" priority="-1" />
  <xsl:template match="@*|node()" mode="M18" priority="-2">
    <xsl:apply-templates mode="M18" select="*" />
  </xsl:template>

<!--PATTERN metadataExtensionInfo (14) -> mandatory-->
<svrl:text>metadataExtensionInfo (14) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M19" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:metadataExtensionInfo" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:metadataExtensionInfo">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>metadataExtensionInfo missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M19" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M19" priority="-1" />
  <xsl:template match="@*|node()" mode="M19" priority="-2">
    <xsl:apply-templates mode="M19" select="*" />
  </xsl:template>

<!--PATTERN identificationInfo (15) -> restricted cardinality-->
<svrl:text>identificationInfo (15) -&gt; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M20" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:identificationInfo)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:identificationInfo)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>identificationInfo cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M20" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M20" priority="-1" />
  <xsl:template match="@*|node()" mode="M20" priority="-2">
    <xsl:apply-templates mode="M20" select="*" />
  </xsl:template>

<!--PATTERN distributionInfo (17) -> mandatory; restricted cardinality-->
<svrl:text>distributionInfo (17) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M21" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:distributionInfo" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:distributionInfo">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distributionInfo missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:distributionInfo)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:distributionInfo)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distributionInfo cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M21" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M21" priority="-1" />
  <xsl:template match="@*|node()" mode="M21" priority="-2">
    <xsl:apply-templates mode="M21" select="*" />
  </xsl:template>

<!--PATTERN dataQualityInfo (18) -> mandatory; restricted cardinality-->
<svrl:text>dataQualityInfo (18) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M22" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:dataQualityInfo" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:dataQualityInfo">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>dataQualityInfo missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:dataQualityInfo)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:dataQualityInfo)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>dataQualityInfo cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M22" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M22" priority="-1" />
  <xsl:template match="@*|node()" mode="M22" priority="-2">
    <xsl:apply-templates mode="M22" select="*" />
  </xsl:template>

<!--PATTERN pointOfContact (29) -> mandatory; restricted cardinality-->
<svrl:text>pointOfContact (29) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M23" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:pointOfContact" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:pointOfContact">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>pointOfContact missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:pointOfContact)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:pointOfContact)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>pointOfContact cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M23" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M23" priority="-1" />
  <xsl:template match="@*|node()" mode="M23" priority="-2">
    <xsl:apply-templates mode="M23" select="*" />
  </xsl:template>

<!--PATTERN descriptiveKeywords (33) -> mandatory-->
<svrl:text>descriptiveKeywords (33) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M24" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:descriptiveKeywords" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:descriptiveKeywords">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>descriptiveKeywords missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M24" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M24" priority="-1" />
  <xsl:template match="@*|node()" mode="M24" priority="-2">
    <xsl:apply-templates mode="M24" select="*" />
  </xsl:template>

<!--PATTERN resourceConstraints (35) -> mandatory-->
<svrl:text>resourceConstraints (35) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:DataIdentification" mode="M25" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:resourceConstraints" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:resourceConstraints">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>resourceConstraints missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M25" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M25" priority="-1" />
  <xsl:template match="@*|node()" mode="M25" priority="-2">
    <xsl:apply-templates mode="M25" select="*" />
  </xsl:template>

<!--PATTERN spatialRepresentationType (37) -> mandatory-->
<svrl:text>spatialRepresentationType (37) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:DataIdentification" mode="M26" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:spatialRepresentationType" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:spatialRepresentationType">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>spatialRepresentationType missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M26" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M26" priority="-1" />
  <xsl:template match="@*|node()" mode="M26" priority="-2">
    <xsl:apply-templates mode="M26" select="*" />
  </xsl:template>

<!--PATTERN language (39) -> restricted cardinality; modified datatype; restricted domain-->
<svrl:text>language (39) -&gt; restricted cardinality; modified datatype; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M27" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:language)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:language)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:language/gmd:LanguageCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:language/gmd:LanguageCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language datatype must be LanguageCode</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:language/gmd:LanguageCode/@codeListValue='eng'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:language/gmd:LanguageCode/@codeListValue='eng'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>language is not "eng"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M27" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M27" priority="-1" />
  <xsl:template match="@*|node()" mode="M27" priority="-2">
    <xsl:apply-templates mode="M27" select="*" />
  </xsl:template>

<!--PATTERN characterSet (40) -> mandatory; restricted textual domain-->
<svrl:text>characterSet (40) -&gt; mandatory; restricted textual domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M28" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:characterSet" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:characterSet">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>characterSet missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue='utf8'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>characterSet is not "utf8"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M28" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M28" priority="-1" />
  <xsl:template match="@*|node()" mode="M28" priority="-2">
    <xsl:apply-templates mode="M28" select="*" />
  </xsl:template>

<!--PATTERN topicCategory (41) -> mandatory; restricted cardinality; restricted textual domain-->
<svrl:text>topicCategory (41) -&gt; mandatory; restricted cardinality; restricted textual domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M29" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:topicCategory" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:topicCategory">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>topicCategory missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:topicCategory)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:topicCategory)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>topicCategory cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:topicCategory/gmd:MD_TopicCategoryCode='oceans'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:topicCategory/gmd:MD_TopicCategoryCode='oceans'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>topicCategory is not "oceans"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M29" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M29" priority="-1" />
  <xsl:template match="@*|node()" mode="M29" priority="-2">
    <xsl:apply-templates mode="M29" select="*" />
  </xsl:template>

<!--PATTERN extent (45) -> mandatory-->
<svrl:text>extent (45) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" mode="M30" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification | sdn:SDN_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:extent" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:extent">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>extent missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M30" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M30" priority="-1" />
  <xsl:template match="@*|node()" mode="M30" priority="-2">
    <xsl:apply-templates mode="M30" select="*" />
  </xsl:template>

<!--PATTERN keyword (53) -> restricted domain-->
<svrl:text>keyword (53) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:keyword" mode="M31" priority="1000">
    <svrl:fired-rule context="gmd:keyword" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gco:CharacterString or sdn:SDN_DeviceCategoryCode or sdn:SDN_PlatformCategoryCode or sdn:SDN_ParameterDiscoveryCode or sdn:SDN_EDMERPCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gco:CharacterString or sdn:SDN_DeviceCategoryCode or sdn:SDN_PlatformCategoryCode or sdn:SDN_ParameterDiscoveryCode or sdn:SDN_EDMERPCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>keyword type must be one of CharacterString or SDN_DeviceCategoryCode or SDN_PlatformCategoryCode or SDN_ParameterDiscoveryCode or SDN_EDMERPCode</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M31" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M31" priority="-1" />
  <xsl:template match="@*|node()" mode="M31" priority="-2">
    <xsl:apply-templates mode="M31" select="*" />
  </xsl:template>

<!--PATTERN distance (61) -> mandatory; restricted domain-->
<svrl:text>distance (61) -&gt; mandatory; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Resolution" mode="M32" priority="1000">
    <svrl:fired-rule context="gmd:MD_Resolution" />
    <xsl:variable name="list" select="'https://www.seadatanet.org/urnurl/SDN:P06::'" />
    <xsl:variable name="uom" select="gmd:distance/*/@uom" />
    <xsl:variable name="uomValue" select="substring-after($uom,$list)" />
    <xsl:variable name="length" select="string-length($uomValue)" />
    <xsl:variable name="catalog" select="document($uom)" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:distance" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:distance">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distance missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="starts-with($uom,$list)" />
      <xsl:otherwise>
        <svrl:failed-assert test="starts-with($uom,$list)">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distance uom must start with https://www.seadatanet.org/urnurl/SDN:P06::</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]" />
      <xsl:otherwise>
        <svrl:failed-assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>the given uomValue is not included in P06 list</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M32" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M32" priority="-1" />
  <xsl:template match="@*|node()" mode="M32" priority="-2">
    <xsl:apply-templates mode="M32" select="*" />
  </xsl:template>

<!--PATTERN MD_AggregateInformation (66.1)-->
<svrl:text>MD_AggregateInformation (66.1)</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_AggregateInformation" mode="M33" priority="1000">
    <svrl:fired-rule context="gmd:MD_AggregateInformation" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>aggregateDataSetName or aggregateDataSetIdentifier missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M33" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M33" priority="-1" />
  <xsl:template match="@*|node()" mode="M33" priority="-2">
    <xsl:apply-templates mode="M33" select="*" />
  </xsl:template>

<!--PATTERN aggregateDataSetName (66.2) -> restricted domain-->
<svrl:text>aggregateDataSetName (66.2) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:aggregateDataSetName" mode="M34" priority="1000">
    <svrl:fired-rule context="gmd:aggregateDataSetName" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:CI_Citation/gmd:title" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:CI_Citation/gmd:title">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>title missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:CI_Citation/gmd:alternateTitle" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:CI_Citation/gmd:alternateTitle">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>alternateTitle missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:CI_Citation/gmd:date" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:CI_Citation/gmd:date">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>date missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M34" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M34" priority="-1" />
  <xsl:template match="@*|node()" mode="M34" priority="-2">
    <xsl:apply-templates mode="M34" select="*" />
  </xsl:template>

<!--PATTERN inititativeType (66.5) -> mandatory-->
<svrl:text>inititativeType (66.5) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_AggregateInformation" mode="M35" priority="1000">
    <svrl:fired-rule context="gmd:MD_AggregateInformation" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:initiativeType" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:initiativeType">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>inititativeType missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M35" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M35" priority="-1" />
  <xsl:template match="@*|node()" mode="M35" priority="-2">
    <xsl:apply-templates mode="M35" select="*" />
  </xsl:template>

<!--PATTERN useLimitation (68) -> mandatory-->
<svrl:text>useLimitation (68) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Constraints" mode="M36" priority="1000">
    <svrl:fired-rule context="gmd:MD_Constraints" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:useLimitation" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:useLimitation">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>useLimitation missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M36" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M36" priority="-1" />
  <xsl:template match="@*|node()" mode="M36" priority="-2">
    <xsl:apply-templates mode="M36" select="*" />
  </xsl:template>

<!--PATTERN report (80) -> mandatory-->
<svrl:text>report (80) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:DQ_DataQuality" mode="M37" priority="1000">
    <svrl:fired-rule context="gmd:DQ_DataQuality" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:report" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:report">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>report  missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M37" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M37" priority="-1" />
  <xsl:template match="@*|node()" mode="M37" priority="-2">
    <xsl:apply-templates mode="M37" select="*" />
  </xsl:template>

<!--PATTERN lineage (81) -> mandatory-->
<svrl:text>lineage (81) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:DQ_DataQuality" mode="M38" priority="1000">
    <svrl:fired-rule context="gmd:DQ_DataQuality" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:lineage" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:lineage">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>lineage missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M38" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M38" priority="-1" />
  <xsl:template match="@*|node()" mode="M38" priority="-2">
    <xsl:apply-templates mode="M38" select="*" />
  </xsl:template>

<!--PATTERN statement (83) -> mandatory-->
<svrl:text>statement (83) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:LI_Lineage" mode="M39" priority="1000">
    <svrl:fired-rule context="gmd:LI_Lineage" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:statement" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:statement">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>statement missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M39" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M39" priority="-1" />
  <xsl:template match="@*|node()" mode="M39" priority="-2">
    <xsl:apply-templates mode="M39" select="*" />
  </xsl:template>

<!--PATTERN geometricObjects (178) -> mandatory; restricted cardinality-->
<svrl:text>geometricObjects (178) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_VectorSpatialRepresentation" mode="M40" priority="1000">
    <svrl:fired-rule context="gmd:MD_VectorSpatialRepresentation" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:geometricObjects" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:geometricObjects">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>geometricObjects missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:geometricObjects) = 1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:geometricObjects) = 1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>geometricObjects cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M40" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M40" priority="-1" />
  <xsl:template match="@*|node()" mode="M40" priority="-2">
    <xsl:apply-templates mode="M40" select="*" />
  </xsl:template>

<!--PATTERN resolution (182) -> mandatory; restricted domain-->
<svrl:text>resolution (182) -&gt; mandatory; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Dimension" mode="M41" priority="1000">
    <svrl:fired-rule context="gmd:MD_Dimension" />
    <xsl:variable name="list" select="'https://www.seadatanet.org/urnurl/SDN:P06::'" />
    <xsl:variable name="uom" select="gmd:resolution/*/@uom" />
    <xsl:variable name="uomValue" select="substring-after($uom,$list)" />
    <xsl:variable name="length" select="string-length($uomValue)" />
    <xsl:variable name="catalog" select="document($uom)" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:resolution" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:resolution">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>resolution missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:resolution/gco:Measure" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:resolution/gco:Measure">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>resolution must be of type measure</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="starts-with($uom,$list)" />
      <xsl:otherwise>
        <svrl:failed-assert test="starts-with($uom,$list)">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distance uom must start with https://www.seadatanet.org/urnurl/SDN:P06::</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]" />
      <xsl:otherwise>
        <svrl:failed-assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $uomValue]">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>the given uomValue is not included in P06 list</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M41" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M41" priority="-1" />
  <xsl:template match="@*|node()" mode="M41" priority="-2">
    <xsl:apply-templates mode="M41" select="*" />
  </xsl:template>

<!--PATTERN code (205) -> restricted domain-->
<svrl:text>code (205) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Identifier" mode="M42" priority="1000">
    <svrl:fired-rule context="gmd:MD_Identifier" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:code/gco:CharacterString or gmd:code/sdn:SDN_EDMEDCode or gmd:code/sdn:SDN_CSRCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:code/gco:CharacterString or gmd:code/sdn:SDN_EDMEDCode or gmd:code/sdn:SDN_CSRCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>CharacterString or EDMED code or CSR code missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M42" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M42" priority="-1" />
  <xsl:template match="@*|node()" mode="M42" priority="-2">
    <xsl:apply-templates mode="M42" select="*" />
  </xsl:template>

<!--PATTERN code (208) -> restricted domain-->
<svrl:text>code (208) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:RS_Identifier" mode="M43" priority="1000">
    <svrl:fired-rule context="gmd:RS_Identifier" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:code/sdn:SDN_CRSCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:code/sdn:SDN_CRSCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>SDN_CRSCode code list missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M43" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M43" priority="-1" />
  <xsl:template match="@*|node()" mode="M43" priority="-2">
    <xsl:apply-templates mode="M43" select="*" />
  </xsl:template>

<!--PATTERN distributionFormat (271) -> mandatory-->
<svrl:text>distributionFormat (271) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Distribution" mode="M44" priority="1000">
    <svrl:fired-rule context="gmd:MD_Distribution" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:distributionFormat" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:distributionFormat">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distributionFormat missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M44" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M44" priority="-1" />
  <xsl:template match="@*|node()" mode="M44" priority="-2">
    <xsl:apply-templates mode="M44" select="*" />
  </xsl:template>

<!--PATTERN distributor (272) -> mandatory; restricted cardinality-->
<svrl:text>distributor (272) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Distribution" mode="M45" priority="1000">
    <svrl:fired-rule context="gmd:MD_Distribution" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:distributor" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:distributor">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distributor missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:distributor)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:distributor)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>distributor cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M45" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M45" priority="-1" />
  <xsl:template match="@*|node()" mode="M45" priority="-2">
    <xsl:apply-templates mode="M45" select="*" />
  </xsl:template>

<!--PATTERN transferOptions (273) -> mandatory-->
<svrl:text>transferOptions (273) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Distribution" mode="M46" priority="1000">
    <svrl:fired-rule context="gmd:MD_Distribution" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:transferOptions" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:transferOptions">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>transferOptions missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M46" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M46" priority="-1" />
  <xsl:template match="@*|node()" mode="M46" priority="-2">
    <xsl:apply-templates mode="M46" select="*" />
  </xsl:template>

<!--PATTERN onLine (277) -> mandatory-->
<svrl:text>onLine (277) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DigitalTransferOptions" mode="M47" priority="1000">
    <svrl:fired-rule context="gmd:MD_DigitalTransferOptions" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:onLine" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:onLine">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>onLine missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M47" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M47" priority="-1" />
  <xsl:template match="@*|node()" mode="M47" priority="-2">
    <xsl:apply-templates mode="M47" select="*" />
  </xsl:template>

<!--PATTERN name (285) -> restricted domain-->
<svrl:text>name (285) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Format" mode="M48" priority="1000">
    <svrl:fired-rule context="gmd:MD_Format" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:name/sdn:SDN_FormatNameCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:name/sdn:SDN_FormatNameCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>New CodeList missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M48" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M48" priority="-1" />
  <xsl:template match="@*|node()" mode="M48" priority="-2">
    <xsl:apply-templates mode="M48" select="*" />
  </xsl:template>

<!--PATTERN extensionOnLineResource (304) -> mandatory-->
<svrl:text>extensionOnLineResource (304) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_MetadataExtensionInformation" mode="M49" priority="1000">
    <svrl:fired-rule context="gmd:MD_MetadataExtensionInformation" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:extensionOnLineResource" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:extensionOnLineResource">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>extensionOnLineResource missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M49" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M49" priority="-1" />
  <xsl:template match="@*|node()" mode="M49" priority="-2">
    <xsl:apply-templates mode="M49" select="*" />
  </xsl:template>

<!--PATTERN geographicElement (336) -> mandatory-->
<svrl:text>geographicElement (336) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_Extent" mode="M50" priority="1000">
    <svrl:fired-rule context="gmd:EX_Extent" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:geographicElement" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:geographicElement">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>geographicElement missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M50" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M50" priority="-1" />
  <xsl:template match="@*|node()" mode="M50" priority="-2">
    <xsl:apply-templates mode="M50" select="*" />
  </xsl:template>

<!--PATTERN temporalElement (337) -> mandatory-->
<svrl:text>temporalElement (337) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_Extent" mode="M51" priority="1000">
    <svrl:fired-rule context="gmd:EX_Extent" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:temporalElement" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:temporalElement">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>temporalElement missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M51" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M51" priority="-1" />
  <xsl:template match="@*|node()" mode="M51" priority="-2">
    <xsl:apply-templates mode="M51" select="*" />
  </xsl:template>

<!--PATTERN EX_BoundingPolygon (341) -> restricted domain-->
<svrl:text>EX_BoundingPolygon (341) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_BoundingPolygon" mode="M52" priority="1000">
    <svrl:fired-rule context="gmd:EX_BoundingPolygon" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="not(gmd:extentTypeCode)" />
      <xsl:otherwise>
        <svrl:failed-assert test="not(gmd:extentTypeCode)">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>extentTypeCode is not permitted</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M52" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M52" priority="-1" />
  <xsl:template match="@*|node()" mode="M52" priority="-2">
    <xsl:apply-templates mode="M52" select="*" />
  </xsl:template>

<!--PATTERN EX_GeographicBoundingBox (343) -> restricted domain-->
<svrl:text>EX_GeographicBoundingBox (343) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_GeographicBoundingBox" mode="M53" priority="1000">
    <svrl:fired-rule context="gmd:EX_GeographicBoundingBox" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="not(gmd:extentTypeCode)" />
      <xsl:otherwise>
        <svrl:failed-assert test="not(gmd:extentTypeCode)">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>extentTypeCode is not permitted</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M53" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M53" priority="-1" />
  <xsl:template match="@*|node()" mode="M53" priority="-2">
    <xsl:apply-templates mode="M53" select="*" />
  </xsl:template>

<!--PATTERN westBoundLongitude (344) -> restricted domain-->
<svrl:text>westBoundLongitude (344) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:westBoundLongitude" mode="M54" priority="1000">
    <svrl:fired-rule context="gmd:westBoundLongitude" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1" />
      <xsl:otherwise>
        <svrl:failed-assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>precision of two decimal places is required</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M54" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M54" priority="-1" />
  <xsl:template match="@*|node()" mode="M54" priority="-2">
    <xsl:apply-templates mode="M54" select="*" />
  </xsl:template>

<!--PATTERN eastBoundLongitude (345) -> restricted domain-->
<svrl:text>eastBoundLongitude (345) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:eastBoundLongitude" mode="M55" priority="1000">
    <svrl:fired-rule context="gmd:eastBoundLongitude" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1" />
      <xsl:otherwise>
        <svrl:failed-assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>precision of two decimal places is required</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M55" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M55" priority="-1" />
  <xsl:template match="@*|node()" mode="M55" priority="-2">
    <xsl:apply-templates mode="M55" select="*" />
  </xsl:template>

<!--PATTERN southBoundLatitude (346) -> restricted domain-->
<svrl:text>southBoundLatitude (346) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:southBoundLatitude" mode="M56" priority="1000">
    <svrl:fired-rule context="gmd:southBoundLatitude" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1" />
      <xsl:otherwise>
        <svrl:failed-assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>precision of two decimal places is required</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M56" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M56" priority="-1" />
  <xsl:template match="@*|node()" mode="M56" priority="-2">
    <xsl:apply-templates mode="M56" select="*" />
  </xsl:template>

<!--PATTERN northBoundLatitude (347) -> restricted domain-->
<svrl:text>northBoundLatitude (347) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:northBoundLatitude" mode="M57" priority="1000">
    <svrl:fired-rule context="gmd:northBoundLatitude" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1" />
      <xsl:otherwise>
        <svrl:failed-assert test="string-length(normalize-space(substring-after(gco:Decimal, '.'))) > 1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>precision of two decimal places is required</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M57" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M57" priority="-1" />
  <xsl:template match="@*|node()" mode="M57" priority="-2">
    <xsl:apply-templates mode="M57" select="*" />
  </xsl:template>

<!--PATTERN geographicIdentifier (349) -> restricted domain-->
<svrl:text>geographicIdentifier (349) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:geographicIdentifier" mode="M58" priority="1000">
    <svrl:fired-rule context="gmd:geographicIdentifier" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:MD_Identifier" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:MD_Identifier">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>identifier if present must be MD_Identifier</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M58" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M58" priority="-1" />
  <xsl:template match="@*|node()" mode="M58" priority="-2">
    <xsl:apply-templates mode="M58" select="*" />
  </xsl:template>

<!--PATTERN extent (351) -> restricted domain-->
<svrl:text>extent (351) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_TemporalExtent/gmd:extent" mode="M59" priority="1000">
    <svrl:fired-rule context="gmd:EX_TemporalExtent/gmd:extent" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gml:TimePeriod" />
      <xsl:otherwise>
        <svrl:failed-assert test="gml:TimePeriod">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>extent if present must be gml:TimePeriod</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M59" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M59" priority="-1" />
  <xsl:template match="@*|node()" mode="M59" priority="-2">
    <xsl:apply-templates mode="M59" select="*" />
  </xsl:template>

<!--PATTERN verticalDatum (358) -> restricted domain-->
<svrl:text>verticalDatum (358) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_VerticalExtent" mode="M60" priority="1000">
    <svrl:fired-rule context="gmd:EX_VerticalExtent" />
    <xsl:variable name="list" select="'https://www.seadatanet.org/urnurl/SDN:L11::'" />
    <xsl:variable name="identifier" select="gmd:verticalCRS/gml:VerticalCRS/gml:verticalDatum/gml:VerticalDatum/gml:identifier" />
    <xsl:variable name="uom" select="gmd:verticalCRS/gml:VerticalCRS/gml:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@gml:uom" />
    <xsl:variable name="identifierValue" select="substring-after($identifier,$list)" />
    <xsl:variable name="length" select="string-length($identifierValue)" />
    <xsl:variable name="catalog" select="document($identifier)" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="starts-with($identifier,$list)" />
      <xsl:otherwise>
        <svrl:failed-assert test="starts-with($identifier,$list)">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>vertical datum identifier must start with https://www.seadatanet.org/urnurl/SDN:L11::</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $identifierValue]" />
      <xsl:otherwise>
        <svrl:failed-assert test="$catalog//dc:identifier[substring(.,string-length(.) - $length + 1,$length) = $identifierValue]">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>the given vertical datum identifier is not included in L11 list</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="$uom='metres'" />
      <xsl:otherwise>
        <svrl:failed-assert test="$uom='metres'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>unit of measure must be 'metres'</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M60" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M60" priority="-1" />
  <xsl:template match="@*|node()" mode="M60" priority="-2">
    <xsl:apply-templates mode="M60" select="*" />
  </xsl:template>

<!--PATTERN identifier (365)-->
<svrl:text>identifier (365)</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:identifier" mode="M61" priority="1000">
    <svrl:fired-rule context="gmd:identifier" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:MD_Identifier or gco:nilReason" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:MD_Identifier or gco:nilReason">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>identifier if present must be MD_Identifier</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M61" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M61" priority="-1" />
  <xsl:template match="@*|node()" mode="M61" priority="-2">
    <xsl:apply-templates mode="M61" select="*" />
  </xsl:template>

<!--PATTERN individualName (375), organisationName (376) -> obligation restriction-->
<svrl:text>individualName (375), organisationName (376) -&gt; obligation restriction</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:CI_ResponsibleParty" mode="M62" priority="1000">
    <svrl:fired-rule context="gmd:CI_ResponsibleParty" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:individualName or gmd:organisationName" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:individualName or gmd:organisationName">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>individualName or organisationName missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M62" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M62" priority="-1" />
  <xsl:template match="@*|node()" mode="M62" priority="-2">
    <xsl:apply-templates mode="M62" select="*" />
  </xsl:template>

<!--PATTERN organisationName (376) -> restricted domain-->
<svrl:text>organisationName (376) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:CI_ResponsibleParty" mode="M63" priority="1000">
    <svrl:fired-rule context="gmd:CI_ResponsibleParty" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="../../../../sdn:additionalDocumentation or gmd:organisationName/sdn:SDN_EDMOCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="../../../../sdn:additionalDocumentation or gmd:organisationName/sdn:SDN_EDMOCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>SDN_EDMOCode code list missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M63" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M63" priority="-1" />
  <xsl:template match="@*|node()" mode="M63" priority="-2">
    <xsl:apply-templates mode="M63" select="*" />
  </xsl:template>

<!--PATTERN contactInfo (378) -> mandatory; restricted cardinality-->
<svrl:text>contactInfo (378) -&gt; mandatory; restricted cardinality</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:CI_ResponsibleParty" mode="M64" priority="1000">
    <svrl:fired-rule context="gmd:CI_ResponsibleParty" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:contactInfo" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:contactInfo">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>contactInfo missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:contactInfo)=1" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:contactInfo)=1">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>contactInfo cardinality differs from 1</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M64" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M64" priority="-1" />
  <xsl:template match="@*|node()" mode="M64" priority="-2">
    <xsl:apply-templates mode="M64" select="*" />
  </xsl:template>

<!--PATTERN country (385) -> restricted data type and domain-->
<svrl:text>country (385) -&gt; restricted data type and domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:country" mode="M65" priority="1000">
    <svrl:fired-rule context="gmd:country" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="sdn:SDN_CountryCode" />
      <xsl:otherwise>
        <svrl:failed-assert test="sdn:SDN_CountryCode">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>country must be of type SDN_CountryCode</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M65" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M65" priority="-1" />
  <xsl:template match="@*|node()" mode="M65" priority="-2">
    <xsl:apply-templates mode="M65" select="*" />
  </xsl:template>

<!--PATTERN electronicMailAddress (386) -> mandatory-->
<svrl:text>electronicMailAddress (386) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:CI_Address" mode="M66" priority="1000">
    <svrl:fired-rule context="gmd:CI_Address" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:electronicMailAddress" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:electronicMailAddress">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>electronicMailAddress missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M66" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M66" priority="-1" />
  <xsl:template match="@*|node()" mode="M66" priority="-2">
    <xsl:apply-templates mode="M66" select="*" />
  </xsl:template>

<!--PATTERN address (389) -> mandatory-->
<svrl:text>address (389) -&gt; mandatory</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:CI_Contact" mode="M67" priority="1000">
    <svrl:fired-rule context="gmd:CI_Contact" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:address" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:address">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>address missing</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M67" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M67" priority="-1" />
  <xsl:template match="@*|node()" mode="M67" priority="-2">
    <xsl:apply-templates mode="M67" select="*" />
  </xsl:template>

<!--PATTERN linkage (397) -> restricted domain-->
<svrl:text>linkage (397) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:linkage" mode="M68" priority="1000">
    <svrl:fired-rule context="gmd:linkage" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="starts-with(gmd:URL,'http://') or starts-with(gmd:URL,'https://') or starts-with(gmd:URL,'ftp://')" />
      <xsl:otherwise>
        <svrl:failed-assert test="starts-with(gmd:URL,'http://') or starts-with(gmd:URL,'https://') or starts-with(gmd:URL,'ftp://')">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>linkage should start with http://, https://, ftp://</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M68" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M68" priority="-1" />
  <xsl:template match="@*|node()" mode="M68" priority="-2">
    <xsl:apply-templates mode="M68" select="*" />
  </xsl:template>

<!--PATTERN otherConstraints (72) -> conditional-->
<svrl:text>otherConstraints (72) -&gt; conditional</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_LegalConstraints" mode="M69" priority="1000">
    <svrl:fired-rule context="gmd:MD_LegalConstraints" />
    <xsl:variable name="restrictionCode" select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:otherConstraints or $restrictionCode!='otherRestrictions'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:otherConstraints or $restrictionCode!='otherRestrictions'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>otherConstraints missing while accessConstraints="otherRestrictions"</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M69" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M69" priority="-1" />
  <xsl:template match="@*|node()" mode="M69" priority="-2">
    <xsl:apply-templates mode="M69" select="*" />
  </xsl:template>

<!--PATTERN shortName (308) -> conditional-->
<svrl:text>shortName (308) -&gt; conditional</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_ExtendedElementInformation" mode="M70" priority="1000">
    <svrl:fired-rule context="gmd:MD_ExtendedElementInformation" />
    <xsl:variable name="datatypeCode" select="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:shortName or $datatypeCode='codelistElement'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:shortName or $datatypeCode='codelistElement'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if dataType notEqual 'codelistElement' then shortName is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M70" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M70" priority="-1" />
  <xsl:template match="@*|node()" mode="M70" priority="-2">
    <xsl:apply-templates mode="M70" select="*" />
  </xsl:template>

<!--PATTERN shortName (309) -> conditional-->
<svrl:text>shortName (309) -&gt; conditional</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_ExtendedElementInformation" mode="M71" priority="1000">
    <svrl:fired-rule context="gmd:MD_ExtendedElementInformation" />
    <xsl:variable name="datatypeCode" select="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:domainCode or $datatypeCode!='codelistElement'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:domainCode or $datatypeCode!='codelistElement'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if dataType = 'codelistElement' then domainCode is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M71" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M71" priority="-1" />
  <xsl:template match="@*|node()" mode="M71" priority="-2">
    <xsl:apply-templates mode="M71" select="*" />
  </xsl:template>

<!--PATTERN condition (312) -> conditional-->
<svrl:text>condition (312) -&gt; conditional</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_ExtendedElementInformation" mode="M72" priority="1000">
    <svrl:fired-rule context="gmd:MD_ExtendedElementInformation" />
    <xsl:variable name="obligationCode" select="gmd:obligation/gmd:MD_ObligationCode/@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:condition or $obligationCode!='conditional'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:condition or $obligationCode!='conditional'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if obligation = 'conditional' then condition is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M72" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M72" priority="-1" />
  <xsl:template match="@*|node()" mode="M72" priority="-2">
    <xsl:apply-templates mode="M72" select="*" />
  </xsl:template>

<!--PATTERN obligation (311), maximumOccurrence (314), domainValue (315) -> conditional-->
<svrl:text>obligation (311), maximumOccurrence (314), domainValue (315) -&gt; conditional</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_ExtendedElementInformation" mode="M73" priority="1000">
    <svrl:fired-rule context="gmd:MD_ExtendedElementInformation" />
    <xsl:variable name="datatypeCode" select="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:obligation or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:obligation or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then obligation is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:maximumOccurrence or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:maximumOccurrence or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then maximumOccurrence is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:domainValue or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:domainValue or $datatypeCode='codelist' or $datatypeCode='enumeration' or $datatypeCode='codelistElement'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>if dataType notEqual 'codelist', 'enumeration' or 'codelistElement' then domainValue is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M73" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M73" priority="-1" />
  <xsl:template match="@*|node()" mode="M73" priority="-2">
    <xsl:apply-templates mode="M73" select="*" />
  </xsl:template>

<!--PATTERN EX_GeographicBoundingBox (343) -> restricted domain-->
<svrl:text>EX_GeographicBoundingBox (343) -&gt; restricted domain</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:EX_GeographicBoundingBox" mode="M74" priority="1000">
    <svrl:fired-rule context="gmd:EX_GeographicBoundingBox" />
    <xsl:variable name="west" select="number(gmd:westBoundLongitude/gco:Decimal)" />
    <xsl:variable name="east" select="number(gmd:eastBoundLongitude/gco:Decimal)" />
    <xsl:variable name="south" select="number(gmd:southBoundLatitude/gco:Decimal)" />
    <xsl:variable name="north" select="number(gmd:northBoundLatitude/gco:Decimal)" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="-180.0 &lt;= $west and $west &lt;= 180.0" />
      <xsl:otherwise>
        <svrl:failed-assert test="-180.0 &lt;= $west and $west &lt;= 180.0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>westBoundLongitude must be in -180; 180</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="-180.0 &lt;= $east and $east &lt;= 180.0" />
      <xsl:otherwise>
        <svrl:failed-assert test="-180.0 &lt;= $east and $east &lt;= 180.0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>eastBoundLongitude must be in -180; 180</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="-90.0 &lt;= $south and $south &lt;= $north and $south &lt;=90.0" />
      <xsl:otherwise>
        <svrl:failed-assert test="-90.0 &lt;= $south and $south &lt;= $north and $south &lt;=90.0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>southBoundLatitude must be in -90; 90 and less than northBoundLatitude</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="-90.0 &lt;= $north and $south &lt;= $north and $north &lt;= 90.0" />
      <xsl:otherwise>
        <svrl:failed-assert test="-90.0 &lt;= $north and $south &lt;= $north and $north &lt;= 90.0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>northBoundLatitude must be in -90; 90 and greater than southBoundLatitude</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M74" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M74" priority="-1" />
  <xsl:template match="@*|node()" mode="M74" priority="-2">
    <xsl:apply-templates mode="M74" select="*" />
  </xsl:template>

<!--PATTERN null elements are not allowed-->
<svrl:text>null elements are not allowed</svrl:text>

	<!--RULE -->
<xsl:template match="*" mode="M75" priority="1000">
    <svrl:fired-rule context="*" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="* or normalize-space(text()) or @gco:nilReason or @xlink:href" />
      <xsl:otherwise>
        <svrl:failed-assert test="* or normalize-space(text()) or @gco:nilReason or @xlink:href">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>null objects are not permitted unless for optional elements - in that case a reason for the null must be provided - a xlink pointer is also allowed</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M75" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M75" priority="-1" />
  <xsl:template match="@*|node()" mode="M75" priority="-2">
    <xsl:apply-templates mode="M75" select="*" />
  </xsl:template>

<!--PATTERN associationType (66.5) -> A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo-->
<svrl:text>associationType (66.5) -&gt; A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M76" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(//gmd:associationType[gmd:DS_AssociationTypeCode/@codeListValue='source']) &lt; 2" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(//gmd:associationType[gmd:DS_AssociationTypeCode/@codeListValue='source']) &lt; 2">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>A maximum of one associationType.AssociationTypeCode/@codeListValue = 'source' for all aggregationInfo</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M76" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M76" priority="-1" />
  <xsl:template match="@*|node()" mode="M76" priority="-2">
    <xsl:apply-templates mode="M76" select="*" />
  </xsl:template>

<!--PATTERN associationType (66.5) -> At least one keyword with type 'platform_class' should be documented-->
<svrl:text>associationType (66.5) -&gt; At least one keyword with type 'platform_class' should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M77" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']) > 0" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']) > 0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>At least one keyword with type 'platform_class' should be documented</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M77" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M77" priority="-1" />
  <xsl:template match="@*|node()" mode="M77" priority="-2">
    <xsl:apply-templates mode="M77" select="*" />
  </xsl:template>

<!--PATTERN associationType (66.5) -> At least one keyword with type 'parameter' should be documented-->
<svrl:text>associationType (66.5) -&gt; At least one keyword with type 'parameter' should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M78" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']) > 0" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(//gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']) > 0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>At least one keyword with type 'parameter' should be documented</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M78" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M78" priority="-1" />
  <xsl:template match="@*|node()" mode="M78" priority="-2">
    <xsl:apply-templates mode="M78" select="*" />
  </xsl:template>

<!--PATTERN RS_Identifier -> L101 thesaurus should be documented-->
<svrl:text>RS_Identifier -&gt; L101 thesaurus should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:RS_identifier" mode="M79" priority="1000">
    <svrl:fired-rule context="gmd:RS_identifier" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:authority/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L10'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:authority/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L10'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L10 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:authority/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L10'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:authority/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L10'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L10 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M79" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M79" priority="-1" />
  <xsl:template match="@*|node()" mode="M79" priority="-2">
    <xsl:apply-templates mode="M79" select="*" />
  </xsl:template>

<!--PATTERN MD_Keywords -> SDN thesaurus should be documented-->
<svrl:text>MD_Keywords -&gt; SDN thesaurus should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Keywords[gmd:keyword/sdn:SDN_ParameterDiscoveryCode]" mode="M80" priority="1000">
    <svrl:fired-rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_ParameterDiscoveryCode]" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='P02'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='P02'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>P02 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:P02'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:P02'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>P02 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M80" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M80" priority="-1" />
  <xsl:template match="@*|node()" mode="M80" priority="-2">
    <xsl:apply-templates mode="M80" select="*" />
  </xsl:template>

<!--PATTERN MD_Keywords -> SDN thesaurus should be documented-->
<svrl:text>MD_Keywords -&gt; SDN thesaurus should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Keywords[gmd:keyword/sdn:SDN_DeviceCategoryCode]" mode="M81" priority="1000">
    <svrl:fired-rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_DeviceCategoryCode]" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L05'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L05'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L05 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L05'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L05'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L05 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M81" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M81" priority="-1" />
  <xsl:template match="@*|node()" mode="M81" priority="-2">
    <xsl:apply-templates mode="M81" select="*" />
  </xsl:template>

<!--PATTERN MD_Keywords -> SDN thesaurus should be documented-->
<svrl:text>MD_Keywords -&gt; SDN thesaurus should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Keywords[gmd:keyword/sdn:SDN_PlatformCategoryCode]" mode="M82" priority="1000">
    <svrl:fired-rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_PlatformCategoryCode]" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L06'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='L06'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L06 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L06'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:L06'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>L06 should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M82" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M82" priority="-1" />
  <xsl:template match="@*|node()" mode="M82" priority="-2">
    <xsl:apply-templates mode="M82" select="*" />
  </xsl:template>

<!--PATTERN MD_Keywords -> SDN thesaurus should be documented-->
<svrl:text>MD_Keywords -&gt; SDN thesaurus should be documented</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Keywords[gmd:keyword/sdn:SDN_EDMERPCode]" mode="M83" priority="1000">
    <svrl:fired-rule context="gmd:MD_Keywords[gmd:keyword/sdn:SDN_EDMERPCode]" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='EDMERP'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='EDMERP'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>EDMERP should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:EDMERP'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:thesaurusName/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString='https://www.seadatanet.org/urnurl/SDN:EDMERP'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>EDMERP should be referenced</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M83" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M83" priority="-1" />
  <xsl:template match="@*|node()" mode="M83" priority="-2">
    <xsl:apply-templates mode="M83" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE SC7-->
<svrl:text>INSPIRE SC7</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" mode="M84" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='creation'])&lt;2" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='creation'])&lt;2">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE SC7] more than one creation date is not permitted</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M84" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M84" priority="-1" />
  <xsl:template match="@*|node()" mode="M84" priority="-2">
    <xsl:apply-templates mode="M84" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE SC8-->
<svrl:text>INSPIRE SC8</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" mode="M85" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:citation/gmd:CI_Citation/gmd:identifier" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:citation/gmd:CI_Citation/gmd:identifier">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE SC8] the resource identifier is mandatory</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M85" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M85" priority="-1" />
  <xsl:template match="@*|node()" mode="M85" priority="-2">
    <xsl:apply-templates mode="M85" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE SC10-->
<svrl:text>INSPIRE SC10</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" mode="M86" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="count(gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox)>0" />
      <xsl:otherwise>
        <svrl:failed-assert test="count(gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox)>0">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE SC10] at least one geographic bounding box must be provided</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M86" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M86" priority="-1" />
  <xsl:template match="@*|node()" mode="M86" priority="-2">
    <xsl:apply-templates mode="M86" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE SC16-->
<svrl:text>INSPIRE SC16</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode" mode="M87" priority="1000">
    <svrl:fired-rule context="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="@codeListValue='pointOfContact'" />
      <xsl:otherwise>
        <svrl:failed-assert test="@codeListValue='pointOfContact'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE SC16] the role code of the contact responsibleParty must be 'pointOfContact'</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M87" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M87" priority="-1" />
  <xsl:template match="@*|node()" mode="M87" priority="-2">
    <xsl:apply-templates mode="M87" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE SC17-->
<svrl:text>INSPIRE SC17</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_DataIdentification" mode="M88" priority="1000">
    <svrl:fired-rule context="gmd:MD_DataIdentification" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="contains(gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString,'GEMET')" />
      <xsl:otherwise>
        <svrl:failed-assert test="contains(gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString,'GEMET')">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE SC17] at least one keyword from the GEMET thesaurus must be documented</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M88" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M88" priority="-1" />
  <xsl:template match="@*|node()" mode="M88" priority="-2">
    <xsl:apply-templates mode="M88" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE conformance report as regards metadata-->
<svrl:text>INSPIRE conformance report as regards metadata</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M89" priority="1001">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] conformance report against INSPIRE regulation as regards metadata must be documented</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M89" select="*" />
  </xsl:template>

	<!--RULE -->
<xsl:template match="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata']" mode="M89" priority="1000">
    <svrl:fired-rule context="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EC) No 1205/2008 of 3 December 2008 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards metadata']" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2008-12-04'" />
      <xsl:otherwise>
        <svrl:failed-assert test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2008-12-04'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] date must be 2008-12-04</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'" />
      <xsl:otherwise>
        <svrl:failed-assert test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] pass should be true</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M89" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M89" priority="-1" />
  <xsl:template match="@*|node()" mode="M89" priority="-2">
    <xsl:apply-templates mode="M89" select="*" />
  </xsl:template>

<!--PATTERN INSPIRE conformance report as regards interoperability of spatial data sets and services-->
<svrl:text>INSPIRE conformance report as regards interoperability of spatial data sets and services</svrl:text>

	<!--RULE -->
<xsl:template match="gmd:MD_Metadata" mode="M90" priority="1001">
    <svrl:fired-rule context="gmd:MD_Metadata" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services'" />
      <xsl:otherwise>
        <svrl:failed-assert test="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] conformance report against INSPIRE regulation as regards interoperability of spatial data sets and services must be documented</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M90" select="*" />
  </xsl:template>

	<!--RULE -->
<xsl:template match="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services']" mode="M90" priority="1000">
    <svrl:fired-rule context="gmd:dataQualityInfo[*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString='COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services']" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2010-12-08'" />
      <xsl:otherwise>
        <svrl:failed-assert test="*/gmd:report/*/gmd:result/*/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date='2010-12-08'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] date must be 2010-12-08</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'" />
      <xsl:otherwise>
        <svrl:failed-assert test="*/gmd:report/*/gmd:result/*/gmd:pass/gco:Boolean='true'">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>[INSPIRE conformance report] pass should be true</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M90" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M90" priority="-1" />
  <xsl:template match="@*|node()" mode="M90" priority="-2">
    <xsl:apply-templates mode="M90" select="*" />
  </xsl:template>

<!--PATTERN Validation of codelists-->
<svrl:text>Validation of codelists</svrl:text>

	<!--RULE -->
<xsl:template match="*[@codeList]" mode="M91" priority="1000">
    <svrl:fired-rule context="*[@codeList]" />
    <xsl:variable name="catalog" select="document(substring-before(@codeList,'#'))" />
    <xsl:variable name="codeList" select="substring-after(@codeList,'#')" />
    <xsl:variable name="codeListValue" select="@codeListValue" />

		<!--ASSERT -->
<xsl:choose>
      <xsl:when test="$catalog//gmx:codelistItem/*[@gml:id=$codeList or @gml32:id=$codeList]/gmx:codeEntry/*[gml:identifier=$codeListValue or gml32:identifier=$codeListValue]" />
      <xsl:otherwise>
        <svrl:failed-assert test="$catalog//gmx:codelistItem/*[@gml:id=$codeList or @gml32:id=$codeList]/gmx:codeEntry/*[gml:identifier=$codeListValue or gml32:identifier=$codeListValue]">
          <xsl:attribute name="location">
            <xsl:apply-templates mode="schematron-select-full-path" select="." />
          </xsl:attribute>
          <svrl:text>the value is not allowed by the codelist catalogue</svrl:text>
        </svrl:failed-assert>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="M91" select="*" />
  </xsl:template>
  <xsl:template match="text()" mode="M91" priority="-1" />
  <xsl:template match="@*|node()" mode="M91" priority="-2">
    <xsl:apply-templates mode="M91" select="*" />
  </xsl:template>
</xsl:stylesheet>
