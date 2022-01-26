package wtf.choco.veinminer.network;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A utility class to allow for reading and writing of complex types to/from a byte array.
 */
public class PluginMessageByteBuffer {

    private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

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
     */
    public void writeVarInt(int value) {
        this.ensureWriting();

        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
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
     * Write a variable-length long.
     *
     * @param value the value to write
     */
    public void writeVarLong(long value) {
        this.ensureWriting();

        while ((value & -128L) != 0L) {
            this.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }

        this.writeByte((int) value);
    }

    /**
     * Read a variable-length long.
     *
     * @return the read value
     *
     * @throws IllegalStateException if the var long is too large
     */
    public long readVarLong() {
        this.ensureReading();

        long result = 0L;
        int size = 0;

        byte currentByte;

        do {
            currentByte = this.readByte();
            result |= (long) (currentByte & 127) << size++ * 7;
            if (size > 10) {
                throw new IllegalStateException("VarLong too big");
            }
        } while ((currentByte & 128) == 128);

        return result;
    }

    /**
     * Write a boolean primitive.
     *
     * @param value the value to write
     */
    public void writeBoolean(boolean value) {
        this.ensureWriting();
        this.outputStream.write(value ? (byte) 1 : 0);
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
     * Write a UTF-8 String.
     *
     * @param string the string to write
     */
    public void writeString(@NotNull String string) {
        this.ensureWriting();

        byte[] stringBytes = string.getBytes(CHARSET_UTF_8);
        this.writeByteArray(stringBytes);
    }

    /**
     * Read a UTF-8 String.
     *
     * @return the string
     */
    @NotNull
    public String readString() {
        this.ensureReading();
        return new String(readByteArray(), CHARSET_UTF_8);
    }

    /**
     * Write an array of bytes.
     *
     * @param bytes the bytes to write
     */
    public void writeBytes(byte[] bytes) {
        this.ensureWriting();
        this.outputStream.writeBytes(bytes);
    }

    /**
     * Write an array of bytes prefixed by a variable-length int.
     *
     * @param bytes the bytes to write
     */
    public void writeByteArray(byte[] bytes) {
        this.ensureWriting();

        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
    }

    /**
     * Read an array of bytes prefixed by a variable-length int.
     *
     * @return the byte array
     */
    public byte[] readByteArray() {
        this.ensureReading();

        int size = readVarInt();
        byte[] bytes = new byte[size];
        this.inputBuffer.get(bytes);

        return bytes;
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
     */
    public void writeByte(int value) {
        this.ensureWriting();
        this.outputStream.write((byte) value);
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
     * Write a {@link BlockPosition}.
     *
     * @param position the position to write
     */
    public void writeBlockPosition(@NotNull BlockPosition position) {
        this.ensureWriting();
        this.writeVarLong(position.pack());
    }

    /**
     * Read a {@link BlockPosition}.
     *
     * @return the BlockPosition
     */
    @NotNull
    public BlockPosition readBlockPosition() {
        this.ensureReading();
        return BlockPosition.unpack(readVarLong());
    }

    /**
     * Write a {@link NamespacedKey}.
     *
     * @param key the key to write
     */
    public void writeNamespacedKey(@NotNull NamespacedKey key) {
        this.ensureWriting();
        this.writeString(key.namespace());
        this.writeString(key.key());
    }

    /**
     * Read a {@link NamespacedKey}.
     *
     * @return the NamespacedKey
     */
    @NotNull
    public NamespacedKey readNamespacedKey() {
        this.ensureReading();
        return new NamespacedKey(readString(), readString());
    }

    /**
     * Get this byte buffer as a byte array.
     *
     * @return the byte array
     */
    public byte[] asByteArray() {
        this.ensureReading();
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
