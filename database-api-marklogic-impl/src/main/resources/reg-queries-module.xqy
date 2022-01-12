xdmp:document-insert("/gs-modules/reg-queries-module.xqy", 
text{"

xquery version '1.0-ml';
declare namespace html = 'http://www.w3.org/1999/xhtml';
declare namespace gs = 'http://flora.eu/gi-suite/1.0/dataModel/schema';

declare function gs:clearDoc(){

  xdmp:document-insert('/reg-queries/reg-queries-doc.xml',<gs:registered-queries xmlns:gs='http://flora.eu/gi-suite/1.0/dataModel/schema'></gs:registered-queries>) 
};

declare function gs:deregister(){

   (   
     for $id in document('/reg-queries/reg-queries-doc.xml')//gs:registeredQuery/text() return cts:deregister(xs:unsignedLong($id)),
     
     xdmp:log(concat('*** Deregistered ', count(document('/reg-queries/reg-queries-doc.xml')//gs:registeredQuery/text()), ' queries ***')) 
   )  
};

let $timeTreshold := 1000 * 60 * TIME_TRESHOLD (: 30 minutes :)
let $maxQueries := MAX_QUERIES

(: reads the time stamp from the registered queries doc :)
let $propml := document('/reg-queries/reg-queries-doc.xml')//gs:timeStamp/text()

(: get the current time stamp :)
let $curml := xs:long((fn:adjust-dateTime-to-timezone(current-dateTime(), xs:dayTimeDuration('PT0H')) - xs:dateTime('1970-01-01T00:00:00-00:00')) div xs:dayTimeDuration('PT0.001S'))

let $elapsedTime := $curml - $propml

(: count(document('/reg-queries/doc.xml')//gs:registeredQuery) :)
let $queriesCount := count(document('/reg-queries/reg-queries-doc.xml')//gs:registeredQuery)

return if($elapsedTime > $timeTreshold) then (

  'Elapsed time > timeTreshold, deregistering and clearing doc', 
  xdmp:log('*** Elapsed time > timeTreshold, deregistering and clearing doc ***'),
  
  gs:deregister(),
  gs:clearDoc()

) else if ($queriesCount > $maxQueries) then ( 

  concat('More than ',$maxQueries,' queries, deregistering and clearing doc'),
  xdmp:log(concat('*** More than ',$maxQueries,' queries, deregistering and clearing doc ***')),
  
  gs:deregister(),
  gs:clearDoc()

) else ( 

  'Nothing to do', xdmp:log('*** Nothing to do ***')
)

"}, xdmp:permission('app-user', 'execute'))