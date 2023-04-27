package org.example.util.wrappers;

import java.util.StringJoiner;

public class ToStringWrapper {
    protected StringJoiner sj = new StringJoiner(", ");

    protected void addNotNull(String fieldName, Object o) {
        if (o == null) return;
        sj.add(fieldName + "=" + o);
    }

    protected void addTrue(String fieldName, boolean bool) {
        if (!bool) return;
        sj.add(fieldName + "=true");
    }
}
