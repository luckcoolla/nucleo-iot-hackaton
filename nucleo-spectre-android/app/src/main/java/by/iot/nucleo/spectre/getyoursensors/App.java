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

import by.iot.nucleo.spectre.getyoursensors.data.Sensor;
import by.iot.nucleo.spectre.getyoursensors.service.DataService;
import by.iot.nucleo.spectre.getyoursensors.service.SensorsDeserialiser;

public class App extends Application {
    private static final String TAG = App.class.getName();
    private static App instance;
    private MqttAndroidClient mqttAndroidClient;
    private static int uiCounter = 0;
    private static Map<String, List<MqttListener>> mqttListeners = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getContext() {
        return instance;
    }

    public static void incrementUi() {
        ++uiCounter;
        Log.d(TAG, "---incrementUi(), uiCounter = " + uiCounter);
        if (uiCounter == 1) {
            getContext().initMqtt();
            getContext().connectToMqtt();
        }
    }

    public static void decrementUi() {
        Log.d(TAG, "---decrementUi(), uiCounter = " + uiCounter);
        if (uiCounter == 0) {
            getContext().disconnectMqtt();
        }
    }

    public static void addListener(String topic, MqttListener listener) {
        List<MqttListener> mqttListeners = App.mqttListeners.get(topic);
        if (mqttListeners == null) {
            mqttListeners = new ArrayList<>();
        }
        mqttListeners.add(listener);
        App.mqttListeners.put(topic, mqttListeners);
    }

    public static void removeListener(String topic, MqttListener listener) {
        List<MqttListener> mqttListeners = App.mqttListeners.get(topic);
        if (mqttListeners == null) {
            mqttListeners = new ArrayList<>();
        }
        mqttListeners.remove(listener);
        App.mqttListeners.put(topic, mqttListeners);
    }

    private void disconnectMqtt() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException exception) {
            Log.e(TAG, "Failed to disconnect to: "+Settings.MQTT_SERVER_URL, exception);
        }
    }

    protected void connectToMqtt() {
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
                    Toast.makeText(getContext(), "Failed to connect to: Sensors DataBase ", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    protected void initMqtt() {
        String clientId = DataService.getInstance().getUserName() + "; " + Build.MANUFACTURER + " " + Build.MODEL
                + "time = "+ System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), Settings.MQTT_SERVER_URL, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "Reconnected to : "+Settings.MQTT_SERVER_URL);
                    Toast.makeText(getContext(), "Reconnected to : Sensors DataBase", Toast.LENGTH_SHORT).show();
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    Log.d(TAG, "Connected to : "+Settings.MQTT_SERVER_URL);
                    Toast.makeText(getContext(), "Connected to : Sensors DataBase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "The Connection was lost.");
                Toast.makeText(getContext(), "The Connection with Sensors DataBase was lost.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Subscribed for the Sensors DataBase updates!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getContext(), "Failed to subscribe for the Sensors DataBase updates", Toast.LENGTH_SHORT).show();
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
                    List<MqttListener> mqttListeners = App.mqttListeners.get(topic);
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
