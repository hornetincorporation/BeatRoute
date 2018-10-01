package com.hornetincorporation.beatroute;

public class Constants {
    public interface ALARM {
        public static final int START_SERVICE_REQUEST_CODE = 101;
        public static final int STOP_SERVICE_REQUEST_CODE = 102;
        public static final int START_SERVICE_HOUR = 22;
        public static final int START_SERVICE_MIN = 0;
        public static final int START_SERVICE_SEC = 0;
        public static final int STOP_SERVICE_HOUR = 8;
        public static final int STOP_SERVICE_MIN = 0;
        public static final int STOP_SERVICE_SEC = 0;
    }

    public interface SIGN_IN {
        public static final int SIGN_IN_REQUEST_CODE = 301;
    }

    public interface SIGN_UP {
        public static final String PREF_FILE ="com.hornetincorporation.beatroute.UserDetails";
        public static final String USER_ID = "UserID";
        public static final String USER_NAME = "UserName";
        public static final String EMAIL_ID = "EmailID";
        public static final String PHONE_NUMBER = "PhoneNumber";
        public static final String OFFICIAL_ID = "OfficialID";
        public static final String OFFICER = "Officer";
    }

    public interface LOCATION {
        public static final int UPDATE_INTERVAL = 1000 * 60 * 3;
        public static final int FASTEST_INTERVAL = 1000 * 60 * 1;
        public static final float SMALLEST_DISPLACEMENT = 10f;
        public static final int LOCATION_REQUEST_CODE = 401;
    }

    public interface GEOFENCE {
        public static final int GEOFENCE_DURATION = 10 * 60 * 60 * 1000;
        public static final int GEOFENCE_LOITERING_DELAY = 10 * 1000;
        public static final float GEOFENCE_RADIUS = 50.0f;
        public static final int GEOFENCE_REQUEST_CODE = 501;
        public static final int GEOFENCE_ENABLE_CODE = 502;
        public static final int GEOFENCE_STSQUO_CODE = 503;
        public static final int GEOFENCE_NOTIFICATION_ID = 0;
        public static final String GEOFENCE_NOTIFICATION_CHANNEL_ID = "Beeter_Stay_Notification";
        public static final String GEOFENCE_NOTIFICATION_CHANNEL_NAME = "Beeter Stay Notification";
        public static final String GEOFENCE_NOTIFICATION_CHANNEL_DESC = "Beeter Stay Notification";
    }

    public interface GEOFENCE_LOCAL_DB {
        public static final String DATABASE_NAME = "BEATROUTE";
        public static final String TABLE_NAME = "BEATPOINTS";
        public static final String COL_1 = "ID";
        public static final String COL_2 = "GEOREQID";
        public static final String COL_3 = "LATITUDE";
        public static final String COL_4 = "LONGITUDE";
        public static final String COL_5 = "BEATROUTE";
        public static final String COL_6 = "BEATPOINT";
    }

    public interface MARKER {
        public static final int BEETER_LOCATION_MARKER = 601;
        public static final int GEOFENCE_LOCATION_MARKER = 602;
        public static final int CURRENT_LOCATION_MARKER = 603;
        public static final int VISITED_LOCATION_MARKER = 604;
    }

    public interface BEETROOT_LOCAL_DB {
        public static final String DATABASE_NAME = "BEATROUTE";
        public static final String TABLE_NAME = "BEETROOT";
        public static final String COL_1 = "ID";
        public static final String COL_2 = "USERID";
        public static final String COL_3 = "USERNAME";
        public static final String COL_4 = "LOCATION";
        public static final String COL_5 = "DATETIME";
        public static final String COL_6 = "PHOTOURL";
    }

    public interface BEETPOINTVISIT_LOCAL_DB {
        public static final String DATABASE_NAME = "BEATROUTE";
        public static final String TABLE_NAME = "BEETPOINTVISIT";
        public static final String COL_1 = "ID";
        public static final String COL_2 = "USERID";
        public static final String COL_3 = "BPVTRANSITION";
        public static final String COL_4 = "BPVBEETPOINTID";
        public static final String COL_5 = "BPVBEETROUTENPOINT";
        public static final String COL_6 = "BPVLOCATION";
        public static final String COL_7 = "BPVPOINT";
        public static final String COL_8 = "BPVROUTE";
        public static final String COL_9 = "BPVDTIME";
    }

    public interface MAINACTIVITY {
        public static final int MAINACTIVITY_REQUEST_CODE = 0;
    }

    public interface BEETER_TRACKING_SERVICE {
        public static final String PREF_FILE = "com.hornetincorporation.beatroute.SERVICE_RUNNING";
        public static final String IS_SERVICE_RUNNING = "IsServiceRunning";
    }

    public static final int SECS_IN_DAY = 24 * 60 * 60 * 1000;
}