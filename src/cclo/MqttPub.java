package cclo;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author cclo
 */
public class MqttPub {

    String topic = "SoundEvent";
    String content = "MQTT Msg!";
    int qos = 2;
    String broker = "tcp://localhost:1883";
    //String broker = "tcp://iot.eclipse.org:1883";
    String clientId = "CCLo";
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient sampleClient;
    Main pMain;
    public boolean connected = false;

    public MqttPub(Main main_) {
        pMain = main_;
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("嘗試連接到 MQTT Broker: " + broker);
            // pMain.toaster.showToaster("嘗試連接到 MQTT Broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("已經連接到 MQTT Broker: " + broker);
            // pMain.toaster.showToaster("MQTT 已連接");
            // pMain.toaster.showToaster("MQTT 傳送訊息: " + content);
            connected = true;
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            // pMain.toaster.showToaster("訊息已經傳送");
            System.out.println("Message Sent!");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            // me.printStackTrace();
        }

    }

    public void publish(String str_) {
        MqttMessage message = new MqttMessage(str_.getBytes());
        message.setQos(qos);
        try {
            sampleClient.publish(topic, message);
        } catch (Exception e) {
            System.out.println("MQTT 發佈失敗！");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            sampleClient.disconnect();
            System.out.println("MQTT 已中斷");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
