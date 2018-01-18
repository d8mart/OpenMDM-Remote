package com.openmdmremote.service.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.openmdmremote.service.dto.User;

import java.util.ArrayList;
import java.util.List;

public class UsersDataSource {

    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = {
            SQLiteHelper.COLUMN_ID,
            SQLiteHelper.COLUMN_USERNAME};

    public UsersDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public User createUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_USERNAME, username);
        values.put(SQLiteHelper.COLUMN_PASSWORD, password);
        long insertId = database.insert(SQLiteHelper.TABLE_USERS, null, values);
        Cursor cursor = database.query(SQLiteHelper.TABLE_USERS,
                allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        User newUser = cursorToUser(cursor);
        cursor.close();
        return newUser;
    }

    public void deleteUser(User u) {
        long id = u.getId();
        System.out.println("User deleted with id: " + id);
        database.delete(SQLiteHelper.TABLE_USERS, SQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void cleanUsers() {
        database.delete(SQLiteHelper.TABLE_USERS, "1 = 1", null);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Cursor cursor = database.query(SQLiteHelper.TABLE_USERS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User u = cursorToUser(cursor);
            users.add(u);
            cursor.moveToNext();
        }
        cursor.close();
        return users;
    }

    public User getUserByUsernameAndPassword(String username, String password){
        String whereClause = SQLiteHelper.COLUMN_USERNAME + " = ? AND " + SQLiteHelper.COLUMN_PASSWORD + " = ?";
        String[] whereArgs = new String[] {username, password };
        Cursor c = database.query(SQLiteHelper.TABLE_USERS, allColumns, whereClause, whereArgs,
                null, null, null);
        if(c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        return cursorToUser(c);
    }

    public User getUserByUsername(String username){
        String whereClause = SQLiteHelper.COLUMN_USERNAME + " = ?";
        String[] whereArgs = new String[] {username};
        Cursor c = database.query(SQLiteHelper.TABLE_USERS, allColumns, whereClause, whereArgs,
                null, null, null);
        if(c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        return cursorToUser(c);
    }

    private User cursorToUser(Cursor cursor) {
        User u = new User();
        u.setId(cursor.getLong(0));
        u.setUsername(cursor.getString(1));
        return u;
    }
}
