<div id="top"></div>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/ESSI-Lab/DAB">
    <img src="DAB.png" alt="DAB Logo" />
  </a>

<h3 align="center">Discovery and Access Broker (DAB) Community Edition (CE)</h3>

 <p align="center">
    <br />
    <a href="#getting-started"><strong>Getting started»</strong></a>
    <br />
    <br />
    <a href="#license-and-attribution">License and attribution</a>
    <a href="#contact-essi-lab">Contact ESSI-Lab</a>    
  </p>
</div>





Welcome to the GitHub repository of the DAB Community Edition (CE). This repository includes all consolidated and stable functionalities of the DAB. 

## About the DAB

The DAB is a **brokering software framework** for the **mediation and harmonization of geospatial data** with particular reference to Earth observation data, both satellite and non-satellite. DAB makes it possible to connect heterogeneous data sources and make them discoverable and accessible through homogeneous and standard interfaces by different data user tools and applications. The DAB is capable of implementing data discovery and access functionalities by implementing multiple standard interfaces (e.g. OGC, ISO,  ...), and Application Programming Interfaces (API).

<div align="center">
<img src="DAB-diagram.png" width="80%" />
</div>



<p align="right">(<a href="#top">back to top</a>)</p>


## Getting started
<details>
  <summary>Instructions on how to start using the DAB</summary>

<br/>	
	
The DAB is composed by multiple maven modules. GS-service is the main module, capable of starting the DAB Internet services and its web configuration tool.

The command <code>mvn clean install</code> run in the root folder will compile the source code into jar files and finally compose the war package of DAB GS-service.

Finally, it's possible to launch DAB services on a single node configuration by launching the following maven command from the gs-service folder:

<code>mvn jetty:run -Ppreconfigured -DdbUser=XXX -DdbPassword=XXX -DquartzUser=root -DquartzPassword=XXX -DadminUser=XXX -DgoogleClientId=XXX -DgoogleClientSecret=XXX</code>

The DAB will launch using a preconfigured configuration where:
- dbUser/dbPassword are the credentials of a Marklogic db installed on localhost (8002/8004 ports)
- quartzUser/quartzPassword are the credentials of a mysql db installed on localhost  (3306 port)
- adminUser/googleClientId/googleClientSecret are the Google OAuth credentials previously obtained through Google
- an ElasticSearch instance installed on localhost will receive statistics (9200 port)

The DAB can then be configured at the URL: 

http://localhost:9090/gs-service/conf
	
It's also possible to create a brand new configuration at the URL:

http://localhost:9090/gs-service/initialize

The web configurator tool enables to add/remove both profilers and accessors. In this configuration all the profilers are activated. For the accessors, it's needed to specify the data source URL endpoint, the accessor strategy (distributed vs harvested) and the connector type. It's also possible to start harvestings of specific sources.

A demo portal to check that DAB is working as expected will be available at the URL:
	
http://localhost:9090/gs-service/search

In a more complex configuration the DAB can be deployed on a cloud service, where multiple containers can be responsible for different tasks (e.g. frontend, harvesting, access). The DAB Dockerhub repository is currently under construction.

It's possible to create a docker image with the following commands from the gs-service project:

mvn -o -B -Pvaadin-production -Dmaven.test.skip=true clean package

mvn docker:build

This it will create the same DAB image that is also available on Docker Hub.

The docker container can readily be started for example with the following command:

docker run -p 8080:8080 -e "JAVA_OPTS=-Dconfiguration.url=file:///tmp" essilab/dab:latest
	
</details>
	
<p align="right">(<a href="#top">back to top</a>)</p>
	
## License and attribution

While the DAB community edition code is **open source**, it is not a **public domain software**. Therefore, to use the DAB source code you should **give full attribution to the original authors** and **redistribute your modifications** in the same way, accordingly to the **GNU Affero General Public License v3.0** (see LICENSE). To know more about how to correctly utilize the DAB open source code and provide appropriate attribution, please, select the relevant cases:

<details>
	<summary>To use the DAB technology for providing online services</summary>

<br/>		
	
AGPL license is more restrictive with respect to GPL with regard to online service providers making use of the licensed software.

This is the case where a third party downloads the DAB source code and offers its functionalities through an online service (e.g. through a server managed by the third party). In this case **the following statement should appear at the third party site** offering the service:

<code>The brokering service is offered by DAB, a software developed by National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab. More information is available at https://github.com/ESSI-Lab/DAB/</code>

</details>

<details>
	
<summary>To use the DAB technology for a scientific and/or technological publication</summary>

<br/>		
	
Please note at least **one of the following papers should be cited to give correct author attributions** while describing work making use of the DAB:

<code><a href="https://www.sciencedirect.com/science/article/pii/S1364815215000481">Stefano Nativi, Paolo Mazzetti, Mattia Santoro, Fabrizio Papeschi, Max Craglia, Osamu Ochiai, Big Data challenges in building the Global Earth Observation System of Systems, Environmental Modelling & Software, Volume 68, 2015,Pages 1-26, ISSN 1364-8152</a></code>

<code><a href="https://www.igi-global.com/chapter/the-brokering-approach-for-enabling-collaborative-scientific-research/119828">Boldrini, Enrico,et al. "The Brokering Approach for Enabling Collaborative Scientific Research." Collaborative Knowledge in Scientific Research Networks, edited by Paolo Diviacco, et al., IGI Global, 2015, pp. 283-304.</a></code>

<code><a href="https://www.sciencedirect.com/science/article/abs/pii/S1364815215000481">Stefano Nativi, Paolo Mazzetti, Mattia Santoro, Fabrizio Papeschi, Max Craglia, Osamu Ochiai, Big Data challenges in building the Global Earth Observation System of Systems, Environmental Modelling & Software, Volume 68, 2015, Pages 1-26.</a></code>
	
<code><a href="https://ieeexplore.ieee.org/abstract/document/6506981">S. Nativi, M. Craglia and J. Pearlman, "Earth Science Infrastructures Interoperability: The Brokering Approach," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 6, no. 3, pp. 1118-1129, June 2013, doi: 10.1109/JSTARS.2013.2243113.</a></code>	
	
<code><a href="https://ieeexplore.ieee.org/document/4782694">S. Nativi, L. Bigagli, P. Mazzetti, E. Boldrini and F. Papeschi, "GI-Cat: A Mediation Solution for Building a Clearinghouse Catalog Service," 2009 International Conference on Advanced Geographic Information Systems & Web Services, 2009, pp. 68-74, doi: 10.1109/GEOWS.2009.34.</a></code>
	
<code><a href="https://ieeexplore.ieee.org/abstract/document/5200393">S. Nativi and L. Bigagli, "Discovery, Mediation, and Access Services for Earth Observation Data," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 2, no. 4, pp. 233-240, Dec. 2009, doi: 10.1109/JSTARS.2009.2028584.</a></code>	

<code><a href="https://ieeexplore.ieee.org/document/4423731">S. Nativi, L. Bigagli, P. Mazzetti, U. Mattia and E. Boldrini, "Discovery, query and access services for imagery, gridded and coverage data a clearinghouse solution," 2007 IEEE International Geoscience and Remote Sensing Symposium, 2007, pp. 4021-4024, doi: 10.1109/IGARSS.2007.4423731.</a></code>
	
</details>

<details>
	<summary>To start a new branch of the DAB software</summary>

<br/>		
	
The modified source code should be licensed according to AGPL and **redistributed back to the community, preferably through the DAB GitHub repository**. You are welcome and encouraged to <a href="#contact-essi-lab">contact ESSI-Lab</a> to propose code contributions to be applied to the present repository (see contributions section).

In any case the following attribution headers should be preserved in modified versions of the source code:

<code>Discovery and Access Broker (DAB) Community Edition (CE)</code>

<code>Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab</code>


In case a modified version of the DAB is offered by a third party as an Internet service, additionally to the attribution described above also the code (or a link to the DAB GitHub repository holding the modifications) should be made available on the third party site.

</details>	
	
<p align="right">(<a href="#top">back to top</a>)</p>


## Contributions

<details>
	<summary>How to contribute to this project</summary>

<br/>		
	
Contributors to the ESSI-Lab DAB CE project are welcome: you can **report bugs and request enhancements** <a href="#contact-essi-lab">contacting ESSI-Lab</a> or through the GitHub issue tracker.

To **propose code contributions** please <a href="#contact-essi-lab">contact ESSI-Lab</a> to coordinate the development effort and avoid duplications.

The <a href="https://gist.github.com/ESSI-Lab/68833fd7d9896513ae95a575695a85a2">ESSI-Lab Contributor License Agreement</a> (based on HA-CLA-I-OSI) needs to be signed by contributors to enable ESSI-Lab to accept community contributions to the DAB repository even more smoothly. Now the signature of the agreement is fully integrated in the Github pull request.

If you're interested in contributing, please take a look as well at <a href="https://raw.githubusercontent.com/ESSI-Lab/DAB/main/CODE_OF_CONDUCT.md">ESSI-Lab's Contributor Code of Conduct</a>.	
	
</details>
	
<p align="right">(<a href="#top">back to top</a>)</p>

## DAB history and acknowledgments

<details>
	<summary>Learn more about the DAB history and the main contributors</summary>

<br/>	
	
DAB was first conceived and is still maintained and advanced by the [ESSI-Lab](https://essi-lab.eu) based at the [Florence division](https://iia.cnr.it/sede-firenze/) of the [Institute of Atmospheric Pollution Research (IIA)](https://iia.cnr.it/) of [National Research Council of Italy (CNR)](https://www.cnr.it/).

DAB implements the **data brokering approach** as introduced by Stefano Nativi et al. in the position paper: <code><a href="https://ieeexplore.ieee.org/abstract/document/6506981">S. Nativi, M. Craglia and J. Pearlman, "Earth Science Infrastructures Interoperability: The Brokering Approach," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 6, no. 3, pp. 1118-1129, June 2013, doi: 10.1109/JSTARS.2013.2243113.</a></code> by applying the mediation pattern <code><a href="https://ieeexplore.ieee.org/abstract/document/5200393">S. Nativi and L. Bigagli, "Discovery, Mediation, and Access Services for Earth Observation Data," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 2, no. 4, pp. 233-240, Dec. 2009, doi: 10.1109/JSTARS.2009.2028584.</a></code>

### Projects	
	
The DAB software has been developed in the context of numerous National, European, and international projects and initiatives, which have been funded and/or operated by different organizations, over the last ten years, including:

<ul>
 <li>Intergovernmental initiatives
 <ul>
  <li>GEO
  <ul>
   <li><b>GEOSS Global Earth System of Systems</b></li>
  </ul>
  </li>
  <li>WMO
  <ul>
   <li><b>"WHOS-Plata"</b> (2019/2020)</li>
   <li><b>"WHOS-Dominican Republic"</b> (2020/2022)</li>
   <li><b>"WHOS"</b> (2021/2024)</li>
  </ul>
  </li>
  </ul>
 </li>
 <li>European Union funded projects
 <ul>
  <li>Horizon Europe
  <ul>
   <li><b>FAIR-EASE</b> (2022/2025)</li>
   <li><b>Blue-Cloud 2026</b> (2023/2026)</li>
   <li><b>TRIGGER</b> (2022/2027)</li>
  </ul>
  </li>	 
	<li>Horizon 2020
      <ul>
      <li><b>ODIP 2</b> (2015/2018)</li>
      <li><b>ERA-PLANET</b> (2016/2022)</li>
      <li><b>SeaDataCloud</b> (2016/2021)</li>
      <li><b>Blue-Cloud</b> (2019/2023)</li>
      <li><b>I-CHANGE</b> (2021/2025)</li>
      </ul>
	</li>
	<li>EASME
	<ul>
      <li><b>EMODNet Ingestion</b> (2016/2019)</li>
      <li><b>EMODNet Ingestion II</b> (2019/2021)</li>
      <li><b>EMODNet Ingestion III</b> (2022/2024)</li>
      </ul>
    </li>
  <li>FP7
  <ul>
   <li><b>EuroGEOSS</b> (2009/2012)</li>
   <li><b>SeaDataNet II</b> (2011/2015)</li>
   <li><b>GEOWOW</b> (2011/2014)</li>
   <li><b>ODIP</b> (2011/2015)</li>
  </ul>
  </li>
	<li>European Space Agency (ESA)
      <ul>
      <li><b>HMA-T</b> (2008/2009)</li>
      <li><b>Prod-Trees</b> (2012/2015)</li>
      <li><b>DAB4EDGE</b> (2018/2020)</li>
      <li><b>DAB4GPP</b> (2022/2025)</li>
      </ul>
      </li>
      </ul>
</li>
	<li>National agencies
	<ul>
	<li>National Science Foundation (USA)
     <ul><li><b>BCube</b> (2013/2016)</li></ul>
     </li>
    <li>MIUR (Italy)
    <ul><li><b>ND-SoS-Ina</b> (2012/2015)</li></ul>
    </li>
	<li>ARPA-ER (Italy)
		<ul>
	<li><b>P107009S</b> (2016)</li>
	<li><b>PGSIM/2017/771</b> (2017)</li>
	<li><b>PGSIM/2018/0000563</b> (2018)</li>
	<li><b>LoA Arpae SIMC/CNR-IIA</b> (2019)</li>
	</ul>
	</li>
	<li>ISPRA (Italy)
	<ul><li><b>HIS-Central</b> (2021-2025)</li></ul>
</li>
</ul>
</li>
</ul>
	
### Main running deployments
		
As from 2012, the DAB technology has been utilized by [GEO](https://earthobservations.org/) (the Group of Earth Observation) as the enabling component of the **GEO-DAB**: the brokerage data services middleware of the [GEOSS](https://earthobservations.org/geoss.php) (Global Earth Observation System of Systems) platform (formerly known as GCI). GEO-DAB is deployed and operated on a scalable cloud infrastructure. In 2021, the GEO-DAB has connected over 190 different geospatial information systems, which allow the search and discovery of more than 10 million datasets, consisting of over 1 billion single downloadable data files (i.e. data granules in GEO jargon). A useful reference is: <code><a href="https://www.sciencedirect.com/science/article/abs/pii/S1364815215000481">Stefano Nativi, Paolo Mazzetti, Mattia Santoro, Fabrizio Papeschi, Max Craglia, Osamu Ochiai, Big Data challenges in building the Global Earth Observation System of Systems, Environmental Modelling & Software, Volume 68, 2015, Pages 1-26.</a></code>. More info on GEO-DAB is available [here](https://www.geodab.net/)

As of 2017, the DAB software has been also adopted by [WMO](https://public.wmo.int/en) as one of the enabling technologies of the **WMO Hydrological Observing System (WHOS)**: a system of systems capable to share hydrological data, at a global scale. More info on WHOS [here](https://public.wmo.int/en/our-mandate/water/whos). A WHOS webinar was recently hold to introduce users to the WHOS brokering services; the webinar recording is available [here](https://community.wmo.int/news/watch-recording-whos-webinar).

In 2011, the [SeaDataNet](https://www.seadatanet.org/) data platform started utilizing the DAB technology to enable the discovery and access of different international ocean data sources.  More information on SeaDataNet brokering services is availabe [here](https://www.seadatanet.org/Data-Access/Discover-international-data). The DAB mediation and brokering services were also applied by the [ODIP](http://www.odip.org/) community to interconnect the ocean information system-of-systems from USA, Australia, and Europe.
	
### Awards

The DAB technology won the **2014 Geospatial World Innovation Awards** [(media coverage here)](https://www.cnr.it/en/news/5795/il-geospatial-world-innovation-award-all-iia-cnr).
	
### Useful References

The following papers provide a technical description of the DAB brokering framweork.

<code><a href="https://www.doi.org/10.1080/17538947.2022.2099591">Enrico Boldrini, Stefano Nativi, Silvano Pecora, Igor Chernov & Paolo Mazzetti (2022) Multi-scale hydrological system-of-systems realized through WHOS: the brokering framework, International Journal of Digital Earth, 15:1, 1259-1289, DOI: 10.1080/17538947.2022.2099591<a/></code>

<code><a href="https://www.sciencedirect.com/science/article/pii/S1364815215000481">Stefano Nativi, Paolo Mazzetti, Mattia Santoro, Fabrizio Papeschi, Max Craglia, Osamu Ochiai, Big Data challenges in building the Global Earth Observation System of Systems, Environmental Modelling & Software, Volume 68, 2015,Pages 1-26, ISSN 1364-8152</a></code>

<code><a href="https://www.igi-global.com/chapter/the-brokering-approach-for-enabling-collaborative-scientific-research/119828">Boldrini, Enrico,et al. "The Brokering Approach for Enabling Collaborative Scientific Research." Collaborative Knowledge in Scientific Research Networks, edited by Paolo Diviacco, et al., IGI Global, 2015, pp. 283-304.</a></code>

<code><a href="https://www.sciencedirect.com/science/article/abs/pii/S1364815215000481">Stefano Nativi, Paolo Mazzetti, Mattia Santoro, Fabrizio Papeschi, Max Craglia, Osamu Ochiai, Big Data challenges in building the Global Earth Observation System of Systems, Environmental Modelling & Software, Volume 68, 2015, Pages 1-26.</a></code>
	
<code><a href="https://ieeexplore.ieee.org/abstract/document/6506981">S. Nativi, M. Craglia and J. Pearlman, "Earth Science Infrastructures Interoperability: The Brokering Approach," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 6, no. 3, pp. 1118-1129, June 2013, doi: 10.1109/JSTARS.2013.2243113.</a></code>	
	
<code><a href="https://ieeexplore.ieee.org/document/4782694">S. Nativi, L. Bigagli, P. Mazzetti, E. Boldrini and F. Papeschi, "GI-Cat: A Mediation Solution for Building a Clearinghouse Catalog Service," 2009 International Conference on Advanced Geographic Information Systems & Web Services, 2009, pp. 68-74, doi: 10.1109/GEOWS.2009.34.</a></code>
	
<code><a href="https://ieeexplore.ieee.org/abstract/document/5200393">S. Nativi and L. Bigagli, "Discovery, Mediation, and Access Services for Earth Observation Data," in IEEE Journal of Selected Topics in Applied Earth Observations and Remote Sensing, vol. 2, no. 4, pp. 233-240, Dec. 2009, doi: 10.1109/JSTARS.2009.2028584.</a></code>	

<code><a href="https://ieeexplore.ieee.org/document/4423731">S. Nativi, L. Bigagli, P. Mazzetti, U. Mattia and E. Boldrini, "Discovery, query and access services for imagery, gridded and coverage data a clearinghouse solution," 2007 IEEE International Geoscience and Remote Sensing Symposium, 2007, pp. 4021-4024, doi: 10.1109/IGARSS.2007.4423731.</a></code>
	
	

### Contributors
	
Many data scientists, computer scientists, information and software engineers have contributed to the DAB design and development throughout an implementation effort of over ten years, spanning different maturity stages (from prototypical to operational)  and sub-components (e.g. GI-cat, GI-axe, GI-sem, GI-suite, ...).

[DAB development team (contact us)](https://www.uos-firenze.essi-lab.eu/personnel):

- Stefano Nativi (DAB father)
- Paolo Mazzetti (responsible)
- Enrico Boldrini (software design, developer, coordinator of DAB activities with WMO)
- Fabrizio Papeschi (developer)
- Roberto Roncella (developer)
- Massimiliano Olivieri (developer)
- Lorenzo Bigagli (software design)
- Mattia Santoro (software design, developer, coordinator of DAB activities with GEO)
	
Experts who contributed with valuable inputs and feedbacks representing significant data provider and user communities (in alphabetic order):
	
- Alessandro Annoni
- Igor Chernov
- Guido Colangeli
- Max Craglia
- Paola De Salvo
- Ben Domenico
- Gregory Giuliani
- Wim Hugo
- Siri Jodha Khalsa
- Osamu Ochiai
- Washington Otieno
- Anthony Lehmann
- Joan Masó
- Francoise Pearlman
- Jay Pearlman
- Silvano Pecora
- Nicola Pirrone
- Antonello Provenzale
- Barbara Ryan
- Dick Schaap
- Richard Signell
- Joost Van Bemmelen


Previous members of the development team:
	
- Francesco Pezzati (past developer)
- Alessio Baldini (past developer)
- Fabrizio Vitale (past developer)
- Ugo Mattia (past developer)
- Valerio Angelini (past developer)
	
### Credits
	
ESSI-Lab would like to credit all the maintainers and developers of the third party services and technologies that DAB uses. They include:
	
- MarkLogic Server
- MySQL
- ElasticSearch
- Amazon AWS
- Google Cloud Platform
- Google OAuth
- Facebook OAuth
- Twitter OAuth	
- Docker
- Kubernetes
	
ESSI-Lab would like to credit, as well, all the maintainers and developers of the third party libraries that DAB uses. They are here listed, along with their licenses:
	
(The Apache Software License, Version 2.0) ant (ant:ant:1.6.5 - http://www.apache.org/ant/)

(Public Domain) AOP alliance (aopalliance:aopalliance:1.0 - http://aopalliance.sourceforge.net)

(GNU LESSER GENERAL PUBLIC LICENSE) c3p0:JDBC DataSources/Resource Pools (c3p0:c3p0:0.9.1.1 - http://c3p0.sourceforge.net)

(Unknown license) classworlds (classworlds:classworlds:1.1-alpha-2 - http://classworlds.codehaus.org/)

(The Apache Software License, Version 2.0) localstack-utils (cloud.localstack:localstack-utils:0.1.14 - http://localstack.cloud)

(The Apache Software License, Version 2.0) AWS SDK for Java - Core (com.amazonaws:aws-java-sdk-core:1.11.560 - https://aws.amazon.com/sdkforjava)

(The Apache Software License, Version 2.0) AWS Java SDK for Amazon Elasticsearch Service (com.amazonaws:aws-java-sdk-elasticsearch:1.11.560 - https://aws.amazon.com/sdkforjava)

(The Apache Software License, Version 2.0) AWS Java SDK for AWS KMS (com.amazonaws:aws-java-sdk-kms:1.11.560 - https://aws.amazon.com/sdkforjava)

(The Apache Software License, Version 2.0) AWS Java SDK for Amazon S3 (com.amazonaws:aws-java-sdk-s3:1.11.560 - https://aws.amazon.com/sdkforjava)

(The Apache Software License, Version 2.0) AWS Lambda Java Core Library (com.amazonaws:aws-lambda-java-core:1.2.0 - https://aws.amazon.com/lambda/)

(The Apache Software License, Version 2.0) AWS Lambda Java Events Library (com.amazonaws:aws-lambda-java-events:2.1.0 - https://aws.amazon.com/lambda/)

(The Apache Software License, Version 2.0) JMES Path Query library (com.amazonaws:jmespath-java:1.11.560 - https://aws.amazon.com/sdkforjava)

(The MIT License (MIT)) java jwt (com.auth0:java-jwt:3.3.0 - http://www.jwt.io)

(The Apache Software License, Version 2.0) JCommander (com.beust:jcommander:1.35 - http://beust.com/jcommander)

(The Apache Software License, Version 2.0) HPPC Collections (com.carrotsearch:hppc:0.8.1 - http://labs.carrotsearch.com/hppc.html/hppc)

(The Apache Software License, Version 2.0) ClassMate (com.fasterxml:classmate:1.3.1 - http://github.com/cowtowncoder/java-classmate)

(The Apache Software License, Version 2.0) Jackson-annotations (com.fasterxml.jackson.core:jackson-annotations:2.6.6 - http://github.com/FasterXML/jackson)

(The Apache Software License, Version 2.0) Jackson-core (com.fasterxml.jackson.core:jackson-core:2.6.6 - https://github.com/FasterXML/jackson-core)

(The Apache Software License, Version 2.0) jackson-databind (com.fasterxml.jackson.core:jackson-databind:2.6.6 - http://github.com/FasterXML/jackson)

(The Apache Software License, Version 2.0) Jackson-dataformat-CBOR (com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.6.7 - http://wiki.fasterxml.com/JacksonForCbor)

(The Apache Software License, Version 2.0) Jackson dataformat: Smile (com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.10.4 - http://github.com/FasterXML/jackson-dataformats-binary)

(The Apache Software License, Version 2.0) Jackson-dataformat-YAML (com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.6.6 - https://github.com/FasterXML/jackson)

(The Apache Software License, Version 2.0) Jackson-JAXRS-base (com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.8.6 - http://github.com/FasterXML/jackson-jaxrs-providers/jackson-jaxrs-base)

(The Apache Software License, Version 2.0) Jackson-JAXRS-JSON (com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.8.6 - http://github.com/FasterXML/jackson-jaxrs-providers/jackson-jaxrs-json-provider)

(The Apache Software License, Version 2.0) Jackson module: JAXB-annotations (com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.8.6 - http://github.com/FasterXML/jackson-module-jaxb-annotations)

(MIT License) dexx (com.github.andrewoma.dexx:collection:0.7 - https://github.com/andrewoma/dexx)

(Revised BSD License) JSONLD Java :: Core (com.github.jsonld-java:jsonld-java:0.12.1 - http://github.com/jsonld-java/jsonld-java/jsonld-java/)

(The Apache Software License, Version 2.0) compiler (com.github.spullara.mustache.java:compiler:0.9.6 - http://github.com/spullara/mustache.java)

(Common Public License Version 1.0) System Rules (com.github.stefanbirkner:system-rules:1.16.1 - http://stefanbirkner.github.io/system-rules/)

(The Apache Software License, Version 2.0) JCIP Annotations under Apache License (com.github.stephenc.jcip:jcip-annotations:1.0-1 - http://stephenc.github.com/jcip-annotations)

(The Apache Software License, Version 2.0) FindBugs-jsr305 (com.google.code.findbugs:jsr305:1.3.9 - http://findbugs.sourceforge.net/)

(The Apache Software License, Version 2.0) FindBugs-jsr305 (com.google.code.findbugs:jsr305:3.0.2 - http://findbugs.sourceforge.net/)

(The Apache Software License, Version 2.0) error-prone annotations (com.google.errorprone:error_prone_annotations:2.3.4 - http://nexus.sonatype.org/oss-repository-hosting.html/error_prone_parent/error_prone_annotations)

(The Apache Software License, Version 2.0) Guava InternalFutureFailureAccess and InternalFutures (com.google.guava:failureaccess:1.0.1 - https://github.com/google/guava/failureaccess)

(The Apache Software License, Version 2.0) Guava: Google Core Libraries for Java (com.google.guava:guava:30.1-jre - https://github.com/google/guava/guava)

(The Apache Software License, Version 2.0) Guava ListenableFuture only (com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava - https://github.com/google/guava/listenablefuture)

(The Apache Software License, Version 2.0) J2ObjC Annotations (com.google.j2objc:j2objc-annotations:1.3 - https://github.com/google/j2objc/)

(3-Clause BSD License) Protocol Buffers [Core] (com.google.protobuf:protobuf-java:3.5.0 - https://developers.google.com/protocol-buffers/protobuf-java/)

(The Apache Software License, Version 2.0) JSON.simple (com.googlecode.json-simple:json-simple:1.1 - http://code.google.com/p/json-simple/)

(The H2 License, Version 1.0) H2 Database Engine (com.h2database:h2:1.1.119 - http://www.h2database.com)

(The Apache Software License, Version 2.0) status-core (com.indeed:status-core:1.0.27 - https://github.com/indeedeng)

(The Apache Software License, Version 2.0) indeed-util-core (com.indeed:util-core:1.0.19 - http://nexus.sonatype.org/oss-repository-hosting.html/common-parent/oss-parent/util-parent/util-core)

(The Apache Software License, Version 2.0) VarExport (com.indeed:util-varexport:1.0.19 - http://nexus.sonatype.org/oss-repository-hosting.html/common-parent/oss-parent/util-parent/util-varexport)

(The Apache Software License, Version 2.0) MarkLogic Java Client API (com.marklogic:marklogic-client-api:4.0.2 - https://github.com/marklogic/java-client-api)

(Unknown license) MarkLogic XCC (com.marklogic:marklogic-xcc:9.0.1 - http://developer.marklogic.com/products/xcc)

(Eclipse Public License, Version 1.0) (GNU Lesser General Public License, Version 2.1) c3p0 (com.mchange:c3p0:0.9.5.2 - https://github.com/swaldman/c3p0)

(Eclipse Public License, Version 1.0) (GNU Lesser General Public License, Version 2.1) mchange-commons-java (com.mchange:mchange-commons-java:0.2.11 - https://github.com/swaldman/mchange-commons-java)

(The Apache Software License, Version 2.0) jaxb-java-time-adapters (com.migesok:jaxb-java-time-adapters:1.1.3 - https://github.com/migesok/jaxb-java-time-adapters)

(The Apache Software License, Version 2.0) rome (com.rometools:rome:1.13.0 - http://rometools.com/rome)

(The Apache Software License, Version 2.0) rome-utils (com.rometools:rome-utils:1.13.0 - http://rometools.com/rome-utils)

(The Apache Software License, Version 2.0) OkHttp Logging Interceptor (com.squareup.okhttp3:logging-interceptor:3.8.1 - https://github.com/square/okhttp/logging-interceptor)

(The Apache Software License, Version 2.0) OkHttp (com.squareup.okhttp3:okhttp:3.8.1 - https://github.com/square/okhttp/okhttp)

(The Apache Software License, Version 2.0) Okio (com.squareup.okio:okio:1.13.0 - https://github.com/square/okio/okio)

(CDDL/GPLv2+CE) JavaMail API (com.sun.mail:javax.mail:1.5.6 - http://javamail.java.net/javax.mail)

(CDDL/GPLv2+CE) JavaMail API (com.sun.mail:javax.mail:1.6.0 - http://javaee.github.io/javamail/javax.mail)

(The Apache Software License, Version 2.0) Maven JAXB Plugin (com.sun.tools.xjc.maven2:maven-jaxb-plugin:1.1.1 - https://jaxb.dev.java.net/)

(CDDL+GPL License) Old JAXB Core (com.sun.xml.bind:jaxb-core:2.2.11 - http://jaxb.java.net/jaxb-bundles/jaxb-core)

(CDDL+GPL License) Old JAXB Runtime (com.sun.xml.bind:jaxb-impl:2.2.11 - http://jaxb.java.net/jaxb-bundles/jaxb-impl)

(CDDL+GPL License) Old JAXB XJC (com.sun.xml.bind:jaxb-xjc:2.2.11 - http://jaxb.java.net/jaxb-bundles/jaxb-xjc)

(The Apache Software License, Version 2.0) T-Digest (com.tdunning:t-digest:3.2 - https://github.com/tdunning/t-digest)

(GNU Lesser General Public License, Version 2.1) com.vividsolutions:jts-core (com.vividsolutions:jts-core:1.14.0 - http://www.vividsolutions.com/jts)

(GNU Lesser General Public License, Version 2.1) com.vividsolutions:jts-example (com.vividsolutions:jts-example:1.14.0 - http://www.vividsolutions.com/jts)

(GNU Lesser General Public License, Version 2.1) com.vividsolutions:jts-io (com.vividsolutions:jts-io:1.14.0 - http://www.vividsolutions.com/jts)

(The Apache Software License, Version 2.0) HikariCP-java6 (com.zaxxer:HikariCP-java6:2.3.13 - https://github.com/brettwooldridge/HikariCP)

(The Apache Software License, Version 2.0) Apache Commons BeanUtils (commons-beanutils:commons-beanutils:1.9.2 - http://commons.apache.org/proper/commons-beanutils/)

(The Apache Software License, Version 2.0) Apache Commons CLI (commons-cli:commons-cli:1.4 - http://commons.apache.org/proper/commons-cli/)

(The Apache Software License, Version 2.0) Apache Commons Codec (commons-codec:commons-codec:1.9 - http://commons.apache.org/proper/commons-codec/)

(The Apache Software License, Version 2.0) Apache Commons Collections (commons-collections:commons-collections:3.2.2 - http://commons.apache.org/collections/)

(The Apache Software License, Version 2.0) Commons DBCP (commons-dbcp:commons-dbcp:1.4 - http://commons.apache.org/dbcp/)

(The Apache Software License, Version 2.0) Commons Digester (commons-digester:commons-digester:1.8.1 - http://commons.apache.org/digester/)

(The Apache Software License, Version 2.0) Apache Commons FileUpload (commons-fileupload:commons-fileupload:1.4 - http://commons.apache.org/proper/commons-fileupload/)

(Apache License) HttpClient (commons-httpclient:commons-httpclient:3.1 - http://jakarta.apache.org/httpcomponents/httpclient-3.x/)

(The Apache Software License, Version 2.0) Apache Commons IO (commons-io:commons-io:2.6 - http://commons.apache.org/proper/commons-io/)

(The Apache Software License, Version 2.0) Commons Lang (commons-lang:commons-lang:2.6 - http://commons.apache.org/lang/)

(The Apache Software License, Version 2.0) Commons Logging (commons-logging:commons-logging:1.1.3 - http://commons.apache.org/proper/commons-logging/)

(The Apache Software License, Version 2.0) Apache Commons Net (commons-net:commons-net:3.6 - http://commons.apache.org/proper/commons-net/)

(The Apache Software License, Version 2.0) Commons Pool (commons-pool:commons-pool:1.5.4 - http://commons.apache.org/pool/)

(The Apache Software License, Version 2.0) Apache Commons Validator (commons-validator:commons-validator:1.6 - http://commons.apache.org/proper/commons-validator/)

(BSD 3-Clause License) bufr (edu.ucar:bufr:4.6.6 - no url defined)

(BSD 3-Clause License) cdm (edu.ucar:cdm:4.6.12 - no url defined)

(Unknown license) httpservices (edu.ucar:httpservices:4.6.12 - no url defined)

(BSD 3-Clause License) netcdf4 (edu.ucar:netcdf4:4.6.12 - no url defined)

(BSD 3-Clause License) udunits (edu.ucar:udunits:4.6.12 - no url defined)

(The Apache Software License, Version 2.0) okhttp-digest (io.github.rburgst:okhttp-digest:1.21 - https://github.com/rburgst/okhttp-digest)

(Lesser General Public License (LGPL)) Image I/O-Extensions - GeoCore (it.geosolutions.imageio-ext:imageio-ext-geocore:1.1.24 - no url defined)

(Lesser General Public License (LGPL)) Image I/O-Extensions - Multithreading ImageRead (it.geosolutions.imageio-ext:imageio-ext-imagereadmt:1.1.24 - no url defined)

(Lesser General Public License (LGPL)) Image I/O-Extensions - Custom Streams (it.geosolutions.imageio-ext:imageio-ext-streams:1.1.24 - no url defined)

(Lesser General Public License (LGPL)) Improved TIFF Plugin (it.geosolutions.imageio-ext:imageio-ext-tiff:1.1.24 - no url defined)

(Lesser General Public License (LGPL)) Image I/O-Extensions - utilities classes and methods (it.geosolutions.imageio-ext:imageio-ext-utilities:1.1.24 - no url defined)

(Lesser General Public License (LGPL)) jt-affine (it.geosolutions.jaiext.affine:jt-affine:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-algebra (it.geosolutions.jaiext.algebra:jt-algebra:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-bandcombine (it.geosolutions.jaiext.bandcombine:jt-bandcombine:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-bandmerge (it.geosolutions.jaiext.bandmerge:jt-bandmerge:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-bandselect (it.geosolutions.jaiext.bandselect:jt-bandselect:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-binarize (it.geosolutions.jaiext.binarize:jt-binarize:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-border (it.geosolutions.jaiext.border:jt-border:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-buffer (it.geosolutions.jaiext.buffer:jt-buffer:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-classifier (it.geosolutions.jaiext.classifier:jt-classifier:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-colorconvert (it.geosolutions.jaiext.colorconvert:jt-colorconvert:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-colorindexer (it.geosolutions.jaiext.colorindexer:jt-colorindexer:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-crop (it.geosolutions.jaiext.crop:jt-crop:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-errordiffusion (it.geosolutions.jaiext.errordiffusion:jt-errordiffusion:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-format (it.geosolutions.jaiext.format:jt-format:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-imagefunction (it.geosolutions.jaiext.imagefunction:jt-imagefunction:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-iterators (it.geosolutions.jaiext.iterators:jt-iterators:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-lookup (it.geosolutions.jaiext.lookup:jt-lookup:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-mosaic (it.geosolutions.jaiext.mosaic:jt-mosaic:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-nullop (it.geosolutions.jaiext.nullop:jt-nullop:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-orderdither (it.geosolutions.jaiext.orderdither:jt-orderdither:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-piecewise (it.geosolutions.jaiext.piecewise:jt-piecewise:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-rescale (it.geosolutions.jaiext.rescale:jt-rescale:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-rlookup (it.geosolutions.jaiext.rlookup:jt-rlookup:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-scale (it.geosolutions.jaiext.scale:jt-scale:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-scale2 (it.geosolutions.jaiext.scale2:jt-scale2:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-stats (it.geosolutions.jaiext.stats:jt-stats:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-translate (it.geosolutions.jaiext.translate:jt-translate:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-utilities (it.geosolutions.jaiext.utilities:jt-utilities:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-vectorbin (it.geosolutions.jaiext.vectorbin:jt-vectorbin:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-warp (it.geosolutions.jaiext.warp:jt-warp:1.0.24 - http://maven.apache.org)

(Lesser General Public License (LGPL)) jt-zonal (it.geosolutions.jaiext.zonal:jt-zonal:1.0.24 - http://maven.apache.org)

(Common Development and Distribution License (CDDL) v1.0) JavaBeans Activation Framework (JAF) (javax.activation:activation:1.1 - http://java.sun.com/products/javabeans/jaf/index.jsp)

(CDDL + GPLv2 with classpath exception) javax.annotation API (javax.annotation:javax.annotation-api:1.2 - http://jcp.org/en/jsr/detail?id=250)

(Unknown license) Expression Language API (2.1 Maintenance Release) (javax.el:el-api:2.2 - no url defined)

(CDDL + GPLv2 with classpath exception) Expression Language API 2.2 (javax.el:javax.el-api:2.2.5 - http://uel.java.net)

(CDDL/GPLv2+CE) JavaMail API jar (javax.mail:javax.mail-api:1.6.0 - http://javaee.github.io/javamail/javax.mail-api)

(Unknown license) jai_codec (javax.media:jai_codec:1.1.3 - no url defined)

(Unknown license) jai_core (javax.media:jai_core:1.1.3 - no url defined)

(Unknown license) jai_imageio (javax.media:jai_imageio:1.1 - no url defined)

(CDDL + GPLv2 with classpath exception) Java Servlet API (javax.servlet:javax.servlet-api:3.1.0 - http://servlet-spec.java.net)

(The Apache Software License, Version 2.0) Bean Validation API (javax.validation:validation-api:1.1.0.Final - http://beanvalidation.org)

(CDDL 1.1) (GPL2 w/ CPE) javax.ws.rs-api (javax.ws.rs:javax.ws.rs-api:2.0.1 - http://jax-rs-spec.java.net)

(Unknown license) jaxb-api (javax.xml.bind:jaxb-api:2.1 - no url defined)

(Unknown license) jsr173_api (javax.xml.bind:jsr173_api:1.0 - no url defined)

(COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0) (GNU General Public Library) Streaming API for XML (javax.xml.stream:stax-api:1.0-2 - no url defined)

(Unknown license) jgridshift (jgridshift:jgridshift:1.0 - no url defined)

(The Apache Software License, Version 2.0) Joda-Time (joda-time:joda-time:2.9.7 - http://www.joda.org/joda-time/)

(Eclipse Public License 1.0) JUnit (junit:junit:4.12 - http://junit.org)

(Unknown license) log4j (log4j:log4j:1.2.17 - no url defined)

(The GNU General Public License, Version 2) MySQL Connector/J (mysql:mysql-connector-java:6.0.6 - http://dev.mysql.com/doc/connector-j/en/)

(The Apache Software License, Version 2.0) Byte Buddy (without dependencies) (net.bytebuddy:byte-buddy:1.6.5 - http://bytebuddy.net/byte-buddy)

(The Apache Software License, Version 2.0) Byte Buddy Java agent (net.bytebuddy:byte-buddy-agent:1.6.5 - http://bytebuddy.net/byte-buddy-agent)

(ASL, version 2) (LGPL, version 2.1) Java Native Access (net.java.dev.jna:jna:4.1.0 - https://github.com/twall/jna)

(ASL, version 2) (LGPL, version 2.1) Java Native Access (net.java.dev.jna:jna:4.2.2 - https://github.com/java-native-access/jna)

(BSD License) jsr-275 (net.java.dev.jsr-275:jsr-275:1.0-beta-2 - http://maven.apache.org)

(Unknown license) "Java Concurrency in Practice" book annotations (net.jcip:jcip-annotations:1.0 - http://jcip.net/)

(The Apache Software License, Version 2.0) ehcache (net.sf.ehcache:ehcache:2.10.3 - http://ehcache.org)

(The MIT License(MIT)) Java implementation of GeographicLib (net.sf.geographiclib:GeographicLib-Java:1.44 - http://geographiclib.sf.net)

(The MIT License) JOpt Simple (net.sf.jopt-simple:jopt-simple:5.0.2 - http://pholser.github.io/jopt-simple)

(Mozilla Public License Version 2.0) Saxon-HE (net.sf.saxon:Saxon-HE:9.7.0-15 - http://www.saxonica.com/)

(Unknown license) hatbox (net.sourceforge.hatbox:hatbox:1.0.b7 - no url defined)

(Unknown license) opendap (opendap:opendap:2.1 - no url defined)

(The BSD License) ANTLR 4 Runtime (org.antlr:antlr4-runtime:4.7.1 - http://www.antlr.org/antlr4-runtime)

(The Apache Software License, Version 2.0) Apache Commons Compress (org.apache.commons:commons-compress:1.17 - https://commons.apache.org/proper/commons-compress/)

(The Apache Software License, Version 2.0) Apache Commons CSV (org.apache.commons:commons-csv:1.5 - http://commons.apache.org/proper/commons-csv/)

(The Apache Software License, Version 2.0) Apache Commons Lang (org.apache.commons:commons-lang3:3.9 - http://commons.apache.org/proper/commons-lang/)

(The Apache Software License, Version 2.0) Apache CXF Core (org.apache.cxf:cxf-core:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Runtime SOAP Binding (org.apache.cxf:cxf-rt-bindings-soap:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Runtime JAXB DataBinding (org.apache.cxf:cxf-rt-databinding-jaxb:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Runtime JAX-RS Frontend (org.apache.cxf:cxf-rt-frontend-jaxrs:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF JAX-RS Service Description (org.apache.cxf:cxf-rt-rs-service-description:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Runtime HTTP Transport (org.apache.cxf:cxf-rt-transports-http:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Runtime Core for WSDL (org.apache.cxf:cxf-rt-wsdl:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Command Line Tools Common (org.apache.cxf:cxf-tools-common:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache CXF Command Line Tools WADLTo JAXRS Frontend (org.apache.cxf:cxf-tools-wadlto-jaxrs:3.1.9 - http://cxf.apache.org)

(The Apache Software License, Version 2.0) Apache HttpAsyncClient (org.apache.httpcomponents:httpasyncclient:4.1.2 - http://hc.apache.org/httpcomponents-asyncclient)

(The Apache Software License, Version 2.0) Apache HttpClient (org.apache.httpcomponents:httpclient:4.5.2 - http://hc.apache.org/httpcomponents-client)

(The Apache Software License, Version 2.0) Apache HttpClient Cache (org.apache.httpcomponents:httpclient-cache:4.5.5 - http://hc.apache.org/httpcomponents-client)

(The Apache Software License, Version 2.0) Apache HttpCore (org.apache.httpcomponents:httpcore:4.4.12 - http://hc.apache.org/httpcomponents-core-ga)

(The Apache Software License, Version 2.0) Apache HttpCore NIO (org.apache.httpcomponents:httpcore-nio:4.4.4 - http://hc.apache.org/httpcomponents-core-ga)

(The Apache Software License, Version 2.0) Apache HttpClient Mime (org.apache.httpcomponents:httpmime:4.5.1 - http://hc.apache.org/httpcomponents-client)

(The Apache Software License, Version 2.0) Apache Jena - ARQ (SPARQL 1.1 Query Engine) (org.apache.jena:jena-arq:3.10.0 - http://jena.apache.org/jena-arq/)

(The Apache Software License, Version 2.0) Apache Jena - Base Common Environment (org.apache.jena:jena-base:3.10.0 - http://jena.apache.org/jena-base/)

(The Apache Software License, Version 2.0) Apache Jena - Core (org.apache.jena:jena-core:3.10.0 - http://jena.apache.org/jena-core/)

(The Apache Software License, Version 2.0) Apache Jena - IRI (org.apache.jena:jena-iri:3.10.0 - http://jena.apache.org/jena-iri/)

(The Apache Software License, Version 2.0) Apache Jena - Shadowed external libraries (org.apache.jena:jena-shaded-guava:3.10.0 - http://jena.apache.org/jena-shaded-guava/)

(The Apache Software License, Version 2.0) Apache Jena - TDB1 (Native Triple Store) (org.apache.jena:jena-tdb:3.10.0 - http://jena.apache.org/jena-tdb/)

(The Apache Software License, Version 2.0) Apache Log4j API (org.apache.logging.log4j:log4j-api:2.11.1 - https://logging.apache.org/log4j/2.x/log4j-api/)

(The Apache Software License, Version 2.0) Lucene Common Analyzers (org.apache.lucene:lucene-analyzers-common:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-analyzers-common)

(The Apache Software License, Version 2.0) Lucene Memory (org.apache.lucene:lucene-backward-codecs:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-backward-codecs)

(The Apache Software License, Version 2.0) Lucene Core (org.apache.lucene:lucene-core:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-core)

(The Apache Software License, Version 2.0) Lucene Grouping (org.apache.lucene:lucene-grouping:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-grouping)

(The Apache Software License, Version 2.0) Lucene Highlighter (org.apache.lucene:lucene-highlighter:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-highlighter)

(The Apache Software License, Version 2.0) Lucene Join (org.apache.lucene:lucene-join:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-join)

(The Apache Software License, Version 2.0) Lucene Memory (org.apache.lucene:lucene-memory:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-memory)

(The Apache Software License, Version 2.0) Lucene Miscellaneous (org.apache.lucene:lucene-misc:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-misc)

(The Apache Software License, Version 2.0) Lucene Queries (org.apache.lucene:lucene-queries:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-queries)

(The Apache Software License, Version 2.0) Lucene QueryParsers (org.apache.lucene:lucene-queryparser:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-queryparser)

(The Apache Software License, Version 2.0) Lucene Sandbox (org.apache.lucene:lucene-sandbox:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-sandbox)

(The Apache Software License, Version 2.0) Lucene Spatial Extras (org.apache.lucene:lucene-spatial-extras:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-spatial-extras)

(The Apache Software License, Version 2.0) Lucene Spatial 3D (org.apache.lucene:lucene-spatial3d:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-spatial3d)

(The Apache Software License, Version 2.0) Lucene Suggest (org.apache.lucene:lucene-suggest:8.6.2 - https://lucene.apache.org/lucene-parent/lucene-suggest)

(The Apache Software License, Version 2.0) Maven Artifact (org.apache.maven:maven-artifact:3.0.5 - http://maven.apache.org/ref/3.0.5//maven-artifact)

(The Apache Software License, Version 2.0) Maven Artifact Manager (org.apache.maven:maven-artifact-manager:2.0.4 - http://maven.apache.org/maven-artifact-manager)

(The Apache Software License, Version 2.0) Maven Model (org.apache.maven:maven-model:3.0.5 - http://maven.apache.org/ref/3.0.5//maven-model)

(The Apache Software License, Version 2.0) Maven Plugin API (org.apache.maven:maven-plugin-api:3.0.5 - http://maven.apache.org/ref/3.0.5//maven-plugin-api)

(The Apache Software License, Version 2.0) Maven Profile Model (org.apache.maven:maven-profile:2.0.4 - http://maven.apache.org/maven-profile)

(The Apache Software License, Version 2.0) Maven Project Builder (org.apache.maven:maven-project:2.0.4 - http://maven.apache.org/maven-project)

(The Apache Software License, Version 2.0) Maven Repository Metadata Model (org.apache.maven:maven-repository-metadata:2.0.4 - http://maven.apache.org/maven-repository-metadata)

(The Apache Software License, Version 2.0) Maven Local Settings Model (org.apache.maven:maven-settings:2.0.4 - http://maven.apache.org/maven-settings)

(The Apache Software License, Version 2.0) Maven Wagon API (org.apache.maven.wagon:wagon-provider-api:1.0-alpha-6 - no url defined)

(The Apache Software License, Version 2.0) Apache Thrift (org.apache.thrift:libthrift:0.10.0 - http://thrift.apache.org)

(The Apache Software License, Version 2.0) Apache Velocity (org.apache.velocity:velocity:1.7 - http://velocity.apache.org/engine/devel/)

(The Apache Software License, Version 2.0) XmlSchema Core (org.apache.ws.xmlschema:xmlschema-core:2.2.1 - http://ws.apache.org/commons/xmlschema20/xmlschema-core/)

(The MIT License) Checker Qual (org.checkerframework:checker-qual:3.5.0 - https://checkerframework.org)

(The Apache Software License, Version 2.0) Plexus Classworlds (org.codehaus.plexus:plexus-classworlds:2.4 - http://plexus.codehaus.org/plexus-classworlds/)

(The Apache Software License, Version 2.0) Plexus :: Component Annotations (org.codehaus.plexus:plexus-component-annotations:1.5.5 - http://plexus.codehaus.org/plexus-containers/plexus-component-annotations/)

(Unknown license) Default Plexus Container (org.codehaus.plexus:plexus-container-default:1.0-alpha-9 - no url defined)

(Unknown license) Plexus Common Utilities (org.codehaus.plexus:plexus-utils:1.1 - no url defined)

(The BSD License) Stax2 API (org.codehaus.woodstox:stax2-api:3.1.4 - http://wiki.fasterxml.com/WoodstoxStax2)

(The Apache Software License, Version 2.0) Woodstox (org.codehaus.woodstox:woodstox-core-asl:4.4.1 - http://woodstox.codehaus.org)

(The Eclipse Public License Version 1.0) org.eclipse.emf.common (org.eclipse.emf:org.eclipse.emf.common:2.12.0 - http://www.eclipse.org/emf)

(The Eclipse Public License Version 1.0) org.eclipse.emf.ecore (org.eclipse.emf:org.eclipse.emf.ecore:2.12.0 - http://www.eclipse.org/emf)

(The Eclipse Public License Version 1.0) org.eclipse.emf.ecore.xmi (org.eclipse.emf:org.eclipse.emf.ecore.xmi:2.12.0 - http://www.eclipse.org/emf)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: Http Utility (org.eclipse.jetty:jetty-http:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: IO Utility (org.eclipse.jetty:jetty-io:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: Security (org.eclipse.jetty:jetty-security:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: Server Core (org.eclipse.jetty:jetty-server:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: Servlet Handling (org.eclipse.jetty:jetty-servlet:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Public License - Version 1.0) (The Apache Software License, Version 2.0) Jetty :: Utilities (org.eclipse.jetty:jetty-util:9.2.15.v20160210 - http://www.eclipse.org/jetty)

(Eclipse Distribution License v. 1.0) (Eclipse Public License v1.0) SDO API (org.eclipse.persistence:commonj.sdo:2.1.1 - http://www.eclipse.org/eclipselink)

(Eclipse Distribution License v. 1.0) (Eclipse Public License v1.0) EclipseLink (non-OSGi) (org.eclipse.persistence:eclipselink:2.5.0 - http://www.eclipse.org/eclipselink)

(Eclipse Distribution License v. 1.0) (Eclipse Public License v1.0) Javax Persistence (org.eclipse.persistence:javax.persistence:2.1.0 - http://www.eclipse.org/eclipselink)

(The Apache Software License, Version 2.0) EJML (org.ejml:ejml-core:0.32 - http://ejml.org/)

(The Apache Software License, Version 2.0) EJML (org.ejml:ejml-ddense:0.32 - http://ejml.org/)

(The Apache Software License, Version 2.0) server (org.elasticsearch:elasticsearch:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) elasticsearch-cli (org.elasticsearch:elasticsearch-cli:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) elasticsearch-core (org.elasticsearch:elasticsearch-core:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) elasticsearch-geo (org.elasticsearch:elasticsearch-geo:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) elasticsearch-secure-sm (org.elasticsearch:elasticsearch-secure-sm:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) elasticsearch-x-content (org.elasticsearch:elasticsearch-x-content:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) Elastic JNA Distribution (org.elasticsearch:jna:5.5.0 - https://github.com/java-native-access/jna)

(The Apache Software License, Version 2.0) rest (org.elasticsearch.client:elasticsearch-rest-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) rest-high-level (org.elasticsearch.client:elasticsearch-rest-high-level-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) aggs-matrix-stats (org.elasticsearch.plugin:aggs-matrix-stats-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) lang-mustache (org.elasticsearch.plugin:lang-mustache-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) mapper-extras (org.elasticsearch.plugin:mapper-extras-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) parent-join (org.elasticsearch.plugin:parent-join-client:7.9.2 - https://github.com/elastic/elasticsearch)

(The Apache Software License, Version 2.0) rank-eval (org.elasticsearch.plugin:rank-eval-client:7.9.2 - https://github.com/elastic/elasticsearch)

(BSD-style license) FreeMarker (org.freemarker:freemarker:2.3.16 - http://freemarker.org)

(MIT License) gdal (org.gdal:gdal:2.1.0 - http://gdal.org)

(Lesser General Public License (LGPL)) API interfaces (org.geotools:gt-api:19.2 - no url defined)

(Lesser General Public License (LGPL)) Grid Coverage module (org.geotools:gt-coverage:19.2 - no url defined)

(Lesser General Public License (LGPL)) API interfaces (org.geotools:gt-coverage-api:19.2 - no url defined)

(Lesser General Public License (LGPL)) OGC CQL to Filter parser (org.geotools:gt-cql:19.2 - no url defined)

(Lesser General Public License (LGPL)) DataStore Support (org.geotools:gt-data:19.2 - no url defined)

(BSD License for HSQL) (EPSG database distribution license) (Lesser General Public License (LGPL)) EPSG Authority Service using HSQL database (org.geotools:gt-epsg-hsql:19.2 - no url defined)

(Lesser General Public License (LGPL)) GeoTIFF grid coverage exchange module (org.geotools:gt-geotiff:19.2 - no url defined)

(Lesser General Public License (LGPL)) WorldImage datasource module (org.geotools:gt-image:19.2 - no url defined)

(Lesser General Public License (LGPL)) imagemosaic datasource module (org.geotools:gt-imagemosaic:19.2 - no url defined)

(Lesser General Public License (LGPL)) JDBC DataStore Support (org.geotools:gt-jdbc:19.2 - no url defined)

(Lesser General Public License (LGPL)) Main module (org.geotools:gt-main:19.2 - no url defined)

(Lesser General Public License (LGPL)) Metadata (org.geotools:gt-metadata:19.2 - no url defined)

(Lesser General Public License (LGPL)) NetCDF gridcoverage module (org.geotools:gt-netcdf:19.2 - no url defined)

(Lesser General Public License (LGPL)) (OGC copyright) Open GIS Interfaces (org.geotools:gt-opengis:19.2 - no url defined)

(Lesser General Public License (LGPL)) Referencing services (org.geotools:gt-referencing:19.2 - no url defined)

(Lesser General Public License (LGPL)) Render (org.geotools:gt-render:19.2 - no url defined)

(Lesser General Public License (LGPL)) Shapefile module (org.geotools:gt-shapefile:19.2 - no url defined)

(Lesser General Public License (LGPL)) Feature transforming feature source wrapper (org.geotools:gt-transform:19.2 - no url defined)

(Lesser General Public License (LGPL)) H2 DataStore (org.geotools.jdbc:gt-jdbc-h2:19.2 - no url defined)

(Lesser General Public License (LGPL)) Open Web Services Model (org.geotools.ogc:net.opengis.ows:19.2 - no url defined)

(Lesser General Public License (LGPL)) Web Coverage Service Model (org.geotools.ogc:net.opengis.wcs:19.2 - no url defined)

(Lesser General Public License (LGPL)) Xlink Model (org.geotools.ogc:org.w3.xlink:19.2 - no url defined)

(CDDL + GPLv2 with classpath exception) Expression Language 2.2 Implementation (org.glassfish.web:javax.el:2.2.6 - http://uel.java.net)

(New BSD License) Hamcrest Core (org.hamcrest:hamcrest-core:1.3 - https://github.com/hamcrest/JavaHamcrest/hamcrest-core)

(Public Domain, per Creative Commons CC0) HdrHistogram (org.hdrhistogram:HdrHistogram:2.1.9 - http://hdrhistogram.github.io/HdrHistogram/)

(The Apache Software License, Version 2.0) Hibernate Validator Engine (org.hibernate:hibernate-validator:5.3.4.Final - http://hibernate.org/validator/hibernate-validator)

(The Apache Software License, Version 2.0) Hibernate Validator Annotation Processor (org.hibernate:hibernate-validator-annotation-processor:5.3.4.Final - http://hibernate.org/validator/hibernate-validator-annotation-processor)

(BSD-Style License) W3C XLink 1.0 (org.hisrc.w3c:xlink-v_1_0:1.3.0 - https://github.com/highsource/w3c-schemas/w3c-schema-parent/xlink-v_1_0)

(BSD-Style License) W3C XLink 1.0 (org.hisrc.w3c:xlink-v_1_0:1.4.0 - https://github.com/highsource/w3c-schemas/w3c-schema-parent/xlink-v_1_0)

(HSQLDB License, a BSD open source license) HyperSQL Database (org.hsqldb:hsqldb:2.4.1 - http://hsqldb.org)

(Simplified BSD) Support and utility classes (org.jaitools:jt-utils:1.4.0 - http://jaitools.org/jt-utils/)

(Simplified BSD) VectorBinarize operator (org.jaitools:jt-vectorbinarize:1.4.0 - http://jaitools.org/operator/jt-vectorbinarize/)

(Simplified BSD) ZonalStats operator (org.jaitools:jt-zonalstats:1.4.0 - http://jaitools.org/operator/jt-zonalstats/)

(Apache License, version 2.0) JBoss Logging 3 (org.jboss.logging:jboss-logging:3.3.0.Final - http://www.jboss.org)

(Similar to Apache License but with the acknowledgment clause removed) JDOM (org.jdom:jdom2:2.0.4 - http://www.jdom.org)

(Similar to Apache License but with the acknowledgment clause removed) JDOM (org.jdom:jdom2:2.0.6 - http://www.jdom.org)

(The JSON License) JSON in Java (org.json:json:20160810 - https://github.com/douglascrockford/JSON-java)

(BSD-Style License) JAXB2 Basics - Runtime (org.jvnet.jaxb2_commons:jaxb2-basics-runtime:0.11.0 - https://github.com/highsource/jaxb2-basics/jaxb2-basics-runtime)

(BSD-Style License) JAXB2 Basics - Runtime (org.jvnet.jaxb2_commons:jaxb2-basics-runtime:0.9.4 - https://github.com/highsource/jaxb2-basics/jaxb2-basics-runtime)

(BSD-Style License) OGC Filter 1.1.0 (org.jvnet.ogc:filter-v_1_1_0:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/filter-v_1_1_0)

(BSD-Style License) OGC GML 3.1.1 (org.jvnet.ogc:gml-v_3_1_1:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/gml-v_3_1_1)

(BSD-Style License) OGC GML 3.2.0 (org.jvnet.ogc:gml-v_3_2_0:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/gml-v_3_2_0)

(BSD-Style License) OGC GML 3.2.1 (org.jvnet.ogc:gml-v_3_2_1:2.4.0 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/gml-v_3_2_1)

(BSD-Style License) OGC ISO 19139 20060504 (org.jvnet.ogc:iso19139-v_20060504:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/iso19139-v_20060504)

(BSD-Style License) OGC OWS 1.0.0 (org.jvnet.ogc:ows-v_1_0_0:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/ows-v_1_0_0)

(BSD-Style License) OGC WFS 1.1.0 (org.jvnet.ogc:wfs-v_1_1_0:2.6.1 - https://github.com/highsource/ogc-schemas/ogc-schema-parent/wfs-v_1_1_0)

(The MIT license) winp (org.jvnet.winp:winp:1.23 - http://kohsuke.org/winp/)

(The Apache Software License, Version 2.0) LogicNG (org.logicng:logicng:1.6.0 - http://www.logicng.org)

(The MIT License) mockito-core (org.mockito:mockito-core:2.7.0 - http://mockito.org)

(The Apache Software License, Version 2.0) Objenesis (org.objenesis:objenesis:2.5 - http://objenesis.org)

(Unknown license) GeoDB Core (org.opengeo:geodb:0.7-RC2 - no url defined)

(Aduna BSD license) OpenRDF Sesame: Model (org.openrdf.sesame:sesame-model:4.1.2 - http://www.openrdf.org/sesame-core/sesame-model/)

(Aduna BSD license) OpenRDF Sesame: Rio - API (org.openrdf.sesame:sesame-rio-api:4.1.2 - http://www.openrdf.org/sesame-core/sesame-rio/sesame-rio-api/)

(Aduna BSD license) OpenRDF Sesame: Rio - Datatypes (org.openrdf.sesame:sesame-rio-datatypes:4.1.2 - http://www.openrdf.org/sesame-core/sesame-rio/sesame-rio-datatypes/)

(Aduna BSD license) OpenRDF Sesame: Rio - Languages (org.openrdf.sesame:sesame-rio-languages:4.1.2 - http://www.openrdf.org/sesame-core/sesame-rio/sesame-rio-languages/)

(Aduna BSD license) OpenRDF Sesame: Rio - RDF/XML (org.openrdf.sesame:sesame-rio-rdfxml:4.1.2 - http://www.openrdf.org/sesame-core/sesame-rio/sesame-rio-rdfxml/)

(Aduna BSD license) OpenRDF Sesame: util (org.openrdf.sesame:sesame-util:4.1.2 - http://www.openrdf.org/sesame-core/sesame-util/)

(BSD) ASM Core (org.ow2.asm:asm:5.0.4 - http://asm.objectweb.org/asm/)

(The Apache Software License, Version 2.0) org.ow2.authzforce:authzforce-ce-core-pdp-api (org.ow2.authzforce:authzforce-ce-core-pdp-api:15.3.0 - https://authzforce.ow2.org)

(The Apache Software License, Version 2.0) org.ow2.authzforce:authzforce-ce-core-pdp-engine (org.ow2.authzforce:authzforce-ce-core-pdp-engine:13.3.1 - https://authzforce.ow2.org)

(The Apache Software License, Version 2.0) org.ow2.authzforce:authzforce-ce-pdp-ext-model (org.ow2.authzforce:authzforce-ce-pdp-ext-model:7.5.0 - https://authzforce.ow2.org)

(The Apache Software License, Version 2.0) org.ow2.authzforce:authzforce-ce-xacml-model (org.ow2.authzforce:authzforce-ce-xacml-model:7.5.0 - https://authzforce.ow2.org)

(The Apache Software License, Version 2.0) org.ow2.authzforce:authzforce-ce-xmlns-model (org.ow2.authzforce:authzforce-ce-xmlns-model:7.5.0 - https://authzforce.ow2.org)

(The Apache Software License, Version 2.0) quartz (org.quartz-scheduler:quartz:2.2.0 - http://www.quartz-scheduler.org/quartz)

(The Apache Software License, Version 2.0) quartz (org.quartz-scheduler:quartz:2.3.0 - http://www.quartz-scheduler.org/quartz)

(The Apache Software License, Version 2.0) quartz-jobs (org.quartz-scheduler:quartz-jobs:2.3.0 - http://www.quartz-scheduler.org/quartz-jobs)

(MIT License) JCL 1.1.1 implemented over SLF4J (org.slf4j:jcl-over-slf4j:1.7.21 - http://www.slf4j.org)

(MIT License) SLF4J API Module (org.slf4j:slf4j-api:1.7.21 - http://www.slf4j.org)

(MIT License) SLF4J LOG4J-12 Binding (org.slf4j:slf4j-log4j12:1.7.25 - http://www.slf4j.org)

(GNU LGPL 3) SonarQube Java :: JaCoCo Listeners (org.sonarsource.java:sonar-jacoco-listeners:3.8 - http://docs.sonarqube.org/display/PLUG/Plugin+Library/java/sonar-jacoco-listeners)

(The Apache Software License, Version 2.0) Sisu Guice - Core Library (org.sonatype.sisu:sisu-guice:3.1.0 - http://code.google.com/p/google-guice/sisu-guice/)

(Eclipse Public License, Version 1.0) (The Apache Software License, Version 2.0) Sisu-Inject-Bean : Aggregate OSGi bundle (org.sonatype.sisu:sisu-inject-bean:2.3.0 - http://sisu.sonatype.org/sisu-inject/containers/guice-bean/sisu-inject-bean/)

(Eclipse Public License, Version 1.0) Sisu-Inject-Plexus : Aggregate OSGi bundle (org.sonatype.sisu:sisu-inject-plexus:2.3.0 - http://sisu.sonatype.org/sisu-inject/containers/guice-bean/guice-plexus/sisu-inject-plexus/)

(The Apache Software License, Version 2.0) Spring AOP (org.springframework:spring-aop:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) Spring Beans (org.springframework:spring-beans:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) Spring Context (org.springframework:spring-context:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) Spring Core (org.springframework:spring-core:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) Spring Expression Language (SpEL) (org.springframework:spring-expression:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) Spring Web (org.springframework:spring-web:4.1.9.RELEASE - https://github.com/spring-projects/spring-framework)

(The Apache Software License, Version 2.0) SnakeYAML (org.yaml:snakeyaml:1.26 - http://www.snakeyaml.org)

(The Apache Software License, Version 2.0) software.amazon.ion:ion-java (software.amazon.ion:ion-java:1.0.2 - https://github.com/amznlabs/ion-java/)

(CPL) WSDL4J (wsdl4j:wsdl4j:1.6.3 - http://sf.net/projects/wsdl4j)

(The Apache Software License, Version 2.0) XML Commons Resolver Component (xml-resolver:xml-resolver:1.2 - http://xml.apache.org/commons/components/resolver/)



<div> Icons appearing on this page are made by <a href="https://www.freepik.com" title="Freepik"> Freepik </a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

<br/>

Altough we have made our best effort to give the appropriate credits, some entries might be missing or incorrect: you are welcome to report to ESSI-Lab any of such issues you might identifiy.
	
</details>	
	
<p align="right">(<a href="#top">back to top</a>)</p>

## Contact ESSI-Lab

You are welcome to contact us for any info about the DAB, including bug reports, feature enhancements and code contributions.

[http://essi-lab.eu/](http://essi-lab.eu/)

[info@essi-lab.eu](info@essi-lab.eu)

GitHub repository link: [https://github.com/ESSI-Lab/DAB](https://github.com/ESSI-Lab/DAB)

<p align="right">(<a href="#top">back to top</a>)</p>
