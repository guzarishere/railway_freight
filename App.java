import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.Duration;
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
        if (!ListQueriesHandler.getDispatchers().isEmpty() ||
                !ListQueriesHandler.getCustomers().isEmpty() ||
                !ListQueriesHandler.getContracts().isEmpty()) {

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to save the current lists before loading a new file?",
                    "Save Current Data",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (confirm == JOptionPane.YES_OPTION) {
                saveListsToFile();
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a file to load data from");

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getName().endsWith(".txt")) {
                JOptionPane.showMessageDialog(this, "Invalid file format. Please select a .txt file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                ListQueriesHandler.getDispatchers().clear();
                ListQueriesHandler.getCustomers().clear();
                ListQueriesHandler.getContracts().clear();

                String line;
                String currentSection = "";

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.equals("=== Dispatchers ===")) {
                        currentSection = "dispatchers";
                    } else if (line.equals("=== Customers ===")) {
                        currentSection = "customers";
                    } else if (line.equals("=== Contracts ===")) {
                        currentSection = "contracts";
                    } else if (!line.isEmpty()) {
                        switch (currentSection) {
                            case "dispatchers" -> ListQueriesHandler.getDispatchers().add(Dispatcher.fromDataString(line));
                            case "customers" -> ListQueriesHandler.getCustomers().add(Customer.fromDataString(line));
                            case "contracts" -> ListQueriesHandler.getContracts().add(Contract.fromDataString(line));
                            default -> {
                                ListQueriesHandler.getDispatchers().clear();
                                ListQueriesHandler.getCustomers().clear();
                                ListQueriesHandler.getContracts().clear();
                                JOptionPane.showMessageDialog(this, "Invalid file format. Unexpected data outside sections.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }

                if (ListQueriesHandler.getDispatchers().isEmpty() &&
                        ListQueriesHandler.getCustomers().isEmpty() &&
                        ListQueriesHandler.getContracts().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "The file is empty or has no valid data.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                updateDispatcherList();
                updateCustomerList();
                updateContractList();
                JOptionPane.showMessageDialog(this, "Data loaded successfully from:\n" + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                ListQueriesHandler.getDispatchers().clear();
                ListQueriesHandler.getCustomers().clear();
                ListQueriesHandler.getContracts().clear();
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createDispatcher() {
        JTextField companyNameField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField houseField = new JTextField();
        JTextField postcodeField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField countryField = new JTextField();
        JTextField phoneNumberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField workExpField = new JTextField();

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
        panel.add(new JLabel("Dispatcher Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Work Experience (0 <= years <= " + Dispatcher.MAX_WORK_EXP + "):"));
        panel.add(workExpField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Create Dispatcher", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Address address = new Address(
                        streetField.getText(),
                        houseField.getText(),
                        postcodeField.getText(),
                        cityField.getText(),
                        countryField.getText()
                );
                Dispatcher dispatcher = new Dispatcher(
                        companyNameField.getText(),
                        address,
                        phoneNumberField.getText(),
                        nameField.getText(),
                        Integer.parseInt(workExpField.getText())
                );
                Dispatcher duplicate = ListQueriesHandler.getDispatchers().stream().filter(d -> d.equals(dispatcher)).findAny().orElse(null);
                if (duplicate != null) throw new IllegalArgumentException("Such dispatcher already exists");
                ListQueriesHandler.getDispatchers().add(dispatcher);
                updateDispatcherList();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Incorrect work experience format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editDispatcher(String dispatcherStr) {
        Dispatcher dispatcher = ListQueriesHandler.getDispatchers().stream()
                .filter(d -> d.toString().equals(dispatcherStr))
                .findFirst()
                .orElse(null);

        if (dispatcher == null) throw new NullPointerException("Dispatcher not found");

        JTextField companyNameField = new JTextField(dispatcher.getCompany_name());
        JTextField streetField = new JTextField(dispatcher.getAddress().getStreet());
        JTextField houseField = new JTextField(dispatcher.getAddress().getHouse());
        JTextField postcodeField = new JTextField(dispatcher.getAddress().getPostcode());
        JTextField cityField = new JTextField(dispatcher.getAddress().getCity());
        JTextField countryField = new JTextField(dispatcher.getAddress().getCountry());
        JTextField phoneNumberField = new JTextField(dispatcher.getPhone_number());
        JTextField nameField = new JTextField(dispatcher.getName());
        JTextField workExpField = new JTextField(String.valueOf(dispatcher.getWork_exp()));

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
        panel.add(new JLabel("Dispatcher Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Work Experience (0 <= years <= " + Dispatcher.MAX_WORK_EXP + "):"));
        panel.add(workExpField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Dispatcher", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Address address = new Address(
                        streetField.getText(),
                        houseField.getText(),
                        postcodeField.getText(),
                        cityField.getText(),
                        countryField.getText()
                );

                // Check for duplicates
                Dispatcher duplicate = ListQueriesHandler.getDispatchers().stream()
                        .filter(d -> d.getCompany_name().equals(companyNameField.getText()) &&
                                d.getAddress().equals(address) && d.getName().equals(nameField.getText()) &&
                                d.getPhone_number().equals(phoneNumberField.getText()) && d.getWork_exp() == Integer.parseInt(workExpField.getText()))
                        .findAny()
                        .orElse(null);
                if (duplicate != null) throw new IllegalArgumentException("Such dispatcher already exists");

                dispatcher.setCompany_name(companyNameField.getText());
                dispatcher.setAddress(address);
                dispatcher.setPhone_number(phoneNumberField.getText());
                dispatcher.setName(nameField.getText());
                dispatcher.setWork_exp(Integer.parseInt(workExpField.getText()));
                updateDispatcherList();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Incorrect work experience format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createCustomer() {
        JTextField companyNameField = new JTextField();
        JTextField streetField = new JTextField();
        JTextField houseField = new JTextField();
        JTextField postcodeField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField countryField = new JTextField();
        JTextField phoneNumberField = new JTextField();

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

        JTextField companyNameField = new JTextField(customer.getCompany_name());
        JTextField streetField = new JTextField(customer.getAddress().getStreet());
        JTextField houseField = new JTextField(customer.getAddress().getHouse());
        JTextField postcodeField = new JTextField(customer.getAddress().getPostcode());
        JTextField cityField = new JTextField(customer.getAddress().getCity());
        JTextField countryField = new JTextField(customer.getAddress().getCountry());
        JTextField phoneNumberField = new JTextField(customer.getPhone_number());

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

        JTextField dateField = new JTextField(contract.getDate());
        JTextField weightField = new JTextField(String.valueOf(contract.getWeight()));
        JTextField cargoTypeField = new JTextField(contract.getCargo_type());
        JTextField deliveryTimeField = new JTextField(formattedDeliveryTime);
        JTextField departureStationField = new JTextField(contract.getDeparture_st());
        JTextField arrivalStationField = new JTextField(contract.getArrival_st());
        JTextField costField = new JTextField(String.valueOf(contract.getCost()));

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
