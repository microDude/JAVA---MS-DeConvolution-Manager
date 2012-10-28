/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

/**
 * 
 */

import java.io.*;
import java.util.*;

import javax.swing.ProgressMonitor;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import org.jdom.xpath.XPath;

/**
 * @author Gregory
 *
 */
public class XMLloader {
	protected File myFile;
	protected double[] ionVectorArray;
	protected double[] MZVector;
	protected short[][] AbundenceMatrix;
 
	protected boolean DataExists_flag;
        protected MS_DataStructure myDataStructure;
	
	/**
	 * Constructor
	 * @author Gregory
	 * 
	 */
	public XMLloader(String FileName) {
		this.myFile = new File(FileName);
		this.DataExists_flag = false;
	}
        
        public XMLloader(File FileName) {
		this.myFile = FileName;
		this.DataExists_flag = false;
	}
        
	public void loadXMLFile() throws Exception {
		String XMLexpression;
		Element result;
		List nodeList;
		int MZNumRows = 0;
		int NumIONColumns = 0;
		int i = 0;
		int j = 0;
                double G = 0.0;
                
                //Build a Progress Bar Dialog
                ProgressMonitor progressBar = new ProgressMonitor(null,
                                                    "Loading XML Data",
                                                    "",0,100);
                progressBar.setNote("Starting");
                progressBar.setProgress(0);
              
		try{
                        progressBar.setNote("Creating XML Builder");
                        progressBar.setProgress(5);
                                
                        SAXBuilder builder = new SAXBuilder();
                        Document doc = builder.build(this.myFile);
                        //Element root = doc.getRootElement();

                        //Grab the number of rows MZ
                        XMLexpression = "/REC_ToF_MassSpectra/NumMZRows";
                        result = (Element) XPath.selectSingleNode( doc,XMLexpression);
                        MZNumRows = Integer.parseInt(result.getText());

                        //Grab the number of columns ION
                        XMLexpression = "/REC_ToF_MassSpectra/NumIONColumns";
                        result = (Element) XPath.selectSingleNode( doc,XMLexpression);
                        NumIONColumns = Integer.parseInt(result.getText());

		    //PreAllocate space for the resultant vectors and matrix
		    this.ionVectorArray = new double[NumIONColumns];
		    this.MZVector = new double[MZNumRows];
		    this.AbundenceMatrix = new short[MZNumRows][NumIONColumns];
		    
                    progressBar.setNote("Populating the Data Variables");
                    progressBar.setProgress(10);
                   
                    
		    //Grab the entire MZ vector array and place it into memory
		    XMLexpression = "/REC_ToF_MassSpectra/MZVector";
		    result = (Element) XPath.selectSingleNode( doc,XMLexpression);
		    String[] StrLengthMZVector = result.getText().split(",");
			//Convert to Doubles
			i = 0;
			for (String token:StrLengthMZVector){
				this.MZVector[i] = Double.parseDouble(token);
				i++;
			}
			
		    //Grab the entire ION vector array and place it into memory
		    XMLexpression = "/REC_ToF_MassSpectra/IONVector";
		    result = (Element) XPath.selectSingleNode( doc,XMLexpression);
		    String[] StrLengthIONVector = result.getText().split(",");
			//Convert to Doubles
			i = 0;
			for (String token:StrLengthIONVector){
				this.ionVectorArray[i] = Double.parseDouble(token);
				i++;
			}
			
                    progressBar.setNote("Populating the Spectral Matrix");
                    progressBar.setProgress(25);
                   
		    //Grab the entire Abundence Matrix and place it into memory
		    XMLexpression = "/REC_ToF_MassSpectra/SpectraMatrix/MZ";
		    nodeList = XPath.selectNodes(doc, XMLexpression);
		    Iterator iter = nodeList.iterator();
		    i = 0;
                    G = (75/(this.MZVector.length - 1));
		    while(iter.hasNext()) {
                        if (progressBar.isCanceled()){
                            break;
                        }
                        else{
                            progressBar.setProgress((int)((i*G)+25));
                            result = (Element) iter.next();
                            String[] matrixRow = result.getText().split(",");
                            //Convert to Integer
                            j = 0;
                            for (String token:matrixRow){
                                this.AbundenceMatrix[i][j] = Short.parseShort(token);
                                j++;
                            }
                            i++;
                        }  
		    }
		    
                    if (!progressBar.isCanceled()){
                        //populate a DataStrucure
                        myDataStructure = new MS_DataStructure();
                        myDataStructure.setFileName(this.myFile);
                        myDataStructure.setIonVectorArray(this.ionVectorArray);
                        myDataStructure.setMZVector(this.MZVector);
                        myDataStructure.setAbundenceMatrix(this.AbundenceMatrix);
                        
                        this.DataExists_flag = true; //Set the flag to true
                    } 
                    
		    progressBar.close();
                    
		}
		catch(IOException ex){
			ex.printStackTrace();
			throw new Exception("Error Occured when loading XML file"); 
		}
		
	}
        
        public MS_DataStructure getMSDataStructure() throws Exception{  
            if(this.DataExists_flag){
                return this.myDataStructure;
            }
            else{
                throw new Exception("MS DataStrucure does not exist, XMLloader");
            }    
        }
	
	public double[] getIonVectorArray() throws Exception{
		if (this.DataExists_flag){
			return this.ionVectorArray;
                }
                else{
			throw new Exception("getIonVectorArray(), No Data exists yet, run loadXMLFile() first");
                }
	}
	
	public double[] getMZVector() throws Exception{
		if (this.DataExists_flag) {
                        return this.MZVector;
                }
		else {
                        throw new Exception("getMZVector(), No Data exists yet, run loadXMLFile() first");
                }
	}
	
	public short[][] getAbundenceMatrix() throws Exception{
		if (this.DataExists_flag) {
                        return this.AbundenceMatrix;
                }
		else {
                        throw new Exception("getAbundenceMatrix(), No Data exists yet, run loadXMLFile() first");
                }
	}
	
	
	/**
	 * @param args
	 */
	private static void main(String[] args) {
		XMLloader document = new XMLloader("GREGS_TEST_2.xml");
		
		try{
			document.loadXMLFile();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}
	
}
