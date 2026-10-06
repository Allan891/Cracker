import java.io.*;
import java.util.*;

public class PasswordCrack {

    static class User {

        String encryptedPassword;
        String salt;
        String plainPassword = "";
        boolean cracked = false;

        List<String> personalWords = new ArrayList<>();

        User(String encryptedPassword) {
            this.encryptedPassword = encryptedPassword;
            this.salt = encryptedPassword.substring(0, 2);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            return;
        }

        String dictionaryFile = args[0];
        String passwdFile = args[1];

        List<String> dictionary = loadDictionary(dictionaryFile);
        List<User> users = loadUsers(passwdFile);

        // STEP 1: plain dictionary words against all users

        for (String word : dictionary) {
            tryWordForAllUsers(word, users);
            if (allPasswordsCracked(users)) {
                return;
            }
        }

        //STEP 2: personal information words
         
        for (User user : users) {
            if (user.cracked) {
                continue;
            }

            for (String word : user.personalWords) {
                tryWordForSingleUser(word, user);

                if (user.cracked) {
                    break;
                }
            }
        }

        if (allPasswordsCracked(users)) {
            return;
        }

        // STEP 3: Single mangles of dictionary words

        for (String word : dictionary) {
            for (String mangled : mangle(word)) {
                tryWordForAllUsers(mangled, users);
            }

            if (allPasswordsCracked(users)) {
                return;
            }
        }

        // STEP 4: Single mangles of personal words
        for (User user : users) {
            if (user.cracked) {
                continue;
            }

            for (String word : user.personalWords) {
                for (String mangled : mangle(word)) {
                    tryWordForSingleUser(mangled, user);

                    if (user.cracked) {
                        break;
                    }
                }

                if (user.cracked) {
                    break;
                }
            }
        }

        if (allPasswordsCracked(users)) {
            return;
        }

        // STEP 5: Double mangles of dictionary words
        for (String word : dictionary) {

            for (String firstMangle : mangle(word)) {

                for (String secondMangle : mangle(firstMangle)) {

                    tryWordForAllUsers(secondMangle, users);
                }
            }

            if (allPasswordsCracked(users)) {
                return;
            }
        }

        // STEP 6: Double mangles of personal words
        for (User user : users) {
            if (user.cracked) {
                continue;
            }

            for (String word : user.personalWords) {
                for (String firstMangle : mangle(word)) {

                    if (user.cracked) {
                        break;
                    }

                    for (String secondMangle : mangle(firstMangle)) {

                        tryWordForSingleUser(secondMangle, user);

                        if (user.cracked) {
                            break;
                        }
                    }
                }

                if (user.cracked) {
                    break;
                }
            }
        }
    }

    static List<String> loadDictionary(String filename) throws Exception {

        List<String> words = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(new FileReader(filename));

        String line;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            if (!line.isEmpty()) {
                words.add(line);
            }
        }

        reader.close();

        return words;
    }

    static List<User> loadUsers(String filename) throws Exception {

        List<User> users = new ArrayList<>();

        BufferedReader reader =
                new BufferedReader(new FileReader(filename));

        String line;

        while ((line = reader.readLine()) != null) {

            String[] parts = line.split(":");

            if (parts.length < 5) {
                continue;
            }

            User user = new User(parts[1]);

            String username = parts[0];
            user.personalWords.add(username);

            String[] names = parts[4].split(" ");

            List<String> cleanedNames = new ArrayList<>();

            for (String name : names) {

                name = name.trim();

                if (!name.isEmpty()) {

                    user.personalWords.add(name);
                    cleanedNames.add(name);
                }
            }

            //Realistic personal combinations
            if (cleanedNames.size() >= 2) {

                String firstName = cleanedNames.get(0);
                String lastName = cleanedNames.get(1);
                
                user.personalWords.add(firstName + lastName);
                user.personalWords.add(lastName + firstName);

                user.personalWords.add(
                        firstName.toLowerCase()
                                + lastName.toLowerCase());

                user.personalWords.add(
                        firstName.charAt(0)
                                + lastName.toLowerCase());

                user.personalWords.add(firstName + "1");
                user.personalWords.add(lastName + "1");

                user.personalWords.add(firstName + "123");
                user.personalWords.add(lastName + "123");

                user.personalWords.add(firstName + "!");
                user.personalWords.add(lastName + "!");

                user.personalWords.add(firstName + "2024");
                user.personalWords.add(lastName + "2024");
            }
            users.add(user);
        }
        reader.close();
        return users;
    }

    static void tryWordForAllUsers(
            String word,
            List<User> users) {

        word = normalizePassword(word);

        if (word.isEmpty()) {
            return;
        }

        for (User user : users) {
            if (user.cracked) {
                continue;
            }

            String encryptedGuess = jcrypt.crypt(user.salt, word);

            if (encryptedGuess.equals(user.encryptedPassword)) {
                System.out.println(word);
                user.plainPassword = word;
                user.cracked = true;
            }
        }
    }

    static void tryWordForSingleUser(
            String word,
            User user) {
        if (user.cracked) {
            return;
        }

        word = normalizePassword(word);

        if (word.isEmpty()) {
            return;
        }

        String encryptedGuess = jcrypt.crypt(user.salt, word);

        if (encryptedGuess.equals(user.encryptedPassword)) {

            System.out.println(word);

            user.plainPassword = word;
            user.cracked = true;
        }
    }

    /*
     * UNIX crypt only uses the first 8 characters
     */
    static String normalizePassword(String word) {

        if (word.length() > 8) {
            return word.substring(0, 8);
        }

        return word;
    }

    static boolean allPasswordsCracked(List<User> users) {

        for (User user : users) {

            if (!user.cracked) {
                return false;
            }
        }

        return true;
    }

    static String toggleCase(String word) {

        StringBuilder builder = new StringBuilder();

        for (char c : word.toCharArray()) {

            if (Character.isUpperCase(c)) {

                builder.append(Character.toLowerCase(c));

            } else if (Character.isLowerCase(c)) {

                builder.append(Character.toUpperCase(c));

            } else {

                builder.append(c);
            }
        }

        return builder.toString();
    }

    static Set<String> mangle(String word) {

        Set<String> mangledWords = new HashSet<>();

        if (word == null || word.isEmpty()) {
            return mangledWords;
        }

        mangledWords.add(word);

        mangledWords.add(word.toLowerCase());
        mangledWords.add(word.toUpperCase());

        String capitalized = Character.toUpperCase(word.charAt(0))
                            + word.substring(1).toLowerCase();

        mangledWords.add(capitalized);

        // Common endings
        mangledWords.add(word + "1");
        mangledWords.add(word + "12");
        mangledWords.add(word + "123");
        mangledWords.add(word + "1234");

        mangledWords.add(word + "!");
        mangledWords.add(word + "?");
        mangledWords.add(word + ".");
        mangledWords.add(word + "@");

        mangledWords.add(word + "2024");
        mangledWords.add(word + "2025");
        mangledWords.add(word + "2026");

        // Prefixes
        mangledWords.add("!" + word);
        mangledWords.add("@" + word);
        mangledWords.add("_" + word);

        // Suffix symbols
        mangledWords.add(word + "_");
        mangledWords.add(word + "-");

        // Numbers before and after word
        for (int i = 0; i <= 9; i++) {

            mangledWords.add(word + i);
            mangledWords.add(i + word);
        }

        // Reverse word
        String reversed =
                new StringBuilder(word)
                        .reverse()
                        .toString();

        mangledWords.add(reversed);
        mangledWords.add(reversed + "1");
        mangledWords.add(reversed + "123");

        // Toggle uppercase/lowercase
        mangledWords.add(toggleCase(word));

        // Remove first or last character
        if (word.length() > 1) {
            mangledWords.add(word.substring(1));
            mangledWords.add(word.substring(0, word.length() - 1));
        }

        // Duplicate final character
        mangledWords.add(
                word + word.charAt(word.length() - 1));

        //Simple leetspeak replacements
         
        mangledWords.add(word.replace('a', '@'));
        mangledWords.add(word.replace('e', '3'));
        mangledWords.add(word.replace('i', '1'));
        mangledWords.add(word.replace('o', '0'));
        mangledWords.add(word.replace('s', '$'));

        mangledWords.add(
                word.toLowerCase()
                        .replace('a', '@')
                        .replace('e', '3')
                        .replace('i', '1')
                        .replace('o', '0')
                        .replace('s', '$'));

        return mangledWords;
    }
}