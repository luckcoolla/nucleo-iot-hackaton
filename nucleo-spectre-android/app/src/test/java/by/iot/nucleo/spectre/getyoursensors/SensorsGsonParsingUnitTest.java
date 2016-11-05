package by.iot.nucleo.spectre.getyoursensors;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import by.iot.nucleo.spectre.getyoursensors.model.Sensor;
import by.iot.nucleo.spectre.getyoursensors.model.SensorType;
import by.iot.nucleo.spectre.getyoursensors.service.SensorsDeserialiser;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP,
        manifest = "src/main/AndroidManifest.xml")
public class SensorsGsonParsingUnitTest {
    private final static String VALID_SENSORS_INPUT =
"[{\"sensorType\": \"temperature\",\"v\": 24.31},{\"sensorType\": \"humidity\",\"v\": 43.00},{\"sensorType\": \"pressure\",\"v\": 1002.76},{\"sensorType\": \"accelerometer\",\"vs\": [{\"name\": \"x\",\"v\": 22},{\"name\": \"y\",\"v\": -5},{\"name\": \"z\",\"v\": 995}]},{\"sensorType\": \"gyroscope\",\"vs\": [{\"name\": \"x\",\"v\": -1400},{\"name\": \"y\",\"v\": -630},{\"name\": \"z\",\"v\": 1120}]},{\"sensorType\": \"magnetometer\",\"vs\": [{\"name\": \"x\",\"v\": 356},{\"name\": \"y\",\"v\": -226},{\"name\": \"z\",\"v\": 330}]}]";

    private final static SensorType[] assertTypes = {SensorType.TEMPERATURE, SensorType.HUMIDITY, SensorType.PRESSURE,
            SensorType.ACCELEROMETER, SensorType.GYROSCOPE, SensorType.MAGNETOMETER};
    @Test
    public void sensors_gson_parsing_isCorrect() throws Exception {
        final Sensor[] sensors = SensorsDeserialiser.getSensors(VALID_SENSORS_INPUT);
        assertEquals(sensors.length, 6);
        //single value
        assertNotNull(sensors[0]);
        assertEquals(sensors[0].getSensorName(), null);
        assertEquals(sensors[0].getSensorType(), SensorType.TEMPERATURE);
        assertEquals(sensors[0].getValue(), 24.31f, 0.1f);
        assertEquals(sensors[0].getValues(), null);
        //values list
        //{"sensorType": "magnetometer","vs": [{"name": "x","v": 356},{"name": "y","v": -226},{"name": "z","v": 330}]}
        assertNotNull(sensors[5]);
        assertEquals(sensors[5].getSensorName(), null);
        assertEquals(sensors[5].getSensorType(), SensorType.MAGNETOMETER);
        assertEquals(sensors[5].getValue(), 0.0f, 0.1f);
        assertEquals(sensors[5].getValues().length, 3);
        assertEquals(sensors[5].getValues()[0].getValue(), 356f, 0.1f);
        assertEquals(sensors[5].getValues()[0].getType(), "x");
        assertEquals(sensors[5].getValues()[1].getValue(), -226f, 0.1f);
        assertEquals(sensors[5].getValues()[2].getValue(), 330f, 0.1f);
        //all types
        int i = 0;
        for (Sensor sensor: sensors) {
            assertEquals(sensor.getSensorType(), assertTypes[i++]);
        }
    }

    private final static String NON_VALID_GSON = "{\"Kirrik-ik-ik\":{\"ST\":\"Nucleo-IoT-mbed\",\"Temp\":24.11,\"Pressure\":1002.01,\"Humidity\":41.86,\"Gyroscope\":{\"x\":48510,\"y\":61040,\"z\":88690},\"Axelerometr\":{\"x\":-764,\"y\":673,\"z\":545},\"Magnetometr\":{\"x\":732,\"y\":-161,\"z\":713}}}";
    @Test
    /**
     * non-valid gson test
     */
    public void sensorFromStas() {
        final Sensor[] sensors = SensorsDeserialiser.getSensors(NON_VALID_GSON);
        assertEquals(sensors.length, 6);
    }
}