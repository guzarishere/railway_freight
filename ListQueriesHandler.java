import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;

public class ListQueriesHandler {
    private final static LinkedList<Dispatcher> dispatchers = new LinkedList<>();
    private final static LinkedList<Customer> customers = new LinkedList<>();
    private final static LinkedList<Contract> contracts = new LinkedList<>();

    static LinkedList<Dispatcher> getDispatchers() {return  dispatchers;}

    static LinkedList<Customer> getCustomers() {return  customers;}

    static LinkedList<Contract> getContracts() {return  contracts;}

    static String getContractsList() {
        if (contracts.isEmpty()) return "There's no created contracts";
        String list = "";
        int n = 1;

        for (Contract contract : contracts) {
            list += n++ + ". " + contract + "\n";
        }

        return list.replace("; ", ";\n");
    }

    static String getDispatchersAddresses() {
        if (dispatchers.isEmpty()) return "There's no dispatchers";
        String list = "";
        int n = 1;

        for (Dispatcher dispatcher : dispatchers) {
            list += n++ + ". " + dispatcher.getAddress() + "\n";
        }

        return list;
    }

    static String getCustomerCompanies() {
        if (customers.isEmpty()) return "There's no customers";
        String list = "";
        int n = 1;

        for (Customer customer : customers) {
            list += n++ + ". " + customer.getCompany_name() + "\n";
        }

        return list;
    }

    static String getMaxDeliveryTime() {
        if (contracts.isEmpty()) return "There's no created contracts";

        Contract max = contracts.getFirst();
        for (Contract contract : contracts) {
            if (contract.getDelivery_time().compareTo(max.getDelivery_time()) > 0) {
                max = contract;
            }
        }

        return max.toString().replace("; ", ";\n");
    }

    static String getMostPopularArrival() {
        if (contracts.isEmpty()) return "There's no created contracts";

        HashMap<String, Integer> stations = new HashMap<>();
        // Adding stations with popularity values to hashmap
        for (Contract contract : contracts) {
            String station = contract.getArrival_st();
            if (!stations.containsKey(station)) {
                stations.put(station, 1);
            }
            else {
                int n = stations.get(station);
                stations.replace(station, n+1);
            }
        }
        String max = contracts.getFirst().getArrival_st();
        // Finding max popularity value
        for (String station : stations.keySet()) {
            if (stations.get(station) > stations.get(max)) {
                max = station;
            }
        }

        return max;
    }

    static String getAvgDeliveryTime() {
        if (contracts.isEmpty()) return "There's no created contracts";

        Duration avg = Duration.ZERO;
        for (Contract contract : contracts) {
            avg = avg.plus(contract.getDelivery_time());
        }
        avg = avg.dividedBy(contracts.size());

        return avg.toDaysPart() + " days, " + avg.toHoursPart() + " hours, " + avg.toMinutesPart() + " minutes";
    }
}