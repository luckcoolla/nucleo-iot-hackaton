/* SpwfInterface NetworkSocketAPI Example Program
 * Copyright (c) 2015 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "mbed.h"
#include "SpwfInterface.h"
#include "TCPSocket.h"
#include "MQTTClient.h"
#include "MQTTWiFi.h"
#include <ctype.h>
#include "x_nucleo_iks01a1.h"
#include "mbed.h"
#include "assert.h"
#include <Ticker.h>
//------------------------------------
// Hyperterminal configuration
// 9600 bauds, 8-bit data, no parity
//------------------------------------
Serial pc(SERIAL_TX, SERIAL_RX); 
SpwfSAInterface spwf(D8, D2, false);
bool quickstartMode = true;    

#define ORG_QUICKSTART  // comment to connect to play.internetofthings.ibmcloud.com
    
#define MQTT_MAX_PACKET_SIZE 500   
#define MQTT_MAX_PAYLOAD_SIZE 500 

 // Configuration values needed to connect to IBM IoT Cloud
#ifdef ORG_QUICKSTART
#define ORG "quickstart"     // connect to quickstart.internetofthings.ibmcloud.com/ For a registered connection, replace with your org 
#define ID ""
#define AUTH_TOKEN ""
#define DEFAULT_TYPE_NAME "iot-hackathon"
#else
#define ORG "play"             // connect to play.internetofthings.ibmcloud.com/ For a registered connection, replace with your org
#define ID "testnucleo2"       // For a registered connection, replace with your id
#define AUTH_TOKEN "centrallab"// For a registered connection, replace with your auth-token
#define DEFAULT_TYPE_NAME "sensor"
#endif

#define TYPE DEFAULT_TYPE_NAME       // For a registered connection, replace with your type
#define MQTT_PORT 1883
#define MQTT_TLS_PORT 8883
#define IBM_IOT_PORT MQTT_PORT
// WiFi network credential
#define SSID   "Headquarters2"  // Network must be visible otherwise it can't connect
#define PASSW  "play-basketball-4ever"
#define USERNAME "Kir"
//#warning "Wifi SSID & password empty"
#define APP_LOOP_PERIOD 3000 // in ms

#if defined(TARGET_STM)
#define LED_OFF (0)
#else
#define LED_OFF (1)
#endif
#define LED_ON  (!LED_OFF)

/*** Typedefs ----------------------------------------------------------------- ***/
typedef struct {
    int32_t AXIS_X;
    int32_t AXIS_Y;
    int32_t AXIS_Z;
} AxesRaw_TypeDef;
/*** Static variables --------------------------------------------------------- ***/
#ifdef DBG_MCU
/* betzw: enable debugging while using sleep modes */
#include "DbgMCU.h"
static DbgMCU enable_dbg;
#endif // DBG_MCU

static X_NUCLEO_IKS01A1 *mems_expansion_board = X_NUCLEO_IKS01A1::Instance();
static GyroSensor *gyroscope = mems_expansion_board->GetGyroscope();
static MotionSensor *accelerometer = mems_expansion_board->GetAccelerometer();
static MagneticSensor *magnetometer = mems_expansion_board->magnetometer;
static HumiditySensor *humidity_sensor = mems_expansion_board->ht_sensor;
static PressureSensor *pressure_sensor = mems_expansion_board->pt_sensor;
static TempSensor *temp_sensor1 = mems_expansion_board->ht_sensor;
static TempSensor *temp_sensor2 = mems_expansion_board->pt_sensor;

static Ticker ticker;
static DigitalOut myled(LED1, LED_OFF);

static volatile bool timer_irq_triggered = false;
static volatile bool ff_irq_triggered = false;

/*** Helper Functions -------------------------------------------------------- ***/
/* print floats & doubles */
static char *printDouble(char* str, double v, int decimalDigits=2)
{
  int i = 1;
  int intPart, fractPart;
  int len;
  char *ptr;

  /* prepare decimal digits multiplicator */
  for (;decimalDigits!=0; i*=10, decimalDigits--);

  /* calculate integer & fractinal parts */
  intPart = (int)v;
  fractPart = (int)((v-(double)(int)v)*i);

  /* fill in integer part */
  sprintf(str, "%i.", intPart);

  /* prepare fill in of fractional part */
  len = strlen(str);
  ptr = &str[len];

  /* fill in leading fractional zeros */
  for (i/=10;i>1; i/=10, ptr++) {
      if(fractPart >= i) break;
      *ptr = '0';
  }

  /* fill in (rest of) fractional part */
  sprintf(ptr, "%i", fractPart);

  return str;
}

    
char id[30] = ID;                 // mac without colons  
char org[12] = ORG;        
int connack_rc = 0; // MQTT connack return code
const char* ip_addr = "";
char* host_addr = "";
char type[30] = TYPE;
char auth_token[30] = AUTH_TOKEN; // Auth_token is only used in non-quickstart mode
bool netConnecting = false;
//int connectTimeout = 1000;
int connectTimeout = 10;
bool mqttConnecting = false;
bool netConnected = false;
bool connected = false;
int retryAttempt = 0;



int connect(MQTT::Client<MQTTWiFi, Countdown, MQTT_MAX_PACKET_SIZE>* client, MQTTWiFi* ipstack)
{   
    char* hostname = "128.199.46.150"; //"ip_server_mqtt";
    SpwfSAInterface& WiFi = ipstack->getWiFi();
    ip_addr = WiFi.get_ip_address();
    printf ("ID: %s\n\r", id);
    // Construct clientId - d:org:type:id
    char clientId[32];
    sprintf(clientId, "nucleo:%s", id);    
    // Network debug statements 
    LOG("=====================================\n\r");
    LOG("Connecting WiFi.\n\r");
    LOG("IP ADDRESS: %s\n\r", WiFi.get_ip_address());
    LOG("MAC ADDRESS: %s\n\r", WiFi.get_mac_address());
    LOG("Server Hostname: %s\n\r", hostname);
    LOG("Client ID: %s\n\r", clientId);
    LOG("=====================================\n\r");
    
    netConnecting = true;
    ipstack->open(&ipstack->getWiFi());
    int rc = ipstack->connect(hostname, IBM_IOT_PORT, connectTimeout);
    if (rc != 0)
    {
        WARN("IP Stack connect returned: %d\n", rc);    
        return rc;
    }
    netConnected = true;
    netConnecting = false;

    // MQTT Connect
    mqttConnecting = true;
    MQTTPacket_connectData data = MQTTPacket_connectData_initializer;
    data.MQTTVersion = 4;
    data.struct_version=0;
    data.clientID.cstring = clientId;
 
    if (!quickstartMode) 
    {        
        data.username.cstring = "use-token-auth";
        data.password.cstring = auth_token;
    }    
    if ((rc = client->connect(data)) == 0) 
    {       
        connected = true;
        printf ("--->Connected\n\r");
    }
    else {
        WARN("MQTT connect returned %d\n", rc);        
    }
    if (rc >= 0)
        connack_rc = rc;
    mqttConnecting = false;
    return rc;
}

int getConnTimeout(int attemptNumber)
{  // First 10 attempts try within 3 seconds, next 10 attempts retry after every 1 minute
   // after 20 attempts, retry every 10 minutes
    return (attemptNumber < 10) ? 3 : (attemptNumber < 20) ? 60 : 600;
}

void attemptConnect(MQTT::Client<MQTTWiFi, Countdown, MQTT_MAX_PACKET_SIZE>* client, MQTTWiFi* ipstack)
{
    connected = false;
           
    while (connect(client, ipstack) != MQTT_CONNECTION_ACCEPTED) 
    {    
        if (connack_rc == MQTT_NOT_AUTHORIZED || connack_rc == MQTT_BAD_USERNAME_OR_PASSWORD) {
            printf ("File: %s, Line: %d\n\r",__FILE__,__LINE__);        
            return; // don't reattempt to connect if credentials are wrong
        }          
        int timeout = getConnTimeout(++retryAttempt);
        WARN("Retry attempt number %d waiting %d\n", retryAttempt, timeout);
        
        // if ipstack and client were on the heap we could deconstruct and goto a label where they are constructed
        //  or maybe just add the proper members to do this disconnect and call attemptConnect(...)
        
        // this works - reset the system when the retry count gets to a threshold
        if (retryAttempt == 5)
            NVIC_SystemReset();
        else
            wait(timeout);
    }
}

int publish(MQTT::Client<MQTTWiFi, Countdown, MQTT_MAX_PACKET_SIZE>* client, MQTTWiFi* ipstack)
{
    float TEMPERATURE_Value;
    float HUMIDITY_Value;
    float PRESSURE_Value;
    float PRESSURE_Temp_Value;
    AxesRaw_TypeDef MAG_Value;
    AxesRaw_TypeDef ACC_Value;
    AxesRaw_TypeDef GYR_Value;
    unsigned int ret = 0;
    
    /* Switch LED On */
    myled = LED_ON;
    printf("===\n");

    /* Determine Environmental Values */
    ret |= (!CALL_METH(temp_sensor1, GetTemperature, &TEMPERATURE_Value, 0.0f) ? 0x0 : 0x1);
    ret |= (!CALL_METH(humidity_sensor, GetHumidity, &HUMIDITY_Value, 0.0f) ? 0x0 : 0x2);;
    ret |= (!CALL_METH(pressure_sensor, GetPressure, &PRESSURE_Value, 0.0f) ? 0x0 : 0x4);;
    ret |= (!CALL_METH(temp_sensor2, GetFahrenheit, &PRESSURE_Temp_Value, 0.0f) ? 0x0 : 0x8);;
    ret |= (!CALL_METH(magnetometer, Get_M_Axes, (int32_t *)&MAG_Value, 0) ? 0x0 : 0x10);;
    ret |= (!CALL_METH(accelerometer, Get_X_Axes, (int32_t *)&ACC_Value, 0) ? 0x0 : 0x20);;
    ret |= (!CALL_METH(gyroscope, Get_G_Axes, (int32_t *)&GYR_Value, 0) ? 0x0 : 0x40);
    
    /* Switch LED Off */
    myled = LED_OFF;
    
    MQTT::Message message;
    char* pubTopic = new char[32]();
    
    sprintf(pubTopic,"iot/%s/%s", USERNAME, id);
                
    char buf[MQTT_MAX_PAYLOAD_SIZE];
    char temp[16];
    char press[16];
    char hum[16];
    memset(temp, '\0', sizeof(char)*16);
    memset(press, '\0', sizeof(char)*16);
    memset(hum, '\0', sizeof(char)*16);
    printDouble(temp, TEMPERATURE_Value);
    printDouble(press, PRESSURE_Value);
    printDouble(hum, HUMIDITY_Value);
    sprintf(buf, "[{\"sensorType\": \"temperature\",\"v\": %s},{\"sensorType\": \"humidity\",\"v\": %s},{\"sensorType\": \"pressure\",\"v\": %s},{\"sensorType\": \"accelerometer\",\"vs\": [{\"name\": \"x\",\"v\": %ld},{\"name\": \"y\",\"v\": %ld},{\"name\": \"z\",\"v\": %ld}]},{\"sensorType\": \"gyroscope\",\"vs\": [{\"name\": \"x\",\"v\": %ld},{\"name\": \"y\",\"v\": %ld},{\"name\": \"z\",\"v\": %ld}]},{\"sensorType\": \"magnetometer\",\"vs\": [{\"name\": \"x\",\"v\": %ld},{\"name\": \"y\",\"v\": %ld},{\"name\": \"z\",\"v\": %ld}]}]",
        temp,
        hum,
        press,
        ACC_Value.AXIS_X, ACC_Value.AXIS_Y, ACC_Value.AXIS_Z,
        GYR_Value.AXIS_X, GYR_Value.AXIS_Y, GYR_Value.AXIS_Z,
        MAG_Value.AXIS_X, MAG_Value.AXIS_Y, MAG_Value.AXIS_Z);
    message.qos = MQTT::QOS0;
    message.retained = false;
    message.dup = false;
    message.payload = (void*)buf;
    message.payloadlen = strlen(buf);
    LOG("Publishing %s\n", buf);
    return client->publish(pubTopic, message);
} 
    
int main()
{
    const char * ssid = SSID; // Network must be visible otherwise it can't connect
    const char * seckey = PASSW;
    
   
    pc.printf("\r\nX-NUCLEO-IDW01M1 mbed Application\r\n");     
    pc.printf("connecting to AP\r\n");            

    quickstartMode=false;
    if (strcmp(org, "quickstart") == 0){quickstartMode = true;}
    MQTTWiFi ipstack(spwf, ssid, seckey, NSAPI_SECURITY_WPA2);
    MQTT::Client<MQTTWiFi, Countdown, MQTT_MAX_PACKET_SIZE> client(ipstack);
    if (quickstartMode) {
        pc.printf("*** quickstart = true ***\r\n");
        char mac[50];
        char *digit=NULL;
        sprintf (id,"%s", "");                
        sprintf (mac,"%s",ipstack.getWiFi().get_mac_address()); 
        strcpy (mac, ipstack.getWiFi().get_mac_address());
        digit = strtok (mac,":");
        while (digit != NULL)
        {
            strcat (id, digit);
            digit = strtok (NULL, ":");
        }                
   }
   pc.printf("*** attempting to connect ***\r\n");
   attemptConnect(&client, &ipstack);
   if (connack_rc == MQTT_NOT_AUTHORIZED || connack_rc == MQTT_BAD_USERNAME_OR_PASSWORD)    
   {
        pc.printf("*** connack_rc is bad, mat' ego tak ***\r\n");
        while (true)
        wait(1.0); // Permanent failures - don't retry
   }
   int count = 0;    
    while (true)
    {
        if (++count == /*100*/2)
        {               // Publish a message every second
            if (publish(&client, &ipstack) != 0) 
                attemptConnect(&client, &ipstack);   // if we have lost the connection
            count = 0;
        }        
        client.yield(/*10*/1);  // allow the MQTT client to receive messages
    }
}

