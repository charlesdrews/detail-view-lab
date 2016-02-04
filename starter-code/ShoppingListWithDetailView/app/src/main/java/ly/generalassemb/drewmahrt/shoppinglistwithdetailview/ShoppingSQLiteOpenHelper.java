package ly.generalassemb.drewmahrt.shoppinglistwithdetailview;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by drewmahrt on 12/28/15.
 */
public class ShoppingSQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String TAG = ShoppingSQLiteOpenHelper.class.getCanonicalName();

    private static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "SHOPPING_DB";
    public static final String SHOPPING_LIST_TABLE_NAME = "SHOPPING_LIST";

    public static final String COL_ID = "_id";
    public static final String COL_ITEM_NAME = "ITEM_NAME";
    public static final String COL_ITEM_PRICE = "PRICE";
    public static final String COL_ITEM_DESCRIPTION = "DESCRIPTION";
    public static final String COL_ITEM_TYPE = "TYPE";
    public static final String COL_IS_ON_SALE = "IS_ON_SALE";
    public static final String COL_SALE_PRICE = "SALE_PRICE";

    public static final String[] SHOPPING_COLUMNS = {COL_ID,COL_ITEM_NAME,COL_ITEM_DESCRIPTION,COL_ITEM_PRICE,COL_ITEM_TYPE};

    private static final String CREATE_SHOPPING_LIST_TABLE =
            "CREATE TABLE " + SHOPPING_LIST_TABLE_NAME +
                    "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ITEM_NAME + " TEXT, " +
                    COL_ITEM_DESCRIPTION + " TEXT, " +
                    COL_ITEM_PRICE + " TEXT, " +
                    COL_ITEM_TYPE + " TEXT )";

    private static ShoppingSQLiteOpenHelper mInstance;

    public static ShoppingSQLiteOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ShoppingSQLiteOpenHelper(context.getApplicationContext());

            // this deletes the table and recreates it with two extra columns
            // clumsy, I know, but I don't know where the code is that initializes
            // the db with it's original columns, so I can't modify that.
            mInstance.initializeDataForTesting();
        }
        return mInstance;
    }

    private ShoppingSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHOPPING_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST_TABLE_NAME);
        this.onCreate(db);
        /*
        db.execSQL("ALTER TABLE " + SHOPPING_LIST_TABLE_NAME + " ADD COLUMN " +
                COL_IS_ON_SALE + " INT");
        db.execSQL("ALTER TABLE " + SHOPPING_LIST_TABLE_NAME + " ADD COLUMN " +
                COL_SALE_PRICE + " TEXT");
        db.execSQL("UPDATE " + SHOPPING_LIST_TABLE_NAME + " SET " +
                COL_IS_ON_SALE + " = 0");
        db.execSQL("UPDATE " + SHOPPING_LIST_TABLE_NAME + " SET " +
                COL_IS_ON_SALE + " = 1 WHERE " + COL_ID + " % 2 = 0");
        db.execSQL("UPDATE " + SHOPPING_LIST_TABLE_NAME + " SET " +
                COL_SALE_PRICE + " = "  + COL_ITEM_PRICE);
        db.execSQL("UPDATE " + SHOPPING_LIST_TABLE_NAME + " SET " +
                COL_SALE_PRICE + " = cast((cast(" + COL_ITEM_PRICE + " AS REAL) * 0.8) AS TEXT)" +
                " WHERE " + COL_IS_ON_SALE + " = 1");
        */
    }

    //Add new itinerary list
    public long addItem(String name, String description, String price, String type,
                        boolean isOnSale, String salePrice)
    {
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, name);
        values.put(COL_ITEM_DESCRIPTION, description);
        values.put(COL_ITEM_PRICE, price);
        values.put(COL_ITEM_TYPE, type);
        values.put(COL_IS_ON_SALE, (isOnSale ? 1 : 0));
        values.put(COL_SALE_PRICE, salePrice);

        SQLiteDatabase db = this.getWritableDatabase();
        long returnId = db.insert(SHOPPING_LIST_TABLE_NAME, null, values);
        db.close();
        return returnId;
    }

    public Cursor getShoppingList(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME, // a. table
                SHOPPING_COLUMNS, // b. column names
                null, // c. selections
                null, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        return cursor;
    }

    public int deleteItem(int id){
        SQLiteDatabase db = getWritableDatabase();
        int deleteNum = db.delete(SHOPPING_LIST_TABLE_NAME,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return deleteNum;
    }

    public Cursor searchShoppingList(String query){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME, // a. table
                SHOPPING_COLUMNS, // b. column names
                COL_ITEM_NAME + " LIKE ?", // c. selections
                new String[]{"%" + query + "%"}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        return cursor;
    }

    public String getNameById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_ITEM_NAME},
                COL_ID+"=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
        } else {
            return "Error: item not found";
        }
    }

    public String getDescriptionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_ITEM_DESCRIPTION},
                COL_ID+"=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_ITEM_DESCRIPTION));
        } else {
            return "Error: item not found";
        }
    }

    public String getPriceById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_ITEM_PRICE},
                COL_ID+"=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_ITEM_PRICE));
        } else {
            return "Error: item not found";
        }
    }

    public String getTypeById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_ITEM_TYPE},
                COL_ID+"=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_ITEM_TYPE));
        } else {
            return "Error: item not found";
        }
    }

    public boolean isOnSaleById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_IS_ON_SALE},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return (cursor.getInt(cursor.getColumnIndex(COL_IS_ON_SALE)) == 1);
        } else {
            return false;
        }
    }

    public String getSalePriceById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SHOPPING_LIST_TABLE_NAME,
                new String[]{COL_SALE_PRICE},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_SALE_PRICE));
        } else {
            return "Error: item not found";
        }
    }

    public void initializeDataForTesting() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SHOPPING_LIST_TABLE_NAME);
        db.execSQL("CREATE TABLE " + SHOPPING_LIST_TABLE_NAME + "(" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ITEM_NAME + " TEXT, " +
                        COL_ITEM_DESCRIPTION + " TEXT, " +
                        COL_ITEM_PRICE + " TEXT, " +
                        COL_ITEM_TYPE + " TEXT, " +
                        COL_IS_ON_SALE + " INTEGER, " +
                        COL_SALE_PRICE + " TEXT)"
        );
        addItem("Bread", "Whole Wheat Bread", "2.35", "Food", true, "2.30");
        addItem("Milk", "1 Gallon Skim Milk", "3.50", "Dairy", false, "3.50");
        addItem("Ice Cream", "Mint Chocolate Chip", "2.20", "Dairy", true, "1.95");
        addItem("Paper Plates", "White Paper Plates", "7.50", "Dishes", false, "7.50");
        addItem("Chicken Breasts", "Boneless Skinless", "2.30", "Poultry", true, "2.00");
        addItem("Carrots", "Baby Carrots", "4.00", "Produce", false, "4.00");
        addItem("Lettuce", "Iceberg", "3.14", "Produce", true, "2.85");
    }
}
