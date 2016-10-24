package by.iot.nucleo.spectre.getyoursensors.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SensorComplexValue implements Serializable {
    @SerializedName("name") private String type;
    @SerializedName("v") private float value;

    public float getValue ()
    {
        return value;
    }

    public void setValue (float value)
    {
        this.value = value;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassSensorComplexValue [value = "+value+", type = "+type+"]";
    }
}
