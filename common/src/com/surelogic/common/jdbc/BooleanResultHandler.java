package com.surelogic.common.jdbc;

public class BooleanResultHandler implements ResultHandler<Boolean> {

    @Override
    public Boolean handle(Result result) {
        for (Row r : result) {
            return r.nullableBoolean();
        }
        return null;
    }

}
