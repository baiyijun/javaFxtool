package org.baiyi.javaFxTool.utils.business;

import org.baiyi.javaFxTool.model.business.Cs2JkMqPOJO;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author: bai
 * @Date: 2020/2/17 16:39
 * @Description:
 */

@XmlRegistry
public class ObjectFactory {

    private final static QName QNAME = new QName("http://com.chinalife/pension/jhjk", "CS_JK_OCPT_ParseJHFileSucc");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sinosoft.ocpt.jaxb.xml
     */
    public ObjectFactory() {
    }


    public Cs2JkMqPOJO Cs2JkMqPOJO() {
        return new Cs2JkMqPOJO();
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CSJKOCPTParseJHFileSucc }{@code >}}
     */
    @XmlElementDecl(namespace = "http://com.chinalife/pension/jhjk", name = "CS_JK_OCPT_ParseJHFileSucc")
    public JAXBElement<Cs2JkMqPOJO> createCSJKOCPTParseJHFileSucc(Cs2JkMqPOJO value) {
        return new JAXBElement<Cs2JkMqPOJO>(QNAME, Cs2JkMqPOJO.class, null, value);
    }


}
