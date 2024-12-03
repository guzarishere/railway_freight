public class Customer extends Counterparty {
    private String contract_id;

    public Customer(String company_name, Address address, String phone_number) {
        super(company_name, address, phone_number);
        this.contract_id = ""; // will be set when the contract is created
    }

    public String getContract_id() {
        return contract_id;
    }

    // This method takes a Contract object as a parameter
    // so that you can only set id from an existing contract
    public void setContract_id(Contract contract) {
        if (contract == null) {
            this.contract_id = "";
        }
        else {
            this.contract_id = contract.getId();
        }
    }

    @Override
    public String toString() {
        return
                "Company name: " + company_name +
                "; Address: " + address +
                "; Phone number: " + phone_number +
                "; Contract ID: " + (contract_id.isEmpty() ? "not found" : contract_id);
    }

    public String toDataString() {
        return String.format("%s;%s;%s;%s;%s;%s;%s",
                company_name,
                address.getStreet(), address.getHouse(), address.getPostcode(),
                address.getCity(), address.getCountry(),
                phone_number);
    }

    public static Customer fromDataString(String data) {
        String[] parts = data.split(";");
        if (parts.length != 7) throw new IllegalArgumentException("Invalid customer data");
        Address address = new Address(parts[1], parts[2], parts[3], parts[4], parts[5]);
        return new Customer(parts[0], address, parts[6]);
    }

}
