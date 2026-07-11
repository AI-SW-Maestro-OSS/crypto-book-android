# Proto DataStore generated messages (UserPreferences and friends) are populated and compared
# reflectively by the protobuf-lite runtime: MessageSchema looks up the generated backing fields
# (e.g. `language_`) by name. R8 must therefore not rename or remove those classes/fields, or the
# app crashes at startup with "Field <name>_ for <message> not found" / NoSuchFieldException.
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
