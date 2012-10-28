/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

/**
 * @author Gregory
 * @date 09/13/2011
 */

import java.io.*;
import java.util.Arrays;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.Document;
//import org.jdom.Namespace;

/**
 * @author Gregory
 * @Date 09/13/2011
 * 
 */
public class SpectrumFileConverter {
	protected File myFile;
	protected double[] ionVectorArray;
	protected double[] MZVector;
	protected short[][] AbundenceMatrix;
	protected boolean DataExists_flag;
	
	/**
	 * Constructor
	 * @author Gregory
	 * @param String FileName
	 */
	public SpectrumFileConverter(String FileName) {
		this.myFile = new File(FileName);
		this.DataExists_flag = false;
	}
	
	public int returnIonVectorLength() throws Exception {
		int numLengthIonVector = 0;
		String line = null;
		
		try {
			FileReader fileReader = new FileReader(this.myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			
			//Scan through the header file and find the Keyword Extractions
			for (int i = 0;i < 25;i++){
				line = reader.readLine();
				if(line.contains("Extractions")){
					//Copy over the number
					String[] StrLengthIonVector = line.split(" ", 15);
					numLengthIonVector = Integer.parseInt(StrLengthIonVector[4]);
					break;
				}
				line = null;
			}	
			reader.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception("SpectrumFileConverter:returnIonVectorLength() Could not find Ion data Length");
		}
		
		return numLengthIonVector;
	}
	
	public int returnMZLength() throws Exception {
		int numLengthMZVector = 0;
		String line = null;
		
		try {
			FileReader fileReader = new FileReader(this.myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			
			//Scan through the header file and find the Keyword Histogram
			for (int i = 0;i < 25;i++){
				line = reader.readLine();
				if(line.contains("Histogram")){
					//Copy over the number
					String[] StrLengthMZVector = line.split(" ", 15);
					numLengthMZVector = Integer.parseInt(StrLengthMZVector[2]);
					break;
				}
				line = null;
			}		
			reader.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception("SpectrumFileConverter:returnMZLength() Could not find MZ data Length");
		}
		
		return numLengthMZVector;
	}

	private void createIonVector() throws Exception {
		String line = null;
		
		//Create Protected Variable
		ionVectorArray = new double[this.returnIonVectorLength()];
		
		try {
			FileReader fileReader = new FileReader(this.myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			
			//Skip over the header file, which is 12 lines
			for (int i = 1;i <= 12;i++){
				line = reader.readLine();
			}
			reader.skip(1);  //Need to skip one character the bastards!
			line = reader.readLine(); //Grab the actual Vector
			
			//Parse the string
			String[] result = line.split(" ");
			
			//Convert to Doubles
			int i = 0;
			for (String token:result){
				ionVectorArray[i] = Double.parseDouble(token);
				i++;
			}
			reader.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new Exception("SpectrumFileConverter:createIonVector() Could Not Create or Find IonVector data, during File Conversion.");
		}
	
	}
	
	private void create_MZVector_AbundenceMatrix() throws Exception {
		String line = null;
		int IonLength = 0;
		int MZLength = 0;
		
		try{
			//Declare Variables, assign range for local/global variables
			IonLength = this.returnIonVectorLength();
			MZLength = this.returnMZLength();
			MZVector = new double[MZLength];
			AbundenceMatrix = new short[MZLength][IonLength];
			
			//Create the Text Reader
			FileReader fileReader = new FileReader(this.myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			
			//Skip over the header lines and ION line, which is 13 lines
			for (int i = 1;i <= 13;i++){
				line = reader.readLine();
			}
			
			//Loop through all 'n' lines of the MZ matrix
			//Saving the first variable as a entry in the MZ vector
			//Saving all other entries as elements of the Abundence Matrix
			for(int i = 0; i <= (MZLength - 1); i++){
				//reader.skip(1);  				//Need to skip one character the bastards!
				line = reader.readLine(); 			//Grab the actual Vector
                                line = line.trim();
				
                                //System.out.println("I = " + i);
                                
				if(!"".equals(line)){
                                        //System.out.println("Line = " + line);
					String[] result = line.split(" "); 	//Parse the string
					
					int j = 0;
					for (String token:result){
						//Populate the MZVector
						if(j == 0){
							MZVector[i] = Double.parseDouble(token);		
						}
						else{
							//Populate the matrix
							//note: index goes from 1 to 400, with a offset of -1 on the matrix
							//      also account for skipping over the first entry
							AbundenceMatrix[i][(j-1)] = Short.parseShort(token);
						}
						j++;
					}
				}
			}
			reader.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new Exception("create_MZVector_AbundenceMatrix(), Error occured, while building the Abundence Matrix");
		}
		
	}
	
	public boolean convertFile() throws Exception{
		boolean finish_flag = false;
		
		try{
			this.createIonVector();
			this.create_MZVector_AbundenceMatrix();
			finish_flag = true;
		}
		catch(Exception ex){
			ex.printStackTrace();
			finish_flag = false;
			throw new Exception("convertFile(), Error occured while converting the file.");
		}
		finally{
			this.DataExists_flag = true;  //Set the flag
		}
		
		return finish_flag;
	}
	
	public double[] getIonVectorArray() throws Exception{
		if (this.DataExists_flag) {
                    return this.ionVectorArray;
                }
		else {
                    throw new Exception("getIonVectorArray(), No Data exists yet, run convertFile() first");
                }
	}
	
	public double[] getMZVector() throws Exception{
		if (this.DataExists_flag){
			return this.MZVector;
                }
                else{
			throw new Exception("getMZVector(), No Data exists yet, run convertFile() first");
                }
        }
	
	public short[][] getAbundenceMatrix() throws Exception{
		if (this.DataExists_flag){
			return this.AbundenceMatrix;
                }
                else{
			throw new Exception("getAbundenceMatrix(), No Data exists yet, run convertFile() first");
                }
	}
	
	public boolean XMLConverter(File ProjectName) throws Exception{
		boolean finish_flag = false;
		
		if (this.DataExists_flag){
			//Create the XML Document and store it to the directory
			//Look at pg.657 in ISBN: 0-201-77186-1 for details
			Element root = new Element("REC_ToF_MassSpectra");
			DocType type = new DocType("null");
			Document doc = new Document(root,type);
			
			//Add the Project Name
			Element projectName = new Element("ProjectName");
			projectName.setText(ProjectName.getName());
			//Add the number of MZ Rows
			Element NumMZ = new Element("NumMZRows");
			NumMZ.setText(Integer.toString(this.returnMZLength()));
			//Add the number of ION Columns
			Element NumIon = new Element("NumIONColumns");
			NumIon.setText(Integer.toString(this.returnIonVectorLength()));
			//Add the above three elements to the root
			root.addContent(projectName);
			root.addContent(NumMZ);
			root.addContent(NumIon);
			
			/*
			 * @depreciated
			//Create the MZ Vector
			Element MZVector = new Element("MZVector");
			for(int i = 0; i <= (this.MZVector.length - 1); i++){
				Element MZele = new Element("element");
				MZele.setAttribute("index", Integer.toString(i));
				MZele.setText(Double.toString(this.MZVector[i]));
				MZVector.addContent(MZele);
			}
			root.addContent(MZVector);
			
			//Create the Ion Vector
			Element IONVector = new Element("IONVector");
			for(int j = 0; j <= (this.ionVectorArray.length - 1); j++){
				Element IONele = new Element("element");
				IONele.setAttribute("index", Integer.toString(j));
				IONele.setText(Double.toString(this.ionVectorArray[j]));
				IONVector.addContent(IONele);
			}
			root.addContent(IONVector);
			*/
			//Create the MZ Vector
			Element MZVector = new Element("MZVector");
			MZVector.setAttribute("length", Integer.toString(this.MZVector.length));
			//Convert the array of doubles back to a solid string
			String arrayMZ = Arrays.toString(this.MZVector)
                                                                    .replace(", ", ",")
                                                                    .replace("[", "")
                                                                    .replace("]", "");
			MZVector.setText(arrayMZ);
			root.addContent(MZVector);
			
			//Create the Ion Vector
			Element IONVector = new Element("IONVector");
			IONVector.setAttribute("length", Integer.toString(this.ionVectorArray.length));
			String arrayION = Arrays.toString(this.ionVectorArray)
                                                                            .replace(", ", ",")
                                                                            .replace("[", "")
                                                                            .replace("]", "");
			IONVector.setText(arrayION);
			root.addContent(IONVector);

			/*
			 * @depreciated
			//Create the SpectraMatrix and insert values
			Element Spectra = new Element("SpectraMatrix");
			for(int i = 0; i <= (this.MZVector.length - 1); i++){
				Element MZ = new Element("MZ");
				MZ.setAttribute("value", Double.toString(this.MZVector[i]));
				MZ.setAttribute("index", Integer.toString(i));
				for(int j = 0; j <= (this.ionVectorArray.length - 1); j++){
					Element eV = new Element("eV");
					eV.setAttribute("value", Double.toString(this.ionVectorArray[j]));
					eV.setAttribute("index", Integer.toString(j));
					eV.setText(Integer.toString(this.AbundenceMatrix[i][j]));
					MZ.addContent(eV);
				}
				Spectra.addContent(MZ);
			}
			root.addContent(Spectra);
			*/
			//Create the SpectraMatrix and insert values
			Element Spectra = new Element("SpectraMatrix");
			for(int i = 0; i <= (this.MZVector.length - 1); i++){
				Element MZ = new Element("MZ");
				
				//Setting the Namespace
				//MZ.setNamespace(Namespace.getNamespace("value", ("mz " + Double.toString(this.MZVector[i]))));
				
				MZ.setAttribute("value",Double.toString(this.MZVector[i]));
				MZ.setAttribute("index", Integer.toString(i));
				//Convert the array of integers back to a solid string
				String arrayInt = Arrays.toString(this.AbundenceMatrix[i])
                                                                                        .replace(", ", ",")
                                                                                        .replace("[", "")
                                                                                        .replace("]", "");
				MZ.setText(arrayInt);
				Spectra.addContent(MZ);
			}
			root.addContent(Spectra);
				
			//serialize with two-space indents and extra line breaks
			try{
				File tempFile = new File(ProjectName.getPath() 
                                                                            + "\\" 
                                                                            + "orginal_dataset"
                                                                            + ".xml");
                                FileWriter writer = new FileWriter(tempFile);
                                 
				XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
				serializer.output(doc,writer);
				
				writer.close();
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
			finish_flag = true;
		}
		else{
			finish_flag = false;
			throw new Exception("No Data exists yet, run convertFile() first");
		}
			
		return finish_flag;
	}
	
	
//	@SuppressWarnings("unused")
//	private static void main (String[] args){
//		double[] ionVectorArray_test;
//		double[] MZVector_test;
//		int[][] AbundenceMatrix_test;
//		
//		SpectrumFileConverter ACQ40 = new SpectrumFileConverter("Gregory_Test_DataSet1.txt");
//
//		try{
//			if(ACQ40.convertFile()){
//				ionVectorArray_test = ACQ40.getIonVectorArray();
//				MZVector_test = ACQ40.getMZVector();
//				AbundenceMatrix_test = ACQ40.getAbundenceMatrix();
//			}
//			else{
//				ionVectorArray_test = new double[0];
//				MZVector_test = new double[0];
//				AbundenceMatrix_test = new int[0][0];
//			}
//			
//			ACQ40.XMLConverter("GREGS_TEST_3");
//			
//			
//			
//			/*
//			System.out.println("// **********************");
//			System.out.println("// Project: Test Convert a File");
//			System.out.println("// Loads and then displays data");
//			System.out.println("// **********************");
//			System.out.println(" ");
//			
//			//Now print the actual vector
//			//Ion Vector
//			System.out.print("ionVector = [ ");
//			for (int i = 0;i <= (ionVectorArray_test.length - 1); i++) {
//				System.out.print(ionVectorArray_test[i] + " ");
//			}
//			System.out.print("]");
//			System.out.println(" ");
//			System.out.println(ionVectorArray_test.length);
//			
//			//MZ Vector
//			System.out.print("MZVector = [ ");
//			for (int i = 0;i <= (MZVector_test.length - 1); i++) {
//				System.out.print(MZVector_test[i] + " ");
//			}
//			System.out.print("]");
//			System.out.println(" ");
//			System.out.println(MZVector_test.length);
//			
//			//Print a view values from Matrix
//			System.out.println(AbundenceMatrix_test[100][100]);
//			System.out.println(AbundenceMatrix_test[800][100]);
//			
//			System.out.println("FINISHED ");
//			*/
//			
//		}
//		catch(Exception ex){
//			ex.printStackTrace();
//		}
//
//	}
        
}
