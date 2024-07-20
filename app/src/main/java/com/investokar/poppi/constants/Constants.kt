package com.investokar.poppi.constants

interface Constants {

    companion object {
        // for version 6.2
        const val WEB_SITE = "https://poppi.hindbyte.com/" //web site url address
        const val API_DOMAIN = "https://poppi.hindbyte.com/" // url address to which the application sends requests | with back slash "/" at the end | example: https://mysite.com/ | for emulator on localhost: http://10.0.2.2/
        const val API_FILE_EXTENSION = "" // Don`t change value for this constant!
        const val API_VERSION = "v2" // Don`t change value for this constant!
        const val METHOD_ACCOUNT_GOOGLE_AUTH = API_DOMAIN + "api/" + API_VERSION + "/method/account.google" + API_FILE_EXTENSION
        const val SIGNIN_EMAIL = 0
        const val SIGNIN_OTP = 1
        const val SIGNIN_GOOGLE = 3
        const val SIGNIN_APPLE = 4
        const val SIGNIN_TWITTER = 5
        const val OAUTH_TYPE_GOOGLE = 1

        //
        const val PA_BUY_LEVEL = 0

        const val PT_UNKNOWN = 0
        const val PT_CARD = 1
        const val PT_GOOGLE_PURCHASE = 2
        const val PT_APPLE_PURCHASE = 3
        const val PT_BONUS = 4

        // Attention! You can only change values of following constants:
        // EMOJI_KEYBOARD, WEB_SITE_AVAILABLE, GOOGLE_PAY_TEST_BUTTON, FACEBOOK_AUTHORIZATION,
        // APP_TEMP_FOLDER, API_DOMAIN, WEB_SITE,
        // VERIFIED_BADGE_COST, DISABLE_ADS_COST,
        // PRO_MODE_COST
        // It is forbidden to change value of constants, which are not indicated above !!!
        const val VOLLEY_REQUEST_SECONDS = 15 //SECONDS TO REQUEST
        const val GOOGLE_AUTHORIZATION = true // Allow login, signup with Google and "Services" section in Settings
        const val WEB_SITE_AVAILABLE = true // false = Do not show menu items (Open in browser, Copy profile link) in profile page | true = show menu items (Open in browser, Copy profile link) in profile page
        const val GOOGLE_PAY_TEST_BUTTON = true // false = Do not show google pay test button in section upgrades
        const val APP_TEMP_FOLDER = "chat" //directory for temporary storage of images
        const val METHOD_NOTIFICATIONS_CLEAR = API_DOMAIN + "api/" + API_VERSION + "/method/notifications.clear" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_GET_SETTINGS = API_DOMAIN + "api/" + API_VERSION + "/method/account.getSettings" + API_FILE_EXTENSION
        const val METHOD_DIALOGS_NEW_GET = API_DOMAIN + "api/" + API_VERSION + "/method/dialogs_new.get" + API_FILE_EXTENSION
        const val METHOD_CHAT_UPDATE = API_DOMAIN + "api/" + API_VERSION + "/method/chat.update" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_LOGIN = API_DOMAIN + "api/" + API_VERSION + "/method/account.signin" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_SIGNUP = API_DOMAIN + "api/" + API_VERSION + "/method/account.signup" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_AUTHORIZE = API_DOMAIN + "api/" + API_VERSION + "/method/account.authorize" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_SET_GCM_TOKEN = API_DOMAIN + "api/" + API_VERSION + "/method/account.setGcmToken" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_RECOVERY = API_DOMAIN + "api/" + API_VERSION + "/method/account.recovery" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_SET_PASSWORD = API_DOMAIN + "api/" + API_VERSION + "/method/account.setPassword" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_DEACTIVATE = API_DOMAIN + "api/" + API_VERSION + "/method/account.deactivate" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_SAVE_SETTINGS = API_DOMAIN + "api/" + API_VERSION + "/method/account.saveSettings" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_LOGOUT = API_DOMAIN + "api/" + API_VERSION + "/method/account.logout" + API_FILE_EXTENSION
        const val METHOD_ACCOUNT_SET_GEO_LOCATION = API_DOMAIN + "api/" + API_VERSION + "/method/account.setGeoLocation" + API_FILE_EXTENSION
        const val METHOD_SUPPORT_SEND_TICKET = API_DOMAIN + "api/" + API_VERSION + "/method/support.sendTicket" + API_FILE_EXTENSION
        const val METHOD_PROFILE_GET = API_DOMAIN + "api/" + API_VERSION + "/method/profile.get" + API_FILE_EXTENSION
        const val METHOD_PROFILE_REPORT = API_DOMAIN + "api/" + API_VERSION + "/method/profile.report" + API_FILE_EXTENSION
        const val METHOD_PROFILE_FANS_GET = API_DOMAIN + "api/" + API_VERSION + "/method/profile.getFans" + API_FILE_EXTENSION
        const val METHOD_PROFILE_ILIKED_GET = API_DOMAIN + "api/" + API_VERSION + "/method/profile.getILiked" + API_FILE_EXTENSION
        const val METHOD_PROFILE_LIKE = API_DOMAIN + "api/" + API_VERSION + "/method/profile.like" + API_FILE_EXTENSION
        const val METHOD_BLACKLIST_GET = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.get" + API_FILE_EXTENSION
        const val METHOD_BLACKLIST_ADD = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.add" + API_FILE_EXTENSION
        const val METHOD_BLACKLIST_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.remove" + API_FILE_EXTENSION
        const val METHOD_NOTIFICATIONS_GET = API_DOMAIN + "api/" + API_VERSION + "/method/notifications.get" + API_FILE_EXTENSION
        const val METHOD_APP_CHECK_USERNAME = API_DOMAIN + "api/" + API_VERSION + "/method/app.checkUsername" + API_FILE_EXTENSION
        const val METHOD_APP_TERMS = API_DOMAIN + "api/" + API_VERSION + "/method/app.privacy-policy" + API_FILE_EXTENSION
        const val METHOD_APP_THANKS = API_DOMAIN + "api/" + API_VERSION + "/method/app.thanks" + API_FILE_EXTENSION
        const val METHOD_CHAT_GET = API_DOMAIN + "api/" + API_VERSION + "/method/chat.get" + API_FILE_EXTENSION
        const val METHOD_CHAT_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/chat.remove" + API_FILE_EXTENSION
        const val METHOD_CHAT_GET_PREVIOUS = API_DOMAIN + "api/" + API_VERSION + "/method/chat.getPrevious" + API_FILE_EXTENSION
        const val METHOD_MSG_NEW = API_DOMAIN + "api/" + API_VERSION + "/method/msg.new" + API_FILE_EXTENSION
        const val METHOD_MSG_UPLOAD_IMG = API_DOMAIN + "api/" + API_VERSION + "/method/msg.uploadimg" + API_FILE_EXTENSION


        // added for version 3.4
        const val METHOD_CHAT_NOTIFY = API_DOMAIN + "api/" + API_VERSION + "/method/chat.notify" + API_FILE_EXTENSION

        // added for version 3.6
        const val METHOD_HOTGAME_GET = API_DOMAIN + "api/" + API_VERSION + "/method/hotgame.get" + API_FILE_EXTENSION

        // added for version 4.1
        const val TAG_UPDATE_BADGES = "update_badges"

        // added dor version 4.3
        const val METHOD_APP_CHECK_EMAIL = API_DOMAIN + "api/" + API_VERSION + "/method/app.checkEmail" + API_FILE_EXTENSION

        // added dor version 4.5
        const val METHOD_ACCOUNT_UPLOAD_IMAGE = API_DOMAIN + "api/" + API_VERSION + "/method/profile.uploadimg" + API_FILE_EXTENSION
        const val METHOD_PAYMENTS_NEW = API_DOMAIN + "api/" + API_VERSION + "/method/payments.new" + API_FILE_EXTENSION
        const val METHOD_PAYMENTS_GET = API_DOMAIN + "api/" + API_VERSION + "/method/payments.get" + API_FILE_EXTENSION

        //
        const val METHOD_GALLERY_NEW = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.new" + API_FILE_EXTENSION
        const val METHOD_GALLERY_GET = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.get" + API_FILE_EXTENSION
        const val METHOD_GALLERY_REPORT = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.report" + API_FILE_EXTENSION
        const val METHOD_GALLERY_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.remove" + API_FILE_EXTENSION
        const val METHOD_GALLERY_LIKE = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.like" + API_FILE_EXTENSION
        const val METHOD_GALLERY_LIKES = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.likes" + API_FILE_EXTENSION
        const val METHOD_GALLERY_GET_ITEM = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.getItem" + API_FILE_EXTENSION
        const val METHOD_GALLERY_UPLOAD_IMG = API_DOMAIN + "api/" + API_VERSION + "/method/gallery.uploadimg" + API_FILE_EXTENSION

        // for version 5.2
        const val METHOD_SEARCH_PEOPLE = API_DOMAIN + "api/" + API_VERSION + "/method/search.people" + API_FILE_EXTENSION
        
        const val APP_TYPE_ALL = -1
        const val APP_TYPE_MANAGER = 0
        const val APP_TYPE_WEB = 1
        const val APP_TYPE_ANDROID = 2
        const val APP_TYPE_IOS = 3
        const val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 3 //ACCESS_COARSE_LOCATION
        const val LIST_ITEMS = 20
        const val HOTGAME_LIST_ITEMS = 500
        const val ENABLED = 1
        const val DISABLED = 0
        const val GCM_ENABLED = 1
        const val GCM_DISABLED = 0
        const val MESSAGES_ENABLED = 1
        const val MESSAGES_DISABLED = 0
        const val ERROR_SUCCESS = 0
        const val GENDER_MALE = 0
        const val GENDER_FEMALE = 1
        const val NOTIFY_TYPE_LIKE = 0
        const val NOTIFY_TYPE_MESSAGE = 2
        const val NOTIFY_TYPE_IMAGE_LIKE = 9
        const val NOTIFY_TYPE_MEDIA_APPROVE = 10
        const val NOTIFY_TYPE_MEDIA_REJECT = 11
        const val NOTIFY_TYPE_PROFILE_PHOTO_APPROVE = 12
        const val NOTIFY_TYPE_PROFILE_PHOTO_REJECT = 13
        const val NOTIFY_TYPE_ACCOUNT_APPROVE = 14
        const val NOTIFY_TYPE_ACCOUNT_REJECT = 15
        const val GCM_NOTIFY_CONFIG = 0
        const val GCM_NOTIFY_SYSTEM = 1
        const val GCM_NOTIFY_CUSTOM = 2
        const val GCM_NOTIFY_LIKE = 3
        const val GCM_NOTIFY_ANSWER = 4
        const val GCM_NOTIFY_QUESTION = 5
        const val GCM_NOTIFY_PERSONAL = 8
        const val GCM_NOTIFY_MESSAGE = 9
        const val GCM_NOTIFY_SEEN = 15
        const val GCM_NOTIFY_TYPING = 16
        const val GCM_NOTIFY_URL = 17
        const val GCM_NOTIFY_TYPING_START = 27
        const val GCM_NOTIFY_TYPING_END = 28
        
        const val GCM_NOTIFY_CHANGE_ACCOUNT_SETTINGS = 30
        const val ERROR_LOGIN_TAKEN = 300
        const val ERROR_EMAIL_TAKEN = 301
        const val ERROR_MULTI_ACCOUNT = 500
        const val ERROR_OTP_VERIFICATION = 506
        const val ERROR_OTP_PHONE_NUMBER_TAKEN = 507
        const val ACCOUNT_STATE_ENABLED = 0
        const val ACCOUNT_STATE_DISABLED = 1
        const val ACCOUNT_STATE_BLOCKED = 2
        const val ACCOUNT_STATE_DEACTIVATED = 3
        const val GALLERY_ITEM_TYPE_IMAGE = 0
        const val ERROR_UNKNOWN = 100
        const val ERROR_ACCESS_TOKEN = 101
        const val ERROR_ACCOUNT_ID = 400
        const val UPLOAD_TYPE_PHOTO = 0
        const val ACTION_NEW = 1
        const val ACTION_EDIT = 2
        const val SELECT_POST_IMG = 3
        const val VIEW_CHAT = 4
        const val CREATE_POST_IMG = 5
        const val SELECT_CHAT_IMG = 6
        const val CREATE_CHAT_IMG = 7
        const val FEED_NEW_POST = 8
        const val FRIENDS_SEARCH = 9
        const val ITEM_EDIT = 10
        const val STREAM_NEW_POST = 11
        const val SELECT_PHOTO_IMG = 20
        const val CREATE_PHOTO_IMG = 21
        const val TAG = "TAG"
    }
}