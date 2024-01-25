package me.alpha432.oyvey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
//import oshi.hardware.CentralProcessor;
//import oshi.hardware.ComputerSystem;
//import oshi.hardware.HardwareAbstractionLayer;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.spec.KeySpec;
import java.util.NoSuchElementException;
import java.util.Scanner;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.InputStreamReader;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class HWID {
    // VERSION 0.4

    public static final Logger LOGGER = LogManager.getLogger(HWID.class);

    private static final int RETRIES = 5;

    private static final String SALT = "l7FhV9aKSqPq734TOc3Enihmd2SnaHNhr21Au48TeqyjJMVaPJg6ESrIYCSTcFB7";
    private static final String LINK = "https://pastebin.com/raw/L2vqkkqV";

    private static final SystemInfo systemInfo = new SystemInfo();

    public static boolean isWhitelisted(String eHWID) { return isWhitelisted(eHWID, RETRIES); };
    public static boolean isWhitelisted(String eHWID, int retries) {
        if (retries <= 0) return false;
        boolean result = false;

        try {
            /*
            URLConnection socket = new URL(LINK).openConnection();
            socket.setUseCaches(false);
            socket.setDefaultUseCaches(false);


            HttpURLConnection conn = (HttpURLConnection) socket;
            conn.setUseCaches(false);
            conn.setDefaultUseCaches(false);
            conn.setRequestProperty("Pragma",  "no-cache");
            conn.setRequestProperty("Expires",  "0");
            */

            //conn.connect();
            //Scanner scanner = new Scanner(socket.getInputStream(), "UTF-8");
            Scanner scanner = new Scanner(new URL(LINK).openStream(), "UTF-8");

            while (scanner.hasNextLine()) {
                String d = scanner.next();
                System.out.println(d);

                if (eHWID.equals(d)) {
                    result = true;
                    //LOGGER.info("YES!!!");
                }else {
                    //LOGGER.info("NO!!!!");
                }
            }

            scanner.close();
            //conn.disconnect();

        } catch (FileNotFoundException fe) {
            return isWhitelisted(eHWID,retries-1);
        } catch (NoSuchElementException e) {
            // ignore
        } catch (Exception e) {
            //System.out.println(e.toString());
            LOGGER.error("FEHLER: "+e.toString());
            e.printStackTrace();
        }

        LOGGER.info("RESULT: "+Boolean.toString(result));

        return result;
    }

    public static String getHWID() {
        byte[] out;
        String hwstr = System.getProperty("file.separator")
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + Runtime.getRuntime().availableProcessors()
                + System.getProperty("user.name")
                + System.getProperty("user.home")
                + System.getenv("os" )
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + System.getProperty("user.name")
                + System.getenv("SystemRoot")
                + System.getenv("HOMEDRIVE")
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITECTURE")
                + System.getenv("PROCESSOR_ARCHITEW6432")
                + System.getenv("NUMBER_OF_PROCESSORS")
                + System.getProperty("file.separator");

        //LOGGER.warn("HW CPU COUT: " + getCPUHWID());

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            KeySpec keySpec = new PBEKeySpec(hwstr.toCharArray(), SALT.getBytes(), 1000, 512);
            SecretKey secret = keyFactory.generateSecret(keySpec);
            out = secret.getEncoded();
        } catch (Exception e) {
            //System.out.println(e);
            return e.toString();
        }
        return bytesToHex(out);
    }

    private static String bytesToHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte sData : data) {
            result.append(String.format("%02x", sData));
        }
        return result.toString();
    }

    /*
    private static String getCPUHWID() {
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        String processorSerialNumber = computerSystem.getSerialNumber();
        String processorIdentifier = centralProcessor.getProcessorIdentifier().toString();

        //return "#" + processorIdentifier + "#" + processorSerialNumber;
        return String.valueOf(centralProcessor.getLogicalProcessorCount());
    }
     */
}