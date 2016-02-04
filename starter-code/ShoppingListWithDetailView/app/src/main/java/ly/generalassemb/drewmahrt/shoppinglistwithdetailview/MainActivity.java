package ly.generalassemb.drewmahrt.shoppinglistwithdetailview;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import ly.generalassemb.drewmahrt.shoppinglistwithdetailview.setup.DBAssetHelper;

public class MainActivity extends AppCompatActivity {
    private ListView mShoppingListView;
    private CursorAdapter mCursorAdapter;
    private ShoppingSQLiteOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ignore the two lines below, they are for setup
        DBAssetHelper dbSetup = new DBAssetHelper(MainActivity.this);
        dbSetup.getReadableDatabase();

        mShoppingListView = (ListView)findViewById(R.id.shopping_list_view);
        mHelper = ShoppingSQLiteOpenHelper.getInstance(MainActivity.this);

        Cursor cursor = mHelper.getShoppingList();

        mCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,new String[]{ShoppingSQLiteOpenHelper.COL_ITEM_NAME},new int[]{android.R.id.text1},0);
        mShoppingListView.setAdapter(mCursorAdapter);

        mShoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Cursor currentCursor = mCursorAdapter.getCursor();
                currentCursor.moveToPosition(position);
                intent.putExtra("ID", currentCursor.getInt(currentCursor.getColumnIndex(ShoppingSQLiteOpenHelper.COL_ID)));
                startActivity(intent);
            }
        });

        Button addItemBtn = (Button) findViewById(R.id.add_item_btn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddItemDialog();
            }
        });

        handleIntent(getIntent());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCursorAdapter.changeCursor(mHelper.searchShoppingList(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCursorAdapter.changeCursor(mHelper.searchShoppingList(newText));
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor cursor = mHelper.searchShoppingList(query);
            mCursorAdapter.changeCursor(cursor);
            mCursorAdapter.notifyDataSetChanged();
        }
    }

    private void launchAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add an item");

        final EditText nameEdit = new EditText(MainActivity.this);
        final EditText descEdit = new EditText(MainActivity.this);
        final EditText priceEdit = new EditText(MainActivity.this);
        final EditText typeEdit = new EditText(MainActivity.this);
        final CheckBox isOnSaleCkBox = new CheckBox(MainActivity.this);
        final EditText salePriceEdit = new EditText(MainActivity.this);

        nameEdit.setHint("Enter name (required)");
        descEdit.setHint("Enter description (optional)");
        priceEdit.setHint("Enter price as #.## (required)");
        typeEdit.setHint("Enter type (required)");
        isOnSaleCkBox.setText("Item on sale?");
        salePriceEdit.setHint("Enter sale price as #.##");

        isOnSaleCkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    salePriceEdit.setHint("Enter sale price as #.## (required)");
                } else {
                    salePriceEdit.setHint("Sale price - N/A");
                }
            }
        });

        LinearLayout ll = new LinearLayout(MainActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(nameEdit);
        ll.addView(descEdit);
        ll.addView(priceEdit);
        ll.addView(typeEdit);
        ll.addView(isOnSaleCkBox);
        ll.addView(salePriceEdit);
        builder.setView(ll);

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button okBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEdit.getText().toString().isEmpty()) {
                    nameEdit.setError("Name cannot be blank");
                    nameEdit.requestFocus();
                } else if (!priceEdit.getText().toString().matches("\\d\\.\\d\\d")) {
                    priceEdit.setError("Must be formatted #.##");
                    priceEdit.requestFocus();
                } else if (typeEdit.getText().toString().isEmpty()) {
                    typeEdit.setError("Type cannot be blank");
                    typeEdit.requestFocus();
                } else if (isOnSaleCkBox.isChecked() && !salePriceEdit.getText().toString().matches("\\d\\.\\d\\d")) {
                    salePriceEdit.setError("Must be formatted #.##");
                    salePriceEdit.requestFocus();
                } else {
                    mHelper.addItem(nameEdit.getText().toString(),
                            descEdit.getText().toString(),
                            priceEdit.getText().toString(),
                            typeEdit.getText().toString(),
                            isOnSaleCkBox.isChecked(),
                            (isOnSaleCkBox.isChecked()) ? salePriceEdit.getText().toString() : priceEdit.getText().toString()
                    );
                    mCursorAdapter.changeCursor(mHelper.getShoppingList());
                    alertDialog.dismiss();
                }
            }
        });
    }
}
