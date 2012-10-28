/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deconvolutionapp;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

//JFreeChart Utilities
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Gregory
 */
public class XYChartHelpers {
    
    ChartType selectedChartType;
    Dimension selectedchartDimension;
    
    public XYChartHelpers(ChartType chartType,Dimension chartDimension){
        this.selectedChartType = chartType;
        this.selectedchartDimension = chartDimension;
    }
    
    public JPanel returnFullChart(double[] Xaxis, int[] Yaxis){

        return this.createPanel(this.createDataset(Xaxis, Yaxis));

    }
    
    public JPanel returnFullChart(double[] Xaxis, double[] Yaxis){

        return this.createPanel(this.createDataset(Xaxis, Yaxis));

    }
    
    public JPanel returnFullChart(double[] Yaxis){

        //Create the Xaxis as a linear scale of the Yaxis
        double[] Xaxis = new double[Yaxis.length]; 
        for(int i = 0;i <= (Yaxis.length - 1); i++){
            Xaxis[i] = i;
        }
        
        return this.createPanel(this.createDataset(Xaxis, Yaxis));

    }
    
    public XYDataset createDataset(double[] Xaxis, int[] Yaxis){
        
        XYSeries vector = new XYSeries("VectorData");
        
        for (int i = 0; i <= (Xaxis.length - 1); i++){
            vector.add(Xaxis[i], Yaxis[i]);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(vector);

        return dataset;
    }
    
    public XYDataset createDataset(double[] Xaxis, double[] Yaxis){
        
        XYSeries vector = new XYSeries("VectorData");
        
        for (int i = 0; i <= (Xaxis.length - 1); i++){
            vector.add(Xaxis[i], Yaxis[i]);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(vector);

        return dataset;
    }
    
    public JPanel createPanel(XYDataset dataSet) {
        JFreeChart chart = this.createChart(dataSet);
        ChartPanel panel = new ChartPanel(chart);
        
        switch (this.selectedChartType){
            case MZChart:{
                panel.setMouseWheelEnabled(false);
                panel.setMouseZoomable(false);
                panel.setDomainZoomable(true);
                panel.setRangeZoomable(false);
                
                break;
            }
            case IonChart:{
                panel.setMouseWheelEnabled(false);
                panel.setDomainZoomable(false);
                panel.setRangeZoomable(false);
                break;
            }
            case ResultChart:{
                panel.setMouseWheelEnabled(false);
                panel.setDomainZoomable(true);
                panel.setRangeZoomable(true);
                break;
            }
            default:{
                panel.setMouseWheelEnabled(false);
                panel.setDomainZoomable(false);
                panel.setRangeZoomable(false);
                break;
            }
        }
        
        panel.setSize(this.selectedchartDimension);

        return panel;
        
    }
    
    private JFreeChart createChart(XYDataset dataset) {

        // create the chart...
        switch (this.selectedChartType){
            case MZChart:{
                JFreeChart chart = ChartFactory.createXYLineChart(
                null,                           // chart title
                "m/z",                          // x axis label
                "Counts",                       // y axis label
                dataset,                        // data
                PlotOrientation.VERTICAL,
                false,                          // include legend
                false,                          // tooltips
                false                           // urls
                );
                
                // get a reference to the plot for further customisation...
                XYPlot plot = (XYPlot) chart.getPlot();
                
                plot.setBackgroundPaint(Color.WHITE);
                //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
                plot.setDomainGridlinePaint(Color.BLACK);
              
                plot.setRangeGridlinesVisible(false);

                plot.setDomainPannable(true);
                plot.setRangePannable(false);

                XYLineAndShapeRenderer renderer
                        = (XYLineAndShapeRenderer) plot.getRenderer();
                renderer.setBaseShapesVisible(false);
                renderer.setBaseShapesFilled(false);
                renderer.setSeriesPaint(0, Color.BLUE);

                // change the auto tick unit selection to integer units only...
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                return chart;
            }
            case IonChart:{
                JFreeChart chart = ChartFactory.createXYLineChart(
                null,                        // chart title
                "eV",                       // x axis label
                "Counts",                   // y axis label
                dataset,                    // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,                       // tooltips
                false                       // urls
                );
                
                // get a reference to the plot for further customisation...
                XYPlot plot = (XYPlot) chart.getPlot();

                plot.setBackgroundPaint(Color.BLACK);
                //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
                plot.setDomainGridlinePaint(Color.white);

                plot.setRangeGridlinesVisible(false);

                plot.setDomainPannable(true);
                plot.setRangePannable(false);

                XYLineAndShapeRenderer renderer
                        = (XYLineAndShapeRenderer) plot.getRenderer();
                renderer.setBaseShapesVisible(false);
                renderer.setBaseShapesFilled(false);
                renderer.setSeriesPaint(0, Color.YELLOW);

                // change the auto tick unit selection to integer units only...
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
       
                return chart;
            }
            case ResultChart:{
                JFreeChart chart = ChartFactory.createXYLineChart(
                null,                        // chart title
                "eV",                       // x axis label
                "Counts",                   // y axis label
                dataset,                    // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,                       // tooltips
                false                       // urls
                );
                
                // get a reference to the plot for further customisation...
                XYPlot plot = (XYPlot) chart.getPlot();

                plot.setBackgroundPaint(Color.BLACK);
                //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
                plot.setDomainGridlinePaint(Color.white);

                plot.setRangeGridlinesVisible(false);

                plot.setDomainPannable(true);
                plot.setRangePannable(false);

                XYLineAndShapeRenderer renderer
                        = (XYLineAndShapeRenderer) plot.getRenderer();
                renderer.setBaseShapesVisible(false);
                renderer.setBaseShapesFilled(false);
                renderer.setSeriesPaint(0, Color.YELLOW);

                // change the auto tick unit selection to integer units only...
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
       
                return chart;
            }
            default:{
                JFreeChart chart = ChartFactory.createXYLineChart(
                null,                        // chart title
                "eV",                       // x axis label
                "Counts",                   // y axis label
                dataset,                    // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,                       // tooltips
                false                       // urls
                );
                
                // get a reference to the plot for further customisation...
                XYPlot plot = (XYPlot) chart.getPlot();

                plot.setBackgroundPaint(Color.BLACK);
                //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
                plot.setDomainGridlinePaint(Color.white);

                plot.setRangeGridlinesVisible(false);

                plot.setDomainPannable(true);
                plot.setRangePannable(false);

                XYLineAndShapeRenderer renderer
                        = (XYLineAndShapeRenderer) plot.getRenderer();
                renderer.setBaseShapesVisible(false);
                renderer.setBaseShapesFilled(false);
                renderer.setSeriesPaint(0, Color.YELLOW);

                // change the auto tick unit selection to integer units only...
                NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

                return chart;
            }
        }

    }
    
}
