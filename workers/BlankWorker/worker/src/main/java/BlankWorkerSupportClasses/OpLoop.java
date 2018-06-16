/* Copyright Deloitte Consulting LLP 2018 */

package BlankWorkerSupport;

import improbable.collections.Option;
import improbable.worker.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import BlankWorkerSupport.EntityWrapper;

public class OpLoop {

    private static String workerId;
    private static Connection connection;
    private static Dispatcher dispatcher;
    private static long millisecondsPerFrame = 200;

    private static Metrics metrics = new Metrics();
    private static double load;

    private static final Logger logger = new Logger();
    private static RequestId<EntityQueryRequest> statsRequestId;

    public static void oneStep(){

    }


    public static void startWorker(String[] args) {
        initializeWorker(args);

        logger.info("Worker starting");

        LocalView localView = new LocalView(connection, logger);
        localView.registerDispatcher(dispatcher);

        long start;
        long end;
        long elapsedMillis;
        int numberOfCycles = 5;
        int i = 0;


        while (true) {

            start = System.currentTimeMillis();

            processOpList();

            localView.applyWorkerActions();

            end = System.currentTimeMillis();


            elapsedMillis = end - start;
            load = Math.max(
                    (double) elapsedMillis / millisecondsPerFrame,
                    0.0
            );
            if(elapsedMillis < millisecondsPerFrame){
                try {
                    Thread.sleep(millisecondsPerFrame - elapsedMillis);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    private static boolean initializeWorker(String[] args) {
        HashMap<String, String> argsMap = mapArgs(args);

        java.util.Iterator it = argsMap.entrySet().iterator();
        int num = 0;
        String currentKey = "";
        String idKey = "";
        while (it.hasNext()) {
            Map.Entry kv = (Map.Entry) it.next();
            currentKey = (String) kv.getKey();

            if (currentKey.contains("workerId")) {
                idKey = currentKey;
                workerId = argsMap.get(idKey);

            }

        }

        if (workerId == null) {
            return true;
        } else {

            connect(argsMap);
            dispatcher = initializeDispatcher();

            return false;
        }
    }

    private static HashMap<String, String> mapArgs(String[] args) {
        HashMap<String, String> argsMap = new HashMap<>();
        for (String arg : args) {

            String[] kv = arg.split("=");
            if (kv.length == 2) {
                argsMap.put(kv[0], kv[1]);
            } else {
                argsMap.put(kv[0], "true");
            }
        }
        return argsMap;
    }

    private static void connect(Map<String, String> argsMap) {
        // Hashmap.get fails for mysterious reasons here.
        // Feel free to call .get() outside the loop and see what happens. Is this even a real language?

        java.util.Iterator it = argsMap.entrySet().iterator();
        int num = 0;
        String currentKey = "";
        String idKey = "";
        String hostname = "";
        int port = 0;
        ConnectionParameters parameters = new ConnectionParameters();
        String workerId = "";

        while (it.hasNext()) {
            Map.Entry kv = (Map.Entry) it.next();
            currentKey = (String) kv.getKey();

            if (currentKey.contains("hostName")) {
                hostname = argsMap.get(currentKey);

            } else if (currentKey.contains("port")) {
                port = Integer.parseInt(argsMap.get(currentKey));
            } else if (currentKey.contains("workerType")) {
                parameters.workerType = argsMap.get(currentKey);
            } else if (currentKey.contains("workerId")) {
                workerId = argsMap.get(currentKey);
            }

        }

        parameters.network.connectionType = NetworkConnectionType.RakNet;
        parameters.network.useExternalIp = true;

        connection = Connection.connectAsync(hostname, port, workerId, parameters).get();

        if (!connection.isConnected()) {
            logger.warn("Failed to connect.");
            System.exit(32);
        }

        logger.connect(connection, workerId);
    }

    private static Dispatcher initializeDispatcher() {
        try {
            logger.info("Initializing dispatcher...");
            Dispatcher dispatcher = new Dispatcher();
            if (dispatcher == null) {
                logger.warn("Dispatcher initialized as null!!");
            }
            dispatcher.onDisconnect(disconnect -> System.exit(0));

            dispatcher.onMetrics(OpLoop::sendDefaultAndLoadMetrics);

            return dispatcher;
        } catch (Exception e) {
            logger.warn("Dispatcher error!" + e.getMessage());
            return null;
        }

    }

    private static void sendDefaultAndLoadMetrics(Ops.Metrics metricsRequestOp) {
        connection.sendMetrics(metricsRequestOp.metrics);

        metrics.load = Option.of(load);

        connection.sendMetrics(metrics);
    }

    private static void processOpList() {
        try (OpList opList = connection.getOpList(0)) {
            dispatcher.process(opList);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


}
