package org.reficio.p2.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 3/11/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Site {

    @XmlElement
    private String id;

    @XmlElement(name = "artifact")
    @XmlElementWrapper(name="artifacts")
    private List<String> artifacts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getArtifacts() {
        return new ArrayList<String>(artifacts);
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }


}
