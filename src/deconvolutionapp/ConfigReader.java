/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

import java.io.*;

//XML
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import org.jdom.output.*;
import org.jdom.Document;


/**
 * Class used to load and read a configuration file
 * 
 * @author Gregory
 */
public class ConfigReader {
    
    //Config File Parameters
    File ConfigFile;
    SAXBuilder builder;
    Document doc;
    
    /**
     * Constructor
     */
    public ConfigReader(){
        try{
            ConfigFile = new File("GlobalConfig.xml");
            builder = new SAXBuilder();
            doc = builder.build(ConfigFile); 
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
           
    }
    
    /**
     * Reads the desired parameter from the configuration XML file
     * 
     * @param XPATH String type, which contains the XPATH code to read the desired parameter
     * @return Reads the specified parameter from the configuration file
     * @throws Exception 
     */
    public String getConfigParameter(String XPATH) throws Exception{
        
        //Populate Default Fields
        try{
           Element result = (Element) XPath.selectSingleNode(doc,XPATH);
           return result.getText();
        }
        catch(Exception ex){
           ex.printStackTrace();
           throw new Exception("Unable to read Config File");
        }
        
    }
    
    /**
     * Writes the last opened project directory to the configuration XML file.
     * This is a convenience function for other methods.
     * 
     * @param LastDirectory File type used to set the Last opened directory
     * @throws Exception 
     */
    public void setLastDirectory(File LastDirectory) throws Exception{
        
        try{
            Element result = (Element) XPath.selectSingleNode( doc,"/root/LastOpenedDirectory");
            //System.out.println("Element Text : " + result.getText());
            result.setText(LastDirectory.getPath());
            
            //Rewrite the File Output
            FileWriter writer = new FileWriter(ConfigFile);
                                 
            XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
            serializer.output(doc,writer);
				
            writer.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
            throw new Exception("Unable to write to Config File");
        }
    }

}
