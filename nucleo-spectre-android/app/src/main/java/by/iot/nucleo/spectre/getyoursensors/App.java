package by.iot.nucleo.spectre.getyoursensors;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.iot.nucleo.spectre.getyoursensors.model.Sensor;
import by.iot.nucleo.spectre.getyoursensors.service.DataService;
import by.iot.nucleo.spectre.getyoursensors.service.MqttService;
import by.iot.nucleo.spectre.getyoursensors.service.SensorsDeserialiser;

public class App extends Application {
    private static final String TAG = App.class.getName();
    private static App instance;
    private static int uiCounter = 0;
    private static MqttService mqttService;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mqttService = new MqttService();
    }

    public static App getContext() {
        return instance;
    }

    public static void incrementUi() {
        ++uiCounter;
        Log.d(TAG, "---incrementUi(), uiCounter = " + uiCounter);
        if (uiCounter == 1) {
            mqttService.initMqtt();
            mqttService.connectToMqtt();
        }
    }

    public static void decrementUi() {
        Log.d(TAG, "---decrementUi(), uiCounter = " + uiCounter);
        if (uiCounter == 0) {
            mqttService.disconnectMqtt();
        }
    }
}
