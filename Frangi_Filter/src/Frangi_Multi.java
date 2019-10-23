
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_ALL;
import ij.process.ImageProcessor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Arnold Fertin
 */
public class Frangi_Multi implements PlugInFilter
{
    private double sigma0, sigmaM;

    private int qLevel;

    private ImagePlus imp;

    private double k, beta;

    private boolean smax;

    static final double K = 0.5, BETA = 0.5;

    @Override
    public void run(ImageProcessor ip)
    {
        if (!doDialog())
        {
            return;
        }
        final ImagePlus imgv = FrangiFilter.exec(imp, sigma0, sigmaM, qLevel, k, beta, smax);
        imgv.setTitle("Frangi Filter");
        imgv.show();
    }

    public boolean doDialog()
    {
        final GenericDialog gd = new GenericDialog("Frangi filter...", IJ.getInstance());
        gd.addNumericField("Sigma_0", 1.0, 2);
        gd.addNumericField("Sigma_max", 1.0, 2);
        gd.addNumericField("Q_level", 1, 1);
        gd.addNumericField("K", K, 2);
        gd.addNumericField("Beta", BETA, 2);
        gd.addCheckbox("S_max", true);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return false;
        }
        sigma0 = (double) gd.getNextNumber();
        sigmaM = (double) gd.getNextNumber();
        qLevel = (int) gd.getNextNumber();
        k = (double) gd.getNextNumber();
        beta = (double) gd.getNextNumber();
        smax = gd.getNextBoolean();
        return true;
    }

    @Override
    public int setup(String string,
                     ImagePlus ip)
    {
        imp = ip;
        return DOES_ALL;
    }
}
