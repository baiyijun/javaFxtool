package org.baiyi.javaFxTool.services.business;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.baiyi.javaFxTool.model.business.Cs2JkMqPOJO;
import org.baiyi.javaFxTool.utils.business.SerializeUtil;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.HashMap;

/**
 * @author: bai
 * @Date: 2020/2/17 15:46
 * @Description:
 */
@Slf4j
@Service
public class MqServices {

    public void sendMq(String path, HashMap map) {


        String contextType = "CS_JK_OCPT_ParseFileSucc";
        String context = path;
        Cs2JkMqPOJO csjkocptParseJHFileSucc = new Cs2JkMqPOJO();
        csjkocptParseJHFileSucc.setCeid(context);
        String mar = "";

        String mqAddress = (String) map.get("mqAddress");
        String[] split = mqAddress.split(":");
        String host = split[0];
        String port = split[1];
        try {
            mar = SerializeUtil.marshal(csjkocptParseJHFileSucc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            log.info("mq消息发送开始,消息内容为-->{" + mar + "}");//10.231.199.150
            boolean qm_dtcp_gw = send(mar, contextType, host, port, "QM_DTCP_GW", "DTCP.JAVA.CHL", "DTCP.DICP.SED.QUEUE");
            if (qm_dtcp_gw) {
                log.info("mq消息发送成功");
            } else {
                log.error("mq消息发送失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean send(String context, String contextType, String hostName, String port, String queueManager, String channel, String sendQueue) {
        try {
            Connection conn = null;
            MQQueueConnectionFactory connFactory = new MQQueueConnectionFactory();
            connFactory.setTransportType(1);
            connFactory.setHostName(hostName);
            connFactory.setPort(Integer.parseInt(port));
            connFactory.setChannel(channel);
            connFactory.setQueueManager(queueManager);
            conn = connFactory.createConnection();
            conn.start();
            Session session = conn.createSession(true, 1);
            Destination destination = session.createQueue(sendQueue);
            MessageProducer msgProducer = session.createProducer(destination);
            msgProducer.setDeliveryMode(1);
            TextMessage txtMsg = session.createTextMessage(context);
            txtMsg.setStringProperty("UserDefineMessageType", contextType);
            msgProducer.send(txtMsg);
            session.commit();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
