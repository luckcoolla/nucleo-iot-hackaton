package by.iot.nucleo.spectre.getyoursensors.data;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import by.iot.nucleo.spectre.getyoursensors.R;

public class Sensor implements Serializable {
    @SerializedName("sensor_name") private String sensorName;
    @SerializedName("sensorType") private SensorType sensorType;
    @SerializedName("v") private float value;
    @SerializedName("vs") private SensorComplexValue[] values;
    @SerializedName("sensor_img_url") private String sensorImageUrl;

    public SensorComplexValue[] getValues ()
    {
        return values;
    }

    public void setValues (SensorComplexValue[] values)
    {
        this.values = values;
    }

    public String getSensorName()
    {
        return (sensorName == null)? "" : sensorName;
    }

    public void setSensorName(String sensorName)
    {
        this.sensorName = sensorName;
    }

    public SensorType getSensorType()
    {
        if (sensorType == null) {
            Log.e(Sensor.class.getName(), "sensorType is null");
            return SensorType.TEMPERATURE;
        } else {
            return sensorType;
        }
    }

    public void setSensorType(SensorType sensorType)
    {
        this.sensorType = sensorType;
    }

    public float getValue ()
    {
        return value;
    }

    public void setValue (float value)
    {
        this.value = value;
    }

    public String getSensorImageUrl() {
        return sensorImageUrl;
    }

    public int getDefaultSensorImageId() {
        return R.drawable.x_nucleo_iks01a1;
    }

    public void setSensorImageUrl(String sensorImageUrl) {
        this.sensorImageUrl = sensorImageUrl;
    }

    @Override
    public String toString()
    {
        return "ClassSensor [values.length = "+((values != null)?values.length:0)
                +", sensorName = "+ sensorName +", sensorType = "+ sensorType +", value = "+value+"]";
    }
}
