package eu.floraresearch.drm.headermng;

import java.util.Calendar;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class NextDataHeaderManager implements HeaderManager {

    @Override
    // @formatter:off
    public String createHeader() {
	return "<table>"+
            	"<tr>"+
            	    "<td><a href=\"http://www.nextdataproject.it\" target=\"_blank\"><img src=\"img/NEXTDATA.png\" width='160' height='162'></img></a></td>"+
            	    "<td><a href=\"https://www.earthobservations.org/geoss.shtml\" target=\"_blank\"><img style=\"margin-left: 260px\" src=\"img/GEO_DAB.png\"></img></a></td>"+
            	"</tr>"+
            
            	"<tr>"+
            	    "<td><label style=\"font-size: 60px;\">Date " + ISO8601DateTimeUtils.getISO8601DateTime(Calendar.getInstance().getTime()) + "</label></td>"+
//		    "<td></td>"+
            	    "<td>"+
            	     "<a href=\"http://www.cineca.it/\" target=\"_blank\"><img style=\"margin-left: 470px; margin-top: -30px \" src=\"img/CINECA.png\"></img></a>"+
            	     "<a href=\"http://essi-lab.eu/nextdata/sosina\" target=\"_blank\"><img style=\"margin-left: 5px; margin-top: -30px\" src=\"img/SOSINA.png\"></img></a> "+
            	    
            	 "</tr>"+       	    
	"</table>";
    }
}
