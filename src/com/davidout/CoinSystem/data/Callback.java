package com.davidout.CoinSystem.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Callback<T> {

    void onSuccess(ResultSet result) throws SQLException;
    void onException(Throwable cause);
    void onDataNotFound();
}
