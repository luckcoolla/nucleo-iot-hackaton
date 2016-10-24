package by.iot.nucleo.spectre.getyoursensors;

import by.iot.nucleo.spectre.getyoursensors.service.DataService;

public final class Settings {
    public final static String MQTT_SERVER_URL = BuildConfig.MQTT_SERVER_URL;
    public final static String BACKEND_URL = BuildConfig.BACKEND_SERVER_URL;
    public static String getSubscribtionTopic() {
        return "iot/" + DataService.getInstance().getUserName() + "/#";
    }
}
