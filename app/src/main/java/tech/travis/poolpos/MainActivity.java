package tech.travis.poolpos;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/*todo
Flavors are being ignored for now.
 */

public class MainActivity extends Activity {
    public static final String PREFS_NAME = "MainPrefs";
    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    ArrayList<MenuMaker> orderlist = new ArrayList<MenuMaker>();
    ArrayList<MenuMaker> consessionlist = new ArrayList<MenuMaker>();
    ArrayList<MenuMaker> entrylist = new ArrayList<MenuMaker>();
    HashMap<Integer, Integer> buttonlinker = new HashMap<Integer, Integer>();
    ArrayList<MenuMaker> menulist = new ArrayList<MenuMaker>();
    int mode;

    //Added for DEBUGGING
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        fillMenu();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        mode = settings.getInt("Mode", 0);


        createMenuButtons();
        Log.i("Finished", getMethodName());
    }

    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //http://developer.android.com/guide/topics/data/data-storage.html
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Store settings
        editor.putInt("Mode", mode);

        // Commit the edits!
        editor.commit();
    }

    //Sets which menu list to use
    private void setmode()
    {
        View layoutEntry = findViewById(R.id.MenuButtonLayoutEntry);
        View layoutCon = findViewById(R.id.MenuButtonLayoutConsessions);

        if (mode == 0) {
            menulist = entrylist;
            layoutEntry.setVisibility(VISIBLE);
            layoutCon.setVisibility(GONE);
        } else {
            menulist = consessionlist;
            layoutEntry.setVisibility(GONE);
            layoutCon.setVisibility(VISIBLE);
        }
    }

    /*
    * Fills base data or loaded data into needed arrays*/
    private void fillMenu() {
        MenuMaker mm;

        for (int i = 0; i < ItemsList.FOOD[0].length; i++) {

            mm = new MenuMaker(ItemsList.FOOD[0][i], Integer.parseInt(ItemsList.FOOD[1][i]), "");
            consessionlist.add(i, mm);
        }

        for (int i = 0; i < ItemsList.ENTRY[0].length; i++) {
            entrylist.add(i, new MenuMaker(ItemsList.ENTRY[0][i], Integer.parseInt(ItemsList.ENTRY[1][i]), ""));
        }
        Log.i("Finished", getMethodName());
        setmode();
    }

    private void createMenuButtons() {
        mode = 0;
        setmode();
        LinearLayout layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayoutEntry);
        LayoutGen(layoutVertical);
        mode = 1;
        setmode();
        layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayoutConsessions);
        LayoutGen(layoutVertical);
    }

    private void LayoutGen(LinearLayout layoutVertical) {
        LinearLayout rowLayout = null;
        int FoodSize = menulist.size();
        int x = (int) ceil(sqrt(FoodSize));
        int y = (int) ceil(sqrt(FoodSize));
        Button[][] buttons = new Button[y][x];
        int count = x * y + 1;

        //create buttons
        for (int i = 0; i < y; i++) {
            if (count % x == 1) {
                rowLayout = new LinearLayout(this);
                rowLayout.setWeightSum(x);
                layoutVertical.addView(rowLayout);
                count = count - x;
            }
            for (int j = 0; j < x; j++) {
                buttons[i][j] = new Button(this);
                //Set button ID
                buttons[i][j].setId(j + (i * x));
                //set text in buttons
                if ((i * x + j) < FoodSize) {
                    buttons[i][j].setText(menulist.get(i * x + j).getName() + ": " + menulist.get(i * x + j).getPrice());
                    buttons[i][j].setEnabled(true);
                } else {
                    buttons[i][j].setText("NA");
                    buttons[i][j].setEnabled(false);
                }
                buttons[i][j].setTextSize(12);
                buttons[i][j].setOnClickListener(new POSListListener(i * x + j));
                rowLayout.addView(buttons[i][j]);

            }
        }
    }

    public int updatetotal() {
        int total = 0;
        double dtotal;
        TextView txt = (TextView) findViewById(R.id.txtPrice);
        for (MenuMaker iorder : orderlist) {
            total += iorder.getPrice();
        }
        dtotal = total;

        txt.setText(format.format(dtotal / 100));
        Log.i("Finished", getMethodName());
        return total;
    }


    //todo

    /* Todo
    * creates button and tosses to add to order.  No icrementing reused items.*/
    int addtoordermenu(byte mode, int buttonID) {
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
        buttons.setText(menulist.get(buttonID).getName() + ": " + menulist.get(buttonID).getPrice());

        buttons.setTop(5);
        buttons.setHeight(20);
        buttons.setTextSize(12);
        buttons.setOnClickListener(new POSOrderListener(buttonID));

        linearLayout.addView(buttons, param);
        Log.i("Finished", getMethodName());

        MenuMaker mm;
        mm = new MenuMaker(menulist.get(buttonID).getName(), menulist.get(buttonID).getPrice(), "");
        orderlist.add(mm);

        return mm.getUID();

    }

    public void submitorder() {
        String finalorder = "";

        Log.i("Order", finalorder);

        updatetotal();
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void clearorder() {
        orderlist.clear();
        buttonlinker.clear();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.removeAllViews();

        updatetotal();
        Log.i("Finished", getMethodName());
    }

    //XML created onclick listeners
    public void oCclearorder(View v) {
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void oCsubmitorder(View v) {
        submitorder();
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void oCConessionMode(View v) {
        mode = 1;
        setmode();
        //createMenuButtons();


        Log.i("Finished", getMethodName());
    }

    public void oCEntryMode(View v) {
        mode = 0;
        setmode();
        //createMenuButtons();

        Log.i("Finished", getMethodName());
    }

    public void oCEOS(View v) {
        Log.i("Finished", getMethodName());
    }

    //Programicly created onclick listeners
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
            int uID = addtoordermenu(mode, buttonID);
            buttonlinker.put(buttonID, uID);
            updatetotal();
            // v.setVisibility(v.GONE); //hide when clicked
            Log.i("Finished", getMethodName());
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
            Log.v("Removing", "Button: " + buttonID);
            ViewGroup parentView = (ViewGroup) view.getParent();
            parentView.removeView(view);
            int uID = buttonlinker.get(buttonID);

//Based on http://java67.blogspot.com/2014/03/2-ways-to-remove-elementsobjects-from-ArrayList-java.html
            Iterator<MenuMaker> MM = orderlist.iterator();
            while (MM.hasNext()) {
                MenuMaker mmi = MM.next();
                Log.d("Checking", String.valueOf(mmi));
                if (mmi.getUID() == uID) {
                    orderlist.remove(mmi);
                    break;
                }
            }

            updatetotal();
            Log.i("Finished", getMethodName());
        }

    }


    //remove item method
    //find all based on price
    //add item from menu to order.

    //add new item method
    class ButtonAddNewItem implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String itemName = "";
            int itemPrice = 0;
            String itemFlavors = "";
            orderlist.add(new MenuMaker(itemName, itemPrice, itemFlavors));
            Log.i("Finished", getMethodName());
        }

    }
}