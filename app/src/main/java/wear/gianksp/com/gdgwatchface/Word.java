package wear.gianksp.com.gdgwatchface;

/**
 * Created by giancarlosanchez on 25/07/15.
 */
public class Word {

    public String text;
    public int x;
    public int y;

    /**
     * Get line definition
     * @return
     */
    public static String[] getLines() {
        return  new String[] {
                "it is half ten",
                "quarter twenty",
                "five minutes to",
                "past one three",
                "two four five",
                "six seven eight",
                "nine ten eleven",
                "twelve am pm",
        };
    }

    /**
     * Get words
     * @return
     */
    public static String[] getWords() {
        return new String[] {
                "it", "is", "half", "ten",
                "quarter", "twenty",
                "five", "minutes", "to",
                "past", "one", "three",
                "two", "four", "five",
                "six", "seven", "eight",
                "nine", "ten", "eleven",
                "twelve", "am", "pm"};
    }

    /**
     * Get coordinates for X
     * @return
     */
    public static int[] getCoordinatesX() {
        return new int[] {
                0, 0, 0, 0,
                0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0};
    }

    /**
     * Get coordinates for Y
     * @return
     */
    public static int[] getCoordinatesY() {
        return new int[] {
                0, 0, 0, 0,
                1, 1,
                2, 2, 2,
                3, 3, 3,
                4, 4, 4,
                5, 5, 5,
                6, 6, 6,
                7, 7, 7};
    }
}
