/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

//JDOM
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jdom.Document;

//Jama
import Jama.Matrix;

//Java related
import java.io.File;
import java.math.*;

/**
 * Class contains all of the DeConvolution algorithms, as well as, handles loading the 
 * vectors and executing the algorithms
 * 
 * @author      Gregory Gutshall
 * @version     1.0                                 
 * @since       2012-01-20          
 */
public class DeConvolver {
    
    //Class Variables
    /**
     * Name of the DeConvolution Project to be saved
     */
    protected String RunName;
    /**
     * Name of the previously saved Parameter file to use
     */
    protected File ParameterFile;
    /**
     * Name of the previously saved kernel vector to use
     */
    protected File KernelFile;
    /**
     * A element containing pulled data from the XML parameter file
     */
    protected Element ParameterElement;
    /**
     * Encoded string, which contains which algorithms were ran
     */
    protected String methodsRan = ""; //LS,ML,MMV
    
    //Calculation Variables
    /**
     * Convolution Matrix
     */
    protected Matrix A;
    /**
     * Observation vector
     */
    protected Matrix y;
    
    //Result Variables
    /**
     * Estimated sparse x vector for LS
     */
    protected Matrix LS_x_est;
    /**
     * Estimated sparse x vector for ML
     */
    protected Matrix ML_x_est;
    /**
     * Estimated sparse x vector for ISTA
     */
    protected Matrix ISTA_x_est;
    /**
     * Estimated sparse x vector for MMV
     */
    protected Matrix MMV_x_est;
    
    /**
     * Constructor loads in the configuration & parameter file, calls a method to load the kernel
     * vector, calls a method to generate a convolution matrix from a vector (Toeplitz), and loads
     * the observation vector.
     * 
     * @param RunTimeName The name for this Run
     * @param DataStructure Mass Spec Data Structure used to grab the Observation vector y
     * @param DebugFlag Boolean flag used for triggering debug variables
     * @throws Exception Thrown if any of the files are corrupt or missing
     */
    public DeConvolver(String RunTimeName, MS_DataStructure DataStructure,ConfigReader ConfigFile, Boolean DebugFlag) throws Exception{
        
        //Copy over the Variables
        this.RunName = RunTimeName;
        
        //Get a Reference to the Parameter File and Kernel File
        try{
            File ProjectDirectory = new File(ConfigFile.getConfigParameter("/root/LastOpenedDirectory"));
            this.KernelFile = new File(ProjectDirectory.getAbsolutePath() + "\\" + "ConvolutionKernals.xml");
            this.ParameterFile = new File(ProjectDirectory.getAbsolutePath() + "\\" + "RunParameters.xml");
            
            //Read in the ParameterFile
            this.readRunTimeParameters();

            //Generate the Covolution Matrix A
            this.getConvolutionMatrix(DebugFlag);

            //Generate the Observation Vector
            this.getObservationVector(DataStructure,DebugFlag);

        }
        catch (Exception ex){
            ex.printStackTrace();
            throw new Exception("Unable to create RunTime: Check files in Project Directory");
        }
        
    }
    
    /**
     * Reads the parameter file into a XML element stored in RAM
     * @throws Exception File read error 
     */
    private void readRunTimeParameters() throws Exception{

        try{
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(this.ParameterFile);
            
            String XMLexpression = "/RunParameters/RunName[@RunName='" + this.RunName + "']";
            
            //Generate a parameter element field, which will be used later on to run XML queries against to grab
            //the desired parameters
            this.ParameterElement = (Element) XPath.selectSingleNode(doc, XMLexpression);   
        }
        catch (Exception ex){
            throw new Exception("Unable to read ParameterFile in Project Directory");
        }
        
    }
    
    /**
     * Loads the convolution kernel file into local variable.  Then generates a
     * Toeplitz matrix.
     * 
     * @param DebugFlag If true, generates a smooth Maxwell-Boltzman convolution kernel
     * @throws Exception 
     */
    private void getConvolutionMatrix(Boolean DebugFlag) throws Exception{
        
        try{
            //Define the variable for the kernel vector
            double[] KernelVector;
            
            if (DebugFlag == Boolean.FALSE){
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(this.KernelFile);

                //Get the entire Kernel Element
                String KernelName = this.ParameterElement.getChildText("KernelName");
                String XMLexpression = "/Kernel/SavedKernel[@KernelName='" + KernelName + "']";
                Element KernelElement = (Element) XPath.selectSingleNode(doc, XMLexpression);

                //Get the Vector
                String[] StringVector = KernelElement.getChild("KernelVector").getText().split(",");

                //Generate a zero vector of the correct length
                KernelVector = new double[StringVector.length];

                //Convert to Doubles
                int i = 0;
                for (String token:StringVector){
                        KernelVector[i] = Integer.parseInt(token);
                        i++;
                }
            }
            else {
                //Define some local variables
                double eVmin = 0.0;
                double eVmax = 26.0;
                double skew = 20.0;
                double decay = 0.25;
                double amplitude = 100;
                double eV;
                double a1 = 0.0;
                double MaxValue = 1.0;
                
                //Generate a zero vector of length 400
                KernelVector = new double[400];
                
                //Create a Arbitary Convolution Kernel, based off the
                //Maxwell-Boltzman curve.
                for (int i = 0; i <= (KernelVector.length - 1);i++){
                    eV = ((eVmax - eVmin)/KernelVector.length)*i - eVmin; //y=mx+b transform to 0.0 --> 26 eV scale
                    KernelVector[i] = 1000*Math.pow(eV,(skew*decay))*Math.exp((-eV/decay));
                    MaxValue = Math.max(a1, KernelVector[i]);
                    a1 = KernelVector[i];
                }
                //Scale the Amplitude of the A matrix
                for (int i = 0; i <= (KernelVector.length - 1);i++){
                    KernelVector[i] = amplitude*(KernelVector[i]/MaxValue);
                }
            }
            
            //generate a preprocessing engine
            SP_Controller PreProcessEngine = new SP_Controller();
            
            //Build the A matrix
            this.A = PreProcessEngine.buildConvolutionMatrix(KernelVector);
            
            //debug
            System.out.println("A dimension :" + Integer.toString(this.A.getRowDimension()) + "x" + Integer.toString(this.A.getColumnDimension()));
            
        }
        catch (Exception ex){
            throw new Exception("Unable to Build Convolution Matrix A");
        }
        
    }
    
    /**
     * Loads the observation vector y
     * 
     * @param DataStructure Mass Spec data structure passed from constructor
     * @param DebugFlag If true, generates a observation vector via convolution with A
     * @throws Exception 
     */
    private void getObservationVector(MS_DataStructure DataStructure,Boolean DebugFlag) throws Exception{
        
        try{
            Double MZValue = Double.parseDouble(this.ParameterElement.getChildText("ObservationMZValue"));
            Double MZRange = Double.parseDouble(this.ParameterElement.getChildText("ObservationMZRange"));
            
            int indexStart  = DataStructure.getMZindex((MZValue - MZRange));
            int indexEnd    = DataStructure.getMZindex((MZValue + MZRange));
            
            //Generate the New Vector
            this.y = new Matrix(this.A.getRowDimension(), 1);
            
            //Either get the y-vector from memory, or generate it via convolution (for debugging)
            double[] temp;
            if (DebugFlag == Boolean.FALSE){
                //Grab the values for the vector from the MS DataStructure
                temp = DataStructure.getTotalIonRangeArrayDouble(indexStart, indexEnd);
                
                for (int j = 0; j <= this.y.getRowDimension() - 1; j++){
                    this.y.set(j, 0, temp[j]);
                }
            }
            else{
                //Define a sparse vector for x
                Matrix x_test = new Matrix(this.A.getColumnDimension(),1);
                x_test.set(30, 0, 2.0);
                x_test.set(35, 0, 5.0);
                x_test.set(40, 0, 10.0);
                x_test.set(120, 0, 10.0);
                x_test.set(200, 0, 10.0);
                
                //Convolution y=Ax
                this.y = this.A.times(x_test);
            }
            
            //debug
            //System.out.println("MZ Start Index : " + Double.toString(indexStart));
            //System.out.println("MZ End Index : " + Double.toString(indexEnd));
            System.out.println("y dimension :" + Integer.toString(this.y.getRowDimension()) + "x" + Integer.toString(this.y.getColumnDimension()));
 
        }
        catch (Exception ex){
            throw new Exception("Unable to build observation vector y");
        }
    }
    
    /**
     * Returns the first column of the Convolution Matrix A
     * 
     * @return A vector of type double[], which is the first column of A.
     */
    public double[] getConvolutionVector(){
        
        //grab the first column of A
        double[] tempVector = new double[this.A.getRowDimension()];
        for (int j = 0; j <= (this.A.getRowDimension() - 1); j++){
            tempVector[j] = A.get(j, 0);
        }
        
        return tempVector;
    }
    
    /**
     * Returns the observation vector y
     * @return A vector of type double[]
     */
    public double[] getYvector(){
        
        //Convert the matrix to a double[]
        double[] tempVector = new double[this.y.getRowDimension()];
        for (int j = 0; j <= (this.y.getRowDimension() - 1); j++){
            tempVector[j] = this.y.get(j, 0);
        }
        
        return tempVector;
    }
    
    /**
     * Returns the LS x estimate
     * @return A vector of type double[]
     */
    public double[] getLSxestimate(){
        
        //Convert the matrix to a double[]
        double[] tempVector = new double[this.LS_x_est.getRowDimension()];
        for (int j = 0; j <= (this.LS_x_est.getRowDimension() - 1); j++){
            tempVector[j] = this.LS_x_est.get(j, 0);
        }
        
        return tempVector;
    }
    
    /**
     * Returns the ML x estimate
     * @return A vector of type double[]
     */
    public double[] getMLxestimate(){
        
        //Convert the matrix to a double[]
        double[] tempVector = new double[this.ML_x_est.getRowDimension()];
        for (int j = 0; j <= (this.ML_x_est.getRowDimension() - 1); j++){
            tempVector[j] = this.ML_x_est.get(j, 0);
        }
        
        return tempVector;
    }
    
//    public double[] getMMVxestimate(){
//        
//        //Convert the matrix to a double[]
//        double[] tempVector = new double[this.y.getRowDimension()];
//        for (int j = 0; j <= (this.y.getRowDimension() - 1); j++){
//            tempVector[j] = this.y.get(j, 0);
//        }
//        
//        return tempVector;
//    }
    
    /**
     * Returns a encoded string with the methods ran
     * @return A encoded string
     */
    public String getMethodsRan(){
        
        return this.methodsRan;
    }
    
    /**
     * Execute Least Squares Solver
     */
    private void LS_method(){
        
        //Create a mx1 matrix
        Matrix x_est = new Matrix(this.A.getColumnDimension(),1);
        
        //Create a Random Permutation of this vector
        for (int j = 0; j <= x_est.getRowDimension() - 1; j++){
            x_est.set(j, 0, Math.random());
        }
        
        //Prepopulate the variable matrices
        double tau;
        Matrix C = new Matrix(this.A.getRowDimension(),this.A.getColumnDimension());
        Matrix B = new Matrix(this.A.getRowDimension(),this.A.getColumnDimension());
        Matrix eye = Matrix.identity(this.A.getRowDimension(),this.A.getColumnDimension());
        
        //debug
        System.out.println("LS - Method");
        System.out.println("  x_est dimension :" + Integer.toString(x_est.getRowDimension()) + "x" + Integer.toString(x_est.getColumnDimension()));
        System.out.println("  C dimension :" + Integer.toString(C.getRowDimension()) + "x" + Integer.toString(C.getColumnDimension()));
        System.out.println("  eye dimension :" + Integer.toString(eye.getRowDimension()) + "x" + Integer.toString(eye.getColumnDimension()));
        
        //Read in the number of iterations for LS method
        int numIter = Integer.parseInt(this.ParameterElement.getChild("LSParameters").getChildText("LSNumofIter"));
                
        //Perform the Calculation (.solve is Least Squares)
        for (int i= 0; i<= numIter-1; i++){
            
            //Solve Least Squares
                //C = A*diag(abs(x_est))*A';
                //tau=trace(C)*1e-6;
                //x_est=abs(x_est).*(A'*((C+tau*eye(n))\y));
                //x_est=x_est.*(x_est>0);
            
            //Form the diag() matrix for x_est
            for (int j = 0; j <= x_est.getRowDimension() - 1; j++){
                B.set(j, j, Math.abs(x_est.get(j, 0)));
            }

            C = A.times(B).times(A.transpose());
            tau = C.trace() * 1e-6;
            x_est.arrayTimesEquals(A.transpose().times(C.plus(eye.times(tau)).solve(y)));
            
            //Filter off all none positive elements of x_est
            for (int j = 0; j <= x_est.getRowDimension() - 1; j++){
                if (x_est.get(j, 0) <= 0){
                    x_est.set(j, 0, 0);
                }
            }
        }

        //Save the Result
        this.LS_x_est = x_est;
    }
    
    /**
     * Execute Maximum Liklihood Solver
     */
    private void ML_method(){
        
        //Create a mx1 matrix of ones
        Matrix x_est = new Matrix(this.A.getColumnDimension(),1,1);
        
        //Prepopulate the variable matrices
        Matrix z;
        Matrix smallXOffsets = new Matrix(x_est.getRowDimension(),1,1e-10);
        Matrix smallYOffsets = new Matrix(this.y.getRowDimension(),1,1e-10);
        Matrix ones = new Matrix(this.y.getRowDimension(),1,1);
        Matrix y_t = this.y.transpose();
        
        //PreCalculate the ArrayDivision Matrix
        Matrix ArrayDivision = smallYOffsets.plus(A.transpose().times(ones));
        
        //debug
        System.out.println("ML - Method");
        System.out.println("  x_est dimension :" + Integer.toString(x_est.getRowDimension()) + "x" + Integer.toString(x_est.getColumnDimension()));
        System.out.println("  ArrayDivision dimension :" + Integer.toString(ArrayDivision.getRowDimension()) + "x" + Integer.toString(ArrayDivision.getColumnDimension()));
        
        //Read in the number of iterations for ML method
        int numIter = Integer.parseInt(this.ParameterElement.getChild("MLParameters").getChildText("MLNumofIter"));

        //Perform the Calculation (.solve is Least Squares)
        for (int i= 0; i<= numIter-1; i++){
            
            //Solve ML
                //z = (A*x_est+1e-5);
                //x_est = x_est.*((A'*(y./z))./(1e-10+A'*ones(size(y))));
            
            z = A.times(x_est).plus(smallXOffsets);
            x_est.arrayTimesEquals((A.transpose().times(y.arrayRightDivide(z))).arrayRightDivide(ArrayDivision));
            
            //Filter off all none positive elements of x_est
            for (int j = 0; j <= x_est.getRowDimension() - 1; j++){
                if (x_est.get(j, 0) <= 0){
                    x_est.set(j, 0, 0);
                }
            }
        }
        
        this.ML_x_est = x_est;
    }
    
    /**
     * Execute Multiple Measurement Vector Solver
     * @return 
     */
    private void MMV_method(){
        
        double[] x_est = new double[this.A.getColumnDimension()];
        
    }
    
    /**
     * State machine to execute the solvers
     */
    public void executeDeConvolution(){
        
        //LS-Method
        if (Boolean.valueOf(this.ParameterElement.getChild("LSParameters").getChildText("LSSelected"))){
            
            this.LS_method();
            methodsRan = methodsRan + "LS";
        }
        
        //ML-Method
        if (Boolean.valueOf(this.ParameterElement.getChild("MLParameters").getChildText("MLSelected"))){
            
            this.ML_method();
            methodsRan = methodsRan + "ML";
        }
        
        //MMV-Method
        if (Boolean.valueOf(this.ParameterElement.getChild("MMVParameters").getChildText("MMVSelected"))){
            
            methodsRan = methodsRan + "MMV";
        }
    } 
    
    
}
