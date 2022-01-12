xquery version "1.0-ml";
 
let $uri := "/gs-modules/functions-module.xqy"
let $node := <module><![CDATA[

xquery version "1.0-ml";
module namespace  gs = "http://flora.eu/gi-suite/1.0/dataModel/schema";
declare default function namespace "http://www.w3.org/2005/xpath-functions";
 
declare function gs:sourceId-query($sourceId as xs:string, $suiteId as xs:string) as cts:query
{
    (: only source id 
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sourceId'),'=',$sourceId,("score-function=linear"),0.0)
    :)
    
    (: only directory  with index reading :)
    
    cts:directory-query( concat(
        concat('/',$suiteId,'_'),  
        $sourceId,
        try { 
            cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',concat($sourceId,'_dataFolder')))
        } catch ($e) { 
            '' 
        }   
        ,'/'),
        'infinity')
    
        (: only directory 1   

    cts:directory-query(concat('/preprodenvconf_',$sourceId,'-data-1/'),'infinity')
  
   :)
     
     
    (: only directory 1 or 2 
    cts:or-query((
      cts:directory-query(concat('/preprodenvconf_',$sourceId,'-data-1/'),'infinity'),
      cts:directory-query(concat('/preprodenvconf_',$sourceId,'-data-2/'),'infinity') 
    ))
    :) 
    
    (: current strategy 
    cts:and-query((
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sourceId'),'=',$sourceId,("score-function=linear"),0.0),
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','resourceTimeStamp'),'<', 
      try { 
          cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',fn:concat($sourceId,'_endTimeStamp'))) 
      } catch ($e) { '0' },("score-function=linear"),0.0)))
   
   :)
   
  (: current strategy registered  
  cts:registered-query(cts:register( 
    cts:and-query((
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sourceId'),'=',$sourceId,("score-function=linear"),0.0),
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','resourceTimeStamp'),'<', 
      try { 
          cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',fn:concat($sourceId,'_endTimeStamp')),'()') 
      } catch ($e) { '0' },("score-function=linear"),0.0)))
    
  ),"unfiltered")
  
  :)  
};

declare function gs:spatial-contains-ncr-query($south, $west,$north,$east, 
$p0, $w0,       
$p1, $w1,       
$p2, $w2,       
$p3, $w3,       
$p4, $w4,       
$p5, $w5,       
$p6, $w6,       
$p7, $w7,       
$p8, $w8,       
$p9, $w9,       
$p10, $w10,       
$p11, $w11,       
$p12, $w12,       
$p13, $w13,       
$p14, $w14,       
$p15, $w15,       
$p16, $w16,       
$p17, $w17,       
$p18, $w18,
$p19, $w19
) as cts:query
{
  cts:and-query((
  cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sw')),cts:box($south, $west, $north, $east)),
  cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','se')),cts:box($south, $west, $north, $east)),
  cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','nw')),cts:box($south, $west, $north, $east)),
  cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','ne')),cts:box($south, $west, $north, $east)),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isCrossed'),'=','false',("score-function=linear"),0.0),

  cts:or-query((

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p0,("score-function=linear"),$w0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p1,("score-function=linear"),$w1))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p2,("score-function=linear"),$w2),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p3,("score-function=linear"),$w3))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p4,("score-function=linear"),$w4),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p5,("score-function=linear"),$w5))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p6,("score-function=linear"),$w6),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p7,("score-function=linear"),$w7))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p8,("score-function=linear"),$w8),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p9,("score-function=linear"),$w9))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p10,("score-function=linear"),$w10),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p11,("score-function=linear"),$w11))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p12,("score-function=linear"),$w12),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p13,("score-function=linear"),$w13))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p14,("score-function=linear"),$w14),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p15,("score-function=linear"),$w15))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p16,("score-function=linear"),$w16),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<=',$p17,("score-function=linear"),$w17))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p18,("score-function=linear"),$w18),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<=',$p19,("score-function=linear"),$w19)))))))
};

declare function gs:spatial-contains-cr-query($south, $west,$north,$east, 
$p0, $w0,       
$p1, $w1,       
$p2, $w2,       
$p3, $w3,       
$p4, $w4,       
$p5, $w5,       
$p6, $w6,       
$p7, $w7,       
$p8, $w8,       
$p9, $w9,       
$p10, $w10,       
$p11, $w11,       
$p12, $w12,       
$p13, $w13,       
$p14, $w14,       
$p15, $w15,       
$p16, $w16,       
$p17, $w17,       
$p18, $w18,
$p19, $w19
) as cts:query
{
  cts:and-query((

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'<=',$north,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'>=',$south,("score-function=linear"),0.0))),

  cts:or-query((

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p0,("score-function=linear"),$w0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p1,("score-function=linear"),$w1))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p2,("score-function=linear"),$w2),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p3,("score-function=linear"),$w3))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p4,("score-function=linear"),$w4),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p5,("score-function=linear"),$w5))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p6,("score-function=linear"),$w6),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p7,("score-function=linear"),$w7))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p8,("score-function=linear"),$w8),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p9,("score-function=linear"),$w9))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p10,("score-function=linear"),$w10),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p11,("score-function=linear"),$w11))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p12,("score-function=linear"),$w12),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p13,("score-function=linear"),$w13))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p14,("score-function=linear"),$w14),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<',$p15,("score-function=linear"),$w15))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p16,("score-function=linear"),$w16),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<=',$p17,("score-function=linear"),$w17))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'>=',$p18,("score-function=linear"),$w18),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','area'),'<=',$p19,("score-function=linear"),$w19))))),

  cts:or-query((

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isCrossed'),'=','false',("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'<=',$east,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isCrossed'),'=','false',("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'>',$west,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'>=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'<=',$east,("score-function=linear"),0.0)))))))
  
};

declare function gs:spatial-intersects-query($south,$west,$north,$east) as cts:query
{
  cts:or-query((
  cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sw'),
  fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','se'),
  fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','nw'),
  fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','ne')),cts:box($south, $west, $north, $east)),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'<=',$north,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'>=',$south,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'<=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'>=',$east,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'>=',$south,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'<=',$north,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'<=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'>=',$east,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'<=',$east,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'>=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'<=',$south,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'>=',$north,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'>=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'<=',$east,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'<=',$south,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'>=',$north,("score-function=linear"),0.0))),

  cts:and-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','south'),'<=',$south,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','north'),'>=',$north,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','west'),'<=',$west,("score-function=linear"),0.0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','east'),'>=',$east,("score-function=linear"),0.0)))))

};

declare function gs:deleted-excluded-query() as cts:query
{
    cts:not-query(cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'!=','',("score-function=linear"),0.0))
};

declare function gs:weight-query($indexName,$w0,$w1,$w2,$w3,$w4,$w5,$w6,$w7,$w8,$w9) as cts:query
{
  cts:or-query((
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',1,("score-function=linear"),$w0),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',2,("score-function=linear"),$w1),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',3,("score-function=linear"),$w2),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',4,("score-function=linear"),$w3),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',5,("score-function=linear"),$w4),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',6,("score-function=linear"),$w5),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',7,("score-function=linear"),$w6),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',8,("score-function=linear"),$w7),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',9,("score-function=linear"),$w8),
  cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$indexName),'=',10,("score-function=linear"),$w9)))
};

declare function gs:gdc-weight-query() as cts:query
{
  cts:or-query((
  cts:element-word-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isGDC'),'false',("case-insensitive"),0),
  cts:element-word-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isGDC'),'true',("case-insensitive"),25)))
};

declare function gs:temp-extent-query($element,$operator,$value) as cts:query
{
   cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$element),$operator,$value,("score-function=linear"),0.0)
};

declare function gs:temp-extent-now-query($element) as cts:query
{
    cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$element),'=','',("score-function=linear"),0.0)
};

declare function gs:BboxUnion($query, $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget, $queryBbox) { 

    <gs:BboxUnion target='bbox'>  {
         
           if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
          
                   let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                   )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                  ) 
                  
                  return  if ($queryBbox = 'true') then (
                    
                     cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_WEST')), (), 
                        $innerQuery),
                     cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_SOUTH')), (), 
                        $innerQuery),
                     cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_EAST')), (), 
                        $innerQuery),
                     cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_NORTH')), (), 
                        $innerQuery)  
            
                )else(
                
                     cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'west')), (), 
                        $innerQuery),
                     cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'south')), (), 
                        $innerQuery),
                     cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'east')), (), 
                        $innerQuery),
                     cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'north')), (), 
                        $innerQuery)    
                                 
                 )                         
           ) else if ($queryBbox = 'true') then (              
            
                 cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_WEST')), (), $query),
                 cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_SOUTH')), (), $query),
                 cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_EAST')), (), $query),
                 cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_BBOX_NORTH')), (), $query)                 
         
           )else(
           
                 cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'west')), (), $query),
                 cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'south')), (), $query),
                 cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'east')), (), $query),
                 cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'north')), (), $query)                      
           )
        }     
     </gs:BboxUnion> 
};

declare function gs:temporalExtentUnion($query, $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget, $queryExtent) { 

    <gs:TemporalExtentUnion target='tmpExtent'>  {
         
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
             
                 let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                 )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                )  
             
                return  if ($queryExtent = 'true') then (
               
                   cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_tmpExtentBegin')), (), $innerQuery),
                   cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_tmpExtentEnd')), (),  $innerQuery)                  
                
                )else(
                
                   cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'tmpExtentBegin')), (), $innerQuery),
                   cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'tmpExtentEnd')), (),  $innerQuery)                                 
                )
            ) else if ($queryExtent = 'true') then(              
            
                  cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_tmpExtentBegin')), (), $query ),
                  cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'DISCOVERY_MESSAGE_tmpExtentEnd')), (), $query )
            
            )else(
            
                 cts:min(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'tmpExtentBegin')), (), $query ),
                 cts:max(cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', 'tmpExtentEnd')), (), $query )           
            )
        }     
     </gs:TemporalExtentUnion> 
};

declare function gs:countDistinct($query, $target, $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget){

      <gs:CountDistinct
        target='{$target}'>
        {          
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
                         
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                   
                   ) return    
                   
                   count(cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target), (),
                   ('eager'),
                   $innerQuery))        

             )else(
             
                   count(cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target), (),
                   ('eager'),
                   $query))       
             )
        }
    </gs:CountDistinct>  
};

declare function gs:statFunction($function, $query, $target, $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget) {

    if($function = 'MAX') then (

        <gs:Max target='{$target}'>
            {         
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
                
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                   )
                
                  return cts:max(
                       cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                       $innerQuery
                   )
           
                ) else (
               
                   cts:max(
                         cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                         $query
                   )             
                )
             }
        </gs:Max>
    
    )else if($function = 'MIN') then (
     
         <gs:Min target='{$target}'>            
            {          
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
                
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                    )
                              
                   return cts:min(
                       cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                       $innerQuery
                   )
           
                ) else (
               
                   cts:min(
                         cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                         $query
                   )             
                )
             }
         </gs:Min>
    
    )else if($function = 'SUM') then (
    
         <gs:Sum target='{$target}'>
            {          
                 if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
                
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                    )
                
                   return cts:sum-aggregate(
                       cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                       $innerQuery
                   )
           
                ) else (
               
                   cts:sum-aggregate(
                         cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                         $query
                   )             
                )
             }
         </gs:Sum>
    
    ) else (
   
         <gs:Avg target='{$target}'>
            {          
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
                
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                   )
                               
                   return  cts:avg-aggregate(
                       cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                       $innerQuery
                   )
           
                ) else (
               
                   cts:avg-aggregate(
                         cts:element-reference(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $target)), (),
                         $query
                   )             
                )
             }
         </gs:Avg>
    )
};

declare function gs:geoSpatialFrequency($query, $target, $bboxes, $limit, $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget){

    <gs:Frequency target='{$target}'> 
    {
          (              
              for $bbox in $bboxes return 
                                  
                      if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
            
                           let $south := xs:double(tokenize($bbox,'#')[1])
                           let $west := xs:double(tokenize($bbox,'#')[2])
                           let $north := xs:double(tokenize($bbox,'#')[3])
                           let $east := xs:double(tokenize($bbox,'#')[4])  
                 
                           let $innerQuery := if($target = 'bbox') then (
                                                     
                                if(string-length ($groupByVal) > 0) then (
                                
                                        cts:and-query((
                                              $query,
                                              
                                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal),
                                              
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','se')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','nw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','ne')),
                                              cts:box($south, $west, $north, $east))
                                       ))  
                                       
                                )else(
                                
                                     cts:and-query((
                                              $query,
                                              
                                               cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                                 xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                                               cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                                 xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),    
                                              
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','se')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','nw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','ne')),
                                              cts:box($south, $west, $north, $east))
                                       ))                                        
                                )
                                
                          ) else (
                          
                              if(string-length ($groupByVal) > 0) then (
                                
                                        cts:and-query((
                                              $query,
                                              
                                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal),
                                              
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_sw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_se')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_nw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_ne')),
                                              cts:box($south, $west, $north, $east))
                                       ))  
                                       
                                )else(
                                
                                     cts:and-query((
                                              $query,
                                              
                                               cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                                 xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                                               cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                                 xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),    
                                              
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_sw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_se')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_nw')),
                                              cts:box($south, $west, $north, $east)),
                                              cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_ne')),
                                              cts:box($south, $west, $north, $east))
                                       ))                                        
                                )                          
                          ) 
                  
                          return concat(replace($bbox,'#',','),'ITEMSEP',xdmp:estimate(cts:search(doc(),$innerQuery,("unfiltered","score-simple"),0)))                   
                ) else (
  
                     let $south := xs:double(tokenize($bbox,'#')[1])
                     let $west := xs:double(tokenize($bbox,'#')[2])
                     let $north := xs:double(tokenize($bbox,'#')[3])
                     let $east := xs:double(tokenize($bbox,'#')[4])  
                 
                     let $innerQuery := if($target = 'bbox') then (
                                
                          cts:and-query((
                                      $query,                                                                          
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','sw')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','se')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','nw')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','ne')),
                                      cts:box($south, $west, $north, $east))
                               ))             
                          ) else (
                              
                                cts:and-query((
                                      $query,
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_sw')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_se')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_nw')),
                                      cts:box($south, $west, $north, $east)),
                                      cts:element-geospatial-query((fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','DISCOVERY_MESSAGE_BBOX_ne')),
                                      cts:box($south, $west, $north, $east))
                               ))    
                         ) 
                  
                         return concat(replace($bbox,'#',','),'ITEMSEP',xdmp:estimate(cts:search(doc(),$innerQuery,("unfiltered","score-simple"),0)))                     
                )
           )
      }
    </gs:Frequency>
};
 
declare function gs:frequency($query, $target, $limit,  $groupBy, $groupByVal, $groupByRange, $groupByRangeTarget)   
{
     <gs:Frequency target='{$target}'>   
        {          
             if (string-length ($groupBy) > 0 or string-length ($groupByRange) > 0) then (
             
                    let $innerQuery := if(string-length ($groupByVal) > 0) then (
                         
                         cts:and-query(($query, cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema', $groupBy), '=', $groupByVal)))
                    
                    )else(
                    
                         cts:and-query((
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'>=',
                                xs:long(tokenize($groupByRange,'#')[1]) ,("score-function=linear"),0.0), 
                              cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$groupByRangeTarget),'<=',
                                xs:long(tokenize($groupByRange,'#')[2]),("score-function=linear"),0.0),                   
                              $query))                  
                    )
                     
                    let $x := cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$target),(), 
                      ("fragment-frequency","frequency-order","descending",concat('limit=',$limit),"eager"), 
                      $innerQuery)
                      for $term in $x 
                      let $f := cts:frequency($term) 
                      where $term != '' and $f > 0
                      return concat(xdmp:url-encode($term),'ITEMSEP',$f) 
             )else(
             
                     let $x := cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema',$target),(), 
                      ("fragment-frequency","frequency-order","descending",concat('limit=',$limit),"eager"), $query)
                      for $term in $x 
                      let $f := cts:frequency($term) 
                      where $term != '' and $f > 0
                      return concat(xdmp:url-encode($term),'ITEMSEP',$f)           
             )
       }
     </gs:Frequency>
};

]]></module>/text()
 
return (     
    xdmp:document-insert($uri, $node, 
        xdmp:document-get-permissions($uri), 
        xdmp:document-get-collections($uri))
)