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
package Frangi_;

import ij.ImagePlus;
import ij.ImageStack;
import imagescience.feature.Hessian;
import imagescience.image.Coordinates;
import imagescience.image.Dimensions;
import imagescience.image.FloatImage;
import imagescience.image.Image;

/**
 *
 * @author Arnold Fertin
 */
public final class FrangiFilter
{
    private FrangiFilter()
    {
    }

    private static FloatImage filter(final Image img,
                                     final double sigma,
                                     final double beta,
                                     final double gamma)
    {
        final Hessian hess = new Hessian();
        Image[] eigens = new Image[2];
        eigens = hess.run(new FloatImage(img), sigma, false).toArray(eigens);

        final Dimensions dims = img.dimensions();
        final double[] S = new double[dims.x * dims.y];
        final Coordinates coords = new Coordinates(0, 0);
        int i;
        for (coords.y = 0, i = 0; coords.y < dims.y; ++coords.y)
        {
            for (coords.x = 0; coords.x < dims.x; ++coords.x, i++)
            {
                final double l1 = eigens[0].get(coords);
                final double l2 = eigens[1].get(coords);
                S[i] = Math.sqrt(l1 * l1 + l2 * l2);
            }
        }
//        final double Smax;
//        if (smax)
//        {
//            Smax = new Max().evaluate(S);
//        }
//        else
//        {
//            final Percentile perc = new Percentile();
//            perc.setData(S);
//            final double q1 = perc.evaluate(25);
//            final double q3 = perc.evaluate(75);
//            Smax = q3 + 1.5d * (q3 - q1);
//        }
        final double gammaSq = 2 * gamma * gamma;
        final double betaSq = 2 * beta * beta;
        final FloatImage vesselness = new FloatImage(dims);
        for (coords.y = 0, i = 0; coords.y < dims.y; ++coords.y)
        {
            for (coords.x = 0; coords.x < dims.x; ++coords.x, i++)
            {
                double l1 = eigens[0].get(coords);
                double l2 = eigens[1].get(coords);
                if (Math.abs(l2) < Math.abs(l1))
                {
                    final double tmp = l2;
                    l2 = l1;
                    l1 = tmp;
                }
                if (l2 > 0)
                {
                    vesselness.set(coords, 0d);
                }
                else
                {
                    final double Rb = l1 / l2;
                    final double v = Math.exp(-Rb * Rb / betaSq) * (1 - Math.exp(-S[i] * S[i] / gammaSq));
                    vesselness.set(coords, v);
                }
            }
        }

        return vesselness;
    }

    public static ImagePlus exec(final ImagePlus imp,
                                 final double sigma,
                                 final double beta,
                                 final double gamma)
    {
        final Image img = Image.wrap(imp);
        return filter(img, sigma, beta, gamma).imageplus();
    }

    public static ImagePlus exec(final ImagePlus imp,
                                 final double sigma0,
                                 final int noct,
                                 final int qlvl,
                                 final double beta,
                                 final double gamma)
    {
        final double[] scales = createScaleRange(sigma0, noct, qlvl);
        final Image img = Image.wrap(imp);
        final Dimensions dims = new Dimensions(img.dimensions().x, img.dimensions().y, scales.length);
        final FloatImage vessMulti = new FloatImage(dims);
        for (int i = 0; i < scales.length; i++)
        {
            final FloatImage vesselness = filter(img, scales[i], beta, gamma);
            final Coordinates coords1 = new Coordinates(0, 0);
            final Coordinates coords2 = new Coordinates(0, 0, i);
            for (coords1.y = 0; coords1.y < dims.y; ++coords1.y)
            {
                for (coords1.x = 0; coords1.x < dims.x; ++coords1.x)
                {
                    final double v = vesselness.get(coords1);
                    coords2.x = coords1.x;
                    coords2.y = coords1.y;
                    vessMulti.set(coords2, v);
                }
            }
        }

        final ImagePlus res = vessMulti.imageplus();
        final ImageStack stk = res.getStack();
        for (int i = 0; i < scales.length; i++)
        {
            stk.setSliceLabel("sigma = " + scales[i], i + 1);
        }

        return res;
    }

    private static double width2sigma(final double width)
    {
        return 0.5d + width / (2d * Math.sqrt(3d));
    }

//    private static double[] createScaleRange(final double s0,
//                                             final double sm,
//                                             final int q)
//    {
//        final int m = ((int) Math.floor(((double) q) * Math.log(sm / s0) / Math.log(2d))) + 1;
//        final double[] scales = new double[m];
//        for (int i = 0; i < m; i++)
//        {
//            scales[i] = s0 * Math.pow(2, ((double) i) / q);
//        }
//        return scales;
//    }
    private static double[] createScaleRange(final double s0,
                                             final int noct,
                                             final int qlvl)
    {
        final int m = noct * qlvl + 1; //((int) Math.floor(((double) q) * Math.log(sm / s0) / Math.log(2d))) + 1;
        final double[] scales = new double[m];
        for (int i = 0; i < m; i++)
        {
            scales[i] = s0 * Math.pow(2, ((double) i) / qlvl);
//            System.out.println("" + scales[i]);
        }
        return scales;
    }
}
