/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

import java.io.*;
import java.util.*;
import java.text.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.output.*;
import org.jdom.Document;


/**
 *
 * @author Gregory
 */
public class XML_FileSaver {
    protected File myFile;
    protected boolean DataExists_flag;
    
    public XML_FileSaver(String DirectoryName,String FileName){
        this.myFile = new File(DirectoryName + "\\" + FileName +".xml");
        this.DataExists_flag = false;
    }
    
    public Boolean fileExist() throws Exception{
        
        try{
            if (this.myFile.exists()){
                if (this.myFile.canWrite()){
                    return true;
                }
                else{
                    throw new Exception("Can not Write to XML File, check file permissions");
                }
            }
            else{
                return false;
            }   
        }
        catch (Exception ex){
            ex.printStackTrace();
            throw new Exception("Problem looking for XML File");
        }
        
    }
    
    public void saveConvolutionKernelEntry(
                                            String KernelName,
                                            String SmoothingType,
                                            String ARMASamples,
                                            String WindowType,
                                            String PercentPeakValue,
                                            String FixedMin,
                                            String FixedMax,
                                            String OffsetRangeType,
                                            String OffsetDomainType,
                                            String FixedRange,
                                            String FixedDomain,
                                            String NormalizeType,
                                            String NormalizePeakValue,
                                            String NormalizeEnergyValue,
                                            Double MZValue,
                                            Double MZRange,
                                            int[] OrginalVector,
                                            int[] KernelVector){
        //Code Starts Here
        
        try{
            
            //For Existing File, Create new Entry
            if (!this.fileExist()){
                //Create the XML Document and store it to the directory
		//Look at pg.657 in ISBN: 0-201-77186-1 for details
                Element rootMe = new Element("Kernel");
                DocType type = new DocType("null");
                Document docMe = new Document(rootMe,type);
                
                //Create the Default Elements
                Element projectName = new Element("ProjectName");
                projectName.setText(this.myFile.getParent());
                rootMe.addContent(projectName);
                
                //Write the File Out
                FileWriter writer = new FileWriter(this.myFile);

                XMLOutputter serializer = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                serializer.output(docMe,writer);

                writer.close();
            }

            //Now read in the file
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(this.myFile);
            
            Element root = doc.getRootElement();
            
            //Create New SavedKernel and add to Root
            Element savedKernel = new Element("SavedKernel");
            savedKernel.setAttribute("KernelName", KernelName);
            root.addContent(savedKernel);
            
            //Save the Date
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            savedKernel.addContent(new Element("Date").setText(dateFormat.format(cal.getTime())));
            
            //Create the Elements
            Element Properties = new Element("Properties");
            savedKernel.addContent(Properties);
            
            //Smoothing
            Element Smoothing = new Element("Smoothing");
            Smoothing.addContent(new Element("type").setText(SmoothingType));
            Smoothing.addContent(new Element("ARMASamples").setText(ARMASamples));
            Properties.addContent(Smoothing);
            
            //Windowing
            Element Windowing = new Element("Windowing");
            Windowing.addContent(new Element("type").setText(WindowType));
            Windowing.addContent(new Element("PercentPeak").setText(PercentPeakValue));
            Windowing.addContent(new Element("FixedMin").setText(FixedMin));
            Windowing.addContent(new Element("FixedMax").setText(FixedMax));
            Properties.addContent(Windowing);
            
            //Offsetting
            Element Offsetting = new Element("Offsetting");
            Offsetting.addContent(new Element("RangeType").setText(OffsetRangeType));
            Offsetting.addContent(new Element("DomainType").setText(OffsetDomainType));
            Offsetting.addContent(new Element("fixedRange").setText(FixedRange));
            Offsetting.addContent(new Element("fixedDomain").setText(FixedDomain));
            Properties.addContent(Offsetting);
            
            //Normalizing
            Element Normalizing = new Element("Normalizing");
            Normalizing.addContent(new Element("Type").setText(NormalizeType));
            Normalizing.addContent(new Element("NormalizePeak").setText(NormalizePeakValue));
            Normalizing.addContent(new Element("NormalizeEnergry").setText(NormalizeEnergyValue));
            Properties.addContent(Normalizing);
            
            //Orginal Kernel
            savedKernel.addContent(new Element("MZValue").setText(Double.toString(MZValue)));
            savedKernel.addContent(new Element("MZRange").setText(Double.toString(MZRange)));
            Element orginalVector = new Element("OrginalVector");
            String arrayION = Arrays.toString(OrginalVector)
                                                                    .replace(", ", ",")
                                                                    .replace("[", "")
                                                                    .replace("]", "");
            orginalVector.setAttribute("Length",Integer.toString(OrginalVector.length));
            orginalVector.setText(arrayION);
            savedKernel.addContent(orginalVector);
            
            
            //Convolution Kernel
            Element kernelVector = new Element("KernelVector");
            arrayION = Arrays.toString(KernelVector)
                                                                    .replace(", ", ",")
                                                                    .replace("[", "")
                                                                    .replace("]", "");
            kernelVector.setAttribute("Length",Integer.toString(KernelVector.length));
            kernelVector.setText(arrayION);
            savedKernel.addContent(kernelVector);
            
            //Resave the XML file
            //Rewrite the File Output
            FileWriter writer = new FileWriter(this.myFile);
                                 
            XMLOutputter serializer = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            serializer.output(doc,writer);
				
            writer.close();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    public void saveParameterEntry(
                                            String RunName,
                                            String ConvolutionKernelName,
                                            Double MZValue_Observation,
                                            Double MZRange_Observation,
                                            Boolean LS_CheckBox,
                                            Boolean ML_CheckBox,
                                            Boolean ISTA_CheckBox,
                                            Boolean MMV_CheckBox,
                                            Integer LS_NumIterations,
                                            Integer ML_NumIterations,
                                            Integer ISTA_NumIterations,
                                            Integer MMV_NumIterations,
                                            Double ISTA_Lagrangian,
                                            Double MMV_LagrangianMIN,
                                            Double MMV_LagrangianMAX,
                                            Integer MMV_Lagrangian_Num_Iter,
                                            Boolean EpsilonBallPercentile_Radio,
                                            Boolean EpsilonBallFixed_Radio,
                                            Double MMV_EpsilonBallPercentile,
                                            Double MMV_EpsilonBallFixedValue){
        //code starts here
        
        try{
            
            //For Existing File, Create new Entry
            if (!this.fileExist()){
                //Create the XML Document and store it to the directory
		//Look at pg.657 in ISBN: 0-201-77186-1 for details
                Element rootMe = new Element("RunParameters");
                DocType type = new DocType("null");
                Document docMe = new Document(rootMe,type);
                
                //Create the Default Elements
                Element projectName = new Element("ProjectName");
                projectName.setText(this.myFile.getParent());
                rootMe.addContent(projectName);
                
                //Write the File Out
                FileWriter writer = new FileWriter(this.myFile);

                XMLOutputter serializer = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                serializer.output(docMe,writer);

                writer.close();
            }

            //Now read in the file
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(this.myFile);
            
            Element root = doc.getRootElement();
            
            //Create New add to Root
            Element savedRun = new Element("RunName");
            savedRun.setAttribute("RunName", RunName);
            root.addContent(savedRun);
            
            //Save the Date
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            savedRun.addContent(new Element("Date").setText(dateFormat.format(cal.getTime())));
            
            //Save the Convolution Kernel Name Used
            Element KernelName = new Element("KernelName").setText(ConvolutionKernelName);
            savedRun.addContent(KernelName);
            
            //Save the Observation MZ Value
            Element ObservationValue = new Element("ObservationMZValue").setText(Double.toString(MZValue_Observation));
            savedRun.addContent(ObservationValue);
            
            //Save the Observation MZ Range
            Element ObservationRange = new Element("ObservationMZRange").setText(Double.toString(MZRange_Observation));
            savedRun.addContent(ObservationRange);
            
            //Create the LS Elements ******************************
                Element LSParameters = new Element("LSParameters");
                savedRun.addContent(LSParameters);

                //LS Selected
                Element LSSelected = new Element("LSSelected").setText(Boolean.toString(LS_CheckBox));
                LSParameters.addContent(LSSelected);

                //LS Number of Iterations
                Element LSNumIter = new Element("LSNumofIter").setText(Integer.toString(LS_NumIterations));
                LSParameters.addContent(LSNumIter);
            
            //Create the ML Elements ******************************
                Element MLParameters = new Element("MLParameters");
                savedRun.addContent(MLParameters);

                //ML Selected
                Element MLSelected = new Element("MLSelected").setText(Boolean.toString(ML_CheckBox));
                MLParameters.addContent(MLSelected);

                //ML Number of Iterations
                Element MLNumIter = new Element("MLNumofIter").setText(Integer.toString(ML_NumIterations));
                MLParameters.addContent(MLNumIter);
                
            //Create the ISTA Elements ******************************
                Element ISTAParameters = new Element("ISTAParameters");
                savedRun.addContent(ISTAParameters);

                //ISTA Selected
                Element ISTASelected = new Element("ISTASelected").setText(Boolean.toString(ISTA_CheckBox));
                ISTAParameters.addContent(ISTASelected);

                //ISTA Number of Iterations
                Element ISTANumIter = new Element("ISTANumofIter").setText(Integer.toString(ISTA_NumIterations));
                ISTAParameters.addContent(ISTANumIter);
                
                //ISTA Lagrangian
                Element ISTALagrangian = new Element("ISTALagrangian").setText(Double.toString(ISTA_Lagrangian));
                ISTAParameters.addContent(ISTALagrangian);
            
            //Create the MMV Elements ******************************
                Element MMVParameters = new Element("MMVParameters");
                savedRun.addContent(MMVParameters);

                //MMV Selected
                Element MMVSelected = new Element("MMVSelected").setText(Boolean.toString(MMV_CheckBox));
                MMVParameters.addContent(MMVSelected);

                //MMV Number of Iterations
                Element MMVNumIter = new Element("MMVNumofIter").setText(Integer.toString(MMV_NumIterations));
                MMVParameters.addContent(MMVNumIter);
                
                //MMV LagrangianMIN
                Element MMVLagrangianMIN = new Element("MMVLagrangianMIN").setText(Double.toString(MMV_LagrangianMIN));
                MMVParameters.addContent(MMVLagrangianMIN);
                
                //MMV LagrangianMAX
                Element MMVLagrangianMAX = new Element("MMVLagrangianMAX").setText(Double.toString(MMV_LagrangianMAX));
                MMVParameters.addContent(MMVLagrangianMAX);
                
                //MMV Lagrangian Number of Iterations
                Element MMVLagrangianNumIter = new Element("MMVLagrangianNumIter").setText(Integer.toString(MMV_Lagrangian_Num_Iter));
                MMVParameters.addContent(MMVLagrangianNumIter);
                
                //MMV Percentile Selected
                Element MMVLagrangianPercentile = new Element("MMVLagrangianPercentile").setText(Boolean.toString(EpsilonBallPercentile_Radio));
                MMVParameters.addContent(MMVLagrangianPercentile);
                
                //MMV Fixed Selected
                Element MMVLagrangianFixed = new Element("MMVLagrangianFixed").setText(Boolean.toString(EpsilonBallFixed_Radio));
                MMVParameters.addContent(MMVLagrangianFixed);
                
                //MMV_EpsilonBallPercentile
                Element MMVEpsilonBallPercentile = new Element("MMVEpsilonBallPercentile").setText(Double.toString(MMV_EpsilonBallPercentile));
                MMVParameters.addContent(MMVEpsilonBallPercentile);
                
                //MMV_EpsilonBallFixedValue
                Element MMVEpsilonBallFixedValue = new Element("MMVEpsilonBallFixedValue").setText(Double.toString(MMV_EpsilonBallFixedValue));
                MMVParameters.addContent(MMVEpsilonBallFixedValue);

                
            //Resave the XML file ********************************************
            //Rewrite the File Output
            FileWriter writer = new FileWriter(this.myFile);
                                 
            XMLOutputter serializer = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
            serializer.output(doc,writer);
				
            writer.close();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        
    }
    

}
