
/**
 *
 * @author victor
 */
public class RegisterWidth {
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

    public static String byteToString(int value) {
        return byteToString(value, 1);
    }

    public static interface Handler {
        public int width();
        // Check for overflow
        boolean isOverflow (int value);
        public int cast(int value);
        public String toBinaryString(int value);
    };

    public static class int8Width implements Handler {
        public int width() { return 8; }

        public boolean isOverflow (int value) {
            return (value < Byte.MIN_VALUE || Byte.MAX_VALUE < value);
        }

        public int cast (int value) { return (byte) value; }

        public String toBinaryString(int value) {
            return byteToString(value, 1);
        }
    };

    public static class int16Width implements Handler {
        public int width() { return 16; }

        public boolean isOverflow (int value) {
            return (value < Short.MIN_VALUE || Short.MAX_VALUE < value);
        }

        public int cast(int value) { return (short) value; }

        public String toBinaryString(int value) {
            return byteToString(value, 2);
        }
    };

    public static class int32Width implements Handler {
        public int width() { return 32; }

        public boolean isOverflow (int value) {
            // Really?
            return (value < Integer.MIN_VALUE || Integer.MAX_VALUE < value);
        }

        public int cast(int value) { return value; }

        public String toBinaryString(int value) {
            return byteToString(value, 4);
        }
    };

}
