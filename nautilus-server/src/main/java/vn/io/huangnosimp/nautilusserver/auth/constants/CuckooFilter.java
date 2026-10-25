package vn.io.huangnosimp.nautilusserver.auth.constants;

public class CuckooFilter {
    public static class username {
        public static final String BUCKET = "cf:newUsername:ready";
        public static final String LOCK = "lock:cf:newUsername:load";
        public static final String NAME = "cf:newUsername";
    }

    public static class email {
        public static final String BUCKET = "cf:email:ready";
        public static final String LOCK = "lock:cf:email:load";
        public static final String NAME = "cf:email";
    }
}
