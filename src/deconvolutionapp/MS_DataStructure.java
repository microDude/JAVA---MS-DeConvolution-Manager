/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

//imports
import java.io.*;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author Gregory
 */
public class MS_DataStructure implements Serializable {
    protected File myFile;
    protected double[] ionVectorArray;
    protected double[] MZVector;
    protected short[][] AbundenceMatrix;
    
    //Constructor
    public MS_DataStructure(){
       
    }
    
    public void setFileName(File newName){
        this.myFile = newName;
    }
    
    public void setIonVectorArray(double[] vector){
        this.ionVectorArray = vector;
    }
    
    public void setMZVector(double[] vector){
        this.MZVector = vector;
    }
        
    public void setAbundenceMatrix(short[][] matrix){
        this.AbundenceMatrix = matrix;
    }
    
    public File getFileDirectory() throws Exception{
        if (this.myFile.exists()) {
            return this.myFile;
        }
        else {
            throw new Exception("getFileDirectory(), No Data exists yet");
        }
    }
    
    public double[] getIonVectorArray(){
        return this.ionVectorArray;
    }

    public double[] getMZVector(){
        return this.MZVector;
    }

    public short[][] getAbundenceMatrix(){
        return this.AbundenceMatrix;
    }
    
    public int[] getAbundenceMatrix_Row(int RowIndex){
        
        //Preallocate the array
        int[] RowData = new int[this.ionVectorArray.length];
        
        System.out.println("Ion Vector Length = " + this.ionVectorArray.length);
        
        for (int i = 0; i <= (this.ionVectorArray.length - 1); i++){
            RowData[i] = this.AbundenceMatrix[RowIndex][i];
        }
        
        return RowData;
    }
    
    public int[] getAbundenceMatrix_Column(int ColumnIndex){
        
        //Preallocate the array
        int[] ColumnData = new int[this.MZVector.length];
        
        for (int i = 0; i <= (this.MZVector.length - 1); i++){
            ColumnData[i] = this.AbundenceMatrix[i][ColumnIndex];
        }
        
        return ColumnData;
    }
    
    public int[] getTotalMZRangeArray(){
            //prepopulate the array
            int [] MZCountsArray = new int[this.MZVector.length];
            
            //Assign Values
            for (int i = 0; i <= (this.MZVector.length - 1); i++){
                MZCountsArray[i] = 0;
                for (int j = 0; j <= (this.ionVectorArray.length - 1); j++){
                    MZCountsArray[i] = MZCountsArray[i] + this.AbundenceMatrix[i][j];
                }
            }
   
        
        return MZCountsArray;
    }
    
    public int[] getTotalIonRangeArray(int StartingIndex, int EndingIndex) throws Exception {
        
        if (EndingIndex > StartingIndex){
            //Int array to integrate over
            int[] integral = new int[this.ionVectorArray.length];

            //Assign Values
            for (int j = 0; j <= (this.ionVectorArray.length - 1); j++){
                int Temp = 0;
                for (int i = StartingIndex; i <= (EndingIndex - 1); i++){
                    Temp = Temp + this.AbundenceMatrix[i][j];
                }
                integral[j] = Temp;
            }
            
            return integral;
            
        }
        else{
            throw new Exception("Ending Index is larger then Starting Index");
        }

    }
    
    public double[] getTotalIonRangeArrayDouble(int StartingIndex, int EndingIndex) throws Exception {
        
        if (EndingIndex > StartingIndex){
            //Int array to integrate over
            double[] integral = new double[this.ionVectorArray.length];

            //Assign Values
            for (int j = 0; j <= (this.ionVectorArray.length - 1); j++){
                int Temp = 0;
                for (int i = StartingIndex; i <= (EndingIndex - 1); i++){
                    Temp = Temp + this.AbundenceMatrix[i][j];
                }
                integral[j] = (double) Temp;
            }
            
            return integral;
            
        }
        else{
            throw new Exception("Ending Index is larger then Starting Index");
        }

    }
    
    public int getMZindex(double MZvalue) throws Exception{
        int index;
        
        index = Arrays.binarySearch(this.MZVector, MZvalue);
        
        //Only if the binary search did not find a direct match
        if (index < 0) {
            index = -(index) - 1;
            
            //System.out.println("LUT index: " + Integer.toString(index) + " " + "MZValue: " + Double.toString(MZvalue));
            
            if (index < 0 || index > this.MZVector.length) {              
                throw new Exception("MZValue is outside of the MZVector Array");   
            }
            else if(index == 0){
                //Leave index at 0
                index = 0;
            }
            else if(index == this.MZVector.length){
                index = (this.MZVector.length - 1);
            }
            else {
                // Find nearest point
                double d0 = Math.abs(this.MZVector[index - 1] - MZvalue);
                double d1 = Math.abs(this.MZVector[index] - MZvalue);
                index = (d0 <= d1) ? index - 1 : index;
            }
        }
        //Else() Return the exact match
          
        return index;
        
    }
    
    public int getIONindex(double IONvalue) throws Exception{
        int index;
        
        index = Arrays.binarySearch(this.ionVectorArray, IONvalue);
        
        //Only if the binary search did not find a direct match
        if (index < 0) {
            index = -(index) - 1;
            
            //System.out.println("LUT index: " + Integer.toString(index) + " " + "MZValue: " + Double.toString(MZvalue));
            
            if (index < 0 || index > this.ionVectorArray.length) {              
                throw new Exception("ION Value is outside of the ION Vector Array");   
            }
            else if(index == 0){
                //Leave index at 0
                index = 0;
            }
            else if(index == this.ionVectorArray.length){
                index = (this.ionVectorArray.length - 1);
            }
            else {
                // Find nearest point
                double d0 = Math.abs(this.ionVectorArray[index - 1] - IONvalue);
                double d1 = Math.abs(this.ionVectorArray[index] - IONvalue);
                index = (d0 <= d1) ? index - 1 : index;
            }
        }
        //Else() Return the exact match
          
        return index;
        
    }
    
}
