package tech.travis.poolpos;

/**
 * Created by travis on 2/1/15.
 */

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
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

/*todo
Flavors are being ignored for now.
 */

public class MainActivity extends Activity {
    public static final String MAIN_PREFS = "MainPrefs";
    public static final String STORED_ORDERES = "OrderedPrefs";

    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    ArrayList<MenuMaker> consessionlist = new ArrayList<MenuMaker>(); //IDs +200
    ArrayList<MenuMaker> entrylist = new ArrayList<MenuMaker>(); //IDs +100
    HashMap<Integer, Integer> ordertracker = new HashMap<Integer, Integer>();  //Key is the item's UID,  value is count
    ArrayList<MenuMaker> menulist = new ArrayList<MenuMaker>();
    int mode;
    static int ordercount = 0;


    //Added for DEBUGGING
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    //Application events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        fillMenu();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(MAIN_PREFS, 0);

        mode = settings.getInt("Mode", 0);


        createMenuButtons();
        Log.i("Finished", getMethodName());
    }

    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //http://developer.android.com/guide/topics/data/data-storage.html
        SharedPreferences settings = getSharedPreferences(MAIN_PREFS, 0);
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
        initilizeOrder();
    }

    private void initilizeOrder() {
        for (MenuMaker mmi : consessionlist) {
            ordertracker.put(mmi.getUID() + 200, 0);
        }

        for (MenuMaker mmi : entrylist) {
            ordertracker.put(mmi.getUID() + 100, 0);
        }
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
                buttons[i][j].setId(j + (i * x) + ((mode + 1) * 100));
                //set text in buttons
                if ((i * x + j) < FoodSize) {
                    buttons[i][j].setText(menulist.get(i * x + j).getName() + ": " + format.format((double) menulist.get(i * x + j).getPrice() / 100) + "::" + buttons[i][j].getId());
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
        for (MenuMaker mmi : consessionlist) {
            total += ordertracker.get(mmi.getUID() + 200) * mmi.getPrice();
        }

        for (MenuMaker mmi : entrylist) {
            total += ordertracker.get(mmi.getUID() + 100) * mmi.getPrice();
        }
        dtotal = total;

        txt.setText(format.format(dtotal / 100));
        Log.i("Finished", getMethodName());
        return total;
    }


    /* Todo
    * creates button and tosses to add to order.  No icrementing reused items.*/
    int addtoordermenu(int buttonID) {

        //Working here: we need to check to see if this item exits, and if it does incriment item count.

        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //button ID is list menu, working ID is button
        int workingID = buttonID - ((mode + 1) * 100);

        MenuMaker mm;
        mm = new MenuMaker(menulist.get(workingID).getName(), menulist.get(workingID).getPrice(), "", 1);



        ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1);

        //Create Buttons test

        Button buttons = new Button(this);
        //Set button ID
        //todo
        buttons.setId((mode + 1) * 100 + 200 + ordercount++);

        //set text in buttons
        buttons.setText(menulist.get(workingID).getName() + ": " + format.format((double) menulist.get(workingID).getPrice() / 100 * menulist.get(workingID).getCount()) + "::" + buttons.getId());

        buttons.setTop(5);
        buttons.setHeight(20);
        buttons.setTextSize(12);
        buttons.setOnClickListener(new POSOrderListener(workingID));

        linearLayout.addView(buttons, param);
        Log.i("Finished", getMethodName());


        return mm.getUID();

    }

    public void submitorder() {
        String finalorder = "";
        //Working Here
        Log.i("Order", finalorder);

        updatetotal();
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void clearorder() {
        initilizeOrder();
        ordertracker.clear();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.removeAllViews();

        initilizeOrder();
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

            //todo check the list of already created buttons and if exists increase count.
            if (ordertracker.get(buttonID + ((mode + 1) * 100)) > 0) {
                //detect repeat
                Log.w("Repeat", "");
            } else {

                int uID = addtoordermenu(buttonID + ((mode + 1) * 100));

            }
            ordertracker.put(buttonID + ((mode + 1) * 100), ordertracker.get(buttonID + ((mode + 1) * 100)) + 1);
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
            //todo check the list of already created buttons and if exists increase count.

            ViewGroup parentView = (ViewGroup) view.getParent();
            parentView.removeView(view);


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
            //orderlist.add(new MenuMaker(itemName, itemPrice, itemFlavors));
            Log.i("Finished", getMethodName());
        }

    }
}