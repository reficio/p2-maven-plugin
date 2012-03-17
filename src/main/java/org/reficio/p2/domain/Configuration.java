package org.reficio.p2.domain;

import com.sun.tools.corba.se.idl.toJavaPortable.StringGen;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import sun.misc.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 3/11/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

    private String xml = "";

    @XmlElement(name = "site")
    @XmlElementWrapper(name="sites")
    private List<Site> sites = new ArrayList<Site>();

    @XmlElement(name = "repository")
    @XmlElementWrapper(name="repositories")
    private List<String> repositories = new ArrayList<String>();

    @XmlElement(name = "parameters")
    private Parameters parameters = new Parameters();

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<Site> getSites() {
        return new ArrayList<Site>(sites);
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public List<String> getRepositories() {
        return new ArrayList<String>(repositories);
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public static Configuration readConfiguration(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(Configuration.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            String xml = FileUtils.readFileToString(file, "UTF-8");
            Configuration config = (Configuration) unmarshaller.unmarshal(new StringReader(xml));
            config.xml = xml;
            return config;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return xml;
    }

}
