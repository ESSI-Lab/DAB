import type {
    MD_CharacterSetCode,
    CI_ResponsibleParty,
    CI_Citation,
    MD_ScopeCode,
    MD_SpatialRepresentationTypeCode,
    CI_PresentationFormCode,
    MD_TopicCategoryCode,
    MD_RestrictionCode,
    MD_ClassificationCode,
    MD_MaintenanceFrequencyCode,
    SpatialDataServiceType,
    SV_CouplingType,
    DCPList,
    MD_ProgressCode,
    SpatialScopeKeyword,
    PriorityDataSetKeyword,
    ServiceProtocolCode,
    OnLineDescriptionCode,
    LE_Source,
    ParameterDirection,
    CI_OnlineFunctionCode,
    MD_BrowseGraphic
} from './ISO codelists.ts'

import type {
    FormatName,
    ModelMaturityLevel,
    ModelType,
    SupportedPlatform,
    ModelCategory,
    XlinkHref
} from './Custom codelists.ts'


export type Metadata = {
    // ===================== METADATA GENERIC INFO =====================
    // #region
    id: string; // --> gmi:MI_Metadata/fileIdentifier/gco:CharacterString
    resource_author_id: string; // --> DA IGNORARE PER L'HARVESTING DEL DAB! (Solo a scopo di SIM Data Catalog interno)
    character_set: { // --> gmi:MI_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode
        uri?: string; // --> codeList URI
        value: MD_CharacterSetCode; // --> codeListValue
    }; 
    hierarchy_level: { // --> gmi:MI_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode
        uri?: string; // --> codeList URI
        value: MD_ScopeCode // --> codeListValue
    };
    parent_identifier: string | null; // --> gmi:MI_Metadata/gmd:parentIdentifier/gco:CharacterString OPPURE gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:series/gmd:CI_Series
    metadata_profile?: {
        name: string; // --> gmi:MI_Metadata/gmd:metadataStandardName/gco:CharacterString
        version: string; // --> gmi:MI_Metadata/gmd:metadataStandardVersion/gco:CharacterString
    };
    metadata_language: string; // --> gmi:MI_Metadata/gmx:language/gmd:LanguageCode (ISO 639-2/B)
    metadata_updated_at: Date; // --> gmi:MI_Metadata//gmd:dateStamp/gco:Date or gco:DateTime
    metadata_owner: CI_ResponsibleParty; // --> gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty (gmd:CI_RoleCode = pointOfContact)
    metadata_version?: string; // --> NEW! (TESTO LIBERO - Riporta la versione del metadato. Se il presente metadato non rappresenta la versione originale, il campo metadata_original_version diventa obbligatorio)
    metadata_original_version?: string; // --> NEW! (TESTO LIBERO - Riporta l'ID del metadato originale dal quale è stata originata la presente versione - corrisponde al campo Provenance definito da Stefano Nativi in varie call, ma di cui non è stato trovato un riferimento nell'ISO 19115)
    // #endregion
    // ===================== IDENTIFICATION INFO =====================
    // #region
    // --------------------- Generic identification info ---------------------
    // #region
    title: string; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString
    abstract: string; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:abstract/gco:CharacterString
    edition: string; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString
    edition_date: Date; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString
    resource_creation_date: Date; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (gmd:CI_DateTypeCode = creation)
    resource_publication_date?: Date; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (gmd:CI_DateTypeCode = publication)
    resource_revision_date: Date; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (gmd:CI_DateTypeCode = revision)
    resource_expiration_date?: Date | null; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (gmd:CI_DateTypeCode = expiry)
    resource_owner: CI_ResponsibleParty; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty (gmd:CI_RoleCode = owner)
    other_roles?: CI_ResponsibleParty[]; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty
    presentation_form?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:presentationForm/gmd:CI_PresentationFormCode
        uri?: string; // --> codeList URI
        value: CI_PresentationFormCode; // --> codeListValue
    };
    point_of_contact: CI_ResponsibleParty; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:pointOfContact/gmd:CI_ResponsibleParty
    graphic_overview?: MD_BrowseGraphic; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:graphicOverview/gmd:MD_BrowseGraphic
    descriptive_keywords: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:descriptiveKeywords/gmd:MD_Keywords
        keywords: { // --> ./gmd:keyword
            label: string; // --> ./gco:Characterstring
            uri?: string; // --> ./gmx:Anchor
        }[];
        thesaurus?: { // --> ./thesaurusName/gmd:CI_Citation
            name: string; // --> ./gmd:title/gco:CharacterString
            url?: string; // --> ./gmd:title/gmx:Anchor
            publication_date?: Date; // --> ./gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (dateType/CI_DateTypeCode = publication)
            revision_date?: Date; // --> ./gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (dateType/CI_DateTypeCode = revision)
        }
    }[];
    spatial_scope?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:descriptiveKeywords/gmd:MD_Keywords
        keywords: SpatialScopeKeyword; // --> ./gmd:keyword
        thesaurus: { // --> ./gmd:thesaurusName/gmd:CI_Citation
            name: "Spatial scope"; // --> ./gmd:title/gco:CharacterString
            url: "http://inspire.ec.europa.eu/metadata-codelist/SpatialScope"; // --> ./gmd:title/gmx:Anchor
            publication_date: "2019-05-22"; // --> ./gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (dateType/CI_DateTypeCode = publication)
        }
    };
    inspire_priority_dataset?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:descriptiveKeywords/gmd:MD_Keywords
        keywords: PriorityDataSetKeyword; // --> ./gmd:keyword
        thesaurus: { // --> ./gmd:thesaurusName/gmd:CI_Citation
            name: "INSPIRE priority data set"; // --> ./gmd:title/gco:CharacterString
            url: "http://inspire.ec.europa.eu/metadata-codelist/PriorityDataset"; // --> ./gmd:title/gmx:Anchor
            publication_date: "2018-04-04"; // --> ./gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (dateType/CI_DateTypeCode = publication)
        }
    }
    status?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:status/gmd:MD_ProgressCode
        uri?: string; // --> codeList URI
        value: MD_ProgressCode; // --> codeListValue
    };
    // #endregion
    // --------------------- Time and update information ---------------------
    // #region
    update_frequency?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode
        uri?: string; // --> codeList URI
        value: MD_MaintenanceFrequencyCode; // --> codeListValue
    };
    date_of_next_update?: Date | null; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:dateOfNextUpdate/gco:Date or gco:DateTime
    temporal_extent?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod
        start_date: Date; // --> ./gml:beginPosition
        end_date: Date; // --> ./gml:endPosition
    };
    // #endregion
    // --------------------- Geospatial information ---------------------
    // #region
    bbox_4326?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox
        west_bound_longitude: number; // --> ./gmd:westBoundLongitude/gco:Decimal
        east_bound_longitude: number; // --> ./gmd:eastBoundLongitude/gco:Decimal
        south_bound_latitude: number; // --> ./gmd:southBoundLatitude/gco:Decimal
        north_bound_latitude: number; // --> ./gmd:northBoundLatitude/gco:Decimal
    };
    native_epsg?: XlinkHref; // --> gmi:MI_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gmx:Anchor
    bbox_native_epsg_gml_polygon?: string; // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:Polygon/gml:exterior/gml:LinearRing (srsName=<native_epsg>)/gml:posList
    vertical_extent?: { // --> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent
        minimum_value: number; // --> ./gmd:minimumValue/gco:Real
        maximum_value: number; // --> ./gmd:maximumValue/gco:Real
        crs: string; // --> ./gmd:verticalCRS/xlink:href
    };
    // #endregion
    // --------------------- License & legal constraints ---------------------
    // #region
    resource_constraints: { //--> gmi:MI_Metadata/gmd:identificationInfo/[gmd:MD_DataIdentification | srv:SV_ServiceIdentification]/gmd:resourceConstraints
        use_limitation?: string; // --> ./gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString
        access_constraints: { // --> ./gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode
            uri?: string; // --> codeList URI
            value: MD_RestrictionCode; // --> codeListValue
        };
        use_constraints?: { // --> ./gmd:MD_LegalConstraints/gmd:useConstraints/gmd:MD_RestrictionCode
            uri?: string; // --> codeList URI
            value: MD_RestrictionCode; // --> codeListValue
        };
        other_constraints: string; // --> ./gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString
        security_classification?: { // --> ./gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode 
            uri?: string; // --> codeList URI
            value: MD_ClassificationCode // --> codeListValue
        };
        security_note?: string | null; // --> ./gmd:MD_SecurityConstraints/gmd:userNote/gco:CharacterString
    };
    // #endregion
    // --------------------- DATASET itentification info ---------------------
    // #region
    data_language?: string; // --> gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmx:language/gmd:LanguageCode
    dataset_type?: { // --> gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode
        uri?: string; // --> codeList URI
        value: MD_SpatialRepresentationTypeCode // --> codeListValue
    };
    spatial_resolution?: { // --> gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance
        value: number; // node value
        uom: string; // uom attribute
    };
    equivalent_scale?: number; // --> gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer
    raster_nodata_value?: number; // --> NEW! (DOUBLE - Indica il valore NoData utilizzato per i raster)
    topic_categories?: MD_TopicCategoryCode[]; // --> gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode
    // #endregion
    // --------------------- SERVICE itentification info (ISO 19119) ---------------------
    // #region
    service_type?: SpatialDataServiceType; // --> gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName
    service_type_version?: string; // --> gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString
    service_coupling_type?: SV_CouplingType; // --> gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType
    service_operates_on?: string[]; // --> gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/xlink:href
    service_operations?: { // --> gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata
        operation_name: string; // --> ./srv:operationName/gco:CharacterString
        operation_description?: string; // --> ./srv:operationDescription/gco:CharacterString
        dcp: DCPList[]; // --> ./srv:DCP/srv:DCPList
        invocation_name?: string; // --> ./srv:invocationName/gco:CharacterString
        connect_point: string[]; // --> ./srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL
        parameters?: { // --> ./srv:parameters/srv:SV_Parameter
            name: string; // --> ./srv:name/gco:CharacterString
            direction: ParameterDirection; // --> ./srv:direction/gco:SV_ParameterDirection
            description?: string; // --> ./srv:description/gco:CharacterString
            optionality: Boolean; // --> ./srv:optionality/gco:Boolean
            repeatability: Boolean; // --> ./srv:repeatability/gco:Boolean
        }[];
    }[];
    // #endregion
    // #endregion
    // ===================== DISTRIBUTION INFO =====================
    // #region
    resource_provider: CI_ResponsibleParty; // --> gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty (gmd:CI_RoleCode = resourceProvider)
    formats?: { // --> gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format
        name: {
            uri?: string; // --> ./gmd:name/gmx:Anchor
            label: FormatName; // --> ./gmd:name/gco:CharacterString OPPURE inteso come il valore di gmx:Anchor, se presente l'URI
        };
        version: string; // --> ./gmd:version/gco:CharacterString
        specification?: string; // --> ./gmd:specification/gco:CharacterString
    }[];
    online_resources: { // --> gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution_transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource
        protocol: { // --> ./gmd:protocol
            uri: ServiceProtocolCode; // --> ./gmx:Anchor/xlink:href
            label: string; // --> ./gmx:Anchor (anchor label)
        };
        application_profile: { // --> ./gmd:applicationProfile
            uri?: string; // --> ./gmx:Anchor/xlink:href
            label: SpatialDataServiceType; // --> ./gmx:Anchor (anchor label)
        };
        description: { // --> ./gmd:description
            uri?: string; // --> ./gmd:name/gmx:Anchor
            label: OnLineDescriptionCode; // --> ./gmd:name/gco:CharacterString OPPURE inteso come il valore di gmx:Anchor, se presente l'URI
        };
        url: string; // --> ./gmd:linkage/gmd:URL
        name: string; // --> ./gmd:name/gco:CharacterString
        function?: { // --> ./gmd:function/gmd:CI_OnlineFunctionCode
            uri?: string; // --> codeList URI
            value: CI_OnlineFunctionCode; // --> codeListValue
        };
        protocol_request?: string; // --> --> ./gmd:function/gco:CharacterString
        query_string_fragment?: string; // --> NEW! (TESTO LIBERO - Identifica eventuali parametri aggiuntivi da riportare nel query string; per tutte le richieste al servizio OGC - ad esempio il parametro map esposto dai prodotti di MapServer)
        layer_pk?: string; // --> NEW! (TESTO LIBERO - Identifica la chiave primaria del layer; necessaria per le operazioni di editing tramite WFS-T)
        temporal_wms?: boolean // --> NEW! (BOOLEAN - Identifica se il servizio ha disponibile il parametro TIME sul WMS)
        layer_style?: { // --> NEW! (Indica i parametri della legenda alla quale il layer è associato)
            name: string;
            workspace: string;
        };
        source_auth_type: string // --> DA IGNORARE PER L'HARVESTING DEL DAB! (Solo a scopo di SIM Data Catalog interno)
        source_auth_key?: string // --> DA IGNORARE PER L'HARVESTING DEL DAB! (Solo a scopo di SIM Data Catalog interno)
    }[];
    dataset_location?: string; // --> DA IGNORARE PER L'HARVESTING DEL DAB! (Solo a scopo di SIM Data Catalog interno)
    raster_mosaic?: boolean | null; // --> NEW! (BOOLEAN - Indica se il raster è di tipo mosaico oppure se è riferito ad una singola risorsa: questo campo è fondamentale per il funzionamento del servizio di download avanzato - dedicato specificatamente ai raster - sviluppato per il SIM.)
    // #endregion
    // ===================== QUALITY INFO =====================
    // #region
    conformity: { // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/report/DQ_DomainConsistency/result/DQ_ConformanceResult
        specification_title: string; // --> /gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString
        specification_publication_date: Date; // --> /gmd:specification/gmd:CI_Citation/gmd:date/CI_Date/gmd:date/gco:Date or gco:DateTime (dateType/CI_DateTypeCode = publication)
        explanation: string; // --> ./gmd:explanation/gco:CharacterString
        pass: boolean // --> ./gmd:pass/gco:Boolean
    }[];
    positional_accuracy?: { // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/report/DQ_AbsoluteExternalPositionalAccuracy/result/DQ_QuantitativeResult
        unit: string; // --> ./gmd:valueUnit/gml:BaseUnit/gml:identifier
        unit_system: string; // --> ./gmd:valueUnit/gml:BaseUnit/gml:unitsSystem
        value: number; // --> ./gmd:value/gmd:Record/gco:Real
    };
    quality_scope: { // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope
        scope_code: { // --> ./gmd:level/gmd:MD_ScopeCode
            uri?: string; // --> codeList URI
            value: MD_ScopeCode; // --> codeListValue
        };
        scope_details?: string; // --> ./gmd:levelDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString
    };
    // --------------------- Data Lineage  ---------------------
    // #region
    lineage_statement: string; // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString
    lineage_source?: LE_Source[]; // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmi:LE_Source
    lineage_process_step?: { // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LE_ProcessStep
        description: string; // --> ./gmd:description/gco:CharacterString
        rationale?: string; // --> ./gmd:rationale/gco:CharacterString
        date?: Date; // --> ./gmd:dateTime/gco:Date or gco:DateTime
        processor?: CI_ResponsibleParty; // --> ./gmd:processor/gmd:CI_ResponsibleParty (gmd:CI_RoleCode = processor)
        source?: LE_Source // --> ./gmd:source/gmi:LE_Source
        processing_information?: { // --> ./gmi:processingInformation/gmi:LE_Processing
            id: string; // --> ./gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString
            software_reference?: CI_Citation; // --> ./gmi:softwareReference/gmd:CI_Citation
            procedure_description?: string; // --> ./gmi:procedureDescription/gco:CharacterString
            documentation?: CI_Citation[]; // --> ./gmi:documentation/gmd:CI_Citation
            run_time_parameters?: string; // --> ./gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString
            parameter?: { // --> ./mrl:parameter/mrl:LE_ProcessParameter
                name: string; // --> ./mrl:name/gco:MemberName/gco:aName/gco:CharacterString
                direction: ParameterDirection; // --> ./mrl:direction/gco:SV_ParameterDirection
                description: string; // --> ./mrl:description/gco:CharacterString
                optionality: Boolean; // --> ./mrl:optionality/gco:Boolean
                repeatability: Boolean; // --> ./mrl:repeatability/gco:Boolean
                value_type?: XlinkHref; // --> ./mrl:valueType/gco:RecordType
                value?: string; // --> ./mrl:value/gco:Record
                source?: LE_Source; // --> ./mrl:resource/mrl:LE_Source
            }[];
            algorithm?: { // --> ./gmi:algorithm/gmi:LE_Algorithm
                citation: CI_Citation; // --> ./gmi:citation/gmd:CI_Citation
                description: string; // --> ./gmi:description/gco:CharacterString
            };
        };
        output?: LE_Source; // --> ./gmi:output/gmi:LE_Source
        report?: { // --> ./gmi:report
            name: string; // --> ./gmi:name/gco:CharacterString
            description?: string; // --> ./gmi:description/gco:CharacterString
            file_type?: FormatName; // --> ./gmi:fileType/gco:CharacterString
        }
    }[];
    // #endregion
    // #endregion
    // ===================== MODELS / ALGORITHMS METADATA =====================
    // #region
    model_maturity_level?: ModelMaturityLevel; // --> NEW! (NUOVA CODELIST - Indica il livello di maturità del modello o algoritmo)
    model_computational_requirements?: { // --> NEW! (Indica i requisiti minimi computazionali per poter eseguire il modello o algoritmo)
        cpu: string; // --> NEW! (TESTO LIBERO)
        gpu: string; // --> NEW! (TESTO LIBERO)
        ram: string; // --> NEW! (TESTO LIBERO)
        storage: string; // --> NEW! (TESTO LIBERO)
    };
    model_types?: ModelType[]; // --> NEW! (NUOVA CODELIST - Indica la tipologia del modello o algoritmo)
    supported_platforms?: SupportedPlatform[]; // --> NEW! (NUOVA CODELIST - Indica le piattaforme supportate per l'esecuzione del modello o algoritmo)
    model_category?: ModelCategory; // --> NEW! (NUOVA CODELIST - Indica la categoria di utilizzo / obiettivo del modello o algoritmo)
    model_methodology_description?: string; // --> NEW (TESTO LIBERO - Descrive ulteriori dettagli sulla metodologia o framework utilizzati per il modello o algoritmo)
    model_quality_information?: { // --> gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality (quando vengono valorizzati i seguenti campi; si sottointende che il campo quality_scope riportato sopra sia valorizzato con model)
        model_quality_report?: string; // --> ./gmd:report/gmd:DQ_DescriptiveResult/gmd:statement/gco:Characterstring (N.B.: Questo è un campo dell'ISO 19157 - valutare se possibile comprenderlo)
        model_metrics?: { // --> ./gmd:report/gmd:DQ_QuantitativeAttributeAccuracy
            id?: string; // --> ./gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString
            name: string; // --> ./gmd:nameOfMeasure/gco:CharacterString
            description?: string; // --> ./gmd:measureDescription/gco:CharacterString
            value?: number; // --> ./gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gmd:Record/gco:Real
            // N.B.: Nel nodo DQ_QuantitativeResult è obbligatorio il campo gmd:valueUnit. Tuttavia, in questo caso le metriche sono adimensionali - Valutare con ESSI-Tech se possibile utilizzare altri campi dell'ISO o crearne di nuovi)
        }[];
    }
    // #endregion
}