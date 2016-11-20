package nl.bakkerv.p1.parser;

public class DatagramCleaner {

    public static String[] asArray(String source) {

        // Remove windowsy line ends
        source = source.replaceAll("\\r", "");

        // Put gas measurements on one line
        source = source.replaceAll("\\(m3\\)\\n", "(m3)");

        return source.split("\\n");
    }
}
