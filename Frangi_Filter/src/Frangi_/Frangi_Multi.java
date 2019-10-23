package Frangi_;

/* 
 * Copyright or © or Copr. Arnold Fertin 2019
 *
 * 
 *
 * This software is a computer program whose purpose is to perform image processing.
 *
 * This software is governed by the CeCILL-C license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL-C license as circulated
 * by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL-C license and that you
 * accept its terms.
 */
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_ALL;
import ij.process.ImageProcessor;

/**
 *
 * @author Arnold Fertin
 */
public class Frangi_Multi implements PlugInFilter
{
    private double sigma0;

    private int nOctave, qLevel;

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
        final ImagePlus imgv = FrangiFilter.exec(imp, sigma0, nOctave, qLevel, k, beta, smax);
        imgv.setTitle("Frangi Filter");
        imgv.show();
    }

    public boolean doDialog()
    {
        final GenericDialog gd = new GenericDialog("Frangi filter...", IJ.getInstance());
        gd.addNumericField("Sigma_0", 1.0, 2);
        gd.addNumericField("Number_of_octave", 1, 1);
        gd.addNumericField("Number_of_level_per_octave", 1, 1);
        gd.addNumericField("K", K, 2);
        gd.addNumericField("Beta", BETA, 2);
        gd.addCheckbox("S_max", true);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return false;
        }
        sigma0 = (double) gd.getNextNumber();
        nOctave = (int) gd.getNextNumber();
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
