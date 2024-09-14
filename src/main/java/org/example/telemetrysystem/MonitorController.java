package org.example.telemetrysystem;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
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
    private PieChart cpuChart;
    @FXML
    private PieChart ramChart;
    @FXML
    private PieChart storageChart;
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

    @FXML
    private Label nicIdLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label macAddressLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label serverNameLabel;
    @FXML
    private Label locationLabel;

    @FXML
    private void handleRefreshCPU() {
        updateCPUChart();
    }

    @FXML
    private void handleRefreshRAM() {
        updateRAMChart();
    }

    @FXML
    private void handleRefreshStorage() {
        updateStorageChart();
    }

    private static final String COMMUNITY = "ingsoft";
    private static final String IP_ADDRESS = "192.168.1.71"; // Cambia según tu servidor
    private static final int PORT = 161;
    private static final int TIMEOUT = 2000;
    private static final int RETRIES = 3;

    private static final String CPU_OID = "1.3.6.1.4.1.2021.11.10.0";
    private static final String MEM_TOTAL_OID = "1.3.6.1.4.1.2021.4.11.0";
    private static final String MEM_USED_OID = "1.3.6.1.4.1.2021.4.6.0";
    private static final String MEM_AVAILABLE_OID = "1.3.6.1.4.1.2021.4.5.0";
    private static final String STORAGE_TOTAL_OID = "1.3.6.1.4.1.2021.9.1.6.1";
    private static final String STORAGE_USED_OID = "1.3.6.1.4.1.2021.9.1.8.1";
    private static final String STORAGE_AVAILABLE_OID = "1.3.6.1.4.1.2021.9.1.7.1";

    private static final String NIC_ID_OID = "1.3.6.1.2.1.2.2.1.2.2";
    private static final String SPEED_OID = "1.3.6.1.2.1.2.2.1.5.2";
    private static final String MAC_ADDRESS_OID = "1.3.6.1.2.1.2.2.1.6.2";
    private static final String NAME_OID = "1.3.6.1.2.1.1.4.0";
    private static final String SERVER_NAME_OID = "1.3.6.1.2.1.1.5.0";
    private static final String LOCATION_OID = "1.3.6.1.2.1.1.6.0";

    private Snmp snmpClient;
    private CommunityTarget target;

    public void initialize() {
        setupSNMPClient();

        refreshCPUButton.setOnAction(event -> updateCPUChart());
        refreshRAMButton.setOnAction(event -> updateRAMChart());
        refreshStorageButton.setOnAction(event -> updateStorageChart());

        handleRefreshCPU();
        handleRefreshRAM();
        handleRefreshStorage();

        updateGeneralInfo();
    }

    private void setupSNMPClient() {
        try {
            Address targetAddress = new UdpAddress(IP_ADDRESS + "/" + PORT);
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();

            target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);
            target.setRetries(RETRIES);
            target.setTimeout(TIMEOUT);

            snmpClient = new Snmp(transport);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSNMPValue(String oid) {
        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            ResponseEvent response = snmpClient.send(pdu, target);

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

    private double convertKBToMB(double kb) {
        return kb / 1024.0;
    }

    private void updateCPUChart() {
        String cpuUsage = getSNMPValue(CPU_OID);
        if (cpuUsage != null) {
            double usage = Double.parseDouble(cpuUsage);
            double free = 100 - usage;

            PieChart.Data used = new PieChart.Data(String.format("Uso: %.1f%%", usage), usage);
            PieChart.Data freeData = new PieChart.Data(String.format("Libre: %.1f%%", free), free);

            cpuChart.setData(javafx.collections.FXCollections.observableArrayList(used, freeData));
        }
    }

    private void updateRAMChart() {
        String memTotal = getSNMPValue(MEM_TOTAL_OID);
        String memUsed = getSNMPValue(MEM_USED_OID);
        String memAvailable = getSNMPValue(MEM_AVAILABLE_OID);

        if (memTotal != null && memUsed != null && memAvailable != null) {
            double totalMB = convertKBToMB(Double.parseDouble(memTotal));
            double usedMB = convertKBToMB(Double.parseDouble(memUsed));
            double availableMB = convertKBToMB(Double.parseDouble(memAvailable));

            PieChart.Data used = new PieChart.Data(String.format("Usado: %.1f MB", usedMB), usedMB);
            PieChart.Data available = new PieChart.Data(String.format("Disponible: %.1f MB", availableMB), availableMB);

            ramChart.setData(javafx.collections.FXCollections.observableArrayList(used, available));
        }
    }

    private void updateStorageChart() {
        String storageTotal = getSNMPValue(STORAGE_TOTAL_OID);
        String storageUsed = getSNMPValue(STORAGE_USED_OID);
        String storageAvailable = getSNMPValue(STORAGE_AVAILABLE_OID);

        if (storageTotal != null && storageUsed != null && storageAvailable != null) {
            double totalMB = convertKBToMB(Double.parseDouble(storageTotal));
            double usedMB = convertKBToMB(Double.parseDouble(storageUsed));
            double availableMB = convertKBToMB(Double.parseDouble(storageAvailable));

            PieChart.Data used = new PieChart.Data(String.format("Usado: %.1f MB", usedMB), usedMB);
            PieChart.Data available = new PieChart.Data(String.format("Disponible: %.1f MB", availableMB), availableMB);

            storageChart.setData(javafx.collections.FXCollections.observableArrayList(used, available));
        }
    }

    private void updateGeneralInfo() {
        String nicId = getSNMPValue(NIC_ID_OID);
        String speed = getSNMPValue(SPEED_OID);
        String macAddress = getSNMPValue(MAC_ADDRESS_OID);
        String name = getSNMPValue(NAME_OID);
        String serverName = getSNMPValue(SERVER_NAME_OID);
        String location = getSNMPValue(LOCATION_OID);

        nicIdLabel.setText(nicId != null ? String.format("ID de NIC: %s", nicId) : "ID de NIC: No disponible");
        speedLabel.setText(speed != null ? String.format("Velocidad: %s", speed) : "Velocidad: No disponible");
        macAddressLabel.setText(macAddress != null ? String.format("Dirección MAC: %s", macAddress) : "Dirección MAC: No disponible");
        nameLabel.setText(name != null ? String.format("Nombre: %s", name) : "Nombre: No disponible");
        serverNameLabel.setText(serverName != null ? String.format("Nombre del servidor: %s", serverName) : "Nombre del servidor: No disponible");
        locationLabel.setText(location != null ? String.format("Ubicación: %s", location) : "Ubicación: No disponible");
    }
}
