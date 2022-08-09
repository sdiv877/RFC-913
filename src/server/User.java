package server;

import java.util.List;

public class User {
    private String id;
    private List<String> accounts;
    private String password;

    public User(String id, List<String> accounts, String password) {
        this.id = id;
        this.accounts = accounts;
        this.password = password;
    }

    public String getId() {
        return this.id;
    }

    public List<String> getAccounts() {
        return this.accounts;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean containsAccount(String accountName) {
        for (String account : accounts) {
            if (account.equals(accountName)) {
                return true;
            }
        }
        return false;
    }

    public boolean requiresAccount() {
        return accounts.size() > 0;
    }

    public boolean requiresPassword() {
        return password != null;
    }

    public String toString() {
        return "{ Id: " + this.id + ", Accounts: " + this.accounts + ", Password: " + this.password
                + ", RequiresAccount: " + requiresAccount() + ", RequiresPassword: " + requiresPassword() + " }";
    }
}
