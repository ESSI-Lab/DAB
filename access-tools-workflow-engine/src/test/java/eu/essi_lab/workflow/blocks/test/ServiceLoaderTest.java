package eu.essi_lab.workflow.blocks.test;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.NetCDFReprojector;
import eu.essi_lab.workflow.blocks.grid.NetCDF_To_GeoTIFF_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.NetCDF_To_PNG_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.PNG_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML11_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML20_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_OM2_FormatConverter;
import eu.essi_lab.workflow.builder.WorkblockBuilder;

public class ServiceLoaderTest {

    @Test
    public void test() {

	ServiceLoader<WorkblockBuilder> loader = ServiceLoader.load(WorkblockBuilder.class);

	String[] array = new String[] { //
		GeoTIFF_To_NetCDF_FormatConverter.class.getSimpleName(), //
		NetCDFReprojector.class.getSimpleName(), //
		NetCDF_4326_TimeSeries_TemporalSubsetter.class.getSimpleName(), //
		NetCDF_4326_TimeSeries_To_WML11_FormatConverter.class.getSimpleName(), //
		NetCDF_4326_TimeSeries_To_WML20_FormatConverter.class.getSimpleName(), //
		NetCDF_To_GeoTIFF_FormatConverter.class.getSimpleName(), //
		NetCDF_To_PNG_FormatConverter.class.getSimpleName(), //
		PNG_To_NetCDF_FormatConverter.class.getSimpleName(),//
		WML11_4326_TimeSeries_TemporalSubsetter.class.getSimpleName(), //
		WML11_4326_To_NetCDF_FormatConverter.class.getSimpleName(), //
		WML20_4326_To_NetCDF_FormatConverter.class.getSimpleName(), //
		WML20_4326_To_OM2_FormatConverter.class.getSimpleName()//
	};

	Arrays.sort(array);
	List<String> expected = Arrays.asList(array);

	List<String> found = StreamUtils.iteratorToStream(loader.iterator()).//
		map(b -> b.getClass().getSimpleName()).//
		sorted().//
		collect(Collectors.toList());

	Assert.assertTrue(found.containsAll(expected));
    }
}
