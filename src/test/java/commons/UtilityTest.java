package commons;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilityTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void distanceConvertTest()
	{
		float lat1 = 10, lat = 12, lon1 = 0, lon2 = 2;
		OwnMethods.Print("Distance:" + Utility.distFrom(lat1, lon1, lat, lon2));
	}
	
	@Test
	public void distanceTest()
	{
//		double x = 0, y = 0;
		double x = 1.5, y = 1.5;
		MyRectangle rectangle = new MyRectangle(1, 1, 2, 2);
		OwnMethods.Print(Utility.distance(x, y, rectangle));
	}
}