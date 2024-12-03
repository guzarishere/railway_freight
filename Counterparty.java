import java.util.regex.Pattern;

public abstract class Counterparty {
    protected String company_name;
    protected Address address;
    protected String phone_number;

    public Counterparty(String company_name, Address address, String phone_number) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ0-9\\s.,&'’\"()-]+$", company_name)) {
            throw new IllegalArgumentException("Incorrect company name format");
        }

        if (!Pattern.matches("^\\+?[0-9\\s\\-()]{7,15}$", phone_number)) {
            throw new IllegalArgumentException("Incorrect phone number format");
        }

        this.company_name = company_name;
        this.address = address;
        this.phone_number = phone_number;
    }

    public String getCompany_name() {
        return company_name;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setCompany_name(String company_name) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ0-9\\s.,&'’\"()-]+$", company_name)) {
            throw new IllegalArgumentException("Incorrect company name format");
        }

        this.company_name = company_name;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setPhone_number(String phone_number) {
        if (!Pattern.matches("^\\+?[0-9\\s\\-()]{7,15}$", phone_number)) {
            throw new IllegalArgumentException("Incorrect phone number format");
        }

        this.phone_number = phone_number;
    }
}
