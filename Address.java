import java.util.regex.Pattern;

public class Address {
    private String street;
    private String house;
    private String postcode;
    private String city;
    private String country;

    public Address(String street, String house, String postcode, String city, String country) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", street)) {
            throw new IllegalArgumentException("Incorrect street name format");
        }

        if (!Pattern.matches("^\\d+[A-Za-zА-Яа-я]?(/?\\d+)?(-\\d+)?$", house)) {
            throw new IllegalArgumentException("Incorrect house number format");
        }

        if (!Pattern.matches("^[A-Za-z0-9\\s-]{3,10}$", postcode)) {
            throw new IllegalArgumentException("Incorrect postcode format");
        }

        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", city)) {
            throw new IllegalArgumentException("Incorrect city name format");
        }

        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", country)) {
            throw new IllegalArgumentException("Incorrect country name format");
        }

        this.street = street;
        this.house = house;
        this.postcode = postcode;
        this.city = city;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setStreet(String street) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", street)) {
            throw new IllegalArgumentException("Incorrect street name format");
        }

        this.street = street;
    }

    public void setHouse(String house) {
        if (!Pattern.matches("^\\d+[A-Za-zА-Яа-я]?(/?\\d+)?(-\\d+)?$", house)) {
            throw new IllegalArgumentException("Incorrect house number format");
        }

        this.house = house;
    }

    public void setPostcode(String postcode) {
        if (!Pattern.matches("^[A-Za-z0-9\\s-]{3,10}$", postcode)) {
            throw new IllegalArgumentException("Incorrect postcode format");
        }

        this.postcode = postcode;
    }

    public void setCity(String city) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", city)) {
            throw new IllegalArgumentException("Incorrect city name format");
        }

        this.city = city;
    }

    public void setCountry(String country) {
        if (!Pattern.matches("^[A-Za-zА-Яа-яІіЇїЄєҐґ\\s.'’-]+$", country)) {
            throw new IllegalArgumentException("Incorrect country name format");
        }

        this.country = country;
    }

    @Override
    public String toString() {
        return street + " " + house + ", " + postcode + " " + city + ", " + country;
    }
}
