import Company.TranportCompany;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        TranportCompany tranportCompany = new TranportCompany();
        tranportCompany.startWork();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tranportCompany.stopWork();
        }
    }
}
