package com.andrewlevada.carephone;

public class Config {
    public static final String baseNetworkUrl = "https://carephone-server.herokuapp.com/";
    // public static String baseNetworkUrl = "http://192.168.1.46:8080";
    public static final String appSharedPreferences = "carephone_sharedpreferences";
    public static final String smsSyncEmptyWhitelist = "01234567890empty_";

    public static final int statisticsSyncPeriodHours = 3;

    public static class Analytics {
        public static final String userPropertyWhitelistLength = "whitelist_length";
        public static final String userPropertyCaretakerListLength = "caretaker_list_length";
        public static final String userPropertyWhitelistState = "whitelist_state";
        public static final String userPropertyBlockerType = "blocker_type";
        public static final String userPropertyNotificationActions = "notification_actions";

        public static final String eventAddToWhitelist = "add_to_whitelist";
        public static final String eventRemoveFromWhitelist = "remove_from_whitelist";
        public static final String eventLinkUser = "link_user";
        public static final String eventUnlinkUser = "unlink_user";

        public static final String remoteConfigCallNotificationPackages = "call_notification_packages";
        public static final String remoteConfigCallNotificationDeclineActions = "call_notification_decline_actions";
        public static final String remoteConfigCallPeriodsLabelsRU = "periods_labels_ru";
        public static final String remoteConfigCallPeriodsLabelsEN = "periods_labels_en";
        public static final String remoteConfigDonateLink = "donate_link";
    }
}
