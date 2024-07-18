package com.expo.prometheus.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expo.healthcheck.service.AppHealthCheckService;
import com.expo.prometheus.model.AlertEmailInfo;
import com.expo.prometheus.model.MailNotifInfo;
import com.expo.prometheus.service.AlertFileGenerator;
import com.expo.prometheus.service.PrometheusAlertService;

@RestController
@RequestMapping("/api/alertmanager")
@CrossOrigin(origins = "http://localhost:4200/")
public class AlertManagerController {
    private final AlertFileGenerator alertFileGenerator;
    private final PrometheusAlertService prometheusAlertService;
    private final AppHealthCheckService reloadAlertServer;
    @Value("${alertmanager.url}")
    private String alertmanagerUrl;



    public AlertManagerController(AlertFileGenerator alertFileGenerator, PrometheusAlertService prometheusAlertService, AppHealthCheckService reloadAlertServer) {
        this.alertFileGenerator = alertFileGenerator;
        this.prometheusAlertService = prometheusAlertService;
        this.reloadAlertServer = reloadAlertServer;
    }

    @GetMapping("/generate-alert-file")
    public ResponseEntity<String> generateAlertFile() throws IOException {
        alertFileGenerator.generateConfigFile();
        this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);

        return ResponseEntity.ok("Alert file generated successfully.");
    }
    @PostMapping("/add-route")
    public ResponseEntity<Boolean> addRoute(
            @RequestParam("alertname") String alertname,
            @RequestParam("receivername") String receivername,
            @RequestParam("instance") String instance,
            @RequestParam("receiverEmails") List<String> receiverEmails) {
        try {
            alertFileGenerator.addRoute(alertname, receivername, instance, receiverEmails);
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);
        } catch (IOException e) {
            String errorMessage = "Failed to add route: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    @PostMapping("/add-team")
    public ResponseEntity<Boolean> addTeam(
            @RequestParam("alertname") String alertname,
            @RequestParam("receivername") String receivername,
            @RequestParam("instance") String instance) {
        try {
            alertFileGenerator.addTeam(alertname, receivername, instance);
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);
        } catch (IOException e) {
            String errorMessage = "Failed to add route: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


    @PostMapping("/add-new-receiver")
    public ResponseEntity<Boolean> addNewReceiver(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverEmails") List<String> receiverEmails) throws IOException {
       Boolean add= alertFileGenerator.addEmailsToReceiver(receiverName, receiverEmails);
        if(add){
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);

        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);

    }

    // manich bch nestaamelha khater chen3a
    @PostMapping("/update-receiverName")
    public ResponseEntity<String> updateReceiverName(
            @RequestParam("currentReceiverName") String currentReceiverName,
            @RequestParam("newReceiverName") String newReceiverName) throws IOException {
        alertFileGenerator.updateReceiverName(currentReceiverName, newReceiverName);
        this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
        return ResponseEntity.ok("Receiver Name updated successfully.");
    }


    //plutot moch delete alert khater you can't delete an alert
    @DeleteMapping("/delete-alert")
    public ResponseEntity<String> deleteAlert(@RequestParam(value = "alertname") String alertName,
                                              @RequestParam(value="instance")String instance) throws IOException {
        boolean deleted = alertFileGenerator.deleteAlert(alertName,instance);
        if (deleted) {
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok("Alert deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert not found.");
        }
    }

    @PostMapping("/disable-enable-alert")
    public ResponseEntity<Boolean> disableAlert(@RequestParam(value = "alertname") String alertName,
                                               @RequestParam(value = "instance")String instance) throws IOException {
        boolean disabled = alertFileGenerator.disableEnableAlert(alertName,instance);
        System.out.println("disabled"+disabled);
        if (disabled) {
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @DeleteMapping("/delete-email-alert")
    public ResponseEntity<Boolean> deleteEmailFromReceiver(@RequestParam(value = "receiverName") String receiverName,
                                              @RequestParam(value="receiverEmail")String receiverEmail) throws IOException {
        boolean deleted = alertFileGenerator.deleteEmailFromReceiver(receiverName,receiverEmail);
        if (deleted) {
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }


    @GetMapping("/get-alert-emails")
    public ResponseEntity<MailNotifInfo> getAlertEmails(
            @RequestParam(value = "alertname") String alertName,
            @RequestParam(value = "instance") String instance) throws IOException {

        MailNotifInfo mailNotifInfo = alertFileGenerator.getEmailsByAlert(alertName, instance);

        if (mailNotifInfo == null) {
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mailNotifInfo);
        }
    }
    @GetMapping("/get-instance-alert-details")
    public ResponseEntity<List<AlertEmailInfo>> getAlertsAndEmailsByInstance(
            @RequestParam(value = "instance") String instance) throws IOException {

        List<AlertEmailInfo> alertEmailInfo = alertFileGenerator.getAlertsAndEmailsByInstance(instance);

        if (alertEmailInfo == null) {
            return ResponseEntity.notFound().build();
        } else {
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(alertEmailInfo);
        }
    }

    @PostMapping(value = "/modify_alert_instance")
    public ResponseEntity<Boolean> modifyalertinstance(@RequestParam(value = "oldinstance")String oldinstance,@RequestParam(value = "newinstance")String newinstance) throws IOException {

        boolean ismodified=this.alertFileGenerator.modifyInstanceInAlertFile(oldinstance, newinstance);
        if(ismodified){
            this.reloadAlertServer.reloadPrometheusAlertManagerServer(alertmanagerUrl);
            return ResponseEntity.ok(true);

        }
        return ResponseEntity.status(400).body(false);

    }

}
