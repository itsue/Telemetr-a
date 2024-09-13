package org.example.telemetrysystem;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class MonitorController {

    @FXML
    private LineChart<Number, Number> cpuChart;
    @FXML
    private LineChart<Number, Number> ramChart;
    @FXML
    private LineChart<Number, Number> storageChart;
    @FXML
    private Label cpuLabel;
    @FXML
    private Label ramLabel;
    @FXML
    private Label storageLabel;
    @FXML
    private Button refreshCPUButton;
    @FXML
    private Button refreshRAMButton;
    @FXML
    private Button refreshStorageButton;

    // Configuración de SNMP
    private static final String COMMUNITY = "ingsoft";
    private static final String IP_ADDRESS = "192.168.1.71"; // Cambia según tu servidor
    private static final int PORT = 161;
    private static final int TIMEOUT = 2000;
    private static final int RETRIES = 3;

    // OIDs para CPU, Memoria y Almacenamiento
    private static final String CPU_OID = "1.3.6.1.4.1.2021.11.10.0";
    private static final String MEM_TOTAL_OID = "1.3.6.1.4.1.2021.4.11.0";
    private static final String MEM_USED_OID = " 1.3.6.1.4.1.2021.4.6.0";
    private static final String MEM_AVAILABLE_OID = "1.3.6.1.4.1.2021.4.5.0 ";
    private static final String STORAGE_TOTAL_OID = "1.3.6.1.4.1.2021.9.1.6.1";
    private static final String STORAGE_USED_OID = "1.3.6.1.4.1.2021.9.1.8.1";
    private static final String STORAGE_AVAILABLE_OID = "1.3.6.1.4.1.2021.9.1.7.1";

    private Snmp snmpClient;
    private CommunityTarget target;

    public void initialize() {
        // Inicializa el cliente SNMP y el objetivo
        setupSNMPClient();

        // Eventos de los botones de refresco
        refreshCPUButton.setOnAction(event -> updateCPUChart());
        refreshRAMButton.setOnAction(event -> updateRAMChart());
        refreshStorageButton.setOnAction(event -> updateStorageChart());
    }

    // Configuración del cliente SNMP
    private void setupSNMPClient() {
        try {
            // Configuración de la dirección de destino
            Address targetAddress = new UdpAddress(IP_ADDRESS + "/" + PORT);
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Configuración del objetivo SNMP
            target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);
            target.setRetries(RETRIES);
            target.setTimeout(TIMEOUT);

            // Crear objeto SNMP
            snmpClient = new Snmp(transport);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para realizar la consulta SNMP
    private String getSNMPValue(String oid) {
        try {
            // Crear PDU (Protocol Data Unit)
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            // Enviar la solicitud SNMP
            ResponseEvent response = snmpClient.send(pdu, target);

            // Procesar la respuesta
            if (response != null && response.getResponse() != null) {
                return response.getResponse().get(0).getVariable().toString();
            } else {
                System.out.println("Error: No se recibió respuesta");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Convertir KB a MB
    private double convertKBToMB(double kb) {
        return kb / 1024.0;
    }

    // Actualizar gráfico de CPU
    private void updateCPUChart() {
        String cpuUsage = getSNMPValue(CPU_OID);
        if (cpuUsage != null) {
            cpuLabel.setText("CPU Usage: " + cpuUsage + "%");

            double cpuValue = Double.parseDouble(cpuUsage);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("CPU en uso");
            series.getData().add(new XYChart.Data<>(1, cpuValue)); // Usar un valor fijo en el eje X
            cpuChart.getData().clear();
            cpuChart.getData().add(series);

            // Configuración para ocultar la marca de tiempo en el eje X
            cpuChart.getXAxis().setLabel("");
            cpuChart.getXAxis().setTickLabelsVisible(false);
        }
    }

    // Actualizar gráfico de RAM (Total, Usado, Disponible)
    private void updateRAMChart() {
        String ramTotal = getSNMPValue(MEM_TOTAL_OID);
        String ramUsed = getSNMPValue(MEM_USED_OID);
        String ramAvailable = getSNMPValue(MEM_AVAILABLE_OID);

        if (ramTotal != null && ramUsed != null && ramAvailable != null) {
            double totalMB = convertKBToMB(Double.parseDouble(ramTotal));
            double usedMB = convertKBToMB(Double.parseDouble(ramUsed));
            double availableMB = convertKBToMB(Double.parseDouble(ramAvailable));

            ramLabel.setText("Total: " + String.format("%.2f", totalMB) + " MB, Usado: " + String.format("%.2f", usedMB) + " MB, Disponible: " + String.format("%.2f", availableMB) + " MB");

            XYChart.Series<Number, Number> totalSeries = new XYChart.Series<>();
            totalSeries.setName("Total");
            totalSeries.getData().add(new XYChart.Data<>(1, totalMB)); // Usar un valor fijo en el eje X

            XYChart.Series<Number, Number> usedSeries = new XYChart.Series<>();
            usedSeries.setName("Usado");
            usedSeries.getData().add(new XYChart.Data<>(1, usedMB)); // Usar un valor fijo en el eje X

            XYChart.Series<Number, Number> availableSeries = new XYChart.Series<>();
            availableSeries.setName("Disponible");
            availableSeries.getData().add(new XYChart.Data<>(1, availableMB)); // Usar un valor fijo en el eje X

            ramChart.getData().clear();
            ramChart.getData().addAll(totalSeries, usedSeries, availableSeries);

            // Configuración para ocultar la marca de tiempo en el eje X
            ramChart.getXAxis().setLabel("");
            ramChart.getXAxis().setTickLabelsVisible(false);
        }
    }

    // Actualizar gráfico de Almacenamiento (Total, Usado, Disponible)
    private void updateStorageChart() {
        String storageTotal = getSNMPValue(STORAGE_TOTAL_OID);
        String storageUsed = getSNMPValue(STORAGE_USED_OID);
        String storageAvailable = getSNMPValue(STORAGE_AVAILABLE_OID);

        if (storageTotal != null && storageUsed != null && storageAvailable != null) {
            double totalMB = convertKBToMB(Double.parseDouble(storageTotal));
            double usedMB = convertKBToMB(Double.parseDouble(storageUsed));
            double availableMB = convertKBToMB(Double.parseDouble(storageAvailable));

            storageLabel.setText("Total: " + String.format("%.2f", totalMB) + " MB, Usado: " + String.format("%.2f", usedMB) + " MB, Disponible: " + String.format("%.2f", availableMB) + " MB");

            XYChart.Series<Number, Number> totalSeries = new XYChart.Series<>();
            totalSeries.setName("Total");
            totalSeries.getData().add(new XYChart.Data<>(1, totalMB)); // Usar un valor fijo en el eje X

            XYChart.Series<Number, Number> usedSeries = new XYChart.Series<>();
            usedSeries.setName("Usado");
            usedSeries.getData().add(new XYChart.Data<>(1, usedMB)); // Usar un valor fijo en el eje X

            XYChart.Series<Number, Number> availableSeries = new XYChart.Series<>();
            availableSeries.setName("Disponible");
            availableSeries.getData().add(new XYChart.Data<>(1, availableMB)); // Usar un valor fijo en el eje X

            storageChart.getData().clear();
            storageChart.getData().addAll(totalSeries, usedSeries, availableSeries);

            // Configuración para ocultar la marca de tiempo en el eje X
            storageChart.getXAxis().setLabel("");
            storageChart.getXAxis().setTickLabelsVisible(false);
        }
    }
}