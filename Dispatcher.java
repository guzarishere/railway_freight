import java.util.regex.Pattern;

public class Dispatcher extends Counterparty {
    private String name;
    private int work_exp; // Work experience in years
    static final int MAX_WORK_EXP = 50; // Work experience limit in years

    public Dispatcher(String company_name, Address address, String phone_number, String name, int work_exp) {
        super(company_name, address, phone_number);

        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ]+(?:\\s[A-Za-zА-Яа-яЁёЇїІіЄєҐґ]+)+$", name)) {
            throw new IllegalArgumentException("Incorrect dispatcher name format");
        }

        if (work_exp < 0 || work_exp > MAX_WORK_EXP) {
            throw new IllegalArgumentException("Incorrect work experience value (must be 0<=work_exp<=" + MAX_WORK_EXP + ")");
        }

        this.name = name;
        this.work_exp = work_exp;
    }

    public String getName() {
        return name;
    }

    public int getWork_exp() {
        return work_exp;
    }

    public void setName(String name) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яЁёЇїІіЄєҐґ]+(?:\\s[A-Za-zА-Яа-яЁёЇїІіЄєҐґ]+)+$", name)) {
            throw new IllegalArgumentException("Incorrect dispatcher name format");
        }

        this.name = name;
    }

    public void setWork_exp(int work_exp) {
        if (work_exp < 0 || work_exp > MAX_WORK_EXP) {
            throw new IllegalArgumentException("Incorrect work experience value (must be 0<=work_exp<=" + MAX_WORK_EXP + ")");
        }

        this.work_exp = work_exp;
    }

    @Override
    public String toString() {
        return
                "Company name: " + company_name +
                "; Dispatcher name: " + name +
                "; Address: " + address +
                "; Phone number: " + phone_number +
                "; Work experience: " + work_exp + " years";
    }

    public String toDataString() {
        return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%d",
                company_name,
                address.getStreet(), address.getHouse(), address.getPostcode(),
                address.getCity(), address.getCountry(),
                phone_number, name, work_exp);
    }

    public static Dispatcher fromDataString(String data) {
        String[] parts = data.split(";");
        if (parts.length != 9) throw new IllegalArgumentException("Invalid dispatcher data");
        Address address = new Address(parts[1], parts[2], parts[3], parts[4], parts[5]);
        return new Dispatcher(parts[0], address, parts[6], parts[7], Integer.parseInt(parts[8]));
    }

    public void copyFrom(Dispatcher other) {
        setCompany_name(other.getCompany_name());
        address.copyFrom(other.getAddress());
        setPhone_number(other.getPhone_number());
        setName(other.getName());
        setWork_exp(other.getWork_exp());
    }
}
