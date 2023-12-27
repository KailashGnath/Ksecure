package com.github.anevero.sms_my_gps.data;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public final class ListItem {
  private String senderName;
  private final String sender;
  private String messagePrefix;
  private boolean ignoreRequests;

  public ListItem(String senderName, String senderNum, String messagePrefix, boolean ignoreRequests) {
    this.senderName = senderName;
    this.sender = senderNum;
    this.messagePrefix = messagePrefix;
    this.ignoreRequests = ignoreRequests;
  }

  @NonNull
  @Override
  public String toString() {
    if (getSenderName() != null) {
      return getSenderName() + " (" + getSenderNum() + ")";
    } else {
      return getSenderNum();
    }
  }

  public String getSenderName() {
    return senderName;
  }

  public String getSenderNum() {
    return sender;
  }

  public String getMessagePrefix() {
    return messagePrefix;
  }

  public boolean getIgnoreRequests() {
    return ignoreRequests;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public void setMessagePrefix(String messagePrefix) {
    this.messagePrefix = messagePrefix;
  }

  public void setIgnoreRequests(boolean ignoreRequests) {
    this.ignoreRequests = ignoreRequests;
  }

  public static ArrayList<ListItem> fromJson(String json) {
    ArrayList<ListItem> result;
    Gson gson = new Gson();
    result = gson.fromJson(json, new TypeToken<ArrayList<ListItem>>() {
    }.getType());
    return result;
  }

  public static String toJson(ArrayList<ListItem> arrayList) {
    return (new Gson()).toJson(arrayList);
  }

  private static boolean senderMatches(ListItem item, String sender) {
    if (item.sender.charAt(0) == '0') {
      // Ignore the leading '0' to allow matching "sender", which is
      // supplied with the appropriate country code.
      return sender.endsWith(item.sender.substring(1));
    } else {
      return sender.endsWith(item.sender);
    }
  }

  public static ListItem getMatch(ArrayList<ListItem> listItems,
                                  String sender) {
    for (ListItem item : listItems) {
      if (senderMatches(item, sender)) {
        return item;
      }
    }

    return null;
  }

  public static ListItem getMatch(ArrayList<ListItem> listItems,
                                  String sender, String message) {
    for (ListItem item : listItems) {
      if (!item.getIgnoreRequests() &&
          senderMatches(item, sender) &&
          message.startsWith(item.messagePrefix)) {
        return item;
      }
    }

    return null;
  }
}
