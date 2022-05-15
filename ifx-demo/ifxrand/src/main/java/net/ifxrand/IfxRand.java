package net.ifxrand;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;

public class IfxRand {

    /**
     * Generate a random alphanumerical string.
     * @param len Output string length
     * @return Random output string of the specified length
     * @throws SQLException
     */
    public static String rand_str(int len)
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

    /**
     * Calculate an SHA-256 hash over the input string, returning Base64-encoded value
     * @param v Input string
     * @return Base64-encoded SHA-256 hash value
     */
    public static String sha256_str(String v) {
        if (v==null)
            return null;
        try {
            return Base64.getEncoder()
                    .encodeToString(MessageDigest.getInstance("SHA-256")
                            .digest(v.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception ex) {
            return "sha256_str failed";
        }
    }

    /**
     * Normalize SQL by replacing literal parameters with parameter markers.
     * @param sql Input SQL
     * @return Normalized SQL
     */
    public static String sql_norm(String sql) {
        if (sql==null)
            return null;
        final StringBuilder sb = new StringBuilder();
        boolean hasSpace = false;
        for (int ix = 0; ix < sql.length(); ++ix) {
            final char c1 = sql.charAt(ix);
            switch (c1) {
                case '\n': case '\r': case '\t': case ' ':
                    if (!hasSpace) {
                        sb.append(' ');
                        hasSpace = true;
                    }
                    break;
                case '\'':
                    hasSpace = false;
                    sb.append("\'?\'");
                    while (ix+1 < sql.length()) {
                        final char c2 = sql.charAt(ix+1);
                        if (c2=='\'') {
                            if (ix+2 < sql.length()) {
                                final char c3 = sql.charAt(ix+2);
                                ++ix;
                                if (c3!='\'')
                                    break;
                            }
                        }
                        ++ix;
                    }
                    break;
                case '\"':
                    hasSpace = false;
                    sb.append(c1);
                    while (ix+1 < sql.length()) {
                        final char c2 = sql.charAt(ix+1);
                        sb.append(c2);
                        if (c2=='\"') {
                            if (ix+2 < sql.length()) {
                                final char c3 = sql.charAt(ix+2);
                                if (c3=='\"') {
                                    sb.append(c3);
                                    ++ix;
                                } else {
                                    ++ix;
                                    break;
                                }
                            }
                        }
                        ++ix;
                    }
                    break;
                default:
                    hasSpace = false;
                    if (isNumeric(c1) && isPrevNumSep(sql, ix)) {
                        sb.append('?');
                        while (ix+1 < sql.length()) {
                            final char c2 = sql.charAt(ix+1);
                            if (! (isNumeric(c2) || (c2=='.')) ) {
                                break;
                            }
                            ++ix;
                        }
                    } else {
                        sb.append(c1);
                    }
            }
        }
        return sb.toString().trim();
    }

    private static boolean isNumeric(char c) {
        return (c>='0') && (c<='9');
    }

    private static boolean isPrevNumSep(String sql, int ix) {
        if (ix == 0)
            return false;
        char c = sql.charAt(ix-1);
        if (c=='\t' || c=='\n' || c=='\r' || c==' ' || c==','
                || c=='=' || c=='<' || c=='>'
                || c=='+' || c=='-' || c=='/' || c=='*'
                || c=='[' || c==']' || c=='(' || c==')')
            return true;
        return false;
    }

}
