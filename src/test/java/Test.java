


public class Test {

    public static void main(String[] args) {
        validateNumber("234903 210 9321");//234903 210 9321
    }

    private static void validateNumber(String phone) {

        if (phone.startsWith("0"))
            phone = "234" + phone.substring(1);
        else if (phone.startsWith("+"))
            phone = phone.substring(1);
        phone = phone.replaceAll("\\s", "");

        System.out.println(phone);
    }
}
