xquery version "1.0-ml";
import module namespace trgr="http://marklogic.com/xdmp/triggers" 
   at "/MarkLogic/triggers.xqy";

trgr:create-trigger(
  
  "reg-queries-trigger", 
  "Registered queries trigger", 
  
  trgr:trigger-data-event(
      trgr:document-scope("/reg-queries/reg-queries-doc.xml"),
      trgr:document-content("modify"),
      trgr:pre-commit()),
      
  trgr:trigger-module(xdmp:database('DB-NAME'), 
                      "/gs-modules/", 
                      "reg-queries-module.xqy"), 
  fn:true(),
  xdmp:default-permissions() 
)