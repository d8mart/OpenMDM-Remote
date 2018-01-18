package com.openmdmremote;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Daniel on 01/01/2018.
 */

public class CheckRootAccess {

    public static boolean checkRootAccess() {
        try {
            System.out.println("Checking Root Access");
            String line;
            Process p = Runtime.getRuntime().exec("su -c pwd");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            line = in.readLine();
            in.close();
            System.out.println("Command Output: "+line);
            if(line!=null) {
                return true;
            }else{
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error While Executing the Command");
            return false;
        }

    }
}
