package tech.travis.poolpos;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/*todo
Flavors are being ignored for now.
 */

public class MainActivity extends Activity {

    ArrayList<MenuMaker> orderlist = new ArrayList<MenuMaker>();
    ArrayList<MenuMaker> consessionlist = new ArrayList<MenuMaker>();
    ArrayList<MenuMaker> entrylist = new ArrayList<MenuMaker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[][] strArray = ItemsList.FOOD;
        Log.v("Checkmark", "Main 1");
        fillMenu();
        Log.v("Checkmark", "Main 2");
        createMenuButtons();
        Log.v("Checkmark", "Main 3");
    }

    /* todo?
    * Pass by reference to fill given menu from 2d string array*/
    private void fillMenu() {
        MenuMaker mm;
        Log.v("Checkmark", "fillMenu 1");
        for (int i = 0; i < ItemsList.FOOD[0].length; i++) {

            mm = new MenuMaker(ItemsList.FOOD[0][i], Integer.parseInt(ItemsList.FOOD[1][i]), "");
            consessionlist.add(i, mm);
        }
        Log.v("Checkmark", "fillMenu 2");
        for (int i = 0; i < ItemsList.ENTRY[0].length; i++) {
            entrylist.add(i, new MenuMaker(ItemsList.ENTRY[0][i], Integer.parseInt(ItemsList.ENTRY[1][i]), ""));
        }
        Log.v("Checkmark", "fillMenu 3");
    }

    private void createMenuButtons()
    {
        LinearLayout layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayout);
        LinearLayout rowLayout=null;
        int FoodSize = consessionlist.size();
        int x = (int) ceil(sqrt(FoodSize));
        int y = (int) ceil(sqrt(FoodSize));
        Button[][] buttons = new Button[y][x];
        int count=x*y+1;
        //ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT,1);

        //create buttons
        for (int i = 0; i<y; i++)
        {
            if(count%x==1)
            {
                rowLayout = new LinearLayout(this);
                rowLayout.setWeightSum(x);
                layoutVertical.addView(rowLayout);
                count=count-x;
            }
            for(int j=0;j<x;j++)
            {
                buttons[i][j]=new Button(this);
                //Set button ID
                buttons[i][j].setId(i*x+j);
                //set text in buttons
                if ((i*x+j) < FoodSize) {
                    buttons[i][j].setText(consessionlist.get(i).getName() + ": " + consessionlist.get(i).getPrice());
                    buttons[i][j].setEnabled(true);
                }
                else
                {
                    buttons[i][j].setText("NA");
                    buttons[i][j].setEnabled(false);
                }
                buttons[i][j].setTextSize(12);
                buttons[i][j].setOnClickListener(new POSListListener(i * x + j));
                rowLayout.addView(buttons[i][j]);

            }
        }
    }

    //todo
    private void updateorder() {
    }

    public void updatetotal() {
        int total = 0;
        TextView txt = (TextView) findViewById(R.id.txtPrice);
        Log.v("Checkmark", "update 1");
        for (MenuMaker iorder : orderlist) {
            total += iorder.getPrice();
            Log.v("Total", "total");
            txt.setText("$" + total / 100);
        }
        Log.v("Checkmark", "update 2");
    }


    //Admin Mode
    /* Todo
    * creates button but does not add item to order menu.*/
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


//On Click Listeners

    /* Menu button click listener.  Runs when an item is selected to be ordered
     *  */
    class POSListListener implements View.OnClickListener {
        int buttonID;

        public POSListListener(int id) {
            buttonID = id;
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
            String itemName = "";
            int itemPrice = 0;
            String itemFlavors = "";
            orderlist.add(new MenuMaker(itemName, itemPrice, itemFlavors));

        }

    }


    //remove item method
    //find all based on price
    //add item from menu to order.

}