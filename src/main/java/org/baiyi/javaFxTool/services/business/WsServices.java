package org.baiyi.javaFxTool.services.business;

import cn.hutool.http.webservice.SoapClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author: bai
 * @Date: 2020/2/17 15:23
 * @Description:
 */
@Slf4j
@Service
public class WsServices {

    HashMap<String, String> map = new HashMap<>();


    public void parseXML(String filepath, HashMap<String, String> map) {
        String sendcode = "";
        String workflowno = "";
        String buscode = "";
        String filename = "";
        byte[] bytes = null;
        try {
            log.info("开始读取xml");
            bytes = FileUtils.readFileToByteArray(new File(filepath));
            Document rootDoc = null;
            //判断解析是否错误
            SAXReader reader = new SAXReader();
            rootDoc = reader.read(new ByteArrayInputStream(bytes));
            Element rootEle;
            rootEle = rootDoc.getRootElement();
            Iterator<Element> iteEle = rootEle.elementIterator();
            while (iteEle.hasNext()) {
                Element rowOneEle = iteEle.next();
                String tagName = rowOneEle.getName() == null ? "" : rowOneEle.getName().toUpperCase();
                if ("SENDCODE".equals(tagName)) {
                    sendcode = rowOneEle.getText();
                } else if ("PENSIONINFO".equals(tagName)) {
                    Iterator<Element> elementAppno = rowOneEle.elementIterator();
                    while (elementAppno.hasNext()) {
                        Element eletAppno = elementAppno.next();
                        String tagNameAppno = eletAppno.getName();//拿到标签
                        if (tagNameAppno.equalsIgnoreCase("appseriono")) {
                            workflowno = eletAppno.getText();
                        } else if (tagNameAppno.equalsIgnoreCase("filename")) {
                            filename = eletAppno.getText();
                        }
                    }
                } else if ("DATASETTYPE".equals(tagName)) {
                    Iterator<Element> elementAppno = rowOneEle.elementIterator();
                    while (elementAppno.hasNext()) {
                        Element eletAppno = elementAppno.next();
                        String tagNameAppno = eletAppno.getName();//拿到标签
                        if (tagNameAppno.equalsIgnoreCase("typecode")) {
                            buscode = eletAppno.getText();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("文件读取失败");
            ex.printStackTrace();
        }
        try {
            String xml = "";
            if (sendcode.equals("S31") || sendcode.equals("S36")) {
                xml = new String(bytes, "gbk");
            } else {
                xml = new String(bytes, "utf-8");
            }
            //清楚重复数据
            deleteTable(buscode, workflowno);

            //报文重发
            invoke(sendcode, xml);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 数据清理
     *
     * @param typecode
     * @param workflowno
     */
    public void deleteTable(String typecode, String workflowno) {
        String drvie = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:thin:@" + map.get("dbAddress") + "/" + map.get("dbServer");
        String username = map.get("dbUser");
        String password = map.get("dbPass");
        try {
            Class.forName(drvie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            String sql = "";
            sql = "delete from csip_oa_trade_log aa where aa.refno = '" + workflowno + "'";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            switch (typecode) {
                case "1101": {
                    sql = "delete from op_oi_plan aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1102": {
                    sql = "delete from op_oi_invst aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1103": {
                    sql = "delete from op_oi_manager aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1104": {
                    sql = "delete from op_oi_priceday aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1201": {
                    sql = "delete from OP_OI_CONTRIBUTIONPOOL aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    sql = "delete from OP_OI_ContributionDetail aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1212": {
                    sql = "delete from OP_OI_COLLECTDETAIL aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1290": {
                    sql = "delete from op_oi_unitnet aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
                case "1205": {
                    sql = "delete from OP_IO_TASKFILE aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    sql = "delete from OP_IO_PAYLIST aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    sql = "delete from OP_IO_FILESUMXML aa where aa.workflowno = '" + workflowno + "'";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.execute();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void invoke(String managercode, String xml) throws Exception {
        HashMap<String, Object> wsmap = new HashMap<>();
        wsmap.put("xml", xml);
        wsmap.put("managercode", managercode);
        SoapClient wsAddress = SoapClient.create(map.get("wsAddress"))
                .setMethod("web:receiveOARequestFromTransferResponse", "http://ws.account.oa.dicp.org/")
                .setParam("requests", wsmap);
        String send = wsAddress.send(true);
        log.info(send);

    }
}
