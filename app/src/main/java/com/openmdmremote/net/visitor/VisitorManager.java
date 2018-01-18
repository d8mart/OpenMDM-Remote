package com.openmdmremote.net.visitor;

import android.content.Context;

import com.openmdmremote.nativ.WIPC;

import java.util.HashSet;
import java.util.Set;

public class VisitorManager {
    private Set<WebkeyVisitor> visitors = new HashSet<>();
    private VisitorNotifier visitorNotifier;
    Context context;

    private final Object lock = new Object();

    public VisitorManager(Context context) {
        this.context = context;
        visitorNotifier = new VisitorNotifier(context);
    }

    public void visitorLeggedIn(WebkeyVisitor visitor) {
        visitorNotifier.showNotification(visitor.getUsername());
        visitorNotifier.newVisitor(visitor);
    }

    public boolean hasSomeoneLoggedInAndKick(WebkeyVisitor newVisitor) {
        for (WebkeyVisitor v : visitors) {
            if (!v.isLoggedIn()) {
                continue;
            }

            if (v.getUsername().equals(newVisitor.getUsername())) {
                v.requestRestart();
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    // It called when somebody load the login site.
    public WebkeyVisitor addNewVisitor(WebkeyVisitor webkeyVisitor) {
        synchronized (lock) {
            if(visitors.size() == 0) {
                visitorNotifier.showAnonymousNotification();
            }
            visitors.add(webkeyVisitor);
        }
        return webkeyVisitor;
    }

    public void leftVisitor(WebkeyVisitor webkeyVisitor) {
        synchronized (lock) {
            visitors.remove(webkeyVisitor);
            if(visitors.size() == 0) {
                webkeyVisitor.cleanHandlers();
                WIPC.getInstance(context).disconnect();
                visitorNotifier.hideForeground();
            }
        }

        if (visitors.size() == 0) {
            visitorNotifier.leftLastVisitor();
        }
    }

    public void addVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        visitorNotifier.addVisitorChangesListener(visitorChangesListener);

    }

    public void removeVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        visitorNotifier.removeVisitorChangesListener(visitorChangesListener);
    }
}
