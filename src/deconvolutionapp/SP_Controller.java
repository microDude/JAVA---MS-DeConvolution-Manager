/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

//imports
import Jama.Matrix;

/**
 *
 * @author Gregory
 */
public class SP_Controller {

    /**
     * 
     */
    public SP_Controller(){
        //Constructor code goes here
    }
    
    /**
     * 
     * @param array The integer array that needs to be averaged.
     * @param Avglength The window length
     * @return A int[] array that is averaged.
     */
    public int[] ARMA(int[] array,int Avglength){
        int[] PostProcess = new int[array.length];
        
        //Preform the Runnin-Average
        for (int i = Avglength;i <= ((array.length - Avglength) - 1);i++){
            PostProcess[i] = 0;
            for (int j = (-1*Avglength);j <= (Avglength - 1);j++){
                PostProcess[i] = PostProcess[i] + array[i + j];
            }
            PostProcess[i] = (int) PostProcess[i]/(2*Avglength);
        }

        //Curtail the ends of the signal
        for (int i = 0; i <= (Avglength - 1);i++){
            PostProcess[i] = PostProcess[Avglength];
        }
        for (int i = ((array.length - Avglength) - 1);i <= (array.length - 1); i++){
            PostProcess[i] = PostProcess[(array.length - (Avglength + 1))];
        }
        
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @return
     */
    public int[] PoissonEstimate(int[] array){
        int[] PostProcess = new int[array.length];
        
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param PercentofPeak
     * @return
     */
    public int[] RectangularWindow(int[] array,Double PercentofPeak){
        int[] PostProcess;
        int MaxPoint = 0;
        int MaxIndex = 0;
        int StartIndex = 0;
        int EndIndex = 0;
        
        PercentofPeak = (PercentofPeak/100);
        
        //Find the Max point and indexes
        int temp = 0;
        for (int i = 0;i<=(array.length - 1);i++){
            temp = array[i];
            if (!(i == 0)){
                if (temp > MaxPoint){
                    MaxPoint = temp;
                    MaxIndex = i;
                }
            }
        }
        
        //Find the Min Max percent indexes
        for (int i = MaxIndex;i >= 0;i--){
            if (array[i] < (int)(MaxPoint*PercentofPeak)){
                StartIndex = i;
                break;
            }
            else if (i == 0){
                StartIndex = 0;
                break;
            }
        }
        for (int i = MaxIndex;i <= (array.length - 1);i++){
            if (array[i] < (int)(MaxPoint*PercentofPeak)){
                EndIndex = i;
                break;
            }
            else if (i == (array.length - 1)){
                EndIndex = (array.length - 1);
                break;
            }
        }
        
        //Debugginh
        //System.out.println("MaxPoint: " + Integer.toString(MaxPoint));
        //System.out.println("MinCutIndex: " + Integer.toString(StartIndex));
        //System.out.println("MaxCutIndex: " + Integer.toString(EndIndex));
        
        //Call the normal Rectangular Window
        PostProcess = this.RectangularWindow(array, StartIndex, EndIndex);
        
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param StartIndex
     * @param EndIndex
     * @return
     */
    public int[] RectangularWindow(int[] array,int StartIndex, int EndIndex){
        int[] PostProcess = array;
        
        //zero out the samples around the convolution kernel
        for (int i = 0; i < StartIndex ;i++){
            PostProcess[i] = 0;
        }
        for (int i = (EndIndex + 1); i <= (PostProcess.length - 1) ;i++){
            PostProcess[i] = 0;
        }
        
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param PercentofPeak
     * @return
     */
    public int[] BlackmanWindow(int[] array,Double PercentofPeak){
        int[] PostProcess;
        
        //Call the normal Rectangular Window
        PostProcess = this.RectangularWindow(array, PercentofPeak);
        
        //Now apply Blackman Window to the result
        //Code goes here
           
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param StartIndex
     * @param EndIndex
     * @return
     */
    public int[] BlackmanWindow(int[] array,int StartIndex, int EndIndex){
        int[] PostProcess = new int[array.length];
        
        //Call the normal Rectangular Window
        PostProcess = this.RectangularWindow(array, StartIndex,EndIndex);
        
        //Now apply Blackman Window to the result
        //Code goes here
        
        return PostProcess;
    }
    
    private int[] BlackmanWindow_exp(int[] array){
        int[] PostProcess = array;
        int StartIndex = 0;
        int EndIndex = 0;
        
        //Generate exp() window
        Double[] RevWindow = new Double[5];
        Double[] ForWindow = new Double[5];
        for (int i = 0; i <= (RevWindow.length - 1); i++){
            RevWindow[i] = Math.exp((-1*i));
        }
        for (int i = 0; i <= (ForWindow.length - 1); i++){
            ForWindow[i] = RevWindow[(RevWindow.length - i) - 1];
        }
        
        //Find start and end samples, brute force
        for (int i = 0; i <= (array.length - 1); i++){
            if (array[i] > 0){
                StartIndex = i;
                break;
            }
        }
        for (int i = (array.length - 1); i >= 0; i--){
            if (array[i] > 0){
                EndIndex = i;
                break;
            }
        }
        
        //Apply exp() window
        for (int i = 0; i <= (ForWindow.length - 1); i++){
            PostProcess[(StartIndex + i)] = (int)(ForWindow[i] * PostProcess[(StartIndex + i)]);
        }
        for (int i = (RevWindow.length - 1); i >= 0; i--){
            PostProcess[(EndIndex + i)] = (int)(RevWindow[i] * PostProcess[(EndIndex + i)]);
        }

        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param numCounts
     * @return
     */
    public int[] RangeOffset(int[] array, int numCounts){
        int[] PostProcess = array;
        int temp;
        
        for (int i = 0; i <= (PostProcess.length - 1); i++){
            temp = (PostProcess[i] - numCounts);
            if (temp >= 0){
                PostProcess[i] = temp;
            }
            else{
                PostProcess[i] = 0;
            }
        }

        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @return
     */
    public int[] DomainOffset(int[] array){
        int[] PostProcess = new int[array.length];
        int StartIndex = 0;
        
        //Find start samples, brute force
        for (int i = 0; i <= (array.length - 1); i++){
            if (array[i] > 0){
                StartIndex = i;
                break;
            }
            else if( i == (array.length - 1)){
                StartIndex = 0;
                break;
            }
        }
        
        //Circular Shift
        for (int i = 0; i <= (PostProcess.length - 1); i++){
            if (i <= (array.length - StartIndex - 1)){
                PostProcess[i] = array[StartIndex + i];
            }
            else{ //truncate with zeros
                PostProcess[i] = 0;
            }   
        }
        
        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param highPeak
     * @return
     */
    public int[] NormalizePeak(int[] array, Double highPeak){
        int[] PostProcess = array;
        int MaxPoint = 0;
        
        //Find the Max point
        int temp = 0;
        for (int i = 0;i <= (array.length - 1);i++){
            temp = array[i];
            if (!(i == 0)){
                if (temp > MaxPoint){
                    MaxPoint = temp;
                }
            }
        }
        
        //Normalize
        for (int i = 0;i <= (PostProcess.length - 1);i++){
            PostProcess[i] = (int) ((PostProcess[i]*highPeak)/MaxPoint);
        }

        return PostProcess;
    }
    
    /**
     * 
     * @param array
     * @param TotaleV
     * @return
     */
    public int[] NormalizeEnergy(int[] array, Double TotaleV){
        int[] PostProcess = new int[array.length];
        
        return PostProcess;
    }
    
    /**
     * 
     * @param IONarray
     * @return
     */
    public Matrix buildConvolutionMatrix(int[] IONarray){
        double[][] A = new double[IONarray.length][IONarray.length];
        int[] temp = new int[IONarray.length];
        
        
        for(int i = 0;i <= (IONarray.length - 1); i++){
            
            //zero out the next index
            if (i > 0){
                temp[(i - 1)] = 0;  
            }

            //Circular Shift vector
            for(int c = 0;c <= (IONarray.length - 1); c++){
                if ((i+c) <= (IONarray.length - 1)){
                    temp[(i+c)] = IONarray[c];
                }
                else{
                    break;
                } 
            }
            
            //Save Vector to matrix
            for(int j = 0;j <= (IONarray.length - 1); j++){
                A[i][j] = temp[j];
            }

        }
        
        Matrix Ai = new Matrix(A);
        
        return Ai;
    } 
    
    
    /**
     * Builds a Convolution Matrix from a single Convolution vector.
     * 
     * This method takes a convolution vector and then linearly shifts the vector
     * into a matrix on the diagonal.  It returns a transpose of this matrix to
     * make it causal.
     * 
     * @param IONarray The original one-dimensional convolution array
     * @return A JAMA matrix type value, already transposed.
     */
    public Matrix buildConvolutionMatrix(double[] IONarray){
        double[][] A = new double[IONarray.length][IONarray.length];
        double[] temp = new double[IONarray.length];
        
        
        for(int i = 0;i <= (IONarray.length - 1); i++){
            
            //zero out the next index
            if (i > 0){
                temp[(i - 1)] = 0;  
            }

            //Circular Shift vector
            for(int c = 0;c <= (IONarray.length - 1); c++){
                if ((i+c) <= (IONarray.length - 1)){
                    temp[(i+c)] = IONarray[c];
                }
                else{
                    break;
                } 
            }
            
            //Save Vector to matrix
            for(int j = 0;j <= (IONarray.length - 1); j++){
                A[i][j] = temp[j];
            }

        }
        
        Matrix Ai = new Matrix(A);
        
        return Ai.transpose();
    } 
    
}
