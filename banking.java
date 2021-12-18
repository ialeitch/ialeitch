
package banking;

import java.sql.*;
import java.util.*;
import java.sql.PreparedStatement;

public class Main {
    public static void main(String[] args) {
        //System.out.print("Args length: ");
        //System.out.println(args.length);
        //System.out.println("Args: " + args[0] + " " + args[1]);
        final Integer exitCmd = 0;
        final String empty = "";
        Integer userInput = -1;
        boolean isLoggedIn = false;
        Scanner scanner = new Scanner(System.in);
        Map<String, Card> cardMap = new HashMap<>();
        Map<String, Card> transferCardMap = new HashMap<>();
        String dbFile = getFileName(args);
        String loggedInCardNumber = empty;
        String transferCardNumber = empty;
        //connect(dbFile);
        createNewDatabase(dbFile);
        createNewTable(dbFile);
        cardMap = loadCardsFromDb(cardMap, dbFile);
        Card card;
        Card transferCard;
        Integer incomeEntered = 0;
        Integer transferAmount = 0;
        Integer cardBalance = 0;

        while (!userInput.equals(exitCmd)) {
            if (isLoggedIn) {
                printLoggedInMenu();
                userInput = getLoginChoice();
                switch (userInput) {
                    case 1: // Balance
                        // balance
                        card = cardMap.get(loggedInCardNumber);
                        System.out.print("Balance: ");
                        System.out.println(card.getBalance());
                        isLoggedIn = true;
                        //printLoggedInMenu();
                        break;
                    case 2: // Add income
                        card = cardMap.get(loggedInCardNumber);
                        System.out.println();
                        System.out.println("Enter income:");
                        incomeEntered = scanner.nextInt();
                        card.setBalance(card.getBalance() + incomeEntered);
                        saveCardsToDb(cardMap, dbFile);
                        System.out.println("Income was added!");
                        isLoggedIn = true;
                        break;
                    case 3: // Do transfer
                        card = cardMap.get(loggedInCardNumber);
                        System.out.println();
                        System.out.println("Transfer");
                        System.out.println("Enter card number:");
                        transferCardNumber = scanner.nextLine();
                        while (transferCardNumber.equals(empty)) {
                            transferCardNumber = scanner.nextLine();
                        }
                        if (transferCardNumber.equals(loggedInCardNumber)) {
                            System.out.println("You can't transfer money to the same account!");
                        } else {
                            if (!isValidCreditCardNumber(transferCardNumber)) {
                                System.out.println("Probably you made a mistake in the card number. Please try again!");
                            } else {
                                // refresh map from database
                                // saveCardsToDb(cardMap, dbFile);
                                cardMap = loadCardsFromDb(cardMap, dbFile);
                                transferCard = cardMap.get(transferCardNumber);
                                if (transferCard == null) {
                                    System.out.println("Such a card does not exist.");
                                } else {
                                    System.out.println("Enter how much money you want to transfer:");
                                    transferAmount = scanner.nextInt();
                                    cardBalance = card.getBalance();
                                    if (transferAmount > cardBalance) {
                                        System.out.println("Not enough money!");
                                    } else {
                                        card = cardMap.get(loggedInCardNumber);
                                        card.setBalance(card.getBalance() - transferAmount);
                                        transferCard.setBalance(transferCard.getBalance() + transferAmount);
                                        System.out.println("Success!");
                                        saveCardsToDb(cardMap, dbFile);
                                    }
                                }
                            }
                        }
                        isLoggedIn = true;
                        break;
                    case 4: // Close account
                        card = cardMap.get(loggedInCardNumber);
                        card.close();
                        saveCardsToDb(cardMap, dbFile);
                        cardMap = loadCardsFromDb(cardMap, dbFile);
                        System.out.println("The account has been closed!");
                        isLoggedIn = false;
                        break;
                    case 5: // Log out
                        isLoggedIn = false;
                        loggedInCardNumber = empty;
                        System.out.println("You have successfully logged out!");
                        break;
                    case 0: // exit
                        userInput = exitCmd;
                        break;
                    default: // other (should not happen)
                        userInput = exitCmd;
                        System.out.println("Unexpected response");
                        break;
                }
            } else {
                printMenu();
                userInput = getUserInput(scanner);
                switch (userInput) {
                    case 1: // balance
                        if (isLoggedIn) {
                            // balance
                            card = cardMap.get(loggedInCardNumber);
                            System.out.println();
                            System.out.print("Balance: ");
                            System.out.println(card.getBalance());
                            System.out.println("Income was added!");
                            isLoggedIn = true;
                        }
                        else { // create an account
                            createAccount(cardMap);
                            isLoggedIn = false;
                            saveCardsToDb(cardMap, dbFile);
                        }
                        break;
                    case 2: // log into account
                        loggedInCardNumber = cardLogin(cardMap);
                        isLoggedIn = (!loggedInCardNumber.equals(empty));
                        break;
                    case 0: // exit
                        isLoggedIn = false;
                        break;
                    default: // other
                        isLoggedIn = false;
                        break;
                }
                //userInput = getUserInput(scanner);
                //System.out.println("user input is: " + userInput.toString());
            }
            /*
            if (isLoggedIn) {
                userInput = getLoginChoice();
            } else {
                userInput = getUserInput(scanner);
            }
             */
        }
        saveCardsToDb(cardMap, dbFile);
        System.out.println("Bye!");
    }

    private static boolean isWrongChecksum(String transferCardNumber, Card card) {
        boolean isWrong = true;
        String properNumber;
        Integer len = transferCardNumber.length();
        if (len < 16) {
            return true;
        }
        String trialNumber = transferCardNumber.substring(0, len - 1);
        String checkSum = card.getCheckSum(trialNumber);
        properNumber = trialNumber + checkSum;
        isWrong =  !transferCardNumber.equals(trialNumber + checkSum);
        return isWrong;
    }

    private static String cardLogin(Map<String, Card> cardMap) {
        final String empty = "";
        Integer loginMenuChoice = 2;
        boolean isWrong = false;
        String cardNumber = empty;
        String pin = empty;
        String cardPin = empty;
        boolean isLoggedIn = false;
        Scanner scanner = new Scanner(System.in);
        //System.out.println("Log into account");
        System.out.println();
        System.out.println("Enter your card number:");
        cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        pin = scanner.nextLine();
        Card card = cardMap.get(cardNumber);
        if (card == null) {
            isWrong = true;
        }
        if (!isWrong) {
            cardPin = card.getPin();
            if (!cardPin.equals(pin)) {
                isWrong = true;
            }
        }
        if (isWrong) {
            System.out.println("Wrong card number or PIN!");
            return empty;
        } else {
            System.out.println();
            System.out.println("You have successfully logged in!");
            //loginMenuChoice = cardLoginMenu(card);
            return cardNumber;
        }
    }


    private static void saveCardsToDb(Map<String, Card> cardMap, String dbFile) {
        Map<String, Card> localMap = cardMap;
        Integer nextId = getNextId(localMap);
        Integer thisCardId;
        String thisCardNumber;
        String thisCardPin;
        Integer thisCardBalance;
        boolean thisCardIsClosed;
        Card card;
        InsertApp app = new InsertApp();
        UpdateApp updateApp = new UpdateApp();
        DeleteApp deleteApp = new DeleteApp();
        for (String key : cardMap.keySet()) {
            card = localMap.get(key);
            thisCardId = card.getId();
            if (thisCardId == null) {
                thisCardId = nextId;
                nextId++;
            }
            thisCardNumber = key;
            thisCardPin = card.getPin();
            thisCardBalance = card.getBalance();
            thisCardIsClosed = card.getIsClosed();
            deleteApp.delete(thisCardNumber, dbFile);
            if (!thisCardIsClosed) {
                app.insert(thisCardId, thisCardNumber, thisCardPin, thisCardBalance, dbFile);
            }
        }
    }

    private static Integer getNextId(Map<String, Card> localMap) {
        Integer thisId;
        Integer maxId = -1;
        Card card;
        for (String cardNumber : localMap.keySet()) {
            card = localMap.get(cardNumber);
            thisId = card.getId();
            if (thisId != null) {
                if (thisId > maxId) {
                    maxId = thisId;
                }
            }
        }
        return (maxId + 1);
    }

    private static Map<String, Card> loadCardsFromDb(Map<String, Card> cardMap, String dbFile) {
        Map<String, Card> map = cardMap;
        SelectApp app = new SelectApp();
        map = app.SelectAll(map, dbFile);
        return map;
    }

    private static void createNewTable(String dbFile) {
        // SQLite connection string
        //String url = "jdbc:sqlite:C://sqlite/db/tests.db";
        String url = "jdbc:sqlite:" + dbFile;

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	number VARCHAR(32),\n"
                + " pin VARCHAR(8),\n"
                + "	balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private static void createNewDatabase(String dbFile) {

        //String url = "jdbc:sqlite:C:/sqlite/db/" + fileName;
        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                //System.out.println("The driver name is " + meta.getDriverName());
                //System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void connect(String dbFile) {
        Connection conn = null;
        try {
            // db parameters
            //String url = "jdbc:sqlite:C:/sqlite/db/chinook.db";
            String url = "jdbc:sqlite:" + dbFile;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static String getFileName(String[] args) {
        //System.out.println("Args: " + args[0] + " " + args[1]);
        String fileName = "";
        final String fnSwitch = "-filename";
        Integer len = args.length;
        //System.out.print("Args length: ");
        //System.out.println(len);
        for (Integer i = 0; i < len -1; i++) {
            //System.out.println(args[i]);
            if (args[i].toLowerCase().equals(fnSwitch)) {
                fileName = args[i+1];
                //System.out.println(fileName);
            }
        }
        return fileName;
    }

    private static void printMenu() {
        System.out.println();
        String msg = "1. Create an account";
        System.out.println(msg);
        msg = "2. Log into account";
        System.out.println(msg);
        msg = "0. Exit";
        System.out.println(msg);
    }

    private static Integer getUserInput(Scanner scanner) {
        Integer i = 0;
        String lineIn = scanner.nextLine();
        while (!isValid(lineIn, scanner)) {
            System.out.println("Enter 0, 1 or 2");
            printMenu();
            lineIn = scanner.nextLine();
        }
        i = Integer.parseInt(lineIn);
        return i;
    }

    private static boolean isValid(String lineIn, Scanner scanner) {
        Integer i = -1;
        final Integer minMenu = 0;
        final Integer maxMenu = 2;
        try {
            i = Integer.parseInt(lineIn);
        } catch (Exception ex) {
            return false;
        }
        if (i < minMenu || i > maxMenu) {
            return false;
        }
        return true;
    }

    private static Integer doUserChoice (Integer userInput) {
        return userInput;
    }

    private static Integer doUserChoice(Integer userInput, Map<String, Card> cardMap) {
        Integer choice = 0;
        switch (userInput) {
            case 0:
                //exitProgram();
                return 0;
            case 1:
                createAccount(cardMap);
                return 1;
            case 2:
                //choice = cardLogIn(cardMap);
                return choice;
            default:
                return 0;
        }
    }

    private static String cardLogIn(Map <String, Card> cardMap) {
        final String empty = "";
        Integer loginMenuChoice = 2;
        boolean isWrong = false;
        String cardNumber = empty;
        String pin = empty;
        String cardPin = empty;
        boolean isLoggedIn = false;
        Scanner scanner = new Scanner(System.in);
        //System.out.println("Log into account");
        System.out.println("Enter your card number:");
        cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        pin = scanner.nextLine();
        Card card = cardMap.get(cardNumber);
        if (card == null) {
            isWrong = true;
        }
        if (!isWrong) {
            cardPin = card.getPin();
            if (!cardPin.equals(pin)) {
                isWrong = true;
            }
        }
        if (isWrong) {
            System.out.println("Wrong card number or PIN!");
            return empty;
        } else {
            System.out.println("You have successfully logged in!");
            //loginMenuChoice = cardLoginMenu(card);
            return cardNumber;
        }
    }

    private static void exitProgram() {
        // doesn't really exit the program
        //System.out.println("Exit program");
    }

    private static void createAccount(Map <String, Card> cardMap) {
        String cardNumber = "";
        //System.out.println("Create an account");
        Card card = new Card();
        System.out.println();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        cardNumber = card.getCardNumber();
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(card.getPin());
        card.setId(getNextId(cardMap));
        cardMap.put(cardNumber, card);
    }

    private static Integer cardLoginMenu(Card card) {
        boolean loggedIn = true;
        final Integer exitCmd = 0;
        Integer choice = -1;
        while (loggedIn && !choice.equals(exitCmd)) {
            printLoggedInMenu();
            choice = getLoginChoice();
            loggedIn = doLoginChoice(choice, card);
        }
        return choice;
    }

    private static void printLoggedInMenu() {
        System.out.println();
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    private static Integer getLoginChoice() {
        Integer choice = 0;
        final Integer zero = 0;
        String lineIn = "";
        Scanner scanner = new Scanner(System.in);
        lineIn = scanner.nextLine();
        while (!(lineIn.equals("0")
                || lineIn.equals("1")
                || lineIn.equals("2")
                || lineIn.equals("3")
                || lineIn.equals("4")
                || lineIn.equals("5"))) {
            System.out.println("Invalid menu choice - enter 0, 1, 2, 3, 4 or 5");
            printLoggedInMenu();
            lineIn = scanner.nextLine();
        }
        choice = Integer.parseInt(lineIn);
        if (choice.equals(zero)) {
            return zero;
        }
        //return choice + 90;
        return choice;
    }

    private static boolean doLoginChoice(Integer choice, Card card) {
        switch (choice) {
            case 0:
                return false;
            case 1: // Balance
                System.out.print("Balance: ");
                System.out.println(card.getBalance());
                return true;
            case 2: // Add income
                //
                System.out.println("Adding income");
                return true;
            case 3: // Do transfer
                //
                return true;
            case 4: // Close account
                //
                return false;
            case 5:
                System.out.println("You have successfully logged out!");
                return false;
            default:
                return false;
        }
    }
    public static boolean isValidCreditCardNumber(String cardNumber)
    {
        // int array for processing the cardNumber
        int[] cardIntArray=new int[cardNumber.length()];

        for(int i=0;i<cardNumber.length();i++)
        {
            char c= cardNumber.charAt(i);
            cardIntArray[i]=  Integer.parseInt(""+c);
        }

        for(int i=cardIntArray.length-2;i>=0;i=i-2)
        {
            int num = cardIntArray[i];
            num = num * 2;  // step 1
            if(num>9)
            {
                num = num%10 + num/10;  // step 2
            }
            cardIntArray[i]=num;
        }

        int sum = sumDigits(cardIntArray);  // step 3

        //System.out.println(sum);

        if(sum%10==0)  // step 4
        {
            return true;
        }

        return false;

    }
    public static int sumDigits(int[] arr)
    {
        return Arrays.stream(arr).sum();
    }
}

class Card {
    private String cardNumber;
    private String pin;
    private Integer balance;
    private Integer limit;
    private Integer id;
    private boolean isClosed;
    
    Card() {
        this.limit = 10000;
        this.balance = 0;
        this.pin = genPin();
        this.cardNumber = genCardNumber();
        this.isClosed = false;
    }

    Card(Integer cardId, String cardNumber, String cardPin, Integer balance) {
        this.limit = 10000;
        this.balance = balance;
        this.pin = cardPin;
        this.cardNumber = cardNumber;
        this.id = cardId;
        this.isClosed = false;
    }
    
    private String genPin() {
        String pinString = "";
        final String zeros = "0000";
        final Integer pinLength = 4;
        Random random = new Random(System.currentTimeMillis());
        Integer pinNumber = random.nextInt(10000);
        // last 4 digits, left padded with zeros
        pinString = zeros + pinNumber.toString();
        pinString = pinString.substring(pinString.length() - pinLength);
        return pinString;
    }
    
    private String genCardNumber() {
        String cn = "400000" + genPin() + genPin() + "0";
        String checkSum = getCheckSum(cn);
        return cn + checkSum;
        //return "400000" + genPin() + genPin() + "09";
    }

    public boolean getIsClosed() { return this.isClosed; }

    public void setIsClosed(boolean newIsClosed) { this.isClosed = newIsClosed; }

    public void close() { this.isClosed = true; }

    public Integer getId() { return this.id; }

    public void setId(Integer newId) { this.id = newId; }

    public Integer getLimit() {
        return this.limit;
    }
    
    public void setLimit(Integer newLimit) {
        this.limit = newLimit;
    }
    
    public Integer getBalance() {
        return this.balance;
    }
    
    public void setBalance(Integer newBalance) {
        this.balance = newBalance;
    }
    
    public void charge (Integer amount) {
        this.balance += amount;
    }
    
    public void payment (Integer amount) {
        this.balance -= amount;
    }

    String getCardNumber() {
        return this.cardNumber;
    }

    String getPin() {
        return this.pin;
    }

    protected String getCheckSum(String cn) {
        Integer checkSum;
        final Integer ten = 10;
        final Integer nine = 9;
        final Integer two = 2;
        Integer len = cn.length();
        Integer thisInt;
        Integer sum = 0;
        for (Integer i = 0; i < len - 1; i++) {
            thisInt = Integer.parseInt(cn.substring(i,i+1));
            //System.out.print(thisInt); // should be single digit
            if (isEven(i)) {
                thisInt *= two;
                if (thisInt > nine) {
                    thisInt = thisInt - nine;
                }
            }
            sum += thisInt;
        }
        checkSum = ((ten - sum % ten) % ten);
        return checkSum.toString();
    }

    private boolean isEven(Integer thisInt) {
        final Integer two = 2;
        final Integer zero = 0;
        Integer mod2;
        mod2 = thisInt %2;
        return mod2.equals(zero);
    }
}

class SelectApp {

    /**
     * Connect to the test.db database
     * @return the Connection object
     */
    private Connection connect(String dbString) {
        // SQLite connection string
        //String url = "jdbc:sqlite:C://sqlite/db/test.db";
        String url = "jdbc:sqlite:" + dbString;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    /**
     * select all rows in the warehouses table
     */
    public void selectAll(String dbFile){
        String sql = "SELECT id, name, capacity FROM warehouses";
        //String sql = "select id, number, pin, balance from card";

        try (Connection conn = this.connect(dbFile);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" +
                        rs.getString("name") + "\t" +
                        rs.getDouble("capacity"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Map<String, Card> SelectAll(Map<String, Card> map, String dbString) {
        Map<String, Card> localMap = map;
        //String sql = "SELECT id, name, capacity FROM warehouses";
        String sql = "select id, number, pin, balance from card";
        Integer thisId;
        String thisNumber;
        String thisPin;
        Integer thisBalance;

        try (Connection conn = this.connect(dbString);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                thisId = rs.getInt("id");
                thisNumber = rs.getString("number");
                thisPin = rs.getString("pin");
                thisBalance = rs.getInt("balance");
                Card thisCard = new Card(thisId, thisNumber, thisPin, thisBalance);
                localMap.put(thisNumber, thisCard);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return localMap;
    }


    /**
     * @param args the command line arguments
     */
   // public static void main(String[] args) {
        //SelectApp app = new SelectApp();
        //app.selectAll();
    //}
}

class InsertApp {

    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    private Connection connect(String dbString) {
        // SQLite connection string
        //String url = "jdbc:sqlite:C://sqlite/db/test.db";
        String url = "jdbc:sqlite:" + dbString;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Insert a new row into the card table
     *
     * @param cardId
     * @param cardNumber
     * @param cardPin
     * @param cardBalance
     */
    public void insert(Integer cardId, String cardNumber, String cardPin, Integer cardBalance, String dbString) {
        String sql = "INSERT INTO card(id, number, pin, balance) VALUES(?,?,?,?)";

        try (Connection conn = this.connect(dbString);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cardId);
            pstmt.setString(2, cardNumber);
            pstmt.setString(3, cardPin);
            pstmt.setInt(4, cardBalance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {

        InsertApp app = new InsertApp();
        // insert three new rows
        app.insert("Raw Materials", 3000);
        app.insert("Semifinished Goods", 4000);
        app.insert("Finished Goods", 5000);
    }
    */
}

class UpdateApp {

    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    private Connection connect(String dbFile) {
        // SQLite connection string
        //String url = "jdbc:sqlite:C://sqlite/db/test.db";
        String url = "jdbc:sqlite:" + dbFile;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Update data of a warehouse specified by the id
     *
     * @param cardId
     * @param cardNumber
     * @param cardPin
     * @param cardBalance
     */
    public void update(int cardId, String cardNumber, String cardPin, Integer cardBalance, String dbFile) {
       //String sql = "UPDATE warehouses SET name = ? , "
                // + "capacity = ? "
                // + "WHERE id = ?";
        String sql = "update card set id = ?, "
                + "pin = ?, "
                + "balance = ? "
                + "where number = ?";

        try (Connection conn = this.connect(dbFile);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, cardId);
            pstmt.setString(2, cardPin);
            pstmt.setInt(3, cardBalance);
            pstmt.setString(4, cardNumber);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {

        UpdateApp app = new UpdateApp();
        // update the warehouse with id 3
        app.update(3, "Finished Products", 5500);
    }
    */
}

class DeleteApp {

    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    private Connection connect(String dbFile) {
        // SQLite connection string
        //String url = "jdbc:sqlite:C://sqlite/db/test.db";
        String url = "jdbc:sqlite:" + dbFile;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Delete a warehouse specified by the id
     *
     * @param cardNumber
     */
    public void delete(String cardNumber, String dbFile) {
        //String sql = "DELETE FROM warehouses WHERE id = ?";
        String sql = "delete from card where number = ?";

        try (Connection conn = this.connect(dbFile);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, cardNumber);
            // execute the delete statement
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {
        DeleteApp app = new DeleteApp();
        // delete the row with id 3
        app.delete(3);
    }
    */

}
