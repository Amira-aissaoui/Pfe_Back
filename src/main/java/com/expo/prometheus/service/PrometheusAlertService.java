package com.expo.prometheus.service;


import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@CrossOrigin(origins = "*")

public class PrometheusAlertService {


    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;

    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    @Value("${host}")
    private String host;
    @Value("${port}")
    private String port;
    @Value("${username}")
    private String username;
    @Value("${password}")
    private String password;

    String localpath = System.getProperty("user.dir");

    public void pushRuleFile(String ruleFilePath, String type) throws IOException {

        String filePath = "/app/alert.rules.yml";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }
        String destinationDirectory="/app/prometheus/";

        if (type.equals("rule")) {
            destinationDirectory = prometheusConfigPath;
        } else if (type.equals("alert")) {
            destinationDirectory = alertManagerConfigPath;
        } else {
            destinationDirectory = prometheusConfigPath;
        }
         destinationDirectory="/app/prometheus/";

        // Create the destination directory if it does not exist
        Files.createDirectories(Paths.get(destinationDirectory));

        // Extract the file name from the ruleFilePath
        String fileName = file.getName();

        // Create the output file path
        String outputFile = destinationDirectory + File.separator + fileName;

        System.out.println("Output file path: " + outputFile);

        // Copy the file from the local machine to the remote host
        String remoteDestination = username + "@" + host + ":" + destinationDirectory;
        String scpCommand = "scp " + filePath + " " + remoteDestination;
        try {
            executeShellCommand(scpCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("File copied to remote host: " + remoteDestination);

        // Optionally, you can now call the executeShellCommand method to restart the Prometheus server.
        // executeShellCommand(prometheusRestartCommand);
        System.out.println("Prometheus server restarted.");
    }

    public boolean executeShellCommand(String command) throws IOException {
        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(username, host, Integer.parseInt(port));
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.connect();
            System.out.println("SSH connection established.");

            // Print the output of the SSH command
            byte[] buffer = new byte[1024];
            int read;
            while ((read = channel.getInputStream().read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, read));
            }

            channel.disconnect();
            session.disconnect();
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return false;
    }
    public boolean generateFile(String path,String fileName) {
        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(username, host, Integer.parseInt(port));
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("touch " +"/home/"+fileName );
            System.out.println("touch " +"/home/"+"file.txt");
            System.out.println("Connexion SSH Works");
            // Print the output of the SSH command


            channel.disconnect();
            session.disconnect();
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return false;
    }

   /* public void pushRuleFile(String ruleFilePath, String type) throws IOException {

     //   String path = PrometheusAlertService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
       // System.out.println("path"+path);
        String filePath = ruleFilePath;
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found: " + ruleFilePath);
            return;
        }
        String destinationDirectory;

      if (type.equals("rule")) {
            destinationDirectory = prometheusConfigPath;
        } else if (type.equals("alert")) {
            destinationDirectory = alertManagerConfigPath;
        } else {
            destinationDirectory = prometheusConfigPath;
        }


        // Create the destination directory if it does not exist
        Files.createDirectories(Paths.get(destinationDirectory));

        // Extract the file name from the ruleFilePath
        String fileName = file.getName();
        System.out.println("filename"+fileName);
        System.out.println("filename"+File.separator);

        // Create the output file path
        String outputFile = destinationDirectory + File.separator + fileName;
        System.out.println("outputFile"+outputFile);


        // Copy the file from the local machine to the remote host
        String remoteDestination = username + "@" + host + ":" + destinationDirectory;
        String scpCommand = "scp " + filePath + " " + remoteDestination;
        executeShellCommand(scpCommand);
        System.out.println("scpCommand"+scpCommand);

        System.out.println("File copied to remote host: " + remoteDestination);

        // Optionally, you can now call the executeShellCommand method to restart the Prometheus server.
        // executeShellCommand(prometheusRestartCommand);
        System.out.println("Prometheus server restarted.");
    }

    public boolean executeShellCommand(String command) throws IOException {
        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(username, host, Integer.parseInt(port));
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            System.out.println(channel);
            channel.setCommand(command);

            channel.connect();
            System.out.println("Connexion SSH Works");
            channel.disconnect();
            session.disconnect();
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return false;
    }*/
}

