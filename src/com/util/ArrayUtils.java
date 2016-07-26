package com.util;

import java.util.*;

public class ArrayUtils {
    public static <T> boolean isEmpty(T[] values) {
        return values == null || values.length == 0;
    }

    public static <T> int indexOf(T[] values, T value) {
        for (int i = 0; i < values.length; i++) {
            if (equals(values[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> boolean contains(T[] values, T value) {
        if (values == null) {
            return false;
        }
        return indexOf(values, value) != -1;
    }

    public static <T> void swap(T[] values, int i, int j) {
        T tmp = values[i];
        values[i] = values[j];
        values[j] = tmp;
    }

    public static <T> boolean isNull(T... values) {
        if (values == null) {
            return true;
        }
        for (T value : values) {
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    public static <T> T[] toArray(T... values) {
        return values;
    }

    public static <T> T[] merge(T[] values1, T[] values2) {
        if (values1 == null) {
            return values2;
        }
        if (values2 == null) {
            return values1;
        }
        T[] newValues = (T[])java.lang.reflect.Array.newInstance(values1.getClass().getComponentType(), values1.length + values2.length);
        System.arraycopy(values1, 0, newValues, 0, values1.length);
        System.arraycopy(values2, 0, newValues, values1.length, values2.length);
        return newValues;
    }

    public static <T> T[] insert(T[] values, T value, int index) {
        T[] newValues = (T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length + 1);
        if (index > 0) {
            System.arraycopy(values, 0, newValues, 0, index);
        }
        newValues[index] = value;
        if (index < values.length) {
            System.arraycopy(values, index, newValues, index + 1, values.length - index);
        }
        return newValues;
    }

    public static <T> T[] append(T[] values, T value) {
        T[] newValues = (T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length + 1);
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = value;
        return newValues;
    }

    public static <T> T[] remove(T[] values, T[] removes) {
        if (values == null) {
            return null;
        }
        if (removes == null) {
            return values;
        }
        for (T remove : removes) {
            int index = indexOf(values, remove);
            if (index != -1) {
                values = remove(values, index);
            }
        }
        return values;
    }

    public static <T> T[] remove(T[] values, T value) {
        int index = indexOf(values, value);
        if (index != -1) {
            values = remove(values, index);
        }
        return values;
    }

    public static <T> T[] remove(T[] values, int index) {
        T[] newValues = (T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length - 1);
        if (index > 0) {
            System.arraycopy(values, 0, newValues, 0, index);
        }
        if (index < values.length - 1) {
            System.arraycopy(values, index + 1, newValues, index, values.length - index - 1);
        }
        return newValues;
    }

    public static <T> T[] subArray(T[] values, int start) {
        return subArray(values, start, values.length);
    }

    public static <T> T[] subArray(T[] values, int start, int limit) {
        int size = Math.min(values.length - start, limit);
        T[] section = (T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), size);
        System.arraycopy(values, start, section, 0, size);
        return section;
    }

    private static <T> boolean equals(T value1, T value2) {
        if (value1 == null && value2 == null) {
            return true;
        } else if (value1 == null || value2 == null) {
            return false;
        } else {
            return value1.equals(value2);
        }
    }

    public static <T> T[] reverse(T[] values) {
        if (values == null) {
            return null;
        }
        int len = values.length;
        T[] newValues = (T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), len);
        for (int i = 0; i < len; i++) {
            newValues[len - i -1] = values[i];
        }
        return newValues;
    }

    public static <T> T[] intersect(T[] values1, T[] values2) {
        if (values1 == null || values2 == null) {
            return null;
        }
        List<T> results = new ArrayList<T>();
        for (T value1 : values1) {
            if (contains(values2, value1)) {
                results.add(value1);
            }
        }
        return results.toArray((T[])java.lang.reflect.Array.newInstance(values1.getClass().getComponentType(), results.size()));
    }

    public static <T> T[] distinct(T[] values1, T[] values2) {
        if (values1 == null || values2 == null) {
            return null;
        }
        Set<T> results = new TreeSet<T>();
        if (values1 != null) {
            for (T value : values1) {
                results.add(value);
            }
        }
        if (values2 != null) {
            for (T value : values2) {
                results.add(value);
            }
        }
        return results.toArray((T[])java.lang.reflect.Array.newInstance(values1.getClass().getComponentType(), results.size()));
    }

    public static <T> T[] distinct(T[] values) {
        Set<T> results = new HashSet<T>();
        for (T value : values) {
            results.add(value);
        }
        return results.toArray((T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), results.size()));
    }

    public static <T> T[] sort(T[] values, String order) {
        Set<T> set = "asc".equals(order) ? new TreeSet<T>() : new TreeSet<T>(Collections.reverseOrder());
        if (values != null) {
            for (T value : values) {
                set.add(value);
            }
        }
        return set.toArray((T[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), set.size()));
    }

    public static <T> T[] customizedSort(String[] sortOrder , T[] t) {
        List<T> list = new ArrayList<T>();
            for (String protocol : sortOrder) {
                for (T point : t) {
                    if (point.toString().contains(protocol)) {
                        list.add(point);
                    }
                }
            }
        return list.toArray(t);
    }
}
