/*
 * =================================================
 * Copyright 2015 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.jipes.audio;

import com.tagtraum.jipes.math.FullMatrix;
import com.tagtraum.jipes.math.Matrix;
import com.tagtraum.jipes.math.MutableMatrix;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * TestRealAudioMatrix.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRealAudioMatrix {


    @Test(expected = UnsupportedOperationException.class)
    public void testDerive() {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix = new RealAudioMatrix(frameNumber, realData, audioFormat);
        matrix.derive(new float[1], new float[1]);
    }

    @Test
    public void testDeriveFromMatrix() {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix = new RealAudioMatrix(frameNumber, realData, audioFormat);

        final MutableMatrix realData2 = new FullMatrix(6, 6);
        realData2.fill(6f);
        
        final AudioMatrix derived = matrix.derive(realData2, null);
        assertEquals(frameNumber, derived.getFrameNumber());
        assertEquals(realData2.getNumberOfRows(), derived.getNumberOfSamples());
        assertArrayEquals(realData2.getRow(0), derived.getRealData(), 0.000001f);
        assertArrayEquals(new float[realData2.getNumberOfColumns()], derived.getImaginaryData(), 0.000001f);
        assertEquals(audioFormat, derived.getAudioFormat());
        assertEquals((long) (frameNumber * 1000L / audioFormat.getSampleRate()), derived.getTimestamp());
        assertEquals((long) (frameNumber * 1000L * 1000L / audioFormat.getSampleRate()), derived.getTimestamp(TimeUnit.MICROSECONDS));

        assertArrayEquals(toMagnitudes(realData2.getRow(0), null), derived.getMagnitudes(), 0.000001f);
        assertArrayEquals(toPowers(realData2.getRow(0), null), derived.getPowers(), 0.000001f);
        assertArrayEquals(toMagnitudes(realData2.getRow(0), null), derived.getData(), 0.000001f);
    }

    @Test
    public void testBasics() {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix = new RealAudioMatrix(frameNumber, realData, audioFormat);

        assertEquals(frameNumber, matrix.getFrameNumber());
        assertEquals(realData.getNumberOfRows(), matrix.getNumberOfSamples());
        assertArrayEquals(realData.getRow(0), matrix.getRealData(), 0.000001f);
        assertArrayEquals(new float[realData.getNumberOfColumns()], matrix.getImaginaryData(), 0.000001f);
        assertEquals(audioFormat, matrix.getAudioFormat());
        assertEquals((long) (frameNumber * 1000L / audioFormat.getSampleRate()), matrix.getTimestamp());
        assertEquals((long) (frameNumber * 1000L * 1000L / audioFormat.getSampleRate()), matrix.getTimestamp(TimeUnit.MICROSECONDS));

        assertArrayEquals(toMagnitudes(realData.getRow(0), null), matrix.getMagnitudes(), 0.000001f);
        assertArrayEquals(toPowers(realData.getRow(0), null), matrix.getPowers(), 0.000001f);
        assertArrayEquals(toMagnitudes(realData.getRow(0), null), matrix.getData(), 0.000001f);
    }

    @Test
    public void testEqualsHashCode() {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix0 = new RealAudioMatrix(frameNumber, realData, audioFormat);
        final RealAudioMatrix matrix1 = new RealAudioMatrix(frameNumber, realData, audioFormat);
        final RealAudioMatrix matrix2 = new RealAudioMatrix(frameNumber+1, realData, audioFormat);

        assertEquals(matrix0, matrix1);
        assertEquals(matrix0.hashCode(), matrix1.hashCode());
        assertNotEquals(matrix0, matrix2);
        assertNotEquals(matrix0.hashCode(), matrix2.hashCode());
    }

    @Test(expected = CloneNotSupportedException.class)
    public void testClone() throws CloneNotSupportedException {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix = new RealAudioMatrix(frameNumber, realData, audioFormat);
        matrix.clone();
    }

    @Test
    public void testToString() {
        final int frameNumber = 3;
        final Matrix realData = new FullMatrix(5, 6);
        final AudioFormat audioFormat = new AudioFormat(10, 8, 2, true, false);
        final RealAudioMatrix matrix = new RealAudioMatrix(frameNumber, realData, audioFormat);

        assertEquals("RealAudioMatrix{audioFormat=PCM_SIGNED 10.0 Hz, 8 bit, stereo, 2 bytes/frame, , timestamp=300, frameNumber=3, rows=5, columns=6}", matrix.toString());
    }

    private static float[] toMagnitudes(final float[] real, final float[] imaginary) {
        final float[] magnitudes = new float[real.length];
        for (int i=0; i<real.length; i++) {
            final float v = imaginary == null ? 0 : imaginary[i];
            magnitudes[i] = (float)Math.sqrt(real[i] * real[i] + v * v);
        }
        return magnitudes;
    }

    private static float[] toPowers(final float[] real, final float[] imaginary) {
        final float[] powers = new float[real.length];
        for (int i=0; i<real.length; i++) {
            final float v = imaginary == null ? 0 : imaginary[i];
            powers[i] = real[i] * real[i] + v * v;
        }
        return powers;
    }
}
