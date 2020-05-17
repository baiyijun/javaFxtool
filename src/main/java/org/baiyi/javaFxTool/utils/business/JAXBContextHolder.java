package org.baiyi.javaFxTool.utils.business;

import javax.xml.bind.JAXBContext;

/**
 * @author: bai
 * @Date: 2020/2/17 16:38
 * @Description:
 */
public class JAXBContextHolder {
    private static JAXBContext JAXBCONTEXT;

    public static JAXBContext getJAXBContext(){
        if(JAXBCONTEXT == null){
            initJAXBContext();
        }
        return JAXBCONTEXT;
    }

    private static synchronized void initJAXBContext(){
        try{
            if(JAXBCONTEXT == null){
                JAXBCONTEXT = JAXBContext.newInstance(ObjectFactory.class);
            }
        }catch(Exception ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }
}
