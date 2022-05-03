package net.ifxrepo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 *
 * @author zinal
 */
public class KeyQuery {

    private final String key;        // hash of normalized SQL
    private final String sqlNorm;    // normalized SQL
    private final String sqlSample;  // original SQL example

    public KeyQuery(String sql) {
        this.sqlSample = sql;
        this.sqlNorm = normalize(sql);
        this.key = hash(this.sqlNorm);
    }

    public KeyQuery(KeyQuery x) {
        this.key = x.key;
        this.sqlNorm = x.sqlNorm;
        this.sqlSample = x.sqlSample;
    }

    public String getKey() {
        return key;
    }

    public String getSqlNorm() {
        return sqlNorm;
    }

    public String getSqlSample() {
        return sqlSample;
    }

    public static String normalize(String sql) {
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
                                if (c3=='\'')
                                    ++ix;
                                else
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
                    if (isNumeric(c1)) {
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
        return sb.toString();
    }

    public static boolean isNumeric(char c) {
        return (c>='0') && (c<='9');
    }

    public static String hash(String v) {
        try {
            return Base64.getEncoder()
                    .encodeToString(MessageDigest.getInstance("SHA-256")
                            .digest(v.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception ex) {
            throw new RuntimeException("hash(sql) failed", ex);
        }
    }

}
