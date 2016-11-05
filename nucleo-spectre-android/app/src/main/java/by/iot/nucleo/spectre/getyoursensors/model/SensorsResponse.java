package by.iot.nucleo.spectre.getyoursensors.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/*
 * Response example:
    [
      {
         "sensor_name":"Temperature in the bedroom",
         "sensor_type":"temperature",
         "values":[

         ],
         "value":42.0
      },
      {
         "sensor_name":"Humidity in the bedroom",
         "sensor_type":"humidity",
         "values":[

         ],
         "value":42.0
      },
      {
         "sensor_name":"Pressure in the bedroom",
         "sensor_type":"pressure",
         "values":[

         ],
         "value":42.0
      },
      {
         "sensor_name":"Accelerometer in the bedroom",
         "sensor_type":"accelerometer",
         "values":[
            {
               "type":"x",
               "value":0.0
            },
            {
               "type":"y",
               "value":1.0
            },
            {
               "type":"z",
               "value":-1.0
            }
         ],
         "value":0.0
      },
      {
         "sensor_name":"Gyroscope in the bedroom",
         "sensor_type":"gyroscope",
         "values":[
            {
               "type":"x",
               "value":0.0
            },
            {
               "type":"y",
               "value":1.0
            },
            {
               "type":"z",
               "value":-1.0
            }
         ],
         "value":0.0
      },
      {
         "sensor_name":"Magnetometer in the bedroom",
         "sensor_type":"magnetometer",
         "values":[
            {
               "type":"x",
               "value":0.0
            },
            {
               "type":"y",
               "value":1.0
            },
            {
               "type":"z",
               "value":-1.0
            }
         ],
         "value":0.0
      }
   ]
 */
@Deprecated //since we don't need timestamp
public class SensorsResponse implements Serializable {
    //@SerializedName("timestamp") private Date timestamp = new Date(0);
    @SerializedName("sensors") private Sensor[] sensors;

    public Sensor[] getSensors ()
    {
        return sensors;
    }

    public void setSensors (Sensor[] sensors)
    {
        this.sensors = sensors;
    }

//    public Date getTimestamp()
//    {
//        return timestamp;
//    }
//
//    public void setTimestamp(Date timestamp)
//    {
//        this.timestamp = timestamp;
//    }

    @Override
    public String toString()
    {
        return "ClassSensorsResponse [sensors.length = "+((sensors == null)?0:sensors.length)
//                +", timestamp = "+ timestamp
                +"]";
    }
}
