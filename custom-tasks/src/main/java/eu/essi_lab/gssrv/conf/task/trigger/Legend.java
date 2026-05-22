package eu.essi_lab.gssrv.conf.task.trigger;

import java.util.List;

public class Legend {

    String title;
    List<LegendItem> items;

    public Legend(String title, List<LegendItem> items) {
	this.title = title;
	this.items = items;
    }
}
