package com.ipos.pu.ui;

import com.ipos.pu.model.Member;

public class SessionManager {

    private static Member currentMember;

    public static void setCurrentMember(Member member) {
        currentMember = member;
    }

    public static Member getCurrentMember() {
        return currentMember;
    }

    public static void clearSession() {
        currentMember = null;
    }

    public static boolean isLoggedIn() {
        return currentMember != null;
    }
}
