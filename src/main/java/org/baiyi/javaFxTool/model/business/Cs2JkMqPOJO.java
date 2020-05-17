package org.baiyi.javaFxTool.model.business;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author: bai
 * @Date: 2020/2/17 16:34
 * @Description:
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cs2JkMqPOJO", propOrder = {
        "mark",
        "ceid"
})
public class Cs2JkMqPOJO {

    protected String mark;
    protected String ceid;

    /**
     * Gets the value of the mark property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMark() {
        return mark;
    }

    /**
     * Sets the value of the mark property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMark(String value) {
        this.mark = value;
    }

    /**
     * Gets the value of the ceid property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCeid() {
        return ceid;
    }

    /**
     * Sets the value of the ceid property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCeid(String value) {
        this.ceid = value;
    }

}

