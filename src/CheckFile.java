import java.io.*;

/**
 * CheckFile: Check to see if a file is sorted. This assumes that each record
 * is a pair of short ints with the first short being the key value
 *
 * @author CS3114/5040 Instructors and TAs
 * 
 * @version 2024-03-22
 * 
 */
public class CheckFile {

    /**
     * This method checks a file to see if it is properly sorted.
     *
     * @param filename
     *            a string containing the name of the file to check
     * @return true if the file is sorted, false otherwise
     * @throws Exception
     *             either an IOException or a FileNotFoundException
     */
    public static boolean check(String filename) throws Exception {
        DataInputStream dis;
        dis = new DataInputStream(new BufferedInputStream(new FileInputStream(
            filename)));

        boolean isError = false;
        int reccnt = 0;

        // smallest short possible, nothing is less than it:
        short prev = Short.MIN_VALUE;
        short curr;

        try {
            while (true) {
                reccnt++;
                curr = dis.readShort(); // reads the key from file.
                dis.readShort(); // reads and ignores value.
                // or we could do: dis.skipBytes(2);
                if (prev > curr) {
                    isError = true;
                }
                prev = curr; // gets ready for next comparison
            }
        }
        catch (EOFException e) {
            System.out.println(reccnt + " records processed");
        }
        dis.close();
        return !isError;
    }
}
