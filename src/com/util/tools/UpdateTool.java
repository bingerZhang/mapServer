package com.util.tools;

import com.launch.Tool;

/**
 * Created by zlb on 2016/7/26.
 */
public class UpdateTool implements Tool {
    private static final String TYPE = "update";
    @Override
    public String getName() {
        return TYPE;
    }

    @Override
    public void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("[WARN] Invalid config action");
            return;
        }
        String action = args[0];
        if ("radius".equals(action)) {
            updateRadius();
        } else {
            System.out.println("[WARN] Invalid cache action: " + action);
        }
    }
    private void updateRadius(){
        System.out.println("update radius");
    }
}
