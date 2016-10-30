package by.iot.nucleo.spectre.getyoursensors.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import by.iot.nucleo.spectre.getyoursensors.data.Sensor;

public class SensorsDeserialiser {
    public static Sensor[] getSensors(String gsonInput) {
        final Gson gson = new Gson();
        final Type sensorsType = new TypeToken<Sensor[]>(){}.getType();
        return gson.fromJson(gsonInput, sensorsType);
    }
}
