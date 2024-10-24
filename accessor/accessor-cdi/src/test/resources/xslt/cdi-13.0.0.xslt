<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    version="2.0">
    <xsl:output omit-xml-declaration="yes" indent="yes"></xsl:output>
    
    <xsl:template match="//processing-instruction()" />
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*[@gco:isoType]">
        <xsl:variable name="elementName"><xsl:value-of select="replace(@gco:isoType,'_Type','')"/></xsl:variable>
        <xsl:element namespace="http://www.isotc211.org/2005/gmd" name="gmd:{$elementName}">            
            <xsl:apply-templates select="@*[not(local-name(.)='isoType')]|node()" />
        </xsl:element>
    </xsl:template>
    <xsl:template match="gmd:resourceConstraints[gmd:MD_Constraints/gmd:useLimitation]">
        <gmd:resourceConstraints>
            <gmd:MD_LegalConstraints>
                <gmd:accessConstraints>
                    <gmd:MD_RestrictionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions"/>
                </gmd:accessConstraints>
                <gmd:otherConstraints>
                    <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1e">Public access to spatial data sets and services would adversely affect intellectual property rights.</gmx:Anchor>
                </gmd:otherConstraints>
            </gmd:MD_LegalConstraints>
        </gmd:resourceConstraints>
    </xsl:template>
    <xsl:template match="gmd:resourceConstraints[gmd:MD_LegalConstraints/gmd:accessConstraints]">
        <gmd:resourceConstraints>
            <gmd:MD_LegalConstraints>
                <gmd:useConstraints>
                    <gmd:MD_RestrictionCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions" />
                </gmd:useConstraints>
                <gmd:otherConstraints>
                    <xsl:element name="gmx:Anchor">
                        <xsl:attribute name="xlink:href"><xsl:value-of select="gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor/@xlink:href"/></xsl:attribute>
                        <xsl:value-of select="gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor"/>
                    </xsl:element>                    
                </gmd:otherConstraints>
            </gmd:MD_LegalConstraints>
        </gmd:resourceConstraints>
    </xsl:template>
    <xsl:template match="*[@codeListValue and not(namespace-uri(.)='http://www.isotc211.org/2005/gmd') ]">
        <xsl:element namespace="http://www.isotc211.org/2005/gco" name="gco:CharacterString">            
            <xsl:value-of select="@codeListValue"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@xsi:schemaLocation">
        <xsl:attribute name="xsi:schemaLocation">http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd http://www.isotc211.org/2005/gmx http://schemas.opengis.net/iso/19139/20060504/gmx/gmx.xsd</xsl:attribute>
    </xsl:template>
    <xsl:template match="gmd:LanguageCode">
        <xsl:element name="gmd:LanguageCode">
            <xsl:attribute name="codeList">http://www.loc.gov/standards/iso639-2/</xsl:attribute>
            <xsl:attribute name="codeListValue"><xsl:value-of select="@codeListValue"/></xsl:attribute>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="gmd:CI_DateTypeCode[../../../../../../gmd:thesaurusName]">
        <gmd:CI_DateTypeCode codeList="https://vocab.nerc.ac.uk/isoCodelists/sdnCodelists/gmxCodeLists.xml#CI_DateTypeCode"
            codeListValue="publication"
            codeSpace="ISOTC211/19115">publication</gmd:CI_DateTypeCode>
    </xsl:template>
    <!--xsl:template match="gmx:Anchor[contains(@xlink:href,'https://www.seadatanet.org/urnurl/SDN:L08')]">
        <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">no limitations to public access</gmx:Anchor>
    </xsl:template-->
    <xsl:template match="*[not(
        namespace-uri(.)='http://www.isotc211.org/2005/gmd' or 
        namespace-uri(.)='http://www.isotc211.org/2005/gmi' or
        namespace-uri(.)='http://www.isotc211.org/2005/srv' or
        namespace-uri(.)='http://www.isotc211.org/2005/gco' or
        namespace-uri(.)='http://www.isotc211.org/2005/gts' or
        namespace-uri(.)='http://www.isotc211.org/2005/gmx' or
        namespace-uri(.)='http://www.w3.org/1999/xlink' or
        namespace-uri(.)='http://www.opengis.net/gml' or
        namespace-uri(.)='http://www.w3.org/2001/XMLSchema-instance' or
        @gco:isoType or
        @codeListValue
        )]">        
    </xsl:template>
 
</xsl:stylesheet>