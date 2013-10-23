package no.steria.tad;
public class RandomPasswordGeneratorTest {
	 
    public static void main(String[] args) {
        int noOfCAPSAlpha = 1;
        int noOfDigits = 1;
        int noOfSplChars = 0;
        int minLen = 8;
        int maxLen = 8;
 
        for (int i = 0; i < 10; i++) {
            char[] pswd = RandomPasswordGenerator.generatePswd(minLen, maxLen,
                    noOfCAPSAlpha, noOfDigits, noOfSplChars);
            System.out.println("Len = " + pswd.length + ", " + new String(pswd));
        }
    }
}