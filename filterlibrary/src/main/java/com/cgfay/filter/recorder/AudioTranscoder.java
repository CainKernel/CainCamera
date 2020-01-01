package com.cgfay.filter.recorder;

import android.media.AudioFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * 音频倍速转码器, reference from ExoPlayer's SonicAudioProcessor
 */
public final class AudioTranscoder {

    /**
     * A value for various fields to indicate that the field's value is unknown or not applicable.
     */
    public static final int NO_VALUE = -1;

    /**
     * An empty, direct {@link ByteBuffer}.
     */
    private ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    /**
     * The maximum allowed playback speed in {@link #setSpeed(float)}.
     */
    public static final float MAXIMUM_SPEED = 8.0f;
    /**
     * The minimum allowed playback speed in {@link #setSpeed(float)}.
     */
    public static final float MINIMUM_SPEED = 0.1f;
    /**
     * The maximum allowed pitch in {@link #setPitch(float)}.
     */
    public static final float MAXIMUM_PITCH = 8.0f;
    /**
     * The minimum allowed pitch in {@link #setPitch(float)}.
     */
    public static final float MINIMUM_PITCH = 0.1f;
    /**
     * Indicates that the output sample rate should be the same as the input.
     */
    public static final int SAMPLE_RATE_NO_CHANGE = -1;

    /**
     * The threshold below which the difference between two pitch/speed factors is negligible.
     */
    private static final float CLOSE_THRESHOLD = 0.01f;

    /**
     * The minimum number of output bytes at which the speedup is calculated using the input/output
     * byte counts, rather than using the current playback parameters speed.
     */
    private static final int MIN_BYTES_FOR_SPEEDUP_CALCULATION = 1024;

    private int pendingOutputSampleRateHz;
    private int channelCount;
    private int sampleRateHz;

    private Sonic sonic;
    private float speed;
    private float pitch;
    private int outputSampleRateHz;

    private ByteBuffer buffer;
    private ShortBuffer shortBuffer;
    private ByteBuffer outputBuffer;
    private long inputBytes;
    private long outputBytes;
    private boolean inputEnded;

    /**
     * Creates a new audio processor.
     */
    public AudioTranscoder() {
        speed = 1f;
        pitch = 1f;
        channelCount = NO_VALUE;
        sampleRateHz = NO_VALUE;
        outputSampleRateHz = NO_VALUE;
        buffer = EMPTY_BUFFER;
        shortBuffer = buffer.asShortBuffer();
        outputBuffer = EMPTY_BUFFER;
        pendingOutputSampleRateHz = SAMPLE_RATE_NO_CHANGE;
    }

    /**
     * Sets the playback speed. The new speed will take effect after a call to {@link #flush()}.
     *
     * @param speed The requested new playback speed.
     * @return The actual new playback speed.
     */
    public float setSpeed(float speed) {
        this.speed = constrainValue(speed, MINIMUM_SPEED, MAXIMUM_SPEED);
        return this.speed;
    }

    /**
     * Sets the playback pitch. The new pitch will take effect after a call to {@link #flush()}.
     *
     * @param pitch The requested new pitch.
     * @return The actual new pitch.
     */
    public float setPitch(float pitch) {
        this.pitch = constrainValue(pitch, MINIMUM_PITCH, MAXIMUM_PITCH);
        return pitch;
    }

    /**
     * Sets the sample rate for output audio, in hertz. Pass {@link #SAMPLE_RATE_NO_CHANGE} to output
     * audio at the same sample rate as the input. After calling this method, call
     * {@link #configure(int, int, int)} to start using the new sample rate.
     *
     * @param sampleRateHz The sample rate for output audio, in hertz.
     * @see #configure(int, int, int)
     */
    public void setOutputSampleRateHz(int sampleRateHz) {
        pendingOutputSampleRateHz = sampleRateHz;
    }

    /**
     * Returns the specified duration scaled to take into account the speedup factor of this instance,
     * in the same units as {@code duration}.
     *
     * @param duration The duration to scale taking into account speedup.
     * @return The specified duration scaled to take into account speedup, in the same units as
     *     {@code duration}.
     */
    public long scaleDurationForSpeedup(long duration) {
        if (outputBytes >= MIN_BYTES_FOR_SPEEDUP_CALCULATION) {
            return outputSampleRateHz == sampleRateHz
                    ? scaleLargeTimestamp(duration, inputBytes, outputBytes)
                    : scaleLargeTimestamp(duration, inputBytes * outputSampleRateHz,
                    outputBytes * sampleRateHz);
        } else {
            return (long) ((double) speed * duration);
        }
    }

    /**
     * Configures the processor to process input audio with the specified format. After calling this
     * method, {@link #isActive()} returns whether the processor needs to handle buffers; if not, the
     * processor will not accept any buffers until it is reconfigured. Returns {@code true} if the
     * processor must be flushed, or if the value returned by {@link #isActive()} has changed as a
     * result of the call. If it's active, {@link #getOutputSampleRateHz()},
     * {@link #getOutputChannelCount()} and {@link #getOutputEncoding()} return the processor's output
     * format.
     *
     * @param sampleRateHz The sample rate of input audio in Hz.
     * @param channelCount The number of interleaved channels in input audio.
     * @param encoding The encoding of input audio.
     * @return {@code true} if the processor must be flushed or the value returned by
     *     {@link #isActive()} has changed as a result of the call.
     * @throws UnhandledFormatException Thrown if the specified format can't be handled as input.
     */
    public boolean configure(int sampleRateHz, int channelCount, int encoding)
            throws UnhandledFormatException {
        if (encoding != AudioFormat.ENCODING_PCM_16BIT) {
            throw new UnhandledFormatException(sampleRateHz, channelCount, encoding);
        }
        int outputSampleRateHz = pendingOutputSampleRateHz == SAMPLE_RATE_NO_CHANGE
                ? sampleRateHz : pendingOutputSampleRateHz;
        if (this.sampleRateHz == sampleRateHz && this.channelCount == channelCount
                && this.outputSampleRateHz == outputSampleRateHz) {
            return false;
        }
        this.sampleRateHz = sampleRateHz;
        this.channelCount = channelCount;
        this.outputSampleRateHz = outputSampleRateHz;
        return true;
    }

    /**
     * Returns whether the processor is configured and active.
     */
    public boolean isActive() {
        return Math.abs(speed - 1f) >= CLOSE_THRESHOLD || Math.abs(pitch - 1f) >= CLOSE_THRESHOLD
                || outputSampleRateHz != sampleRateHz;
    }

    /**
     * Returns the number of audio channels in the data output by the processor. The value may change
     * as a result of calling {@link #configure(int, int, int)} and is undefined if the instance is
     * not active.
     */
    public int getOutputChannelCount() {
        return channelCount;
    }

    /**
     * Returns the audio encoding used in the data output by the processor. The value may change as a
     * result of calling {@link #configure(int, int, int)} and is undefined if the instance is not
     * active.
     */
    public int getOutputEncoding() {
        return AudioFormat.ENCODING_PCM_16BIT;
    }

    /**
     * Returns the sample rate of audio output by the processor, in hertz. The value may change as a
     * result of calling {@link #configure(int, int, int)} and is undefined if the instance is not
     * active.
     */
    public int getOutputSampleRateHz() {
        return outputSampleRateHz;
    }

    /**
     * Queues audio data between the position and limit of the input {@code buffer} for processing.
     * {@code buffer} must be a direct byte buffer with native byte order. Its contents are treated as
     * read-only. Its position will be advanced by the number of bytes consumed (which may be zero).
     * The caller retains ownership of the provided buffer. Calling this method invalidates any
     * previous buffer returned by {@link #getOutput()}.
     *
     * @param inputBuffer The input buffer to process.
     */
    public void queueInput(ByteBuffer inputBuffer) {
        if (inputBuffer.hasRemaining()) {
            ShortBuffer shortBuffer = inputBuffer.asShortBuffer();
            int inputSize = inputBuffer.remaining();
            inputBytes += inputSize;
            sonic.queueInput(shortBuffer);
            inputBuffer.position(inputBuffer.position() + inputSize);
        }
        int outputSize = sonic.getSamplesAvailable() * channelCount * 2;
        if (outputSize > 0) {
            if (buffer.capacity() < outputSize) {
                buffer = ByteBuffer.allocateDirect(outputSize).order(ByteOrder.nativeOrder());
                shortBuffer = buffer.asShortBuffer();
            } else {
                buffer.clear();
                shortBuffer.clear();
            }
            sonic.getOutput(shortBuffer);
            outputBytes += outputSize;
            buffer.limit(outputSize);
            outputBuffer = buffer;
        }
    }

    /**
     * Queues an end of stream signal. After this method has been called,
     * {@link #queueInput(ByteBuffer)} may not be called until after the next call to
     * {@link #flush()}. Calling {@link #getOutput()} will return any remaining output data. Multiple
     * calls may be required to read all of the remaining output data. {@link #isEnded()} will return
     * {@code true} once all remaining output data has been read.
     */
    public void endOfStream() {
        sonic.queueEndOfStream();
        inputEnded = true;
    }

    /**
     * Returns a buffer containing processed output data between its position and limit. The buffer
     * will always be a direct byte buffer with native byte order. Calling this method invalidates any
     * previously returned buffer. The buffer will be empty if no output is available.
     *
     * @return A buffer containing processed output data between its position and limit.
     */
    public ByteBuffer getOutput() {
        ByteBuffer outputBuffer = this.outputBuffer;
        this.outputBuffer = EMPTY_BUFFER;
        return outputBuffer;
    }

    /**
     * Returns whether this processor will return no more output from {@link #getOutput()} until it
     * has been {@link #flush()}ed and more input has been queued.
     */
    public boolean isEnded() {
        return inputEnded && (sonic == null || sonic.getSamplesAvailable() == 0);
    }

    /**
     * Clears any state in preparation for receiving a new stream of input buffers.
     */
    public void flush() {
        sonic = new Sonic(sampleRateHz, channelCount, speed, pitch, outputSampleRateHz);
        outputBuffer = EMPTY_BUFFER;
        inputBytes = 0;
        outputBytes = 0;
        inputEnded = false;
    }

    /**
     * Resets the processor to its unconfigured state.
     */
    public void reset() {
        sonic = null;
        buffer = EMPTY_BUFFER;
        shortBuffer = buffer.asShortBuffer();
        outputBuffer = EMPTY_BUFFER;
        channelCount = NO_VALUE;
        sampleRateHz = NO_VALUE;
        outputSampleRateHz = NO_VALUE;
        inputBytes = 0;
        outputBytes = 0;
        inputEnded = false;
        pendingOutputSampleRateHz = SAMPLE_RATE_NO_CHANGE;
    }

    /**
     * Constrains a value to the specified bounds.
     *
     * @param value The value to constrain.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return The constrained value {@code Math.max(min, Math.min(value, max))}.
     */
    private static float constrainValue(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Scales a large timestamp.
     * <p>
     * Logically, scaling consists of a multiplication followed by a division. The actual operations
     * performed are designed to minimize the probability of overflow.
     *
     * @param timestamp The timestamp to scale.
     * @param multiplier The multiplier.
     * @param divisor The divisor.
     * @return The scaled timestamp.
     */
    private static long scaleLargeTimestamp(long timestamp, long multiplier, long divisor) {
        if (divisor >= multiplier && (divisor % multiplier) == 0) {
            long divisionFactor = divisor / multiplier;
            return timestamp / divisionFactor;
        } else if (divisor < multiplier && (multiplier % divisor) == 0) {
            long multiplicationFactor = multiplier / divisor;
            return timestamp * multiplicationFactor;
        } else {
            double multiplicationFactor = (double) multiplier / divisor;
            return (long) (timestamp * multiplicationFactor);
        }
    }


    /**
     * Exception thrown when a processor can't be configured for a given input audio format.
     */
    final class UnhandledFormatException extends Exception {

        public UnhandledFormatException(int sampleRateHz, int channelCount, int encoding) {
            super("Unhandled format: " + sampleRateHz + " Hz, " + channelCount + " channels in encoding "
                    + encoding);
        }

    }
}

