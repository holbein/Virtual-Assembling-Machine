
/**
 * This class is there to handle the different possible number of bits, that an object of the class {@link Vam} can use.
 * Contains the static classes {@link int8Width}, {@link int16Width} and  {@link int32Width}, that implement the static interface {@link Handler}
 * and handle the number of bits, corresponding to the number in the class names.
 * @author VictorOle
 * @author SBester001
 * @version 1.2.1
 */
public class RegisterWidth {
    /**
     * Turns <code>value</code> into the corresponding {@link String} of bits with <code>nBytes</code> groups of bytes.<p>
     * byteToString(200, 1) = "11001000" (= -56)</p><p>
     * byteToString(400, 2) = "00000001 1001000"</p>
     * @param value number that should be converted into a {@link String} of bits
     * @param nBytes number of bytes, that should appear in the returned {@link String}
     * @return {@link String} of bits with spaces dividing it into groups of bytes
     */
    public static String byteToString(int value, int nBytes) {

        StringBuilder sb = new StringBuilder(9*nBytes);

        for (int byteId = nBytes-1; byteId >= 0; --byteId) {
            sb.append(
                String.format("%8s", Integer.toBinaryString((value >> (8*byteId)) & 0xFF)).replace(' ', '0')
            );
            
            if (byteId != 0) sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Turns <code>value</code> into the corresponding {@link String} of 8 bits.<p>
     * @param value number that should be converted into a {@link String} of bits
     * @return {@link String} of bits
     * @see #byteToString(int value, int nBytes)
     */
    public static String byteToString(int value) {
        return byteToString(value, 1);
    }

    /**
     * Interface for the classes {@link int8Width}, {@link int16Width} and {@link int32Width}.
     */
    public static interface Handler {
        /**
         * @return current number of bits
         * @see int8Width#width()
         * @see int16Width#width()
         * @see int32Width#width()
         */
        public int width();
        /**
         * Checks if there is an overflow in <code>value</code>.
         * @param value to be checked
         * @return true if the value is above the current max value or below the current min value, otherwise returns false
         * @see int8Width#isOverflow(int value)
         * @see int16Width#isOverflow(int value)
         * @see int32Width#isOverflow(int value)
         */
        boolean isOverflow (int value);
        /**
         * Casts <code>value</code> into the current number of bits.
         * @param value to be cast
         * @return cast value
         * @see int8Width#cast(int value)
         * @see int16Width#cast(int value)
         * @see int32Width#cast(int value)
         */
        public int cast(int value);
        /**
         * Turns <code>value</code> into the corresponding {@link String} of bits.
         * @param value to be turned into a {@link String} of bits
         * @return {@link String} of bits
         * @see int8Width#toBinaryString(int value)
         * @see int16Width#toBinaryString(int value)
         * @see int32Width#toBinaryString(int value)
         * @see RegisterWidth#byteToString(int value, int nBytes)
         */
        public String toBinaryString(int value);
    };

    /**
     * Use an object of this class if the number of bytes used is 1.
     */
    public static class int8Width implements Handler {
        /**
         * @return 8 - the number of bits in a byte
         * @see Handler#width()
         */
        public int width() { return 8; }

        /**
         * Checks if there is an overflow in <code>value</code>.
         * @param value to be checked
         * @return true if the value is above <code>Byte.MAX_VALUE</code> or below <code>Byte.MIN_VALUE</code>, otherwise returns false
         * @see Handler#isOverflow(int value)
         */
        public boolean isOverflow (int value) {
            return (value < Byte.MIN_VALUE || Byte.MAX_VALUE < value);
        }

        /**
         * Casts <code>value</code> into 8 bits.<br>
         * cast(200) = -56
         * @param value to be cast
         * @return cast value
         * @see Handler#cast(int value)
         */
        public int cast (int value) { return (byte) value; }

        /**
         * Turns <code>value</code> into the corresponding {@link String} of bits with 1 group of bytes.
         * @param value to be turned into a {@link String} of bits
         * @return {@link String} of 8 bits
         * @see Handler#toBinaryString(int value)  
         * @see RegisterWidth#byteToString(int value, int nBytes)
         */
        public String toBinaryString(int value) {
            return byteToString(value, 1);
        }
    };

    /**
     * Use an object of this class if the number of bytes used is 2.
     */
    public static class int16Width implements Handler {
        /**
         * @return 16 - the number of bits in a short
         * @see Handler#width()
         */
        public int width() { return 16; }

        /**
         * Checks if there is an overflow in <code>value</code>.
         * @param value to be checked
         * @return true if the value is above <code>Short.MAX_VALUE</code> or below <code>Short.MIN_VALUE</code>, otherwise returns false
         * @see Handler#isOverflow(int value)
         */
        public boolean isOverflow (int value) {
            return (value < Short.MIN_VALUE || Short.MAX_VALUE < value);
        }

        /**
         * Casts <code>value</code> into 16 bits.<br>
         * cast(40000) = -25536
         * @param value to be cast
         * @return cast value
         * @see Handler#cast(int value)
         */
        public int cast(int value) { return (short) value; }

        /**
         * Turns <code>value</code> into the corresponding {@link String} of bits with 2 group of bytes.
         * @param value to be turned into a {@link String} of bits
         * @return {@link String} of 2 times 8 bits, separated by " "
         * @see Handler#toBinaryString(int value)  
         * @see RegisterWidth#byteToString(int value, int nBytes)
         */
        public String toBinaryString(int value) {
            return byteToString(value, 2);
        }
    };

    /**
     * Use an object of this class if the number of bytes used is 4.
     */
    public static class int32Width implements Handler {
        /**
         * @return 32 - the number of bits in an int
         * @see Handler#width()
         */
        public int width() { return 32; }

        /**
         * Checks if there is an overflow in <code>value</code>.
         * @param value to be checked
         * @return true if the value is above <code>Integer.MAX_VALUE</code> or below <code>Integer.MIN_VALUE</code>, otherwise returns false
         * @see Handler#isOverflow(int value)
         */
        public boolean isOverflow (int value) {
            // not really usefull
            return (value < Integer.MIN_VALUE || Integer.MAX_VALUE < value);
        }

        /**
         * Casts <code>value</code> into 32 bits.<br>
         * cast(2000000000) = -294967296
         * @param value to be cast
         * @return cast value
         * @see Handler#cast(int value)
         */
        public int cast(int value) { return value; }

        /**
         * Turns <code>value</code> into the corresponding {@link String} of bits with 4 group of bytes.
         * @param value to be turned into a {@link String} of bits
         * @return {@link String} of 4 times 8 bits, each separated by " "
         * @see Handler#toBinaryString(int value)  
         * @see RegisterWidth#byteToString(int value, int nBytes)
         */
        public String toBinaryString(int value) {
            return byteToString(value, 4);
        }
    };
}
