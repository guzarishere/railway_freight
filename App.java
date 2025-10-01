import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class App extends JFrame {
    private final DefaultListModel<String> dispatcherListModel = new DefaultListModel<>();

    private final DefaultListModel<String> customerListModel = new DefaultListModel<>();

    private final DefaultListModel<String> contractListModel = new DefaultListModel<>();

    public App() {
        setTitle("Railway Freight Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JList<String> dispatcherList = new JList<>(dispatcherListModel);
        tabbedPane.addTab("Dispatchers", createListPanel(dispatcherListModel, dispatcherList));
        JList<String> customerList = new JList<>(customerListModel);
        tabbedPane.addTab("Customers", createListPanel(customerListModel, customerList));
        JList<String> contractList = new JList<>(contractListModel);
        tabbedPane.addTab("Contracts", createListPanel(contractListModel, contractList));

        JPanel queryPanel = createQueryPanel();

        add(tabbedPane, BorderLayout.CENTER);
        add(queryPanel, BorderLayout.SOUTH);
    }

    private JPanel createListPanel(DefaultListModel<String> listModel, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save to File");
        JButton loadButton = new JButton("Load from File");
        JButton createButton = new JButton("Create");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        saveButton.addActionListener(e -> saveListsToFile());
        loadButton.addActionListener(e -> loadListsFromFile());
        createButton.addActionListener(e -> createObject(listModel));
        editButton.addActionListener(e -> editObject(listModel, list));
        deleteButton.addActionListener(e -> deleteObject(listModel, list));

        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3));

        JButton contractsListButton = new JButton("List All Contracts");
        JButton dispatcherAddressesButton = new JButton("Dispatcher Addresses");
        JButton customerCompaniesButton = new JButton("Customer Companies");
        JButton maxDeliveryTimeButton = new JButton("Max Delivery Time");
        JButton mostPopularArrivalButton = new JButton("Most Popular Arrival");
        JButton avgDeliveryTimeButton = new JButton("Average Delivery Time");

        contractsListButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getContractsList()));
        dispatcherAddressesButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getDispatchersAddresses()));
        customerCompaniesButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getCustomerCompanies()));
        maxDeliveryTimeButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getMaxDeliveryTime()));
        mostPopularArrivalButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getMostPopularArrival()));
        avgDeliveryTimeButton.addActionListener(e -> showQueryResult(ListQueriesHandler.getAvgDeliveryTime()));

        panel.add(contractsListButton);
        panel.add(dispatcherAddressesButton);
        panel.add(customerCompaniesButton);
        panel.add(maxDeliveryTimeButton);
        panel.add(mostPopularArrivalButton);
        panel.add(avgDeliveryTimeButton);

        return panel;
    }

    private void createObject(DefaultListModel<String> listModel) {
        if (listModel == dispatcherListModel) createDispatcher();
        else if (listModel == customerListModel) createCustomer();
        else if (listModel == contractListModel) createContract();
    }

    private void editObject(DefaultListModel<String> listModel, JList<String> list) {
        String selectedObject = list.getSelectedValue();
        try {
            if (listModel == dispatcherListModel) editDispatcher(selectedObject);
            else if (listModel == customerListModel) editCustomer(selectedObject);
            else if (listModel == contractListModel) editContract(selectedObject);
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(this, "No item selected");
        }
    }

    private void deleteObject(DefaultListModel<String> listModel, JList<String> list) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
            if (listModel == dispatcherListModel) ListQueriesHandler.getDispatchers().remove(selectedIndex);
            else if (listModel == customerListModel) {
                Customer customer = ListQueriesHandler.getCustomers().get(selectedIndex);
                if (!customer.getContract_id().isEmpty()) {
                    Contract contract = ListQueriesHandler.getContracts().stream()
                            .filter(c -> c.getId().equals(customer.getContract_id())).findAny().orElse(null);
                    if (contract != null) {
                        ListQueriesHandler.getContracts().remove(contract);
                        updateContractList();
                    }
                }
                ListQueriesHandler.getCustomers().remove(selectedIndex);
            }
            else if (listModel == contractListModel) {
                Customer customer = ListQueriesHandler.getCustomers().stream()
                        .filter(c -> c.getContract_id().equals(ListQueriesHandler.getContracts().get(selectedIndex).getId()))
                        .findAny().orElse(null);
                if (customer != null){
                    customer.setContract_id(null);
                    updateCustomerList();
                    ListQueriesHandler.getContracts().remove(selectedIndex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No item selected");
        }
    }

    private void showQueryResult(String result) {
        JTextArea textArea = new JTextArea(20, 30);
        textArea.setText(result);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JOptionPane.showMessageDialog(this, scrollPane, "Query Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveListsToFile() {
        if (ListQueriesHandler.getDispatchers().isEmpty() &&
                ListQueriesHandler.getCustomers().isEmpty() &&
                ListQueriesHandler.getContracts().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All lists are empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath().split("\\.")[0] + ".txt");
            }

            if (selectedFile.exists()) {
                int overwriteOption = JOptionPane.showConfirmDialog(
                        this,
                        "File already exists. Do you want to overwrite it?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwriteOption != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (PrintWriter writer = new PrintWriter(selectedFile)) {
                writer.println("=== Dispatchers ===");
                for (Dispatcher dispatcher : ListQueriesHandler.getDispatchers()) {
                    writer.println(dispatcher.toDataString());
                }
                writer.println("=== Customers ===");
                for (Customer customer : ListQueriesHandler.getCustomers()) {
                    writer.println(customer.toDataString());
                }
                writer.println("=== Contracts ===");
                for (Contract contract : ListQueriesHandler.getContracts()) {
                    writer.println(contract.toDataString());
                }
                JOptionPane.showMessageDialog(this, "Data saved successfully to: " + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadListsFromFile() {
        if (!confirmReplaceCurrentData()) return;

        File selectedFile = chooseFile("Choose a file to load data from", JFileChooser.OPEN_DIALOG);
        if (selectedFile == null) return;

        try {
            loadDataFromFile(selectedFile);
            refreshUILists();
            JOptionPane.showMessageDialog(this, "Data loaded successfully from:\n" + selectedFile.getAbsolutePath());
        } catch (Exception e) {
            clearAllLists();
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmReplaceCurrentData() {
        if (ListQueriesHandler.getDispatchers().isEmpty() &&
                ListQueriesHandler.getCustomers().isEmpty() &&
                ListQueriesHandler.getContracts().isEmpty()) return true;

        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to save the current lists before loading a new file?",
                "Save Current Data", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.CANCEL_OPTION) return false;
        if (confirm == JOptionPane.YES_OPTION) saveListsToFile();
        return true;
    }

    private File chooseFile(String title, int dialogType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        int returnValue = (dialogType == JFileChooser.OPEN_DIALOG) ?
                fileChooser.showOpenDialog(this) : fileChooser.showSaveDialog(this);
        return (returnValue == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    private void loadDataFromFile(File file) throws Exception {
        if (!file.getName().endsWith(".txt")) throw new IllegalArgumentException("Invalid file format");

        clearAllLists();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            parseFileContents(reader);
        }
    }

    private void parseFileContents(BufferedReader reader) throws Exception {
        String line, currentSection = "";
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            switch (line) {
                case "=== Dispatchers ===" -> currentSection = "dispatchers";
                case "=== Customers ===" -> currentSection = "customers";
                case "=== Contracts ===" -> currentSection = "contracts";
                default -> parseEntity(line, currentSection);
            }
        }
    }

    private void parseEntity(String line, String section) {
        if (line.isEmpty()) return;
        switch (section) {
            case "dispatchers" -> ListQueriesHandler.getDispatchers().add(Dispatcher.fromDataString(line));
            case "customers" -> ListQueriesHandler.getCustomers().add(Customer.fromDataString(line));
            case "contracts" -> ListQueriesHandler.getContracts().add(Contract.fromDataString(line));
            default -> throw new IllegalArgumentException("Unexpected data outside sections.");
        }
    }

    private void clearAllLists() {
        ListQueriesHandler.getDispatchers().clear();
        ListQueriesHandler.getCustomers().clear();
        ListQueriesHandler.getContracts().clear();
    }

    private void refreshUILists() {
        updateDispatcherList();
        updateCustomerList();
        updateContractList();
    }

    private void createDispatcher() {
        JPanel panel = buildDispatcherForm(null);  // separate method for building the form
        int result = JOptionPane.showConfirmDialog(null, panel, "Create Dispatcher", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Dispatcher dispatcher = buildDispatcherFromForm(panel);  // separate method for building the object
                if (isDuplicateDispatcher(dispatcher)) { // separate method checking for duplicates
                    throw new IllegalArgumentException("Such dispatcher already exists");
                }
                ListQueriesHandler.getDispatchers().add(dispatcher);
                updateDispatcherList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTextField createLimitedField(String value, int limit) {
        JTextField field = new JTextField();
        field.setDocument(new JTextFieldLimit(limit));
        field.setText(value != null ? value : "");
        return field;
    }

    private JPanel buildDispatcherForm(Dispatcher existing) {
        JTextField companyNameField = createLimitedField(existing != null ? existing.getCompany_name() : "", 50);
        JTextField streetField = createLimitedField(existing != null ? existing.getAddress().getStreet() : "", 50);
        JTextField houseField = createLimitedField(existing != null ? existing.getAddress().getHouse() : "", 10);
        JTextField postcodeField = createLimitedField(existing != null ? existing.getAddress().getPostcode() : "", 10);
        JTextField cityField = createLimitedField(existing != null ? existing.getAddress().getCity() : "", 50);
        JTextField countryField = createLimitedField(existing != null ? existing.getAddress().getCountry() : "", 50);
        JTextField phoneNumberField = createLimitedField(existing != null ? existing.getPhone_number() : "", 16);
        JTextField nameField = createLimitedField(existing != null ? existing.getName() : "", 50);
        JTextField workExpField = createLimitedField(existing != null ? String.valueOf(existing.getWork_exp()) : "", 2);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Company Name:")); panel.add(companyNameField);
        panel.add(new JLabel("Street:")); panel.add(streetField);
        panel.add(new JLabel("House:")); panel.add(houseField);
        panel.add(new JLabel("Postcode:")); panel.add(postcodeField);
        panel.add(new JLabel("City:")); panel.add(cityField);
        panel.add(new JLabel("Country:")); panel.add(countryField);
        panel.add(new JLabel("Phone Number:")); panel.add(phoneNumberField);
        panel.add(new JLabel("Dispatcher Name:")); panel.add(nameField);
        panel.add(new JLabel("Work Experience (0 <= years <= " + Dispatcher.MAX_WORK_EXP + "):"));
        panel.add(workExpField);

        panel.putClientProperty("fields", new JTextField[]{
                companyNameField, streetField, houseField, postcodeField, cityField,
                countryField, phoneNumberField, nameField, workExpField
        });

        return panel;
    }

    private Dispatcher buildDispatcherFromForm(JPanel panel) {
        JTextField[] fields = (JTextField[]) panel.getClientProperty("fields");
        if (Arrays.stream(fields).anyMatch(f -> f.getText().trim().isEmpty())) {
            throw new IllegalArgumentException("All fields must be filled");
        }
        return new Dispatcher(
                fields[0].getText(),
                new Address(fields[1].getText(), fields[2].getText(), fields[3].getText(), fields[4].getText(), fields[5].getText()),
                fields[6].getText(),
                fields[7].getText(),
                Integer.parseInt(fields[8].getText())
        );
    }

    private boolean isDuplicateDispatcher(Dispatcher dispatcher) {
        return ListQueriesHandler.getDispatchers().stream().anyMatch(d -> d.equals(dispatcher));
    }

    private void editDispatcher(String dispatcherStr) {
        Dispatcher dispatcher = findDispatcher(dispatcherStr);
        if (dispatcher == null) throw new NullPointerException("Dispatcher not found");

        JPanel panel = buildDispatcherForm(dispatcher);
        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Dispatcher", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Dispatcher updated = buildDispatcherFromForm(panel);
                if (isDuplicateDispatcher(updated)) {
                    throw new IllegalArgumentException("Such dispatcher already exists");
                }
                dispatcher.copyFrom(updated);
                updateDispatcherList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Dispatcher findDispatcher(String dispatcherStr) {
        return ListQueriesHandler.getDispatchers().stream()
                .filter(d -> d.toString().equals(dispatcherStr))
                .findFirst()
                .orElse(null);
    }

    private void createCustomer() {
        JTextField companyNameField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField houseField = new JTextField();
        JTextField postcodeField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField countryField = new JTextField();
        JTextField phoneNumberField = new JTextField();

        companyNameField.setDocument(new JTextFieldLimit(50));
        streetField.setDocument(new JTextFieldLimit(50));
        houseField.setDocument(new JTextFieldLimit(10));
        postcodeField.setDocument(new JTextFieldLimit(10));
        cityField.setDocument(new JTextFieldLimit(50));
        countryField.setDocument(new JTextFieldLimit(50));
        phoneNumberField.setDocument(new JTextFieldLimit(16));

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Company Name:"));
        panel.add(companyNameField);
        panel.add(new JLabel("Street:"));
        panel.add(streetField);
        panel.add(new JLabel("House:"));
        panel.add(houseField);
        panel.add(new JLabel("Postcode:"));
        panel.add(postcodeField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("Country:"));
        panel.add(countryField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneNumberField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Create Customer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (companyNameField.getText().trim().isEmpty() || streetField.getText().trim().isEmpty() || houseField.getText().trim().isEmpty() ||
                        postcodeField.getText().trim().isEmpty() || cityField.getText().trim().isEmpty() || countryField.getText().trim().isEmpty() ||
                        phoneNumberField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("All fields must be filled");
                }

                Address address = new Address(
                        streetField.getText(),
                        houseField.getText(),
                        postcodeField.getText(),
                        cityField.getText(),
                        countryField.getText()
                );

                // Check for duplicates
                Customer duplicate = ListQueriesHandler.getCustomers().stream()
                        .filter(d -> d.getCompany_name().equals(companyNameField.getText()) &&
                                d.getAddress().equals(address) &&
                                d.getPhone_number().equals(phoneNumberField.getText()))
                        .findAny()
                        .orElse(null);
                if (duplicate != null) throw new IllegalArgumentException("Such customer already exists");

                Customer customer = new Customer(
                        companyNameField.getText(),
                        address,
                        phoneNumberField.getText()
                );
                ListQueriesHandler.getCustomers().add(customer);
                updateCustomerList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCustomer(String customerStr) {
        Customer customer = ListQueriesHandler.getCustomers().stream()
                .filter(c -> c.toString().equals(customerStr))
                .findFirst()
                .orElse(null);

        if (customer == null) throw new NullPointerException("Customer not found");

        JTextField companyNameField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField houseField = new JTextField();
        JTextField postcodeField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField countryField = new JTextField();
        JTextField phoneNumberField = new JTextField();

        companyNameField.setDocument(new JTextFieldLimit(50));
        streetField.setDocument(new JTextFieldLimit(50));
        houseField.setDocument(new JTextFieldLimit(10));
        postcodeField.setDocument(new JTextFieldLimit(10));
        cityField.setDocument(new JTextFieldLimit(50));
        countryField.setDocument(new JTextFieldLimit(50));
        phoneNumberField.setDocument(new JTextFieldLimit(16));

        companyNameField.setText(customer.getCompany_name());
        streetField.setText(customer.getAddress().getStreet());
        houseField.setText(customer.getAddress().getHouse());
        postcodeField.setText(customer.getAddress().getPostcode());
        cityField.setText(customer.getAddress().getCity());
        countryField.setText(customer.getAddress().getCountry());
        phoneNumberField.setText(customer.getPhone_number());

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Company Name:"));
        panel.add(companyNameField);
        panel.add(new JLabel("Street:"));
        panel.add(streetField);
        panel.add(new JLabel("House:"));
        panel.add(houseField);
        panel.add(new JLabel("Postcode:"));
        panel.add(postcodeField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("Country:"));
        panel.add(countryField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneNumberField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Customer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (companyNameField.getText().trim().isEmpty() || streetField.getText().trim().isEmpty() || houseField.getText().trim().isEmpty() ||
                        postcodeField.getText().trim().isEmpty() || cityField.getText().trim().isEmpty() || countryField.getText().trim().isEmpty() ||
                        phoneNumberField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("All fields must be filled");
                }

                Address address = new Address(
                        streetField.getText(),
                        houseField.getText(),
                        postcodeField.getText(),
                        cityField.getText(),
                        countryField.getText()
                );

                // Check for duplicates
                Customer duplicate = ListQueriesHandler.getCustomers().stream()
                        .filter(d -> d.getCompany_name().equals(companyNameField.getText()) &&
                                d.getAddress().equals(address) &&
                                d.getPhone_number().equals(phoneNumberField.getText()))
                        .findAny()
                        .orElse(null);
                if (duplicate != null) throw new IllegalArgumentException("Such customer already exists");

                customer.setCompany_name(companyNameField.getText());
                customer.setAddress(address);
                customer.setPhone_number(phoneNumberField.getText());
                updateCustomerList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createContract() {
        JTextField dateField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField cargoTypeField = new JTextField();
        JTextField deliveryTimeField = new JTextField();
        JTextField departureStationField = new JTextField();
        JTextField arrivalStationField = new JTextField();
        JTextField costField = new JTextField();

        dateField.setDocument(new JTextFieldLimit(10));
        weightField.setDocument(new JTextFieldLimit(10));
        cargoTypeField.setDocument(new JTextFieldLimit(50));
        deliveryTimeField.setDocument(new JTextFieldLimit(10));
        departureStationField.setDocument(new JTextFieldLimit(50));
        arrivalStationField.setDocument(new JTextFieldLimit(50));
        costField.setDocument(new JTextFieldLimit(20));

        List<Customer> availableCustomers = ListQueriesHandler.getCustomers().stream()
                .filter(customer -> customer.getContract_id().equals(""))
                .toList();
        if (availableCustomers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There's no available customers", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<Customer> customerComboBox = new JComboBox<>(new Vector<>(availableCustomers));

        customerComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    label.setText(value.toString());
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerComboBox,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setPreferredSize(new Dimension(600, 300));
        panel.add(new JLabel("Date (DD-MM-YYYY):"));
        panel.add(dateField);
        panel.add(new JLabel("Weight (tonnes, 5 <= w <= 100):"));
        panel.add(weightField);
        panel.add(new JLabel("Cargo Type:"));
        panel.add(cargoTypeField);
        panel.add(new JLabel("Delivery Time (Days Hours Minutes):"));
        panel.add(deliveryTimeField);
        panel.add(new JLabel("Departure Station:"));
        panel.add(departureStationField);
        panel.add(new JLabel("Arrival Station:"));
        panel.add(arrivalStationField);
        panel.add(new JLabel("Cost (UAH, >0):"));
        panel.add(costField);
        panel.add(new JLabel("Customer:"));
        panel.add(scrollPane);

        int result = JOptionPane.showConfirmDialog(null, panel, "Create Contract", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String date = dateField.getText();
                double weight = Double.parseDouble(weightField.getText());
                String cargoType = cargoTypeField.getText();
                String deliveryTime = deliveryTimeField.getText();
                String departureStation = departureStationField.getText();
                String arrivalStation = arrivalStationField.getText();
                double cost = Double.parseDouble(costField.getText());
                Customer customer = (Customer) customerComboBox.getSelectedItem();

                if (date.trim().isEmpty() || weightField.getText().trim().isEmpty() || cargoType.trim().isEmpty() ||
                deliveryTime.trim().isEmpty() || departureStation.trim().isEmpty() || arrivalStation.trim().isEmpty() ||
                costField.getText().trim().isEmpty() || customer == null) {
                    throw new IllegalArgumentException("All fields must be filled");
                }

                Contract contract = new Contract(
                        date, weight, cargoType, deliveryTime, departureStation, arrivalStation, cost, customer
                );

                ListQueriesHandler.getContracts().add(contract);
                updateContractList();
                updateCustomerList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editContract(String contractStr) {
        Contract contract = ListQueriesHandler.getContracts().stream()
                .filter(c -> c.toString().equals(contractStr))
                .findFirst()
                .orElse(null);

        if (contract == null) throw new NullPointerException("Contract not found");

        String formattedDeliveryTime;
        try {
            Duration duration = contract.getDelivery_time();
            long days = duration.toDays();
            long hours = duration.toHoursPart();
            long minutes = duration.toMinutesPart();
            formattedDeliveryTime = String.format("%d %d %d", days, hours, minutes);
        } catch (Exception e) {
            formattedDeliveryTime = "0 0 0";
        }

        JTextField dateField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField cargoTypeField = new JTextField();
        JTextField deliveryTimeField = new JTextField();
        JTextField departureStationField = new JTextField();
        JTextField arrivalStationField = new JTextField();
        JTextField costField = new JTextField();

        dateField.setDocument(new JTextFieldLimit(10));
        weightField.setDocument(new JTextFieldLimit(10));
        cargoTypeField.setDocument(new JTextFieldLimit(50));
        deliveryTimeField.setDocument(new JTextFieldLimit(10));
        departureStationField.setDocument(new JTextFieldLimit(50));
        arrivalStationField.setDocument(new JTextFieldLimit(50));
        costField.setDocument(new JTextFieldLimit(20));

        dateField.setText(contract.getDate());
        weightField.setText(String.valueOf(contract.getWeight()));
        cargoTypeField.setText(contract.getCargo_type());
        deliveryTimeField.setText(formattedDeliveryTime);
        departureStationField.setText(contract.getDeparture_st());
        arrivalStationField.setText(contract.getArrival_st());
        costField.setText(String.valueOf(contract.getCost()));

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Date (DD-MM-YYYY):"));
        panel.add(dateField);
        panel.add(new JLabel("Weight (tonnes, 5 <= w <= 100):"));
        panel.add(weightField);
        panel.add(new JLabel("Cargo Type:"));
        panel.add(cargoTypeField);
        panel.add(new JLabel("Delivery Time (Days Hours Minutes):"));
        panel.add(deliveryTimeField);
        panel.add(new JLabel("Departure Station:"));
        panel.add(departureStationField);
        panel.add(new JLabel("Arrival Station:"));
        panel.add(arrivalStationField);
        panel.add(new JLabel("Cost (UAH, >0):"));
        panel.add(costField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Contract", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String date = dateField.getText();
                double weight = Double.parseDouble(weightField.getText());
                String cargoType = cargoTypeField.getText();
                String deliveryTime = deliveryTimeField.getText();
                String departureStation = departureStationField.getText();
                String arrivalStation = arrivalStationField.getText();
                double cost = Double.parseDouble(costField.getText());

                if (date.trim().isEmpty() || weightField.getText().trim().isEmpty() || cargoType.trim().isEmpty() ||
                        deliveryTime.trim().isEmpty() || departureStation.trim().isEmpty() || arrivalStation.trim().isEmpty() ||
                        costField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("All fields must be filled");
                }

                contract.setDate(date);
                contract.setWeight(weight);
                contract.setCargo_type(cargoType);
                contract.setDelivery_time(deliveryTime);
                contract.setDeparture_st(departureStation);
                contract.setArrival_st(arrivalStation);
                contract.setCost(cost);

                updateContractList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCustomerList() {
        customerListModel.clear();
        for (Customer customer : ListQueriesHandler.getCustomers()) {
            customerListModel.addElement(customer.toString());
        }
    }

    private void updateDispatcherList() {
        dispatcherListModel.clear();
        for (Dispatcher dispatcher : ListQueriesHandler.getDispatchers()) {
            dispatcherListModel.addElement(dispatcher.toString());
        }
    }

    private void updateContractList() {
        contractListModel.clear();
        for (Contract contract : ListQueriesHandler.getContracts()) {
            contractListModel.addElement(contract.toString());
        }
    }

    public static void main(String[] args) {
        App window = new App();
        window.setVisible(true);
    }
}