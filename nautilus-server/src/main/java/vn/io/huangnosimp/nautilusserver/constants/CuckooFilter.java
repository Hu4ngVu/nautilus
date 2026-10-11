package vn.io.huangnosimp.nautilusserver.constants;

public class CuckooFilter {
    public static class username {
        public static final String BUCKET = "cf:username:ready";
        public static final String LOCK = "lock:cf:username:load";
        public static final String NAME = "cf:username";
    }
    public static class email {
        public static final String BUCKET = "cf:email:ready";
        public static final String LOCK = "lock:cf:email:load";
        public static final String NAME = "cf:email";
    }
}
