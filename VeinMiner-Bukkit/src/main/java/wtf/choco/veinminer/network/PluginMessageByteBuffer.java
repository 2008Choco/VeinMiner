package wtf.choco.veinminer.network;

import java.nio.ByteBuffer;

public class PluginMessageByteBuffer {

    private final ByteBuffer buffer;

    public PluginMessageByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public PluginMessageByteBuffer() {
        this(ByteBuffer.allocate(Integer.MAX_VALUE));
    }

    public PluginMessageByteBuffer writeVarInt(int value) {
        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    public int readVarInt() {
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

    public PluginMessageByteBuffer writeBoolean(boolean value) {
        this.buffer.put(value ? (byte) 1 : 0);
        return this;
    }

    public boolean readBoolean() {
        return buffer.get() == 1;
    }

    public PluginMessageByteBuffer writeBytes(byte[] bytes) {
        this.buffer.put(bytes);
        return this;
    }

    public PluginMessageByteBuffer writeBytesAndLength(byte[] bytes) {
        this.writeBytes(bytes, bytes.length);
        return this;
    }

    public PluginMessageByteBuffer writeBytes(byte[] bytes, int length) {
        this.writeVarInt(length);
        this.writeBytes(bytes);
        return this;
    }

    public byte[] readBytes() {
        return readBytes(buffer.remaining());
    }

    public byte[] readBytes(int size) {
        int expectedSize = readVarInt();

        if (expectedSize > size) {
            throw new IllegalStateException("ByteArray with size " + expectedSize + " is bigger than allowed " + size);
        }

        byte[] bytes = new byte[expectedSize];
        this.readBytes(bytes);

        return bytes;
    }

    public void readBytes(byte[] destination) {
        this.buffer.get(destination);
    }

    public PluginMessageByteBuffer writeByte(int value) {
        this.buffer.put((byte) value);
        return this;
    }

    public byte readByte() {
        return buffer.get();
    }

    public byte[] asByteArray() {
        this.buffer.compact();
        return buffer.array();
    }

}
