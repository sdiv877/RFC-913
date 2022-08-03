package server;

import java.util.List;

public class User {
    private String id;
    private List<String> accounts;
    private List<String> passwords;

    public User(String id, List<String> accounts, List<String> passwords) {
        this.id = id;
        this.accounts = accounts;
        this.passwords = passwords;
    }

    public String getId() {
        return this.id;
    }

    public List<String> getAccounts() {
        return this.accounts;
    }

    public List<String> getPasswords() {
        return this.passwords;
    }

    public String toString() {
        return "{ Id: " + this.id + ", Accounts: " + this.accounts + ", Passwords: " + this.passwords + " }";
    }
}
