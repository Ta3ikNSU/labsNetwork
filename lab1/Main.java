public class Main {
    public static void main(String[] args) {
//        if(args.length < 2) throw new IllegalArgumentException("Enter group ip");
        try {
            new FinderOfCopy("230.0.0.0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
