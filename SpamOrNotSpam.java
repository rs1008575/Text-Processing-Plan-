import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class SpamOrNotSpam {

    public static List<EmailData> readEmailData(String filename) {
        List<EmailData> emailDataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read and ignore the header row
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String email = parts[0].trim();
                    int isSpamValue = Integer.parseInt(parts[1].trim());
                    boolean isSpam = true;
                    if (isSpamValue == 1) {
                        isSpam = false;
                    }
        
                    EmailData emailData = new EmailData(email, isSpam);
                    emailDataList.add(emailData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }

        return emailDataList;
    }

    static void printListData(List<EmailData> data) {
        for (int i = 0; i < data.size(); i++) {
            System.out.println("Index:" + (i + 1) + " " + data.get(i).email + "\n isSpam:" + data.get(i).isSpam);
        }
    }

    public static int countWord(String email, String word) {
        int count = 0;
        String[] words = email.split("\\s+"); // Split the email into words using whitespace as the delimiter

        for (String w : words) {
            if (w.equalsIgnoreCase(word)) { // Use equalsIgnoreCase to perform a case-insensitive comparison
                count++;
            }
        }

        return count;
    }

    public static List<EmailAttributes> populateAttributesOfEmail(List<EmailData> data) {
        List<EmailAttributes> attributesList = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            EmailAttributes attributes = new EmailAttributes(); // Create a new instance of EmailAttributes
            attributes.a_count = countWord(data.get(i).email, "a");
            attributes.is_count = countWord(data.get(i).email, "is");
            attributes.the_count = countWord(data.get(i).email, "the");
            attributes.day_count = countWord(data.get(i).email, "day");
            attributes.num_count = countWord(data.get(i).email, "Number");
            attributes.bright_count = countWord(data.get(i).email, "www");
            attributes.am_count = countWord(data.get(i).email, "am");
            attributes.isSpam = data.get(i).isSpam;
            attributesList.add(attributes);
        }
        return attributesList;
    }

    public static EmailAttributes populateAttributesOfEmail(String data) {
        EmailAttributes attributes = new EmailAttributes(); // Create a new instance of EmailAttributes
        attributes.a_count = countWord(data, "a");
        attributes.is_count = countWord(data, "is");
        attributes.the_count = countWord(data, "the");
        attributes.day_count = countWord(data, "day");
        attributes.num_count = countWord(data, "Number");
        attributes.bright_count = countWord(data, "www");
        attributes.am_count = countWord(data, "am");
        return attributes;
    }

    public static void writeEmailAttributesToCSV(List<EmailAttributes> emailAttributesList, String csvFileName) {
        try (FileWriter writer = new FileWriter(csvFileName)) {
            // Write the header row
            writer.write("email,a_count,the_count,is_count,day_count,www_count,num_count,am_count,isSpam\n");

            int i = 0;
            for (EmailAttributes attributes : emailAttributesList) {
                writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%b\n", i + 1,
                        attributes.a_count, attributes.the_count, attributes.is_count,
                        attributes.day_count, attributes.bright_count, attributes.num_count,
                        attributes.am_count, attributes.isSpam));
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSummaryToCSV(List<EmailAttributes> emailAttributesList, String csvFileName) {
        try (FileWriter writer = new FileWriter(csvFileName)) {
            // Write the header row
            writer.write("Attribute,Mean,Min,Max,StandardDeviation\n");

            // Calculate summary statistics for each attribute
            String[] attributeNames = {"a_count", "the_count", "is_count", "day_count", "www_count", "num_count", "am_count"};
            for (String attributeName : attributeNames) {
                List<Double> attributeValues = emailAttributesList.stream()
                        .map(attributes -> {
                            switch (attributeName) {
                                case "a_count":
                                    return (double) attributes.a_count;
                                case "the_count":
                                    return (double) attributes.the_count;
                                case "is_count":
                                    return (double) attributes.is_count;
                                case "day_count":
                                    return (double) attributes.day_count;
                                case "bright_count":
                                    return (double) attributes.bright_count;
                                case "num_count":
                                    return (double) attributes.num_count;
                                case "am_count":
                                    return (double) attributes.am_count;
                                default:
                                    return 0.0;
                            }
                        })
                        .collect(Collectors.toList());

                double mean = attributeValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double min = attributeValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double max = attributeValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

                // Calculate standard deviation manually
                double variance = attributeValues.stream().mapToDouble(value -> Math.pow(value - mean, 2)).average().orElse(0.0);
                double stdDeviation = Math.sqrt(variance);

                // Write the summary to the CSV file
                writer.write(String.format("%s,%.2f,%.2f,%.2f,%.2f\n", attributeName, mean, min, max, stdDeviation));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearConsole() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            EmailAttributesClassifier emailAttributesClassifier = new EmailAttributesClassifier();
            List<EmailAttributes> attributesList = new ArrayList<EmailAttributes>(0);
            int choice = 5;
            while (choice != 0) {
                clearConsole();
                System.out.println("Press 0 to exit");
                System.out.println("Press 1 to load CSV file of spam and not spam emails");
                System.out.println("Press 2 to write attributes for spam and not spam emails in csv file");
                System.out.println("Press 3 to write summary data to csv file");
                System.out.println("Press 4 to train classifiers based on numeric distance");
                System.out.println("Press 5 to predict email as spam or not");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 0:
                        System.out.println("Exiting the program.");
                        System.exit(0);
                        break;
                    case 1:
                        System.out.print("Enter the filename for the CSV file: ");
                        String filename = scanner.nextLine();
                        List<EmailData> data = readEmailData(filename);
                        attributesList = populateAttributesOfEmail(data);
                        System.out.println("CSV file loaded and attributes populated.");
                        break;
                    case 2:
                        if (attributesList.isEmpty()) {
                            System.out.println("Please load the CSV file first (Option 1).");
                        } else {
                            System.out.print("Enter the filename for the CSV file: ");

                            String filename2 = scanner.nextLine();
                            writeEmailAttributesToCSV(attributesList, filename2);
                            System.out.println("Email attributes written to " + filename2);
                        }
                        break;
                    case 3:
                        if (attributesList.isEmpty()) {
                            System.out.println("Please load the CSV file and populate attributes first (Option 1 and 2).");
                        } else {
                            System.out.print("Enter the filename for the CSV file: ");

                            String filename3 = scanner.nextLine();
                            writeSummaryToCSV(attributesList, filename3);
                            System.out.println("Summary data written to " + filename3);
                        }
                        break;
                    case 4:
                        if (attributesList.isEmpty()) {
                            System.out.println("Please load the CSV file and populate attributes first (Option 1 and 2).");
                        } else {
                            emailAttributesClassifier.trainClassifier(attributesList);
                            System.out.println("Classifiers trained based on numeric distance.");
                        }
                        break;
                    case 5:
                        if (attributesList.isEmpty() || emailAttributesClassifier == null) {
                            System.out.println("Please load the CSV file, populate attributes, and train classifiers first (Options 1, 2, and 4).");
                        } else {
                            EmailAttributes newEmail = new EmailAttributes();
                            System.out.print("Please write testing email: ");
                            String email = scanner.nextLine();
                            newEmail = populateAttributesOfEmail(email);
                            boolean isSpam = emailAttributesClassifier.predict(newEmail) > 0.5;
                            if (isSpam) {
                                System.out.println("The new email is classified as spam.");
                            } else {
                                System.out.println("The new email is classified as non-spam.");
                            }
                        }
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose a valid option.");
                }

            }
        }
    }
}

class EmailAttributesClassifier {

    private double[] weights; // Weight vector for the logistic regression model

    public void trainClassifier(List<EmailAttributes> emails) {
        int numFeatures = 7; // Assuming there are 7 features in EmailAttributes
        weights = new double[numFeatures];

        // Training hyperparameters
        double learningRate = 0.01;
        int numIterations = 1000;

        for (int iteration = 0; iteration < numIterations; iteration++) {
            for (EmailAttributes email : emails) {
                double prediction = predict(email);
                double error = email.isSpam ? 1.0 - prediction : 0.0 - prediction;

                for (int i = 0; i < numFeatures; i++) {
                    weights[i] += learningRate * error * email.getFeature(i);
                }
            }
        }
    }

    public double predict(EmailAttributes email) {
        double score = 0;
        for (int i = 0; i < weights.length; i++) {
            score += weights[i] * email.getFeature(i);
        }
        return 1.0 / (1.0 + Math.exp(-score));
    }
}

class EmailData {

    EmailData(String email1, boolean spam) {
        this.email = email1;
        this.isSpam = spam;
    }

    public String email;
    public Boolean isSpam;
}

class EmailAttributes {

    public int a_count;
    public int the_count;
    public int is_count;
    public int day_count;
    public int bright_count;
    public int num_count;
    public int am_count;
    public boolean isSpam;

    public double getFeature(int index) {
        switch (index) {
            case 0:
                return a_count;
            case 1:
                return the_count;
            case 2:
                return is_count;
            case 3:
                return day_count;
            case 4:
                return bright_count;
            case 5:
                return num_count;
            case 6:
                return am_count;
            default:
                return 0.0;
        }
    }
}
