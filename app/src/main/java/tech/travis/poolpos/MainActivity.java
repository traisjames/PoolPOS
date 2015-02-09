package tech.travis.poolpos;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createButtons();
    }

    public void createButtons()
    {
        LinearLayout layoutVertical = (LinearLayout) findViewById(R.id.ButtonLayout);
        LinearLayout rowLayout=null;
        int FoodSize = ItemsList.FOOD[0].length;
        int x = (int) ceil(sqrt(FoodSize));
        int y = (int) ceil(sqrt(FoodSize));
        Button[][] buttons = new Button[y][x];
        int count=x*y+1;
        ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT,1);

        //create buttons
        for (int i = 0; i<y; i++)
        {
            if(count%x==1)
            {
                rowLayout = new LinearLayout(this);
                rowLayout.setWeightSum(x);
                layoutVertical.addView(rowLayout,param);
                count=count-x;
            }
            for(int j=0;j<x;j++)
            {
                buttons[i][j]=new Button(this);
                //Set button ID
                buttons[i][j].setId(i*x+j);
                //set text in buttons
                if ((i*x+j) < FoodSize) {
                    buttons[i][j].setText(ItemsList.FOOD[0][i * x + j] + " " + buttons[i][j].getId());
                }
                else
                {
                    buttons[i][j].setText("NA");
                }
                buttons[i][j].setTextSize(8);
                buttons[i][j].setOnClickListener(new POSClickListener(i*x+j));
                rowLayout.addView(buttons[i][j], param);

            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Button onclick listener
    class POSClickListener implements View.OnClickListener {
        int buttonID;
        public POSClickListener(int id) {
            buttonID = id;
        }

        @Override
        public void onClick(View v) {

        }
    }
}


