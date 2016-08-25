package com.map;

import java.util.Comparator;

/**
 * Created by zlb on 2016/8/8.
 */

public class NodepComparator implements Comparator<Nodep> {
        @Override
        public int compare(Nodep o1, Nodep o2) {
            if(o1.getId()>o2.getId())
                return 1;
            else
                return -1;
        }
}
