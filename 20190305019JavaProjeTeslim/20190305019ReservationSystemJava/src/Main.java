import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class ReservationSystemGUI extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/20190305019";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "123123Furkan";

    private List<Reservation> reservationList;
    private JList<String> reservationListView;

    public ReservationSystemGUI() {
        reservationList = new ArrayList<>();

        setTitle("Restaurant Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Create components
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();
        JLabel dateLabel = new JLabel("Date:");
        JTextField dateField = new JTextField();
        JLabel timeLabel = new JLabel("Time:");
        JTextField timeField = new JTextField();
        JButton addButton = new JButton("Add Reservation");
        JButton cancelButton = new JButton("Cancel Reservation");
        JButton viewListButton = new JButton("View Reservation List");
        reservationListView = new JList<>();

        // Set font size for labels and buttons
        Font labelFont = nameLabel.getFont();
        Font buttonFont = addButton.getFont();
        Font largerFont = new Font(labelFont.getName(), Font.BOLD, 16);
        nameLabel.setFont(largerFont);
        dateLabel.setFont(largerFont);
        timeLabel.setFont(largerFont);
        addButton.setFont(buttonFont.deriveFont(Font.BOLD, 16));
        cancelButton.setFont(buttonFont.deriveFont(Font.BOLD, 16));
        viewListButton.setFont(buttonFont.deriveFont(Font.BOLD, 16));

        // Set layout
        setLayout(new GridLayout(6, 2));

        // Add components to the frame
        add(nameLabel);
        add(nameField);
        add(dateLabel);
        add(dateField);
        add(timeLabel);
        add(timeField);
        add(addButton);
        add(cancelButton);
        add(viewListButton);
        add(new JScrollPane(reservationListView));

        // Add action listener to the "Add Reservation" button
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String date = dateField.getText();
            String time = timeField.getText();
            if (!name.isEmpty() && !date.isEmpty() && !time.isEmpty()) {
                if (isReservationTimeAvailable(date, time)) {
                    Reservation reservation = new Reservation(name, date, time);
                    addReservationToDatabase(reservation);
                    reservationList.add(reservation);
                    updateReservationListView();
                    nameField.setText("");
                    dateField.setText("");
                    timeField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "The selected reservation time is not available. Please choose another time.", "Time Conflict", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add action listener to the "Cancel Reservation" button
        cancelButton.addActionListener(e -> {
            int selectedIndex = reservationListView.getSelectedIndex();
            if (selectedIndex != -1) {
                Reservation reservation = reservationList.get(selectedIndex);
                cancelReservation(reservation);
                reservationList.remove(reservation);
                updateReservationListView();
            }
        });

        // Add action listener to the "View Reservation List" button
        viewListButton.addActionListener(e -> {
            showReservationListGUI();
                });

        // Load existing reservations from the database
        loadReservationsFromDatabase();

        // Update the reservation list view
        updateReservationListView();
    }

    private void loadReservationsFromDatabase() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM reservations");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                Reservation reservation = new Reservation(id, name, date, time);
                reservationList.add(reservation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addReservationToDatabase(Reservation reservation) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.prepareStatement("INSERT INTO reservations (name, date, time) VALUES (?, ?, ?)");
            statement.setString(1, reservation.getName());
            statement.setString(2, reservation.getDate());
            statement.setString(3, reservation.getTime());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelReservation(Reservation reservation) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.prepareStatement("DELETE FROM reservations WHERE id = ?");
            statement.setInt(1, reservation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateReservationListView() {
        String[] reservationInfo = new String[reservationList.size()];
        for (int i = 0; i < reservationList.size(); i++) {
            Reservation reservation = reservationList.get(i);
            reservationInfo[i] = "Name: " + reservation.getName() + ", Date: " + reservation.getDate() + ", Time: " + reservation.getTime();
        }
        reservationListView.setListData(reservationInfo);
    }

    private boolean isReservationTimeAvailable(String date, String time) {
        for (Reservation reservation : reservationList) {
            if (reservation.getDate().equals(date) && reservation.getTime().equals(time)) {
                return false; // Time conflict
            }
        }
        return true; // Time available
    }

    private void showReservationListGUI() {
        JFrame reservationListFrame = new JFrame("Reservation List");
        reservationListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reservationListFrame.setSize(400, 300);
        reservationListFrame.setLocationRelativeTo(null);

        // Create components
        DefaultListModel<String> reservationListModel = new DefaultListModel<>();
        JList<String> reservationListView = new JList<>(reservationListModel);
        JScrollPane scrollPane = new JScrollPane(reservationListView);

        // Load reservations from the database
        loadReservationsFromDatabase(reservationListModel);

        // Set layout
        reservationListFrame.setLayout(new BorderLayout());

        // Add components to the frame
        reservationListFrame.add(scrollPane, BorderLayout.CENTER);

        // Show the reservation list GUI
        reservationListFrame.setVisible(true);
    }

    private void loadReservationsFromDatabase(DefaultListModel<String> reservationListModel) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM reservations");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                reservationListModel.addElement("Name: " + name + ", Date: " + date + ", Time: " + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationSystemGUI().setVisible(true));
    }
}

class Reservation {
    private int id;
    private String name;
    private String date;
    private String time;

    public Reservation(String name, String date, String time) {
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public Reservation(int id, String name, String date, String time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
