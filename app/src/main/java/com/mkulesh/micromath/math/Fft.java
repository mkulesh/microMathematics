package com.mkulesh.micromath.math;

import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

public class Fft
{
    // Direct Fourier Transform (no normalization by default)
    public void fft(CalculatedValue[] x, CalculatedValue[] out)
    {
        _ft(x, out, -1, false);
    }

    // Inverse Fourier Transform (normalization 1/N by default)
    public void ifft(CalculatedValue[] x, CalculatedValue[] out)
    {
        _ft(x, out, 1, true);
    }

    void _ft(CalculatedValue[] x, CalculatedValue[] out, double isign, boolean normalization)
    {
        if (x.length == 0)
        {
            return;
        }
        if (!_isPowerOf2(x.length))
        {
            _dft(x, out, isign, normalization);
        }
        else
        {
            _fftNumericalRecipes(x, out, isign, normalization);
        }
    }

    boolean _isPowerOf2(int n)
    {
        return (n & (n - 1)) == 0;
    }

    // FFT algorithm from the Numerical Recipes
    double[] _nrData = new double[]{ 0 };

    void _fftNumericalRecipes(CalculatedValue[] x, CalculatedValue[] out, double isign, boolean normalization)
    {
        // Prepare arrays
        final int nrDataSize = 2 * x.length + 1;
        if (_nrData.length != nrDataSize)
        {
            _nrData = new double[nrDataSize];
            Arrays.fill(_nrData, 0);
        }

        // Convert complex list to _nrData array
        _nrData[0] = 0;
        for (int k = 1; k < _nrData.length; k++)
        {
            _nrData[k] = k % 2 == 1 ? x[(k - 1) / 2].getReal() : x[(k - 1) / 2].getImaginary();
        }

        // Call the method from Numerical Recipes
        _four1(_nrData, x.length, isign);

        // Convert _nrData back to complex list
        double norm = normalization ? x.length : 1.0;
        for (int k = 0; k < out.length; k++)
        {
            out[k] = new CalculatedValue(CalculatedValue.ValueType.COMPLEX,
                    _nrData[2 * k + 1] / norm, _nrData[2 * k + 2] / norm);
        }
    }

    void _four1(double[] data, int nn, double isign)
    {
        int n, mmax, m, j, istep, i;
        double wtemp, wr, wpr, wpi, wi, theta;
        double tempr, tempi;

        n = nn << 1;
        j = 1;
        for (i = 1; i < n; i += 2)
        {
            if (j > i)
            {
                _swap(data, j, i);
                _swap(data, j + 1, i + 1);
            }
            m = nn;
            while (m >= 2 && j > m)
            {
                j -= m;
                m >>= 1;
            }
            j += m;
        }
        mmax = 2;
        while (n > mmax)
        {
            istep = mmax << 1;
            theta = isign * (6.28318530717959 / mmax);
            wtemp = FastMath.sin(0.5 * theta);
            wpr = -2.0 * wtemp * wtemp;
            wpi = FastMath.sin(theta);
            wr = 1.0;
            wi = 0.0;
            for (m = 1; m < mmax; m += 2)
            {
                for (i = m; i <= n; i += istep)
                {
                    j = i + mmax;
                    tempr = wr * data[j] - wi * data[j + 1];
                    tempi = wr * data[j + 1] + wi * data[j];
                    data[j] = data[i] - tempr;
                    data[j + 1] = data[i + 1] - tempi;
                    data[i] += tempr;
                    data[i + 1] += tempi;
                }
                wr = (wtemp = wr) * wpr - wi * wpi + wr;
                wi = wi * wpr + wtemp * wpi + wi;
            }
            mmax = istep;
        }
    }

    void _swap(double[] data, int i, int j)
    {
        final double t = data[i];
        data[i] = data[j];
        data[j] = t;
    }

    // Computes the discrete Fourier transform (DFT) of the given complex vector.
    void _dft(CalculatedValue[] x, CalculatedValue[] out, double isign, boolean normalization)
    {
        final int n = x.length;
        final double factor = isign * 2 * FastMath.PI / n;
        final CalculatedValue a = CalculatedValue.ZERO;
        for (int k = 0; k < n; k++)
        {
            out[k] = new CalculatedValue(CalculatedValue.ValueType.COMPLEX, 0.0, 0.0);
            for (int t = 0; t < n; t++)
            {
                a.setComplexValue(0.0, factor * t * k);
                a.exp(a);
                a.multiply(a, x[t]);
                out[k].add(out[k], a);
            }
            if (normalization)
            {
                out[k].multiply(1.0 / x.length);
            }
        }
    }
}
