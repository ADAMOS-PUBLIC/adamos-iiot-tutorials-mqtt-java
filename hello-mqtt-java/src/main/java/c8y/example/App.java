package c8y.example;

import org.eclipse.paho.client.mqttv3.*;
import java.util.concurrent.*;

public class App {

    public static void main(String[] args) throws Exception {
        final String clientId = "TestID";
        final String serverURI = "tcp://mqtt.adamos-dev.com:1883";

        //configure MQTT connection
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("tenant/username");
        options.setPassword("password".toCharArray());
        final MqttClient client = new MqttClient(serverURI, clientId, null);

        //connect to the Cumulocity
        client.connect(options);

        //create device
        client.publish("s/us", "100,My Java MQTT device,c8y_MQTTDevice".getBytes(), 2, false);

        //set hardware information
        client.publish("s/us", "110,serialNumber123456,MQTT Java test model,Rev0.1".getBytes(), 2, false);

        //listen for operation
        client.subscribe("s/ds", new IMqttMessageListener() {
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                final String payload = new String(message.getPayload());
                System.out.println("Received operation " + payload);
                if (payload.startsWith("510")) {
                    System.out.println("Simulating device restart...");
                    client.publish("s/us", "501,c8y_Restart".getBytes(), 2, false);
                    System.out.println("...restarting...");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    client.publish("s/us", "503,c8y_Restart".getBytes(), 2, false);
                    System.out.println("...done...");
                }
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    //send temperature measurement
                    client.publish("s/us", new MqttMessage("211,25".getBytes()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 3, TimeUnit.SECONDS);
    }
}