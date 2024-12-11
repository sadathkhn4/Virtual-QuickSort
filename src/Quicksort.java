import java.nio.ByteBuffer;
import java.io.*;

/**
 * The class containing the main method.
 * 
 * @author Sadath-Mohammed-msadath
 * @author Emadeldin-Abdrabou-emazied
 * @version 2024-04-09
 */
// On my honor:
//
// - I have not used source code obtained from another student,
// or any other unauthorized source, either modified or
// unmodified.
//
// - All source code and documentation used in my program is
// either my original work, or was derived by me from the
// source code published in the textbook for this course.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

public class Quicksort {

    /**
     * @param args
     *            Command line parameters. See the project spec!!! %> java
     *            Quicksort
     *            <data-file-name> <numb-buffers> <stat-file-name>
     * 
     *            <data-file-name> is the file to be sorted. The sorting takes
     *            place in that file, so this program does modify the input data
     *            file. Be careful to keep a copy of the original when you do
     *            your testing.
     * 
     *            <numb-buffers> determines the number of buffers allocated for
     *            the buffer pool. This value will be in the range 1–20.
     *
     *            <stat-file-name> is the name of a file that your program will
     *            generate to store runtime statistics; see below for more
     *            information.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // get arguments data file name, number of buffers, and statistics file
        // name

        // the file name to be used for binary data generation
        String dataFile;

        // number of buffers in buffer pool and number of blocks in the file
        int numberOfBuffers;

        // output file that will collect statistics
        String statFile;

        if (args.length != 3) {
            System.err.println("Usage: %> java Quicksort "
                + "<dataFileName (string)>" + " <numbBuffers (integer number)>"
                + " <statFileName (string)>");
            // Exit with error code
            System.exit(1);
        }

        dataFile = args[0];
        numberOfBuffers = Integer.parseInt(args[1]);
        statFile = args[2];

        // generate binary file with the entered name (call binary file
        // generation method)
        // block numbers is set to 15 here as an arbitrary number of blocks
        // in a file
        // The following two statements will be disabled when come to submit to
        // the web-cat
        // FileGenerator datFileGen = new FileGenerator(dataFile, 10);
        //
        // datFileGen.generateFile(FileType.BINARY);

        File randDatFile = new File(dataFile);

        // instantiate the file that will be passed to the buffer pool
        // constructor
        RandomAccessFile inputBinaryFile = new RandomAccessFile(randDatFile,
            "rws");

        // work on the buffer pool
        // generate the bufferPool object by passing the generated binary
        // file and
        // number of buffer arguments to its constructor
        BufferPool buffPool = new BufferPool(numberOfBuffers, 4096,
            inputBinaryFile);

        // determine the position of the last record (0-based index) 1st record
        // is at position zero then last record at position of total number of
        // records (number of blocks * number of records per block)
        // int lastRecordPosition = (numBlocks * 1024) - 1;
        int lastRecordPosition = (int)(inputBinaryFile.length() / 4) - 1;

        long quickSortStartingTime = System.currentTimeMillis();

        // call QuickSort after initializing bufferPool object and
        // initialize the collection of statistics and passing them (writing
        // them) to the statFile ... this index represents the position of
        // record in the file
        quicksort(buffPool, 0, lastRecordPosition);

        long quickSortEndingTime = System.currentTimeMillis();

        long quickSortElapsedTime = quickSortEndingTime - quickSortStartingTime;

        // collect stats and flush
        int[] buffPoolStats = buffPool.flush();

        // Write statistics to the output file
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
            new FileWriter(statFile, true)))) {
            writer.println("Data File Name: " + dataFile);
            writer.println("Cache Hits: " + buffPoolStats[0]);
            writer.println("Disk Reads: " + buffPoolStats[1]);
            writer.println("Disk Writes: " + buffPoolStats[2]);
            writer.println("Runtime: " + quickSortElapsedTime
                + " milliseconds");
            writer.println(); // Add a separator between different runs if
                              // needed
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    // The array A will be replaced with the buffer pool object
    /**
     * Quicksort method
     * 
     * @param buffA
     *            Buffer object (a record to be sorted)
     * @param i
     *            integer record index of the position of the first record
     * @param j
     *            integer record index used for finding pivot it is the index of
     *            the
     *            position of the last record
     * @throws IOException
     */
    static void quicksort(BufferPool buffA, int i, int j) throws IOException {
        // Pick a pivot
        int pivotindex = findpivot(buffA, i, j);

        // Stick pivot at end
        swap(buffA, pivotindex, j);

        int t = i; // temporary

        if (getKeyOfRecord(buffA, t) == getKeyOfRecord(buffA, j)) {
            while (getKeyOfRecord(buffA, t) == getKeyOfRecord(buffA, j)
                && t <= j) {
                t++;
            }
            if (t > j) {
                return;
            }
        }

        // k will be the position of the first record in the right subarray
        int k = partition(buffA, i, j - 1, getKeyOfRecord(buffA, j));

        // check duplicates for optimization
        if (k == i && isDuplicate(buffA, i, j - 1, getKeyOfRecord(buffA,
            pivotindex))) {
            return;
        }

        // Put pivot in place
        swap(buffA, k, j);

        int t1; // variable to be used in outer loops below
        int t2; // variable to be used in inner loop below
        // Sort left partition
        if ((k - i) > 1) {
            if (k - i <= 15) {
                // if small sized data, then do basic sorting
                t1 = i;
                while (t1 <= k - 1) {
                    t2 = t1;
                    while ((t2 > 0) && getKeyOfRecord(buffA,
                        t2) < getKeyOfRecord(buffA, t2 - 1)) {
                        swap(buffA, t2, t2 - 1);
                        t2--;
                    }
                    t1++;
                }
            }
            else {
                // for larger data do quick sorting
                quicksort(buffA, i, k - 1);
            }
        }
        // Sort right partition
        if ((j - k) > 1) {
            if (j - k <= 15) {
                // if small sized data, then do basic sorting
                t1 = k + 1;
                while (t1 <= j) {
                    t2 = t1;
                    while ((t2 > 0) && getKeyOfRecord(buffA,
                        t2) < getKeyOfRecord(buffA, t2 - 1)) {
                        swap(buffA, t2, t2 - 1);
                        t2--;
                    }
                    t1++;
                }
            }
            else {
                // for larger data do quick sorting
                quicksort(buffA, k + 1, j);
            }
        }
    }


    /**
     * Function to get the key of a record given its record index
     * 
     * @param buffR
     *            is BufferPool object
     * @param startingIndex
     *            is staring index
     * @param lastIndex
     *            is last index
     * @param pivotKey
     *            is the pivot key used for quicksort
     * @return boolean
     * @throws IOException
     */
    static boolean isDuplicate(
        BufferPool buffR,
        int startingIndex,
        int lastIndex,
        short pivotKey)
        throws IOException {
        boolean duplicate = false;
        for (int i = startingIndex; i > lastIndex; i--) {
            if (getKeyOfRecord(buffR, i) != pivotKey) {
                duplicate = false;
                break;
            }
            duplicate = true && (getKeyOfRecord(buffR, i) == pivotKey);
        }
        return duplicate;
    }


    /**
     * Function to get the key of a record given its record index
     * 
     * @param buffF
     *            is BufferPool object
     * @param recordIndex
     *            index of the record
     * @return short
     * @throws IOException
     */
    static short getKeyOfRecord(BufferPool buffF, int recordIndex)
        throws IOException {
        byte[] tempRecord = new byte[4];
        buffF.readBytes(tempRecord, recordIndex * 4, 4);
        ByteBuffer bb = ByteBuffer.wrap(tempRecord);
        short key = bb.getShort();
        return key;
    }


    /**
     * Helper function to swap two records in the buffer pool
     * 
     * @param buffP
     *            BufferPool object
     * @param a
     *            integer index of a record position to be swapped with
     * @param b
     *            integer index of a record position to be swapped with
     * @throws IOException
     */
    static void swap(BufferPool buffP, int a, int b) throws IOException {
        byte[] tempRecordA = new byte[4];
        byte[] tempRecordB = new byte[4];

        buffP.readBytes(tempRecordA, a * 4, 4);
        buffP.readBytes(tempRecordB, b * 4, 4);

        // swapping
        buffP.writeBytes(tempRecordA, b * 4, 4);
        buffP.writeBytes(tempRecordB, a * 4, 4);
    }


    /**
     * @param buffA
     *            Buffer object (a record to be sorted)
     * @param left
     *            integer record index
     * @param right
     *            integer record index used for finding pivot for a record
     *            starting at position right
     * @param pivot
     *            Comparable pivot object for partitioning
     * @return left integer value of partition point for a record starting at
     *         position left
     * @throws IOException
     */
    static int partition(BufferPool buffA, int left, int right, short pivot)
        throws IOException {
        while (left <= right) { // Move bounds inward until they meet
            while (getKeyOfRecord(buffA, left) < pivot) {
                left++;
            }
            while ((right >= left) && (getKeyOfRecord(buffA, right) >= pivot)) {
                right--;
            }
            if (right > left) {
                swap(buffA, left, right);
            } // Swap out-of-place values
        }
        return left; // Return first position in right partition
    }

// /**
// * insertion sort method to be called for unsorted part after running quick
// * sort with (k-i) and (j-k) threshold values > 1
// *
// * @param A
// * Buffer object (a record to be sorted)
// * @param lo
// * integer record index used for lower index
// * @param hi
// * integer record index used for higher index
// */
// static void insertionSort1(BufferPool A, int lo, int hi)
// throws IOException {
// for (int i = lo + 1; i <= hi; i++) {
// short key = getKeyOfRecord(A, i);
// int j = i - 1;
// while (j >= lo && getKeyOfRecord(A, j) > key) {
// swap(A, j + 1, j);
// j--;
// }
// swap(A, j + 1, i);
// }
// }
//
//
// /**
// * Another implementation of insertion sort method to be called for unsorted
// * part after running quick sort with (k-i) and (j-k) threshold values > 1
// *
// * @param buffA
// * Buffer object (a record to be sorted)
// * @param left
// * integer record index used for lower index
// * @param right
// * integer record index used for higher index
// */
// static void insertionSort2(BufferPool buffA, int left, int right)
// throws IOException {
// for (int i = left + 1; i <= right; i++) {
// A.readBytes(key, i * 4, 4);
// int j = i - 1;
// while (j >= left && getKeyOfRecord(A, j) > ByteBuffer.wrap(key)
// .getShort()) {
// byte[] temp = new byte[4];
// A.readBytes(temp, j * 4, 4);
// A.writeBytes(temp, (j + 1) * 4, 4);
// j--;
// }
// A.writeBytes(key, (j + 1) * 4, 4);
// }
// }


    /**
     * 
     * @param buffA
     *            Buffer object (a record to be sorted)
     * @param i
     *            Buffer object (a record to be sorted)
     * @param j
     *            integer record index used for finding pivot for a record
     * @return pivot index
     * @throws IOException
     */
    static int findpivot(BufferPool buffA, int i, int j) throws IOException {
        return (i + j) / 2;
// int m = (i + j) / 2;
// int a = getKeyOfRecord(buffA, i);
// int b = getKeyOfRecord(buffA, m);
// int c = getKeyOfRecord(buffA, j);
// if ((a >= b && a <= c) || (a <= b && a >= c)) {
// return i;
// }
// else if ((b >= a && b <= c) || (b <= a && b >= c)) {
// return m;
// }
// else {
// return j;
// }
    }
}
