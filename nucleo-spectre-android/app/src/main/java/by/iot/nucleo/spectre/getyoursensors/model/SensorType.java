package by.iot.nucleo.spectre.getyoursensors.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import by.iot.nucleo.spectre.getyoursensors.App;
import by.iot.nucleo.spectre.getyoursensors.R;

public enum SensorType implements Serializable {
    @SerializedName("accelerometer") ACCELEROMETER(SensorTypeValues.XYZ, new int[]{R.color.color_acc_x, R.color.color_acc_y, R.color.color_acc_z}),
    @SerializedName("gyroscope") GYROSCOPE(SensorTypeValues.XYZ, new int[]{R.color.color_gyr_x, R.color.color_gyr_y, R.color.color_gyr_z}),
    @SerializedName("magnetometer") MAGNETOMETER(SensorTypeValues.XYZ, new int[]{R.color.color_mag_x, R.color.color_mag_y, R.color.color_mag_z}),
    @SerializedName("humidity") HUMIDITY(SensorTypeValues.XY, new int[]{R.color.color_hum}),
    @SerializedName("pressure") PRESSURE(SensorTypeValues.XY, 900f, 1100f, new int[]{R.color.color_pressure}),
    @SerializedName("temperature") TEMPERATURE(SensorTypeValues.XY, new int[]{R.color.color_temp});

    private final SensorTypeValues valuesCount;
    private final float minValue;
    private final float maxValue;
    private final int[] colors;

    SensorType(SensorTypeValues valuesCount, int[] colors) {
        this(valuesCount, 0f, 100f, colors);
    }


    SensorType(SensorTypeValues valuesCount, float minValue, float maxValue, int[] colors) {
        this.valuesCount = valuesCount;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.colors = colors;
    }

    public SensorTypeValues getValuesCount() {
        return valuesCount;
    }

    public int[] getColors() {
        int[] colors = new int[this.colors.length];
        int i = 0;
        for (Integer colorId : this.colors) {
            colors[i++] = App.getContext().getResources().getColor(colorId);
        }
        return colors;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public enum SensorTypeValues {
        XY, XYZ
    }
}
