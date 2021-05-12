package android.util;

public class Log {
    // Custom Log-class brukt KUN i forbindelse med enhetstester.
    // For å unngå feilmeldinger fra Default returverdier under enhetstesting,
    // ellers kreves mock-oppsett mm.
    // Se https://medium.com/@gal_41749/android-unitests-and-log-class-9546b6480006
    // og https://stackoverflow.com/questions/36787449/how-to-mock-method-e-in-log

    public static int v(String tag, String msg) {
        System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }
}
