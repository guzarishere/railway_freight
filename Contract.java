import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

public class Contract {
    private final String id; // randomly generated (20 characters - a-z,A-Z,0-9)
    private String date; // DD-MM-YY
    private double weight; // in tons
    private String cargo_type;
    private Duration delivery_time; // estimated days, hours and minutes
    private String departure_st;
    private String arrival_st;
    private double cost; // in UAH
    private double ins_amount; // Insurance amount in UAH

    // Constants
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 20;
    private static final double MIN_WEIGHT = 5; // in tons
    private static final double MAX_WEIGHT = 100; // in tons
    private static final double INS_RATE = 0.03; // is used to calculate insurance amount based on cost

    public Contract(String date, double weight, String cargo_type, String delivery_time,
                    String departure_st, String arrival_st, double cost, Customer customer) {
        if (!Pattern.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$", date)) {
            throw new IllegalArgumentException("Incorrect date format (must be DD-MM-YYYY)");
        }

        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            throw new IllegalArgumentException("Incorrect weight value (must be " + MIN_WEIGHT + "<=weight<=" + MAX_WEIGHT + ")");
        }

        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", cargo_type)) {
            throw new IllegalArgumentException("Incorrect cargo type format");
        }

        if (!Pattern.matches("^(\\d+)\\s+(\\d+)\\s+(\\d+)$", delivery_time)) {
            throw new IllegalArgumentException("Incorrect delivery time format (must be Days Hours Minutes)");
        }

        String[] time = delivery_time.split(" ");
        long days = Long.parseLong(time[0]);
        long hours = Long.parseLong(time[1]);
        long minutes = Long.parseLong(time[2]);

        if (days < 0 || hours < 0 || hours > 23 || minutes < 0 || minutes > 59 ||
                (days == 0 && hours == 0 && minutes == 0)) {
            throw new IllegalArgumentException("Incorrect delivery time value");
        }

        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", departure_st)) {
            throw new IllegalArgumentException("Incorrect departure station format");
        }

        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", arrival_st)) {
            throw new IllegalArgumentException("Incorrect arrival station format");
        }

        if (departure_st.equals(arrival_st)) {
            throw new IllegalArgumentException("Departure and arrival stations cannot be the same");
        }

        if (cost <= 0) {
            throw new IllegalArgumentException("Incorrect cost value (must be >0");
        }

        this.id = generateId();
        this.date = date;
        this.weight = Math.ceil(weight*1000)/1000.0;
        this.cargo_type = cargo_type;
        this.delivery_time = Duration.ofDays(days).plusHours(hours).plusMinutes(minutes);
        this.departure_st = departure_st;
        this.arrival_st = arrival_st;
        this.cost = Math.ceil(cost*100)/100.0;
        this.ins_amount = calculateIns_amount();

        customer.setContract_id(this); // sets id to customer after the contract is created
    }

    private static String generateId() {
        Random random = new Random();
        StringBuilder id = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }
        return id.toString();
    }

    public double calculateIns_amount() {
        return Math.ceil(INS_RATE * cost * 100)/100.0;
    }

    public double getIns_amount() {
        return ins_amount;
    }

    public String getCargo_type() {
        return cargo_type;
    }

    public Duration getDelivery_time() {
        return delivery_time;
    }

    public String getDeparture_st() {
        return departure_st;
    }

    public String getArrival_st() {
        return arrival_st;
    }

    public double getWeight() {
        return weight;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public double getCost() {
        return cost;
    }

    public void setCargo_type(String cargo_type) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", cargo_type)) {
            throw new IllegalArgumentException("Incorrect cargo type format");
        }

        this.cargo_type = cargo_type;
    }

    // Takes formatted duration string (Days Hours Minutes) as a parameter
    public void setDelivery_time(String delivery_time) {
        if (!Pattern.matches("^(\\d+)\\s+(\\d+)\\s+(\\d+)$", delivery_time)) {
            throw new IllegalArgumentException("Incorrect delivery time format (must be Days Hours Minutes)");
        }

        String[] time = delivery_time.split(" ");
        long days = Long.parseLong(time[0]);
        long hours = Long.parseLong(time[1]);
        long minutes = Long.parseLong(time[2]);

        if (days < 0 || hours < 0 || hours > 23 || minutes < 0 || minutes > 59 ||
                (days == 0 && hours == 0 && minutes == 0)) {
            throw new IllegalArgumentException("Incorrect delivery time value");
        }

        this.delivery_time = Duration.ofDays(days).plusHours(hours).plusMinutes(minutes);
    }

    public void setDeparture_st(String departure_st) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", departure_st)) {
            throw new IllegalArgumentException("Incorrect departure station format");
        }
        if (departure_st.equals(arrival_st)) {
            throw new IllegalArgumentException("Departure and arrival stations cannot be the same");
        }

        this.departure_st = departure_st;
    }

    public void setArrival_st(String arrival_st) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ0-9\\s'`-]+$", arrival_st)) {
            throw new IllegalArgumentException("Incorrect departure station format");
        }
        if (departure_st.equals(arrival_st)) {
            throw new IllegalArgumentException("Departure and arrival stations cannot be the same");
        }

        this.arrival_st = arrival_st;
    }

    public void setWeight(double weight) {
        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            throw new IllegalArgumentException("Incorrect weight value (must be " + MIN_WEIGHT + "<=weight<=" + MAX_WEIGHT + ")");
        }

        this.weight = weight;
        this.ins_amount = calculateIns_amount(); // insurance amount changes after changing weight
    }

    public void setDate(String date) {
        if (!Pattern.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$", date)) {
            throw new IllegalArgumentException("Incorrect date format (must be DD-MM-YYYY)");
        }

        this.date = date;
    }

    public void setCost(double cost) {
        if (cost <= 0) {
            throw new IllegalArgumentException("Incorrect cost value (must be >0");
        }

        this.cost = Math.ceil(cost*100)/100.0;
        this.ins_amount = calculateIns_amount();
    }

    @Override
    public String toString() {
        return
                "Contract ID: " + id +
                "; Date: " + date +
                "; Weight: " + weight + " t" +
                "; Cargo type: " + cargo_type +
                "; Estimated delivery time: " + delivery_time.toDaysPart() + " days, " +
                delivery_time.toHoursPart() + " hours, " + delivery_time.toMinutesPart() + " minutes" +
                "; Departure station: " + departure_st +
                "; Arrival station: " + arrival_st +
                "; Cost: " + cost + " UAH" +
                "; Insurance amount: " + ins_amount + " UAH";
    }

    public String toDataString() {
        Customer customer = ListQueriesHandler.getCustomers().stream().filter(c -> c.getContract_id().equals(id)).findFirst().orElse(null);
        if (customer == null) throw new NullPointerException("Associated customer not found");
        return String.format("%s;%.2f;%s;%s;%s;%s;%.2f;%s",
                date, weight, cargo_type, delivery_time, departure_st, arrival_st, cost, customer.toDataString());
    }

    public static Contract fromDataString(String data) {
        String[] parts = data.split(";");
        if (parts.length < 8) throw new IllegalArgumentException("Invalid contract data");

        try {
            Duration deliveryTime = Duration.parse(parts[3]);
            long days = deliveryTime.toDays();
            long hours = deliveryTime.toHoursPart();
            long minutes = deliveryTime.toMinutesPart();

            String formattedTime = String.format("%d %d %d", days, hours, minutes);

            String customerData = String.join(";", Arrays.copyOfRange(parts, 7, parts.length));
            Customer customer = ListQueriesHandler.getCustomers().stream()
                    .filter(c -> c.toDataString().equals(customerData)).findAny().orElse(null);
            if (customer == null) throw new NullPointerException("Associated customer not found");

            return new Contract(parts[0], Double.parseDouble(parts[1].replace(",", ".")), parts[2], formattedTime, parts[4],
                    parts[5], Double.parseDouble(parts[6].replace(",", ".")), customer);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect delivery time format (must be Days Hours Minutes): " + parts[3]);
        }
    }


}