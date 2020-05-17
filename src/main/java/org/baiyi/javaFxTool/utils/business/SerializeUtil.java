package org.baiyi.javaFxTool.utils.business;

import com.sun.xml.internal.bind.api.JAXBRIContext;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author: bai
 * @Date: 2020/2/17 16:36
 * @Description:
 */
public class SerializeUtil {
    public static String marshal(Object obj) throws JAXBException {
        JAXBContext jaxbContext=JAXBContextHolder.getJAXBContext();
        Marshaller marshaller=jaxbContext.createMarshaller();
        JAXBElement<?> jaxbElement=convertToJaxbElement(obj);
        StringWriter stringWriter=new StringWriter();
        marshaller.marshal(jaxbElement, stringWriter);
        String str=stringWriter.toString();
        return str;
    }

    public static Object unmarshal(String xmltest) throws JAXBException{
        StringReader stringReader=new StringReader(xmltest);
        JAXBContext jaxbContext=JAXBContextHolder.getJAXBContext();
        Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
        Object object=unmarshaller.unmarshal(stringReader);
        return convertToPlainObj(object);
    }

    private static JAXBElement<?> convertToJaxbElement(Object obj) {
        if(obj instanceof JAXBElement){
            return (JAXBElement<?>) obj;
        } else {
            JAXBRIContext context = (JAXBRIContext) JAXBContextHolder.getJAXBContext();
            QName qname = context.getRuntimeTypeInfoSet().getClassInfo(obj.getClass()).getTypeName();
            JAXBElement<?> element = new JAXBElement(qname, obj.getClass(), obj);
            return element;
        }
    }

    private static Object convertToPlainObj(Object obj) {
        if(obj instanceof JAXBElement){
            return ((JAXBElement<?>) obj).getValue();
        } else {
            return obj;
        }
    }
}
