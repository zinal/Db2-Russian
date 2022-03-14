package net.ifxrand;

import java.sql.SQLException;
import java.util.Random;

public class IfxRand {

    /**
     * Generate a random alphanumerical string.
     * @param len Output string length
     * @return Random output string of the specified length
     * @throws SQLException
     */
    public static String randomString(int len)
            throws SQLException {
        if (len<=0)
            return "";
        if (len>=1000000)
            throw new SQLException("String length too big: " + len);
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'

        return new Random().ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(len)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

}
