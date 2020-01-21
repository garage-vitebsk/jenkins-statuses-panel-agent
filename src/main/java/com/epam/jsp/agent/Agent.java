package com.epam.jsp.agent;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.korecky.bluetooth.client.hc06.entity.RFCommBluetoothDevice;
import org.korecky.bluetooth.client.hc06.enums.ServiceUUID;
import org.korecky.bluetooth.client.hc06.event.ErrorEvent;
import org.korecky.bluetooth.client.hc06.event.MessageReceivedEvent;
import org.korecky.bluetooth.client.hc06.listener.RFCommClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);
    private static final AtomicBoolean DEVICES_FOUND = new AtomicBoolean(false);
    private static final AtomicBoolean SERVICES_FOUND = new AtomicBoolean(false);
    private static final AtomicBoolean IN_PROGRESS_SWITCHER = new AtomicBoolean(false);

    public static void main(String[] args) throws InterruptedException, IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        List<RFCommBluetoothDevice> devices = new LinkedList<>();

        DiscoveryListener devicesDiscoveryListener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
                    LOGGER.info("Start discovering!");
                    String friendlyName = btDevice.getFriendlyName(false);
                    RFCommBluetoothDevice device = new RFCommBluetoothDevice(friendlyName, btDevice.getBluetoothAddress(), btDevice);
                    devices.add(device);
                } catch (IOException exception) {
                    LOGGER.error("Error during attempt to get device name.", exception);
                }
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                LOGGER.info("Service discovery is completed");
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                LOGGER.info("Service search is completed");
//                new UnsupportedOperationException("Service search is unsupported.");
            }

            @Override
            public void inquiryCompleted(int discType) {
                LOGGER.info("Service inquiry is completed");
                DEVICES_FOUND.set(true);
//                new UnsupportedOperationException("Inquiry is unsupported.");
            }
        };
        agent.startInquiry(DiscoveryAgent.GIAC, devicesDiscoveryListener);
        while (!DEVICES_FOUND.get()) {
            LOGGER.info("Looking for devices!");
            Thread.sleep(2000L);
        }

        //choosing device
        RFCommBluetoothDevice tempDevice = null;
        List<RFCommBluetoothDevice> tempDevices = devices.stream()
                .filter(Objects::nonNull)
                .filter(deviceInstance -> {
                    LOGGER.info("Device name: {}", deviceInstance.getName());
                    return "HC-06".equalsIgnoreCase(deviceInstance.getName());
                })
                .collect(Collectors.toList());
        if (tempDevices.isEmpty()) {
            LOGGER.error("No devices to communicate with!");
        } else {
            if (1 < tempDevices.size()) {
                LOGGER.warn("There are more than 1 device to communicate with");
            }
            tempDevice = tempDevices.get(0);
        }

        // device will get message from this code
        RFCommBluetoothDevice device = tempDevice;

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(URI.create("http://localhost:8080/"));
        BuildInformationService buildInformationService = new JenkinsBuildInformationService(jenkinsHttpClient, "test-test");

        if (device != null) {
            DiscoveryListener serviceDiscoveryListener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    throw new UnsupportedOperationException("Device discovery is unsupported.");
                }

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
                    if (device != null) {
                        for (int i = 0; i < serviceRecords.length; ++i) {
                            String url = serviceRecords[i].getConnectionURL(0, false);
                            if (url != null && url.toLowerCase().startsWith("btspp://")) {
                                device.setUrl(url);
                            }
                        }
                    }
                    SERVICES_FOUND.set(true);
                }

                @Override
                public void serviceSearchCompleted(int transID, int respCode) {
                    LOGGER.info("Service search completed.");
                }

                @Override
                public void inquiryCompleted(int discType) {
                    LOGGER.info("Inquiry was completed");
                }
            };
            agent.searchServices(null, new UUID[]{ServiceUUID.RFCOMM.getUUID()},
                    device.getRemoteDevice(), serviceDiscoveryListener);

            while (!SERVICES_FOUND.get()) {
                LOGGER.info("Looking for services for device {}!", device.getName());
                Thread.sleep(2000L);
            }

            Thread t;
            try {
                StreamConnection connection = (StreamConnection) Connector.open(device.getUrl());
                Random r = new Random();
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try (OutputStream os = connection.openOutputStream()) {
                            //sender string
                            while (true) {

                                List<BuildInformation> buildInformationList = buildInformationService.getBuildInformation();
                                if (null != buildInformationList && !buildInformationList.isEmpty()) {
                                    String jobName = buildInformationList.get(0).getJobName();
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < buildInformationList.size(); i++) {
                                        switch (buildInformationList.get(i).getJobStatus()) {
                                            case SUCCESS:
                                            case FAILED:
                                            case WARNING:
                                            case NO_INFORMATION:
                                                sb.append(defineColor(buildInformationList.get(i).getJobStatus()));
                                                break;
                                            case IN_PROGRESS:
                                                String sign = "";
                                                if (IN_PROGRESS_SWITCHER.get()) {
                                                    if (0 == i) {
                                                        // if it is the first build - it will blink green color
                                                        sb.append("1");
                                                    } else {
                                                        sb.append(defineColor(buildInformationList.get(i - 1)
                                                                .getJobStatus()));
                                                    }
                                                }
                                                IN_PROGRESS_SWITCHER.set(!IN_PROGRESS_SWITCHER.get());
                                            default:
                                                LOGGER.warn("Unknown enum constant");
                                                break;
                                        }
                                    }
                                    sb.append('\0'); // the sign that the name is ended
                                    sb.reverse();
                                    for (int i = buildInformationList.size(); i < 5; i++) {
                                        sb.append("0");
                                    }
                                    sb.replace(0,0, jobName);
                                    String message = sb.toString();
                                    LOGGER.info(message);
                                    os.write(message.getBytes());
                                } else {
                                    LOGGER.info("No information!");
                                }

                                Thread.sleep(2000L);
                            }
                        } catch (IOException | InterruptedException ex) {
                            LOGGER.error("Cannot send message.", ex);
                        }
                    }
                });
                t.start();
                t.join();
            } catch (IOException e) {
                LOGGER.error("Cannot to create connection to device", e);
            }
        }
    }

    private static String defineColor(JobStatus status) {
        switch (status) {
            case SUCCESS:
                return "1";
            case FAILED:
                return "3";
            case WARNING:
                return "2";
            case NO_INFORMATION:
                return "0";
            case IN_PROGRESS:
            default:
                LOGGER.warn("Unknown enum constant");
                return "";
        }
    }

    private static class CustomRFCommClientThread extends Thread {

        private static final Logger LOGGER = LoggerFactory.getLogger(CustomRFCommClientThread.class);
        private final List<RFCommClientEventListener> listenerList = new ArrayList<>();
        private final StreamConnection con;

        /**
         * RFComm client thread
         *
         * @param clientURL URL of RFComm device
         * @param listener  Listener
         * @throws IOException Exceptions
         */
        public CustomRFCommClientThread(String clientURL, RFCommClientEventListener listener) throws IOException {
            listenerList.add(listener);
            con = (StreamConnection) Connector.open(clientURL);
        }

        private void fireBluetooothEvent(EventObject evt) {
            for (RFCommClientEventListener listener : listenerList) {
                if (evt instanceof ErrorEvent) {
                    listener.error((ErrorEvent) evt);
                } else if (evt instanceof MessageReceivedEvent) {
                    listener.messageReceived((MessageReceivedEvent) evt);
                }
            }
        }

        /**
         * Run thread
         */
        @Override
        public void run() {
            try {
                LocalDevice local = LocalDevice.getLocalDevice();
                if (con != null) {
                    try (InputStream is = con.openInputStream()) {
                        StringBuilder messageBuffer = new StringBuilder("");
                        while (true) {
                            //receiver string
                            byte[] buffer = new byte[1024];
                            int countOfReadBytes = is.read(buffer);
                            String received = new String(buffer, 0, countOfReadBytes);
                            messageBuffer.append(received);
                            String terminationChar = String.valueOf((char) 0x0000);
                            String tempString = messageBuffer.toString();
                            if (tempString.contains(terminationChar)) {
                                // Wait until message is complete (wait on termination char)
                                String[] messages = tempString.split(String.format("\\%s", terminationChar));
                                if (messages.length == 1) {
                                    messageBuffer = new StringBuilder("");
                                }
                                if (messages.length > 1) {
                                    for (int i = 0; i < (messages.length - 1); i++) {
                                        fireBluetooothEvent(new MessageReceivedEvent(messages[i], this));
                                    }
                                    messageBuffer = new StringBuilder(messages[messages.length - 1]);
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error("Issue with either opening connection or reading.", e);
                    }
                } else {
                    LOGGER.error("There is no connection.");
                }
            } catch (BluetoothStateException e) {
                LOGGER.error("Issue with getting local device", e);
            }
        }

        /**
         * Send message to bluetooth device
         *
         * @param message String message without termination character. The
         *                character is added automatically.
         */
        public void send(String message) {
            if (con != null) {
                try (OutputStream os = con.openOutputStream()) {
                    //sender string
                    os.write(message.getBytes());
                } catch (IOException ex) {
                    LOGGER.error("Cannot send message.", ex);
                }
            } else {
                LOGGER.error("There is no connection");
            }
        }
    }

}
