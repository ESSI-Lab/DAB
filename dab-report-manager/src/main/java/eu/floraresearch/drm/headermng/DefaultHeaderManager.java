package eu.floraresearch.drm.headermng;

import java.util.Calendar;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class DefaultHeaderManager implements HeaderManager {

    @Override
    // @formatter:off
    public String createHeader() {
	return "<table>"+
            	"<tr>"+
            	    "<td><a href=\"http://www.earthobservations.org/\" target=\"_blank\"><img src=\"img/GEO.png\"></img></a></td>"+
            	
            	    "<td><a href=\"http://www.iia.cnr.it/\" target=\"_blank\"><img style=\"width:100px; margin-left: 5px;\" src=\"http://www.iia.cnr.it/wp-content/uploads/2015/12/cropped-logo_IIA-1.png\"></img></a></td>"+
            	    
            	    "<td><a href=\"http://www.esa.int/ESA\" target=\"_blank\"><img style=\"width:200px; margin-left: -5px;\" src=\"img/esa.jpg\"></img></a></td>"+

            	    "<td><a href=\"https://www.earthobservations.org/geoss.shtml\" target=\"_blank\"><img style=\"margin-left: 200px\" src=\"img/geodab-logo-2.jpg\"></img></a></td>"+
            	"</tr>"+
            
            	"<tr>"+
            	    "<td><label style=\"font-size: 60px;\">Date " + ISO8601DateTimeUtils.getISO8601DateTime(Calendar.getInstance().getTime()) + "</label></td>"+
		    "<td></td>"+
		    "<td></td>"+
            	    "<td>"+
           	     "<a href=\"https://www.uos-firenze.iia.cnr.it/\" target=\"_blank\"><img style=\"margin-left: 200px;\" src=\"img/essi-lab-logo.png\"></img></a>"+

            	     "<a href=\"http://ec.europa.eu/dgs/jrc/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/JRC.png\"></img></a>"+
             	     "<a href=\"http://www.usgs.gov/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/USGS.png\"></img></a> "+
            	     "<a href=\"http://www.u-tokyo.ac.jp/en/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/TOKIO.png\"></img></a> "+
             	 "</tr>"+       	    
	"</table>";
    }
}
