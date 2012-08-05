package de.pribluda.android.accmeter;

/**
 * represents FFT processed sample
 */
public class Sample {

    double sampleRate;
    double[] real;
    double[] imaginary;

    /**
     * sample rate of original data
     * @return
     */
    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * real part of trasnformation
     * @return
     */
    public double[] getReal() {
        return real;
    }

    public void setReal(double[] real) {
        this.real = real;
    }

    /**
     * imaginary part of transformation
     * @return
     */
    public double[] getImaginary() {
        return imaginary;
    }

    public void setImaginary(double[] imaginary) {
        this.imaginary = imaginary;
    }
}
