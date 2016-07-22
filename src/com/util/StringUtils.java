package com.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String EMPTY_STRING = "";

    public static int charCount(String s, char c) {
        if (s == null) {
            return 0;
        }
        int count = 0;
        int size = s.length();
        for (int i = 0; i < size; i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean isBlank(String value) {
        if (value == null || value.length() == 0) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isSpaceChar(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(String value1, String value2) {
        if (value1 == null) {
            return value2 == null;
        }
        return value1.equals(value2);
    }

    public static String[] split(String text, String delimiter) {
        if (text == null) {
            return new String[0];
        }
        List<String> values = new ArrayList<String>();
        for (StringTokenizer st = new StringTokenizer(text, delimiter); st.hasMoreTokens();) {
            String value = st.nextToken();
            values.add(value);
        }
        return values.toArray(new String[0]);
    }

    public static int[] splitInts(String text, String delimiter) {
        if (text == null) {
            return new int[0];
        }
        StringTokenizer st = new StringTokenizer(text, delimiter);
        int[] values = new int[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            values[i++] = Integer.parseInt(value);
        }
        return values;
    }

    public static String join(String[] values, String delimiter) {
        if (values == null) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(delimiter);
                }
                sb.append(values[i]);
            }
            return sb.toString();
        }
    }

    public static String join(int[] values, String delimiter) {
        if (values == null) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(delimiter);
                }
                sb.append(values[i]);
            }
            return sb.toString();
        }
    }

    public static String trim(String text, long length) {
        if (text == null || text.length() == 0) {
            return "";
        }
        int index;
        char[] chars = text.toCharArray();
        for (index = 0; index < chars.length; index++) {
            if (chars[index] < 256) {
                length--;
            } else {
                length -= 2;
            }
            if (length <= 0) {
                break;
            }
        }
        if (index < chars.length) {
            text = new String(chars, 0, index) + "...";
        }
        return text;
    }

    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString();
    }

    public static String extract(String[] patterns, String text) {
        for (String pattern : patterns) {
            String result = extract(pattern, text);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String extract(String pattern, String text) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static String render(String template, Map<String, String> variables) {
        StringBuffer result = new StringBuffer();
        StringBuffer property = new StringBuffer();
        int j = template.length();
        int k = 0;
        for (int i = 0; i < j; i++) {
            char c = template.charAt(i);
            switch (k) {
                case 1: // '\001'
                    if (c == '{') {
                        k = 2;
                        property = new StringBuffer();
                        break;
                    }
                    if (c == '$') {
                        result.append('$');
                    } else {
                        k = 0;
                        result.append('$');
                        result.append(c);
                    }
                    break;

                case 2: // '\002'
                    if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                        k = 2;
                        property.append(c);
                        break;
                    }
                    k = 0;
                    if (c == '}') {
                        String value = variables.get(property.toString());
                        if (value != null) {
                            result.append(value);
                            break;
                        }
                    }
                    result.append("${");
                    result.append(property);
                    result.append(c);
                    break;

                case 0: // '\0'
                    if (c == '$')
                        k = 1;
                    else
                        result.append(c);
                    break;

                default:
                    break;
            }
        }

        switch (k) {
            case 1: // '\001'
                result.append('$');
                break;

            case 2: // '\002'
                result.append("${");
                result.append(property.toString());
                break;
        }
        return result.toString();
    }

    public static String replace(String text, String pattern, String value) {
        if (text == null) {
            return null;
        }
        int i = 0;
        // Make sure that oldString appears at least once before doing any processing.
        if ((i = text.indexOf(pattern, i)) >= 0) {
            // Use char []'s, as they are more efficient to deal with.
            char[] string2 = text.toCharArray();
            char[] newString2 = value.toCharArray();
            int oLength = pattern.length();
            StringBuilder buf = new StringBuilder(string2.length);
            buf.append(string2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            // Replace all remaining instances of oldString with newString.
            while ((i = text.indexOf(pattern, i)) > 0) {
                buf.append(string2, j, i - j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(string2, j, string2.length - j);
            return buf.toString();
        }
        return text;
    }

    public static String stripHTML(String input) {
        StringBuilder output = new StringBuilder();
        boolean inHTML = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '<') {
                inHTML = true;
            } else {
                if (c == '>') {
                    inHTML = false;
                } else {
                    if (!inHTML) {
                        output.append(c);
                    }
                }
            }
        }
        return (output.toString());
    }

    public static String escapeHTML(String in) {
        if (in == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (char c : in.toCharArray()) {
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String quoteJavascript(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        int ln = text.length();
        for (int i = 0; i < ln; i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'' || c == '\\' || c == '>' || c < 0x20) {
                StringBuilder b = new StringBuilder(ln + 4);
                b.append(text.substring(0, i));
                while (true) {
                    if (c == '"') {
                        b.append("\\\"");
                    } else if (c == '\'') {
                        b.append("\\'");
                    } else if (c == '\\') {
                        b.append("\\\\");
                    } else if (c == '>') {
                        b.append("\\>");
                    } else if (c < 0x20) {
                        if (c == '\n') {
                            b.append("\\n");
                        } else if (c == '\r') {
                            b.append("\\r");
                        } else if (c == '\f') {
                            b.append("\\f");
                        } else if (c == '\b') {
                            b.append("\\b");
                        } else if (c == '\t') {
                            b.append("\\t");
                        } else {
                            b.append("\\x");
                            int x = c / 0x10;
                            b.append((char)
                                    (x < 0xA ? x + '0' : x - 0xA + 'A'));
                            x = c & 0xF;
                            b.append((char)
                                    (x < 0xA ? x + '0' : x - 0xA + 'A'));
                        }
                    } else {
                        b.append(c);
                    }
                    i++;
                    if (i >= ln) {
                        return b.toString();
                    }
                    c = text.charAt(i);
                }
            } // if has to be escaped
        } // for each characters
        return text;
    }

    public static String substring(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) {
            return null;
        }
        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) {
            endIndex = text.length();
        }
        return text.substring(startIndex, endIndex);
    }
}
