package com.paceah.awaitingdawn;

/** Deterministic persistence transitions, separated from announcements and online entities. */
public final class CycleStateTransitions {
    public static void advance(PlayerDeathStateStore store,long day,int interval,boolean lives,int max) {
        long previous=store.observedDay;
        long milestone=CalendarRules.latest(day,interval);
        boolean changed=store.observedInterval!=interval;
        if(changed) {
            for(var r:store.players.values()) {
                if(!r.pending || r.eligible) continue;
                // Old saves have no death date: use the start of their original waiting cycle.
                // Eligibility is never revoked, including after a clock rewind or another reload.
                if(r.deathDay<0) r.deathDay=Math.max(0,r.nextDay-store.observedInterval);
                r.nextDay=CalendarRules.next(r.deathDay,interval);
                if(r.nextDay<=milestone) r.eligible=true;
            }
        }
        // Re-crossing an old date can release new deaths, but cannot refill living records twice.
        if(milestone>0 && (changed || day>previous && (previous<0 || milestone>previous))) {
            store.latestMilestone=Math.max(store.latestMilestone,milestone);
            for(var r:store.players.values()) {
                if(r.pending && r.nextDay<=milestone) r.eligible=true;
                refresh(r,milestone,lives,max);
            }
        }
        store.observedDay=day; store.observedInterval=interval; store.setDirty();
    }
    public static void refresh(PlayerDeathStateStore.Record r,long milestone,boolean enabled,int max) {
        if(milestone>r.refresh && enabled) { r.lives=max; r.refresh=milestone; }
        r.lives=Math.min(r.lives,max);
    }
}
