package eu.essi_lab.worflow.check.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.NetCDF_To_GeoTIFF_FormatConverter;
import eu.essi_lab.workflow.blocks.test.NetCDF3_4326_To_3857_CRSConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;

/**
 * @author Fabrizio
 */
public class WorkflowDataFormatAndCRSCheckTest {

    /**
     * This check succeeds.
     * From:
     * GEOTIFF
     * 4326
     * To:
     * NETCDF3
     * 3857
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test1() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new NetCDF3_4326_To_3857_CRSConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertTrue(check);
    }

    /**
     * This check MUST fail because GDALGeoTIFFToNetCDFClassicBuilder
     * do no touch the CRS, and the NetCDFClassic4326To3857Builder do not accept a 3857
     * From:
     * GEOTIFF
     * 3857
     * To:
     * NETCDF3
     * 3857
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test2() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new NetCDF3_4326_To_3857_CRSConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_3857());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check MUST fail because GDALGeoTIFFToNetCDFClassicBuilder
     * do no touch the CRS it is the last (and only block) and the target is 3857
     * From:
     * GEOTIFF
     * 4326
     * To:
     * NETCDF3
     * 3857
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test3() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check MUST fail because GDALGeoTIFFToNetCDFClassicBuilder
     * is the last (and only block) and it can make format transformation but only to NETCDF 3
     * but the target is GML_3_1
     * From:
     * GEOTIFF
     * 4326
     * To:
     * GML_3_1
     * EPSG_4326
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test4() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_4326());
	targetDescriptor.setDataFormat(DataFormat.GML_3_1());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check FAILS because GDALNetCDFClassicToGeoTIFF can transform from NETCDF3
     * to GEOTIFF, but NetCDFClassic4326To3857 do not support GEOTIFF. Since it supports NETCDF3
     * and the init format is NETCDF3, GDALNetCDFClassicToGeoTIFF do not alter the format. Than,
     * since the target format is GEOTIFF and NetCDFClassic4326To3857 cannot transform the format,
     * the check fails
     * From:
     * NETCDF3
     * 4326
     * To:
     * NETCDF3
     * 3857
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test5() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms NETCDF3 in 4326 to GEOTIFF in 4326
	Workblock blocks1 = new NetCDF_To_GeoTIFF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new NetCDF3_4326_To_3857_CRSConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.NETCDF_3());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check FAILS because GDALGeoTIFFToNetCDFClassic can transform from GEOTIFF
     * to NETCDF, but the target is GEOTIFF and the init is GEOTIFF.
     * NetCDFClassic4326To3857 do not support GEOTIFF, so GDALGeoTIFFToNetCDFClassic is forced
     * to transform to NETCDF3 in order to be compliant with NetCDFClassic4326To3857.
     * Than, since the target format is GEOTIFF and NetCDFClassic4326To3857 cannot transform the format,
     * the check fails
     * From:
     * IMAGE_GEOTIFF
     * 4326
     * To:
     * IMAGE_GEOTIFF
     * 3857
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test6() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new NetCDF3_4326_To_3857_CRSConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check FAILS because GDALGeoTIFFToNetCDFClassic can NOT transform from GEOTIFF
     * to GML_3_1, but it can transform to NETCDF3 which is also accepted by NetCDFClassic4326To3857,
     * to it makes the transformation.
     * Than, since the target format is GML_3_1 and NetCDFClassic4326To3857 cannot transform the format,
     * the check fails
     * From:
     * IMAGE_GEOTIFF
     * 4326
     * To:
     * GML_3_1
     * 3857
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test7() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new NetCDF3_4326_To_3857_CRSConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.GML_3_1());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

    /**
     * This check FAILS because GDALGeoTIFFToNetCDFClassic can NOT transform from GRID to
     * TIME_SERIES which is the data type required by Waterml20ToNetCDFClassic
     * From:
     * GRID
     * IMAGE_GEOTIFF
     * 4326
     * To:
     * TIME_SERIES
     * NETCDF_3
     * 4326
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test8() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a WATERML in 4326 to a NETCDF3 in 3857
	Workblock blocks2 = new WML20_4326_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks2);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_4326());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }
}
