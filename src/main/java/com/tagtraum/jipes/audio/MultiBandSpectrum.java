/*
 * =================================================
 * Copyright 2011 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;

/**
 * <p>Represents an audio spectrum divided into bands as produced for example by
 * {@link #createLogarithmicBands(float, float, int)}.</p>
 *
 * <p>The bands <em>do not</em> have to be logarithmically spaced.</p>
 *
 * <p>Even though instances are meant to be immutable, all methods return
 * the original internally used arrays to increase efficiency and reduce
 * memory consumption. As a consequence you must <em>never</em> modify any
 * of those arrays returned to you.</p>
 *
 * Date: Aug 25, 2010
 * Time: 7:48:04 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MultiBandSpectrum extends AbstractAudioSpectrum implements Cloneable {

    private static final double LOG2 = Math.log(2.0);
    private int numberOfBands;
    private float[] bandBoundariesInHz;

    public MultiBandSpectrum(int frameNumber, final AudioSpectrum audioSpectrum, final float[] bandBoundariesInHz) {
        super(frameNumber, null, null, audioSpectrum.getAudioFormat());
        this.bandBoundariesInHz = bandBoundariesInHz;
        this.numberOfBands = bandBoundariesInHz.length-1;
        final float[] frequencies = audioSpectrum.getFrequencies();
        this.powers = computeBins(frequencies, audioSpectrum.getPowers());
        this.realData = new float[powers.length];
        this.imaginaryData = new float[powers.length];
        this.magnitudes = new float[powers.length];
        for (int i=0; i<bandBoundariesInHz.length-1; i++) {
            final float m = (float) Math.sqrt(powers[i]);
            this.magnitudes[i] = m;
            this.realData[i] = m;
        }
    }

    public MultiBandSpectrum(int frameNumber, final float[] real, final float[] imaginary, final AudioFormat audioFormat, final float[] bandBoundariesInHz) {
        super(frameNumber, null, null, audioFormat);
        this.realData = real;
        this.imaginaryData = imaginary;
        this.magnitudes = new float[real.length];
        this.powers = new float[real.length];

        for (int i=0; i<real.length; i++) {
            if (imaginary == null || imaginary[i] == 0) {
                this.magnitudes[i] = Math.abs(real[i]);
                this.powers[i] = real[i]*real[i];
            } else {
                this.powers[i] = real[i]*real[i] + imaginary[i]*imaginary[i];
                this.magnitudes[i] = (float)Math.sqrt(powers[i]);
            }
        }

        this.bandBoundariesInHz = bandBoundariesInHz;
        this.numberOfBands = bandBoundariesInHz.length-1;
    }


    public MultiBandSpectrum(final MultiBandSpectrum multiBandSpectrum) {
        super(multiBandSpectrum);
        this.bandBoundariesInHz = multiBandSpectrum.bandBoundariesInHz;
        this.numberOfBands = multiBandSpectrum.numberOfBands;
        if (multiBandSpectrum.magnitudes != null) this.magnitudes = multiBandSpectrum.magnitudes.clone();
        if (multiBandSpectrum.realData != null) this.realData = multiBandSpectrum.realData.clone();
        if (multiBandSpectrum.imaginaryData != null) this.imaginaryData = multiBandSpectrum.imaginaryData.clone();
        if (multiBandSpectrum.powers != null) this.powers = multiBandSpectrum.powers.clone();
    }

    /**
     * Creates logarithmically spaced frequency bands.
     *
     * @param lowFrequency lower boundary in Hz
     * @param highFrequency upper boundary in Hz
     * @param numberOfBands number of bands in between lower and upper boundary
     * @return array of frequency boundaries in Hz
     */
    public static float[] createLogarithmicBands(final float lowFrequency, final float highFrequency, final int numberOfBands) {
        if (numberOfBands == 1) return new float[] {lowFrequency, highFrequency};
        final double factor = numberOfBands / (Math.log(highFrequency / (double) lowFrequency) / LOG2);
        final float scale[] = new float[numberOfBands+1];
        scale[0] = lowFrequency;
        for (int i=1; i<numberOfBands+1; i++) {
            scale[i] = lowFrequency * (float)Math.pow(2, i/factor);
        }
        return scale;
    }

    private float[] computeBins(final float[] frequencies, final float[] values) {
        return computeBins(frequencies, values, values.length);
    }

    private float[] computeBins(final float[] frequencies, final float[] values, final int length) {
        final float[] bins = new float[numberOfBands+2];
        float currentBin = 0;
        int binIndex = 0;
        for (int i=0; i<frequencies.length; i++) {
            final double freq = frequencies[i];
            while (binIndex < bandBoundariesInHz.length && freq >= bandBoundariesInHz[binIndex]) {
                bins[binIndex] = currentBin;
                currentBin = 0;
                binIndex++;
            }
            currentBin += values[i];
        }
        bins[binIndex] = currentBin;
        // remove outOfBand bins
        final float[] inBandBins = new float[numberOfBands];
        System.arraycopy(bins, 1, inBandBins, 0, inBandBins.length);
        return inBandBins;
    }

    public MultiBandSpectrum derive(final float[] real, final float[] imaginary) {
        return new MultiBandSpectrum(getFrameNumber(), real, imaginary, getAudioFormat(), bandBoundariesInHz);
    }

    /**
     * Array with frequency values in Hz corresponding to the band boundaries.
     *
     * @return array of length numberOfBands+1
     * @see #getNumberOfBands()
     */
    public float[] getBandBoundaries() {
        return bandBoundariesInHz;
    }

    /**
     * Number of bands.
     *
     * @return number of bands
     * @see #getBandBoundaries()
     */
    public int getNumberOfBands() {
        return numberOfBands;
    }

    /**
     * Computes a bin for the given frequency. Returns -1, if the bin is not found.
     *
     * @param frequency frequency
     * @return bin
     */
    public int getBin(final float frequency) {
        float lastFreq = 0;
        for (int bin=0; bin<bandBoundariesInHz.length; bin++) {
            final float freq = bandBoundariesInHz[bin];
            if (frequency > lastFreq && frequency < freq) return bin;
        }
        return -1;
    }

    /**
     * Array with center frequency values in Hz corresponding to the bin numbers.<br/>
     * Note that these values are simply the means of the upper and lower boundaries for each bin.
     * Due to the logarithmic nature of the <a href="http://en.wikipedia.org/wiki/Equal_temperament">equal temperament</a>,
     * if one bin spans from 440hz to 880Hz, i.e. an octave, the mean frequency is not the same as the center semitone.
     *
     * @return array of length numberOfSamples/2 due to symmetry
     * @see #getFrequency(int)
     */
    public float[] getFrequencies() {
        final float[] frequencies = new float[numberOfBands];
        for (int i=0; i<frequencies.length; i++) {
            frequencies[i] = (bandBoundariesInHz[i]+bandBoundariesInHz[i+1]) / 2f;
        }
        return frequencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiBandSpectrum that = (MultiBandSpectrum) o;

        if (!Arrays.equals(bandBoundariesInHz, that.bandBoundariesInHz)) return false;
        if (!Arrays.equals(imaginaryData, that.imaginaryData)) return false;
        if (!Arrays.equals(realData, that.realData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = realData != null ? Arrays.hashCode(realData) : 0;
        result = 31 * result + (imaginaryData != null ? Arrays.hashCode(imaginaryData) : 0);
        result = 31 * result + (bandBoundariesInHz != null ? Arrays.hashCode(bandBoundariesInHz) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MultiBandSpectrum{" +
                "timestamp=" + getTimestamp() +
                ", frameNumber=" + getFrameNumber() +
                ", bandBoundariesInHz=" + bandBoundariesInHz +
                ", numberOfBands=" + numberOfBands +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final MultiBandSpectrum clone = (MultiBandSpectrum)super.clone();
        clone.numberOfBands = numberOfBands;
        clone.bandBoundariesInHz = bandBoundariesInHz.clone();
        return clone;
    }
}
