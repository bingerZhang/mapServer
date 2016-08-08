package com.map;

import java.util.Comparator;

/**
 * Created by zlb on 2016/8/8.
 */

public class PointComparator implements Comparator<Point> {
        @Override
        public int compare(Point o1, Point o2) {
            if(o1.getPoint_id()>o2.getPoint_id())
                return 1;
            else if(o1.getPoint_id()==o2.getPoint_id()){
                if(o1.getId()>o2.getId())
                    return 1;
                else if(o1.getId()==o2.getPoint_id())
                    return 0;
                else
                    return -1;
            }
            else
                return -1;
        }
}
