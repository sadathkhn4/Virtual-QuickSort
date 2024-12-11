import java.io.*;

/**
 * In the following basic bufferPool Class
 * Create a simple buffer pool class that manages a fixed-size array of byte
 * buffers.
 * Implement methods for reading data from and writing data to the buffers.
 * Use simple read and write operations without worrying about disk I/O or
 * asynchronous processing.
 *
 * The project requirements: in this version we change the buffer pool data
 * structure to be primitive array type of buffers
 * 
 * @author Sadath-Mohammed-msadath
 * @author Emadeldin-Abdrabou-emazied
 *
 * @version 2024-04-11
 */
public class BufferPool {
    private Buffer[] buffers;
    private int bufferSize;
    private int numOfBuffers;
    private int nonEmptyBuffers;
    private RandomAccessFile inputFile;
    private int cacheHits;
    private int diskReads;
    private int diskWrites;

    /**
     * Constructor
     * 
     * @param numBuffers
     *            integer number of buffers in the buffer pool
     * @param bufferSize
     *            integer buffer size
     * @param inFile
     *            RandomAccessFile object
     */
    public BufferPool(int numBuffers, int bufferSize, RandomAccessFile inFile) {
        this.buffers = new Buffer[numBuffers];
        this.bufferSize = bufferSize;
        this.numOfBuffers = numBuffers;
        this.inputFile = inFile;
        this.cacheHits = 0;
        this.diskReads = 0;
        this.diskWrites = 0;
        this.nonEmptyBuffers = 0;
    }


    /**
     * Number of buffers Getter
     * 
     * @return numOfBuffers integer value of total number buffers in the buffer
     *         pool
     */
    public int getNumberOfBuffers() {
        return numOfBuffers;
    }


    /**
     * Number of non empty buffers Getter
     * 
     * @return count integer number Of non empty buffers in the buffer pool
     */
    public int getNumberOfNonEmptyBuffers() {
        return nonEmptyBuffers;
    }


    /**
     * Getter of buffer size
     * 
     * @return bufferSize integer value of the buffer size, i.e., how many bytes
     *         in the buffer
     */
    public int getBufferSize() {
        return bufferSize;
    }


    /**
     * Getter of buffer status
     * 
     * @param buffID
     *            integer value that is used to locate the buffer ID (0-based
     *            index) of the stack element in the buffer pool to get its
     *            writing status
     * @return true if buffer's dirty bit is true and false if not
     */
    public boolean getBufferWritingStat(int buffID) {
        return buffers[buffID].getWritingStatus();
    }


    /**
     * @return the totalCacheHits it is simply the size of the list since we
     *         only add one for each call
     */
    public int getTotalCacheHits() {
        return cacheHits;
    }


    /**
     * @return the totalDiskReads it is simply the size of the list since we
     *         only add one for each call
     */
    public int getTotalDiskReads() {
        return diskReads;
    }


    /**
     * @return the totalDiskWrites it is simply the size of the list since we
     *         only add one for each call
     */
    public int getTotalDiskWrites() {
        return diskWrites;
    }


    /**
     * This method handles a reading request to read a record from a file
     * 
     * @param stuff
     *            array of bytes that would store a record that needs to be read
     *            from the file
     * @param byteNo
     *            integer number that refers the starting position that we need
     *            to read starting from it, i.e., position in file to start from
     * @param numBytes
     *            integer number that refers the amount of Bytes that are needed
     *            to be read, i.e., 4 Bytes in our case
     * @throws IOException
     */
    public void readBytes(byte[] stuff, int byteNo, int numBytes)
        throws IOException {
        int block = byteNo / bufferSize;
        int pos = byteNo % bufferSize;

        if (containsBlock(block) >= 0 && containsBlock(block) < numOfBuffers) {
            Buffer buffer = buffers[containsBlock(block)];
            System.arraycopy(buffer.getBufferContents(), pos, stuff, 0,
                numBytes);
            if (containsBlock(block) > 0) {
                moveBufferToFront(buffer);
            }
            cacheHits++;
        }
        else {
            byte[] bufferContents = new byte[bufferSize];
            inputFile.seek(block * bufferSize);
            int bytesRead = inputFile.read(bufferContents);
            if (bytesRead == bufferSize) {
                System.arraycopy(bufferContents, pos, stuff, 0, numBytes);
                Buffer newBuffer = new Buffer(bufferContents, false, bufferSize,
                    block);
                if (numOfBuffers > 1) {
                    updateBuffer(newBuffer);
                }
                else {
                    buffers[0] = newBuffer;
                }
                diskReads++;
            }
        }
    }


    private void moveBufferToFront(Buffer buff) {
        int buffIndex = containsBuffer(buff);
        for (int i = buffIndex; i >= 1; i--) {
            buffers[i] = buffers[i - 1];
        }
        buffers[0] = buff;
    }


    private int containsBuffer(Buffer buff) {
        int buffFound = -1;
        for (int i = 0; i < numOfBuffers; i++) {
            if (buffers[i] != null) {
                if (buffers[i].equals(buff)) {
                    buffFound = i;
                    break;
                }
            }
        }
        return buffFound;
    }


    private int containsBlock(int blockNum) {
        int buffFound = -1;
        for (int i = 0; i < numOfBuffers; i++) {
            if (buffers[i] != null) {
                if (buffers[i].getBlockID() == blockNum) {
                    buffFound = i;
                    break;
                }
            }
        }
        return buffFound;
    }


    private int findFirstNull() {
        int nullFound = -1;
        for (int i = 0; i < numOfBuffers; i++) {
            if (buffers[i] == null) {
                nullFound = i;
                break;
            }
        }
        return nullFound;
    }


    private void updateBuffer(Buffer buff) throws IOException {
        if (nonEmptyBuffers == 0) {
            buffers[0] = buff;
            nonEmptyBuffers++;
            return;
        }

        if (nonEmptyBuffers == numOfBuffers) {
            Buffer buffDirty = buffers[numOfBuffers - 1];
            if (buffDirty.getWritingStatus()) {
                writingBlockToDisk(buffDirty);
            }
            shiftBuffersRight();
            buffers[0] = buff;
            return;
        }

        int firstNullIndex = findFirstNull();
        if (firstNullIndex != -1) {
            shiftBuffersRightFromIndex(firstNullIndex);
            buffers[0] = buff;
            nonEmptyBuffers++;
        }
    }


    private void shiftBuffersRight() {
        for (int i = numOfBuffers - 1; i >= 1; i--) {
            buffers[i] = buffers[i - 1];
        }
    }


    private void shiftBuffersRightFromIndex(int startIndex) {
        for (int i = startIndex; i >= 1; i--) {
            buffers[i] = buffers[i - 1];
        }
    }


    private void writingBlockToDisk(Buffer buff) throws IOException {
        int seekPosition = buff.getBlockID() * bufferSize;
        inputFile.seek(seekPosition);
        inputFile.write(buff.getBufferContents());
        diskWrites++;
    }


    /**
     * Writing to buffer (writing from quicksort). This writing method is to be
     * called by the quicksort class.
     *
     * @param stuff
     *            the array of bytes that need to be written to the buffer
     * @param byteNo
     *            the integer number that refers to the position to start
     *            writing bytes
     * @param numBytes
     *            this parameter specifies the number of bytes to be written
     */

    public void writeBytes(byte[] stuff, int byteNo, int numBytes) {
        int block = byteNo / bufferSize;
        int pos = byteNo % bufferSize;
        int buffIndex = containsBlock(block);
        if (buffIndex > -1) {
            buffers[buffIndex].insertDataIntoBuff(stuff, pos, numBytes);
        }
    }


    /**
     * Flush the buffer pool.
     *
     * @return array containing cacheHits, diskReads, diskWrites
     * 
     * @throws IOException
     */

    public int[] flush() throws IOException {
        for (int i = 0; i < numOfBuffers; i++) {
            if (buffers[i].getWritingStatus()) {
                writingBlockToDisk(buffers[i]);
            }
        }
        int[] readings = new int[3];
        readings[0] = cacheHits;
        readings[1] = diskReads;
        readings[2] = diskWrites;
        for (int i = 0; i < numOfBuffers; i++) {
            buffers[i] = null;
        }
        return readings;
    }

    /**
     * This class is used to create an object buffer; it is a nested class in
     * the BufferPool class.
     *
     * @author EmadEldin-Abdrabou-emazied
     * @author Sadath-Mohammed-msadath
     * @version 2024-03-29
     */

    class Buffer {
        private byte[] block;
        private int blockID;
        private int buffSize;
        private boolean dirtyBitMarker;

        /**
         * Buffer constructor to instantiate a buffer object in the list of
         * buffer pools.
         *
         * @param dirtyBitMark
         *            boolean parameter to set the dirty bit field in the buffer
         * @param bufSize
         *            integer parameter to set the buffer size which is a block
         *            of bytes, i.e., multiple of 4 bytes records
         * @param bID
         *            integer parameter to set the block ID of buffer content
         */
        public Buffer(boolean dirtyBitMark, int bufSize, int bID) {
            this.block = new byte[bufSize];
            this.dirtyBitMarker = dirtyBitMark;
            this.buffSize = bufSize;
            this.blockID = bID;
        }


        /**
         * Buffer constructor with block value to instantiate a buffer object in
         * the list of buffer pools.
         *
         * @param blk
         *            byte array that sets the value of the block object in the
         *            buffer
         * @param dirtyBitMarker
         *            boolean parameter to set the dirty bit field in the buffer
         * @param bufferSize
         *            integer parameter to set the buffer size which is a block
         *            of bytes, i.e., multiple of 4 bytes records
         * @param blockID
         *            integer parameter to set the block ID of buffer content
         */
        public Buffer(
            byte[] blk,
            boolean dirtyBitMarker,
            int bufferSize,
            int blockID) {
            this.block = blk;
            this.dirtyBitMarker = dirtyBitMarker;
            this.buffSize = bufferSize;
            this.blockID = blockID;
        }


        /**
         * Getter for buffer size.
         *
         * @return buffSize integer value of buffer size, i.e., block size
         */
        public int getBuffSize() {
            return buffSize;
        }


        /**
         * Setter for buffer contents with block ID.
         *
         * @param bufContents
         *            array of bytes included in the buffer, i.e., block content
         * @param blkId
         *            integer number of the block ID number
         */
        public void setBufferContents(byte[] bufContents, int blkId) {
            this.block = bufContents;
            this.dirtyBitMarker = true;
            this.blockID = blkId;
        }


        /**
         * Insert data within a block at specific positions.
         *
         * @param data
         *            array of bytes that needs to be inserted in a buffer
         * @param position
         *            integer number of the position at buffer to insert data
         * @param len
         *            integer number of data.length to be inserted in the buffer
         */
        public void insertDataIntoBuff(byte[] data, int position, int len) {
            for (int i = 0; i < len; i++) {
                this.block[position] = data[i];
                position++;
            }
            this.dirtyBitMarker = true;
        }


        /**
         * Getter for buffer contents.
         *
         * @return block array of bytes included in the buffer, i.e., block
         *         content
         */
        public byte[] getBufferContents() {
            return block;
        }


        /**
         * Getter for buffer's writing status.
         *
         * @return true if any contents in the buffer changed and false if not
         */
        public boolean getWritingStatus() {
            return dirtyBitMarker;
        }


        /**
         * Getter for buffer's content Block ID.
         *
         * @return integer value of the Block ID
         */
        public int getBlockID() {
            return blockID;
        }
    }
}
