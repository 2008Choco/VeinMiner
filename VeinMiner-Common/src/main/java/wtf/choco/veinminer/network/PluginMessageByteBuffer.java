package wtf.choco.veinminer.network;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.NotNull;

/**
 * A utility class to allow for reading and writing of complex types to/from a byte array.
 */
public class PluginMessageByteBuffer {

    private ByteBuffer inputBuffer;
    private ByteArrayOutputStream outputStream;

    /**
     * Construct a new {@link PluginMessageByteBuffer} wrapping a {@link ByteBuffer}.
     * Intended for reading data.
     *
     * @param buffer the buffer to wrap
     */
    public PluginMessageByteBuffer(@NotNull ByteBuffer buffer) {
        this.inputBuffer = buffer;
    }

    /**
     * Construct a new {@link PluginMessageByteBuffer} wrapping a byte array. Intended
     * for reading data.
     *
     * @param data the data to wrap
     */
    public PluginMessageByteBuffer(byte[] data) {
        this.inputBuffer = ByteBuffer.wrap(data);
    }

    /**
     * Construct a new {@link PluginMessageByteBuffer}. Intended for writing data.
     */
    public PluginMessageByteBuffer() {
        this.outputStream = new ByteArrayOutputStream();
    }

    /**
     * Write a variable-length integer.
     *
     * @param value the value to write
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public PluginMessageByteBuffer writeVarInt(int value) {
        this.ensureWriting();

        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    /**
     * Read a variable-length integer.
     *
     * @return the read value
     *
     * @throws IllegalStateException if the var int is too large
     */
    public int readVarInt() {
        this.ensureReading();

        int result = 0;
        int size = 0;

        byte currentByte;

        do {
            currentByte = this.readByte();
            result |= (currentByte & 127) << size++ * 7;

            if (size > 5) {
                throw new IllegalStateException("VarInt too big");
            }
        } while ((currentByte & 128) == 128);

        return result;
    }

    /**
     * Write a boolean primitive.
     *
     * @param value the value to write
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public PluginMessageByteBuffer writeBoolean(boolean value) {
        this.ensureWriting();

        this.outputStream.write(value ? (byte) 1 : 0);
        return this;
    }

    /**
     * Read a boolean primitive.
     *
     * @return the read value
     */
    public boolean readBoolean() {
        this.ensureReading();
        return inputBuffer.get() == 1;
    }

    /**
     * Write an array of bytes.
     *
     * @param bytes the bytes to write
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public PluginMessageByteBuffer writeBytes(byte[] bytes) {
        this.ensureWriting();

        this.outputStream.writeBytes(bytes);
        return this;
    }

    /**
     * Write an array of bytes prefixed by a variable-length int.
     *
     * @param bytes the bytes to write
     * @param length the length prefix to write
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public PluginMessageByteBuffer writeByteArray(byte[] bytes, int length) {
        this.ensureWriting();

        this.writeVarInt(length);
        this.writeBytes(bytes);
        return this;
    }

    /**
     * Read the remaining bytes in this buffer as an array.
     *
     * @return the bytes
     */
    public byte[] readBytes() {
        this.ensureReading();
        return readBytes(inputBuffer.remaining());
    }

    /**
     * Read a set amount of bytes from this buffer as an array.
     *
     * @param size the amount of bytes to read
     *
     * @return the bytes
     */
    public byte[] readBytes(int size) {
        this.ensureReading();

        int expectedSize = readVarInt();

        if (expectedSize > size) {
            throw new IllegalStateException("ByteArray with size " + expectedSize + " is bigger than allowed " + size);
        }

        byte[] bytes = new byte[expectedSize];
        this.readBytes(bytes);

        return bytes;
    }

    /**
     * Read a set amount of bytes from this buffer into a destination array. This method
     * will read up to {@code destination.length} bytes.
     *
     * @param destination the array in which the bytes should be written
     */
    public void readBytes(byte[] destination) {
        this.ensureReading();
        this.inputBuffer.get(destination);
    }

    /**
     * Write a raw byte.
     *
     * @param value the value to write
     *
     * @return this instance. Allows for chained method calls
     */
    @NotNull
    public PluginMessageByteBuffer writeByte(int value) {
        this.ensureWriting();

        this.outputStream.write((byte) value);
        return this;
    }

    /**
     * Read a raw byte.
     *
     * @return the byte
     */
    public byte readByte() {
        this.ensureReading();
        return inputBuffer.get();
    }

    /**
     * Get this byte buffer as a byte array.
     *
     * @return the byte array
     */
    public byte[] asByteArray() {
        this.ensureWriting();
        return outputStream.toByteArray();
    }

    private void ensureReading() {
        if (inputBuffer == null) {
            throw new IllegalStateException("Cannot read from a write-only buffer");
        }
    }

    private void ensureWriting() {
        if (outputStream == null) {
            throw new IllegalStateException("Cannot write to a read-only buffer");
        }
    }

}
