package tech.travis.poolpos;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;


public class MainActivity extends Activity {

    MenuMaker orderlist[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createMenuButtons();
        //createOrderButtons(); //This was a test call.

    }

    public void createMenuButtons()
    {
        LinearLayout layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayout);
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
                    buttons[i][j].setEnabled(true);
                }
                else
                {
                    buttons[i][j].setText("NA");
                    buttons[i][j].setEnabled(false);
                }
                buttons[i][j].setTextSize(12);
                buttons[i][j].setOnClickListener(new POSListListener(i * x + j));
                rowLayout.addView(buttons[i][j], param);

            }
        }

    }

    //This method is for testing.  Will be removed for final.
    public void createOrderButtons() {

        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        int ListSize = ItemsList.ENTRY[0].length;
        int x = ListSize;
        Button[] buttons = new Button[x];

        ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1);

        //Create Buttons test

        for (int i = 0; i < x; i++) {

            buttons[i] = new Button(this);
            //Set button ID
            buttons[i].setId(i);
            if (i % 2 == 0) {
                buttons[i].setBackgroundColor(0xff00ff00);
            } else {
                buttons[i].setBackgroundColor(0xffff0000);
            }
            //set text in buttons
            if ((i) < ListSize) {
                buttons[i].setText(ItemsList.ENTRY[0][i] + " " + buttons[i].getId());
                buttons[i].setEnabled(true);
            } else {
                buttons[i].setText("NA");
                buttons[i].setEnabled(false);
            }
            buttons[i].setTop(5);
            buttons[i].setHeight(20);
            buttons[i].setTextSize(12);
            buttons[i].setOnClickListener(new POSOrderListener(i));

            linearLayout.addView(buttons[i], param);
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

    //Todo
    void addtomenu(byte mode, int buttonID) {
        int itemcount = 0;
// Find the ScrollView

        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1);

        //Create Buttons test

        Button buttons = new Button(this);
        //Set button ID
        //todo
        buttons.setId(buttonID);

        //set text in buttons
        buttons.setText(itemcount + " " + ItemsList.FOOD[0][buttonID] + "!" + ItemsList.FOOD[1][buttonID]);

        buttons.setTop(5);
        buttons.setHeight(20);
        buttons.setTextSize(12);
        buttons.setOnClickListener(new POSOrderListener(buttonID));


        linearLayout.addView(buttons, param);


    }

    //Todo
    public void updatetotal() {
    }

    //Button onclick listener
    class POSListListener implements View.OnClickListener {
        int buttonID;

        public POSListListener(int id) {
            buttonID = id;
        }

        public int getButtonID() {
            return buttonID;
        }


        @Override
        public void onClick(View v) {
            byte mode = 1;
            Log.i("Clicked", "Button: " + buttonID);
            Log.i("Price", "Price: " + ItemsList.FOOD[mode][buttonID]);
            addtomenu(mode, buttonID);
            updatetotal();
        }
    }


    //Remove Order items
    class POSOrderListener implements View.OnClickListener {

        int buttonID;

        public POSOrderListener(int id) {
            buttonID = id;
        }

        public int getButtonID() {
            return buttonID;
        }

        @Override
        public void onClick(View view) {
            Log.i("Removing", "Button: " + buttonID);
            ViewGroup parentView = (ViewGroup) view.getParent();
            parentView.removeView(view);
            updatetotal();
        }

    }

    //add new item method
    class ButtonAddNewItem implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = orderlist.length;
            orderlist[orderlist.length] = new MenuMaker();
            String itemName = "";
            int itemPrice = 0;
            String itemFlavors = "";
            orderlist[0].additem(itemName, itemPrice, itemFlavors);
        }

    }


    //remove item method
    //find all based on price
    //add item from menu to order.

}


