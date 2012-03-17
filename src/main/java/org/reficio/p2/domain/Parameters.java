package org.reficio.p2.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 3/11/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters {

    @XmlElement
    public boolean pedantic;

    @XmlElement
    public String additionalArgs = "";

    @XmlElement
    public boolean compressSite;

    @XmlElement
    public int forkTimeoutInSeconds = 0;

    @XmlElement
    public String categoryFileURL = "";

    public String getCategoryFileURL() {
        return categoryFileURL;
    }

    public void setCategoryFileURL(String categoryFileURL) {
        this.categoryFileURL = categoryFileURL;
    }
    public boolean isPedantic() {
        return pedantic;
    }

    public void setPedantic(boolean pedantic) {
        this.pedantic = pedantic;
    }

    public String getAdditionalArgs() {
        return additionalArgs;
    }

    public void setAdditionalArgs(String additionalArgs) {
        this.additionalArgs = additionalArgs;
    }

    public boolean isCompressSite() {
        return compressSite;
    }

    public void setCompressSite(boolean compressSite) {
        this.compressSite = compressSite;
    }

    public int getForkTimeoutInSeconds() {
        return forkTimeoutInSeconds;
    }

    public void setForkTimeoutInSeconds(int forkTimeoutInSeconds) {
        this.forkTimeoutInSeconds = forkTimeoutInSeconds;
    }
}
