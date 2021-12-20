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
  <summary>Getting started</summary>

The DAB is composed by multiple maven modules. GS-service is the main module, capable of starting the DAB Internet services and its web configuration tool.

The command <code>mvn clean install</code> run in the root folder will package the jar files and finally the war package of DAB GS-service.

Finally, it's possible to launch DAB services  from the gs-service folder with the following maven command:

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

</details>
	
<p align="right">(<a href="#top">back to top</a>)</p>
	
## License and attribution

Please note that the DAB community edition **is not public domain software**! While using DAB source code you should **give full attribution to the original authors** and redistribute your modifications in the same way, accordingly to the **GNU Affero General Public License v3.0**. Click on the items below to know more:

<details>
	<summary>In case DAB services are offered by a third party through the Internet</summary>

AGPL license is more restrictive with respect to GPL with regard to online service providers making use of the licensed software.

This is the case where a third party downloads the DAB source code and offers its functionalities through an online service (e.g. through a server managed by the third party). In this case **the following statement should appear at the third party site** offering the service:

<code>The brokering service is offered by DAB, a software developed by ESSI-Lab (Stefano Nativi, Paolo Mazzetti, Enrico Boldrini, Fabrizio Papeschi, Roberto Roncella, Massimiliano Olivieri, Lorenzo Bigagli and Mattia Santoro) of Institute of Atmospheric Pollution Research of National Research Council of Italy. More information is available at https://github.com/ESSI-Lab/DAB/</code>

</details>

<details>
	
<summary>In case DAB is used in the work described by a scientific paper / technical report</summary>

Please note at least **one of the following papers should be cited to give correct author attributions** while describing work making use of the DAB:

<code>@article {DAB2015,title = {Big Data challenges in building the Global Earth Observation System of Systems},journal = {Environmental Modelling and Software},year = {2015},volume = {68},pages = {1-26},author = {Nativi, S. and Mazzetti, P. and Santoro, M. and Papeschi, F. and Craglia, M. and Ochiai, O.}}</code>

<code>@article{DAB2014,title = {The brokering approach for enabling collaborative scientific research},journal = {Collaborative Knowledge in Scientific Research Networks},year = {2014},pages = {283-304},author = {Boldrini, E. and Craglia, M. and Mazzetti, P. and Nativi, S.}}</code>

<code>@article{DAB2009,title = {GI-cat: A mediation solution for building a clearinghouse catalog service},journal = {Proceedings of the International Conference on Advanced Geographic Information Systems and Web Services, GEOWS 2009},year = {2009},pages = {68-74},author = {Nativi, S. and Bigagli, L. and Mazzetti, P. and Boldrini, E. and Papeschi, F.}}</code>

<code>@article{DAB2007,title = {Discovery, query and access services for imagery, gridded and coverage data a clearinghouse solution},journal = {International Geoscience and Remote Sensing Symposium (IGARSS)},year = {2007},pages = {4021-4024},author = {Nativi, S. and Bigagli, L. and Mazzetti, P. and Mattia, U. and Boldrini, E.}}</code>
	
</details>

<details>
	<summary>Changes should be made back available with the same license</summary>

The modified source code should be licensed according to AGPL and **redistributed back to the community, preferably through the DAB GitHub repository**. You are welcome and encouraged to contact ESSI-Lab to propose code contributions to be applied to the present repository (see contributions section).

In any case the following attribution headers should be preserved in modified versions of the source code:

<code>Discovery and Access Broker (DAB) Community Edition (CE)</code>
<code>Copyright (C) 2021  ESSI-Lab (Stefano Nativi, Paolo Mazzetti, Enrico Boldrini, Fabrizio Papeschi, Roberto Roncella, Massimiliano Olivieri, Lorenzo Bigagli and Mattia Santoro) of Institute of Atmospheric Pollution Research of National Research Council of Italy (CNR)</code>


In case a modified version of the DAB is offered by a third party as an Internet service, additionally to the attribution described above also the code (or a link to the DAB GitHub repository holding the modifications) should be made available on the third party site.

</details>	
	
<p align="right">(<a href="#top">back to top</a>)</p>


## Contributions

<details>
	<summary>Contributions</summary>
	
Contributors to the ESSI-Lab DAB CE project are welcome: you can **report bugs and request enhancements** contacting ESSI-Lab or through the GitHub issue tracker.

To **propose code contributions** please contact ESSI-Lab to coordinate the development effort and avoid duplications.

The **ESSI-Lab Contributor License Agreement** needs to be signed by contributors to enable ESSI-Lab to accept community contributions to the DAB repository even more smoothly: now the signature of the agreement is integrated in the pull request.

</details>
	
<p align="right">(<a href="#top">back to top</a>)</p>

## DAB history and acknowledgments

<details>
	<summary>DAB history and acknowledgments</summary>

The DAB was initially conceived and is still designed and developed by ESSI-Lab at Institute on Atmospheric Pollution Research (Florence division) of National Research Council of Italy.

DAB implements the **brokering approach** introduced by Stefano Nativi et al. in the position paper: <code>Nativi S, Craglia M, Pearlman J. The Brokering Approach for Multidisciplinary Interoperability: A Position Paper. International Journal of Spatial Data Infrastructures Research 7; 2012. p. 1-15. JRC78581.</code>

DAB software development took and takes place in the context of numerous international projects and initiatives, funded/operated by different entities on a time range of over 10 years.

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
   <li><b>LoA "WHOS-Plata"</b> WMO ref. 24351/2019-1.5 CLW (2019/2020)</li>
   <li><b>LoA "WHOS-Dominican Republic"</b> WMO ref. 09198/2020- 1.6 S/HWR</li>
   <li><b>LoA "WHOS"</b> WMO ref. 12437/2021-1.4 DSG)</li>
  </ul>
  </li>
  </ul>
 </li>
 <li>European Union funded projects
 <ul>
  <li>FP7
  <ul>
   <li><b>EuroGEOSS</b> (2009/2012)</li>
   <li><b>SeaDataNet II</b> (2011/2015)</li>
   <li><b>GEOWOW</b> (2011/2014)</li>
  </ul>
  </li>
	<li>Horizon 2020
      <ul>
      <li><b>ODIP 2</b> (2015/2018)</li>
      <li><b>ERA-PLANET</b> (2016/-)</li>
      <li><b>SeaDataCloud</b> (2016/2021)</li>
      <li><b>Blue-Cloud</b> (2019-)</li>
      <li><b>I-CHANGE</b> (2021-)</li>
      </ul>
	</li>
	<li>EASME
	<ul>
      <li><b>EMODNet Ingestion</b> (2016/2019)</li>
      <li><b>EMODNet Ingestion II</b> (2019-2021)</li>
      </ul>
    </li>
	<li>European Space Agency (ESA)
      <ul>
      <li><b>HMA-T</b> (2008/2009)</li>
      <li><b>Prod-Trees</b> (2012/2015)</li>
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
		
Since 2012, the DAB framework has been leveraged by GEO as **GEO-DAB**, the brokerage component of the GEOSS Common Infrastructure (GCI; or GEOSS platform). GEO DAB is being operated and managed by CNR-IIA on its cloud infrastructure, it connects over 150 information systems that allow the search of over 400 million data (granules), and relative access if allowed by the provider.

[More info on GEO-DAB](https://www.geodab.net/)

The DAB framework is also currently adopted by WMO as a key component to realize the **WMO Hydrological Observing System (WHOS)**, a system of systems able to share hydrological data at a global scale.

[More info on WHOS](https://public.wmo.int/en/our-mandate/water/whos)

[WHOS webinar recording](https://community.wmo.int/news/watch-recording-whos-webinar) is a valuable introduction to WHOS and its brokering component.

In the context of **SeaDataNet** the DAB framework is used to enable discovery of international ocean data sources.

[More information on SeaDataNet brokering service](https://www.seadatanet.org/Data-Access/Discover-international-data)

DAB won the **2014 Geospatial World Innovation Awards** [(media coverage here)](https://www.cnr.it/en/news/5795/il-geospatial-world-innovation-award-all-iia-cnr).

The following papers provide a technical description of the DAB brokering framweork.

<code>@article {DAB2015,title = {Big Data challenges in building the Global Earth Observation System of Systems},journal = {Environmental Modelling and Software},year = {2015},volume = {68},pages = {1-26},author = {Nativi, S. and Mazzetti, P. and Santoro, M. and Papeschi, F. and Craglia, M. and Ochiai, O.}}</code>

<code>@article{DAB2014,title = {The brokering approach for enabling collaborative scientific research},journal = {Collaborative Knowledge in Scientific Research Networks},year = {2014},pages = {283-304},author = {Boldrini, E. and Craglia, M. and Mazzetti, P. and Nativi, S.}}</code>

<code>@article{DAB2009,title = {GI-cat: A mediation solution for building a clearinghouse catalog service},journal = {Proceedings of the International Conference on Advanced Geographic Information Systems and Web Services, GEOWS 2009},year = {2009},pages = {68-74},author = {Nativi, S. and Bigagli, L. and Mazzetti, P. and Boldrini, E. and Papeschi, F.}}</code>

<code>@article{DAB2007,title = {Discovery, query and access services for imagery, gridded and coverage data a clearinghouse solution},journal = {International Geoscience and Remote Sensing Symposium (IGARSS)},year = {2007},pages = {4021-4024},author = {Nativi, S. and Bigagli, L. and Mazzetti, P. and Mattia, U. and Boldrini, E.}}</code>

Many computer scientists and software engineers have contributed to DAB design and development throughout an implementation effort of over 10 years, spanning different maturity stages (from prototypical to operational)  and components (e.g. GI-cat, GI-axe, GI-sem, GI-suite, ...).

[Active DAB development team (contact us)](https://www.uos-firenze.essi-lab.eu/personnel):

- Stefano Nativi (DAB father)
- Paolo Mazzetti (sofware design)
- Enrico Boldrini (software design, developer)
- Fabrizio Papeschi (developer)
- Roberto Roncella (developer)
- Massimiliano Olivieri (developer)
- Lorenzo Bigagli (software design)
- Mattia Santoro (software design, developer)

Previous members:
	
- Francesco Pezzati (past developer)
- Alessio Baldini (past developer)
- Fabrizio Vitale (past developer)
- Ugo Mattia (past developer)
- Valerio Angelini (past developer)


<div> Icons appearing on this page are made by <a href="https://www.freepik.com" title="Freepik"> Freepik </a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

</details>	
	
<p align="right">(<a href="#top">back to top</a>)</p>

## Contact ESSI-Lab

You are welcome to contact us for any info about the DAB, including bug reports, feature enhancements and code contributions.

[http://essi-lab.eu/](http://essi-lab.eu/)

[info@essi-lab.eu](info@essi-lab.eu)

DAB homepage including extended documentation: [https://confluence.geodab.eu/display/DAB/](https://confluence.geodab.eu/display/DAB/)

GitHub repository link: [https://github.com/ESSI-Lab/DAB](https://github.com/ESSI-Lab/DAB)

<p align="right">(<a href="#top">back to top</a>)</p>
