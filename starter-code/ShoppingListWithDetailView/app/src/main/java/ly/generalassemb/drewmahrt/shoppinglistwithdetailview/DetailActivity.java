package ly.generalassemb.drewmahrt.shoppinglistwithdetailview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detail");

        TextView nameView = (TextView) findViewById(R.id.detail_name);
        TextView descriptionView = (TextView) findViewById(R.id.detail_description);
        TextView priceView = (TextView) findViewById(R.id.detail_price);
        TextView typeView = (TextView) findViewById(R.id.detail_type);
        TextView isOnSaleView = (TextView) findViewById(R.id.detail_is_on_sale);
        TextView salePriceView = (TextView) findViewById(R.id.detail_sale_price);

        int id = getIntent().getIntExtra("ID", -1);
        if (id >= 0) {
            ShoppingSQLiteOpenHelper helper = ShoppingSQLiteOpenHelper.getInstance(DetailActivity.this);

            nameView.setText("Name: " + helper.getNameById(id));
            descriptionView.setText("Description: " + helper.getDescriptionById(id));
            priceView.setText("Price: " + helper.getPriceById(id));
            typeView.setText("Type: " + helper.getTypeById(id));
            isOnSaleView.setText("On sale? " + Boolean.toString(helper.isOnSaleById(id)));
            salePriceView.setText("Sale price: " + helper.getSalePriceById(id));
        } else {
            nameView.setText("Error: no item ID");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
