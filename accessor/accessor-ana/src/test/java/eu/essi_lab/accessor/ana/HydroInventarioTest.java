package eu.essi_lab.accessor.ana;

import java.io.InputStream;

import org.junit.Test;

public class HydroInventarioTest {

    @Test
    public void test() throws Exception {
	InputStream stream = HydroInventarioTest.class.getClassLoader().getResourceAsStream("hydroInventario-telemetry.xml");
	HydroInventario inventario = new HydroInventario(stream);
	inventario.test();
    }

}
