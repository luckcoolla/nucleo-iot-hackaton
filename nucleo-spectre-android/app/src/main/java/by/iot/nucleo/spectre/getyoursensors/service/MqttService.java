package by.iot.nucleo.spectre.getyoursensors.service;

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

import by.iot.nucleo.spectre.getyoursensors.App;
import by.iot.nucleo.spectre.getyoursensors.Settings;
import by.iot.nucleo.spectre.getyoursensors.model.Sensor;

public class MqttService {
    private static final String TAG = MqttService.class.getName();
    private MqttAndroidClient mqttAndroidClient;
    private static Map<String, List<MqttListener>> mqttListeners = new HashMap<>();

    public static void addListener(String topic, MqttListener listener) {
        List<MqttListener> mqttListeners = MqttService.mqttListeners.get(topic);
        if (mqttListeners == null) {
            mqttListeners = new ArrayList<>();
        }
        mqttListeners.add(listener);
        MqttService.mqttListeners.put(topic, mqttListeners);
    }

    public static void removeListener(String topic, MqttListener listener) {
        List<MqttListener> mqttListeners = MqttService.mqttListeners.get(topic);
        if (mqttListeners == null) {
            mqttListeners = new ArrayList<>();
        }
        mqttListeners.remove(listener);
        MqttService.mqttListeners.put(topic, mqttListeners);
    }

    public void disconnectMqtt() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException exception) {
            Log.e(TAG, "Failed to disconnect to: "+ Settings.MQTT_SERVER_URL, exception);
        }
    }

    public void connectToMqtt() {
        //reconnect options
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to: "+Settings.MQTT_SERVER_URL, exception);
                    Toast.makeText(App.getContext(), "Failed to connect to: Sensors DataBase ", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void initMqtt() {
        String clientId = DataService.getInstance().getUserName() + "; " + Build.MANUFACTURER + " " + Build.MODEL
                + "time = "+ System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(App.getContext(), Settings.MQTT_SERVER_URL, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "Reconnected to : "+Settings.MQTT_SERVER_URL);
                    Toast.makeText(App.getContext(), "Reconnected to : Sensors DataBase", Toast.LENGTH_SHORT).show();
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    Log.d(TAG, "Connected to : "+Settings.MQTT_SERVER_URL);
                    Toast.makeText(App.getContext(), "Connected to : Sensors DataBase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost.");
                Toast.makeText(App.getContext(), "The Connection with Sensors DataBase was lost.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(Settings.getSubscribtionTopic(), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed! on topic : " + Settings.getSubscribtionTopic());
                    Toast.makeText(App.getContext(), "Subscribed for the Sensors DataBase updates!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(App.getContext(), "Failed to subscribe for the Sensors DataBase updates", Toast.LENGTH_SHORT).show();
                }
            });
            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(Settings.getSubscribtionTopic(), 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Log.d(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
                    onMessageArrived(topic, message);
                }
            });
        } catch (MqttException ex){
            Log.e(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void onMessageArrived(String topic, MqttMessage mqttMessage) {
//        String boardId = null;
//        //topic parsing: topic example "/iot/userName/deviceIdMacAddress"
//        if (topic.contains(Settings.USER_NAME)) {
//            String[] topicParts = topic.split("/");
//            if (topicParts.length > 0) {
//                boardId = topicParts[topicParts.length - 1];
//            }
//        }
        //message parsing
//        if (boardId != null) {
        try {
            String message = new String(mqttMessage.getPayload());
            Sensor[] sensors = SensorsDeserialiser.getSensors(message);
            if (sensors != null && sensors.length > 0) {
                List<MqttListener> mqttListeners = MqttService.mqttListeners.get(topic);
                if (mqttListeners != null) {
                    for (MqttListener listener : mqttListeners) {
                        listener.onMessageArrived(topic, sensors);
                    }
                } else {
                    Log.w(TAG, "onMessageArrived() There are no listeners for topic : " + topic);
                }
            } else {
                Log.e(TAG, "onMessageArrived() parsing: empty values array");
            }
        } catch (Exception e) {
            Log.e(TAG, "onMessageArrived() parsing failed: message has not been parsed!", e);
        }
//        } else {
//            Log.e(TAG, "onMessageArrived() parsing failed: boardId has not been parsed!");
//        }
    }

    public interface MqttListener {
        void onMessageArrived(String topic, Sensor[] mqttMessage);
    }
}
