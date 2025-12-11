package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator {
    public static void main(String[] args) {
        String[] usernames = {"admin1", "inst1", "stu1", "stu2"};
        for (String i : usernames) {
            String hash = BCrypt.hashpw("pass123", BCrypt.gensalt());
            System.out.println(i + " → " + hash);
        }
    }
}
