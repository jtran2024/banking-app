/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package bankingapp;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class App {
    static Scanner input = new Scanner(System.in);
    static SQLiteDataSource dataSource = new SQLiteDataSource();

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {
        //String url set as program parameter
        dataSource.setUrl(args[0]);

        //Connect to the database
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {

                //Create DB
                createDB(statement);

                //Initialize response and create Map object
                int response = -1;
                HashMap<String, Integer> account = new HashMap<>();

                do {
                    System.out.println("\n1. Create an account\n" +
                            "2. Log into account\n" +
                            "0. Exit");

                    response = input.nextInt();
                    switch (response) {
                        case 1:
                            createAccount(statement, account);
                            break;
                        case 2:
                            logIn(con, account);
                    }
                } while (response != 0);

                System.out.println("\nBye!");

                //Close the Statement Object
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Close the Connection object
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method createAccount
     * @param statement
     * @param account
     * @throws SQLException
     */
    public static void createAccount(Statement statement, HashMap<String, Integer> account) throws SQLException {
        Random rand = new Random();
        Random rand2 = new Random();
        long upper = 999999999L;
        long lower = 100000000L;
        String accountNum = "400000" + (rand.nextLong(upper - lower + 1) + lower);

        accountNum = checkSum(accountNum);

        int pinNum = rand2.nextInt(9999 - 1000 + 1) + 1000;
        System.out.println("\nYour card has been created\nYour card number:\n" + accountNum +
                "\nYour card PIN:\n" + pinNum);
        account.put(accountNum, pinNum);
        insertDB(statement, accountNum, String.valueOf(pinNum));

    }


    /**
     * Luhn's Algorithm to check card validity
     * @param accountNum
     * @return
     */
    public static String checkSum(String accountNum) {
        char numChar;
        int numInteger;
        int sum = 0;
        final int MAX_NUM = 10;
        int checkSum = 0;
        for (int i = 0; i < accountNum.length(); i++) {
            numChar = accountNum.charAt(i);
            numInteger = Integer.parseInt(String.valueOf(numChar));

            if (i % 2 == 0) {
                numInteger *= 2;
            }

            if (numInteger > 9) {
                numInteger -= 9;
            }

            sum += numInteger;
        }

        for (int i = 0; i < MAX_NUM; i++) {
            if ((sum + i) % 10 == 0) {
                checkSum = i;
            }
        }
        return accountNum + checkSum;
    }

    /**
     * Method logIn
     * @param account
     */
    public static void logIn(Connection con, HashMap<String, Integer> account) throws SQLException{
        int response = -1;

        System.out.println("\nEnter your card number:");
        String cardNumber = input.next();
        System.out.println("Enter your PIN:");
        int cardPin = input.nextInt();

        if (checkAcc(con, cardNumber, cardPin)  /* --(using HashMap)  account.containsKey(cardNumber) && account.get(cardNumber).equals(cardPin)*/) {
            System.out.println("\nYou have successfully logged in!");
            int answer = -1;
            int income = 0;

            subModule:
            do {
                System.out.println("\n1. Balance\n" +
                        "2. Add income\n" +
                        "3. Do transfer\n" +
                        "4. Close Account\n" +
                        "5. Log Out\n" +
                        "0. Exit");

                answer = input.nextInt();

                switch (answer) {
                    case 1:
                        checkBalance(con, cardNumber);
                        break;
                    case 2:
                        System.out.println("Enter income:");
                        income = input.nextInt();
                        addIncome(con, income, cardNumber);
                        System.out.println("Income was added!");
                        break;
                    case 3:



                        break;
                    case 4:
                        deleteAcc(con, cardNumber);
                        System.out.println("The account has been closed!");
                        break subModule;
                    case 5:
                        System.out.println("\nYou have successfully logged out");
                        break subModule;
                    case 0:
                        System.exit(0);
                }
            } while (answer != 0);


        } else {
            System.out.println("Wrong card number or PIN");
        }

    }

    /**
     * Method createDB
     * @param statement
     * @throws SQLException
     */
    public static void createDB(Statement statement) throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                "id INTEGER, " +
                "num TEXT," +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0)");

    }

    /**
     * Method InsertDB
     * @param statement
     * @param number
     * @param pinNum
     * @throws SQLException
     */
    public static void insertDB(Statement statement, String number, String pinNum) throws SQLException {
        statement.executeUpdate("INSERT INTO card (num,pin) VALUES ('" + number + "', '" + pinNum + "')");
    }

    public static void addIncome(Connection con, int income, String cardNum) throws SQLException{
        String insert = "UPDATE card SET balance = balance + ? WHERE num = ?";

        try (PreparedStatement statement = con.prepareStatement(insert)){
            statement.setInt(1, income);
            statement.setString(2, cardNum);
            statement.executeUpdate();
        }
    }

    public static void checkBalance(Connection con, String cardNum) throws SQLException{
        String checkBal = "SELECT balance FROM CARD WHERE num = ?";

        try (PreparedStatement statement = con.prepareStatement(checkBal)) {
            statement.setString(1,cardNum);
            statement.executeQuery();
            System.out.print("\nBalance: ");
            System.out.println(statement.executeQuery().getString(1));
        }
    }

    public static boolean checkAcc(Connection con, String cardNumber, int pin) throws SQLException{
        boolean isAccountAvailable = false;
        String queryDB = "SELECT * FROM card WHERE num = ? AND pin = ?";

        try (PreparedStatement statement = con.prepareStatement(queryDB)){
            statement.setString(1, cardNumber);
            statement.setInt(2, pin);
            ResultSet ans = statement.executeQuery();
            isAccountAvailable = ans.isBeforeFirst();
        }
        return isAccountAvailable;
    }

    public static void deleteAcc(Connection con, String cardNumber) throws SQLException{
        String queryDB = "DELETE FROM card WHERE num = ?";

        try (PreparedStatement statement = con.prepareStatement(queryDB)){
            statement.setString(1, cardNumber);
            statement.executeUpdate();
        }
    }

    
}
