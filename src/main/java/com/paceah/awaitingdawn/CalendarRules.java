package com.paceah.awaitingdawn;

/** Pure calendar arithmetic, independent of server/client lifecycle. */
public final class CalendarRules {
    public static long day(long ticks) { return Math.floorDiv(Math.max(0,ticks),24000L); }
    public static boolean immortal(long day,int interval) { return day>0 && day%interval==0; }
    public static long next(long day,int interval) { return (day/interval+1)*interval; }
    public static long latest(long day,int interval) { return day/interval*interval; }
}